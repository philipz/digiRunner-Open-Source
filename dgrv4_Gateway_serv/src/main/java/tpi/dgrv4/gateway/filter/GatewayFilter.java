package tpi.dgrv4.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.component.check.*;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.service.WebsiteService;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.ResHeader;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GatewayFilter extends OncePerRequestFilter {

	@Value("${cors.allow.headers}")
	private String corsAllowHeaders;
	
	// StringBuffer 的高併發替代品 StringBuilder(非同步) + ThreadLocal(多執行緒安全性)
	private static final ThreadLocal<StringBuilder> sbBuilderHolder = ThreadLocal.withInitial(() -> new StringBuilder());

	// 高併發網路流量快速通關分派裝置(記憶單元)
	public static final Map<String, Long> apiRespTimeMap = new ConcurrentHashMap<>();
	
//	public final static String cspDefaultVal = "default-src %s 'self' 'unsafe-inline'; img-src https://* 'self' data:;";

	public final static Object throughputObjLock = new Object();
	public final static LinkedHashMap<Integer, Integer> apiReqThroughput = new LinkedHashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
			// 限制HashMap大小為10個
			return size() > 10;
		}
	};
	public final static LinkedHashMap<Integer, Integer> apiRespThroughput = new LinkedHashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
			// 限制HashMap大小為10個
			return size() > 10;
		}
	};

	public final static String apiId = "apiId";

	public final static String moduleName = "moduleName";

	
	// 高併發網路流量快速通關分派裝置(控制旗標)	
	public final static String startTime = "startTime";
	public final static String endTime = "endTime";
	public final static String setWorkThread = "SetWorkThread";
	public final static String fast = "fast";
	public final static String slow = "slow";
	

	@Autowired
	private TPILogger logger;

	@Autowired
	private ApiStatusCheck apiStatusCheck;

	@Autowired
	private IgnoreApiPathCheck ignoreApiPathCheck;

	@Autowired
	private SqlInjectionCheck sqlInjectionCheck;

	@Autowired
	private TrafficCheck trafficCheck;

	@Autowired
	private XssCheck xssCheck;

	@Autowired
	private XxeCheck xxeCheck;

	@Autowired
	private TokenCheck tokenCheck;

	@Autowired
	private ApiNotFoundCheck apiNotFoundCheck;
	@Autowired
	private TsmpApiCacheProxy tsmpApiCacheProxy;

	@Autowired
	private DgrcRoutingHelper dgrcRoutingHelper;

	@Autowired
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;

	@Autowired
	private TsmpSettingService tsmpSettingService;

	@Autowired
	private CommForwardProcService commForwardProcService;

	@Autowired
	private WebsiteService websiteService;

	@Autowired
	private HostHeaderCheck hostHeaderCheck;

	@Autowired
	private DgrJtiCheck dgrJtiCheck;

	private static final long tsmpcRouting = 0;

	private static final long dgrcRouting = 1;

	private static final long tsmp_dgrcRouting = 2;

	private static final String gcpRouting = "/kibana";

	@Autowired
	private ModeCheck modeCheck;

	@Autowired
	private CusTokenCheck cusTokenCheck;

	@Autowired
	private BotDetectionCheck dotBotDetectionCheck;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【before start】--\n");

		String entry = ""; // uri 的第一個 path, 與 String uri 為同一組
		try {
			request = new CusContentCachingRequestWrapper(request);
			// 為了可以強制更改DB帳號密碼所以保留了傳參數的方式，以防當API取不到正確的值
			// uri 只會異動為 "dgrc"、"tsmpc"
			String uri = request.getRequestURI();
			String QString = request.getQueryString();
			String remoteAdd = request.getRemoteAddr();
			String remoteHost = request.getRemoteHost();
			String method01 = request.getMethod();
			
			showSrcURI(uri, remoteAdd, remoteHost, method01, QString); // debug 顯示 URI & src IP
			
			// 若 Host Header 不符合安全檢查就 true, 否則 false
			if (hostHeaderCheck.check(request)) {
				int status = HttpServletResponse.SC_UNAUTHORIZED;// 401
				throw new DgrException(status, hostHeaderCheck);
			}

			// System.out.println(".....filter.....1");

//			// uri 只會異動為 "dgrc"、"tsmpc" 
//			String uri = request.getRequestURI();

			// 2023/12/15,修改檢查器的回傳格式
			ResponseEntity<?> errRespEntity = dgrJtiCheck.check(uri, request);
			if (errRespEntity != null) {
				responseFlush(response, errRespEntity);
				return;
			}

			boolean isSpecialApi = isSpecialApi(uri); // 是否為 dgrv4 特權 API

			if (!isSpecialApi) {
				// digiRunner.gtw.mode 檢查器, ex: onlyAC
				if (!modeCheck.check(uri)) {
					logger.debug("Request uri = " + uri);
					int status = 403;
					throw new DgrException(status, modeCheck);
				}
			}

			// 客製包打主包之判斷方法
			if (cusTokenCheck.check(request)) {
				int status = 403;
				throw new DgrException(status, cusTokenCheck);
			}

			// 取出 "路由" 模式, ex: tsmc、dgrc、mixed
			int compatibilityPaths = tsmpSettingService.getVal_DGR_PATHS_COMPATIBILITY();
			String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();
			// 判斷不包含特定 path 的路由
			boolean b = (uri.length() >= 4 && (uri.substring(0, 4).equals("/11/")));
			b = b || (uri.length() >= 4 && (uri.substring(0, 4).equals("/17/")));
			b = b || (uri.length() >= 6 && (uri.substring(0, 6).equals("/dgrc/")));
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpc/")));
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpg/")));// 因為v3的noAuth要用tsmpg
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/dgrv4/")));
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/oauth/")));
			b = b || (uri.length() >= 10 && (uri.substring(0, 10).equals("/mocktest/")));
			b = b || (uri.length() >= 8 && (uri.substring(0, 8).equals("/kibana/")));
			b = b || (uri.length() >= 21 && (uri.substring(0, 21).equals("/composer/swagger3.0/")));
			b = b || (uri.length() >= 9 && (uri.substring(0, 9).equals("/website/")));
			b = b || (uri.length() >= 10 && (uri.substring(0, 10).equals("/http-api/")));
			b = b || (uri.length() >= 16 && (uri.substring(0, 16).equals("/_plugin/kibana/")));
			b = b || (uri.length() >= 13 && (uri.substring(0, 13).equals("/_dashboards/"))); // AWS上的OpenSearch的URL路徑
			b = b || (uri.length() >= 12 && (uri.substring(0, 12).equals("/dgrliveness")));	//國壽要求一個 非/dgrv4的探針API
			b = b || (uri.length() >= 9 && (uri.substring(0, 9).equals("/liveness")));	//國壽要求一個 非/dgrv4的探針API
			b = b || (uri.length() >= 10 && (uri.substring(0, 10).equals("/readiness")));	//國壽要求一個 非/dgrv4的探針API
			b = b || (uri.length() >= 8 && (uri.substring(0, 8).equals("/kibana/")))            
					|| isGCPkibana(request, kibanaPrefix); // 判斷是不是走/kibana2的GCP 相關URL
			b = !(b);

			// true 即為 tsmpc, dgrc 路由
			if (b) {
				if (compatibilityPaths == tsmpcRouting) {
					uri = addTsmpcToUri(uri);
					entry = "tsmpc";
				}

				if (compatibilityPaths == dgrcRouting) {
					uri = addDgrcToUri(uri);
					entry = "dgrc";
				}

				// 混合 路由模式
				if (compatibilityPaths == tsmp_dgrcRouting) {
					if (uri.split("/").length == 3 && uri.length() >= 7 && uri.substring(0, 7).equals("/tsmpc/")) {
						// 判定它是 tsmpc
						entry = "tsmpc";
					} else {
						uri = addDgrcToUri(uri);
						entry = "dgrc";
					}
				}
			}

			// dgrc only專用
			// 這是例外狀況。用來判斷以下兩個網址是不是composer，若是composer就走tsmpc，不是就走dgrc。
			// 1.http://ip:port/a/b
			// 2.http://ip:port/tsmpc/a/b
			boolean isComposerAPI = false;
			if (compatibilityPaths == dgrcRouting && uri.split("/").length == 4) {
				isComposerAPI = isComposerAPI(uri);
				if (isComposerAPI) {
					String keyword = "dgrc/";
					int flag = uri.indexOf(keyword);
					if (flag != -1) {
						uri = uri.replaceFirst("dgrc", "tsmpc"); // dgrc 變為 tsmpc
					}
					entry = "tsmpc";
				}
			}

			// 針對URL中有tsmpc或是dgrc，進行檢查器檢查。
			if (isDGRCorTSMPC(uri)) {

				pathsSelector(request, uri, compatibilityPaths, isComposerAPI);
				Object apiId = request.getAttribute(GatewayFilter.apiId);
				Object moduleName = request.getAttribute(GatewayFilter.moduleName);
				if (apiId == null && moduleName == null) {
					TPILogger.tl.warn("uri = " + uri + ", moduleName=null and apiID=null");
					throw new DgrException(404, apiNotFoundCheck);
				} else {
					String path = null;
					if (uri.substring(0, 5).equals("/dgrc")) {
						path = uri.substring(5, uri.length());
						entry = "dgrc";
					}
					if (uri.substring(0, 6).equals("/tsmpc") || uri.substring(0, 6).equals("/tsmpg") // 因為v3的noAuth要用tsmpg
					) {
						path = uri.substring(6, uri.length());
						entry = "tsmpc";
					}
					if (!ignoreApiPathCheck.check(path)) {
						// 20231129修改五大檢查器的回傳格式
						OAuthTokenErrorResp checkResp = checkList(response, request, uri);
						// 進行五種檢查器檢查
						if (checkResp != null) {
							ObjectMapper mapper = new ObjectMapper();
							Integer httpStatus = checkResp.getStatus();
							String errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(checkResp);
							// 將內容輸出
							responseFlush(response, httpStatus, errorRespStr);
							return;
						}
					}
				} // tsmpc , dgrc 5大檢查器 if-end

			}

			// 重新設定uri 轉發到 /tsmpc/** 或 /dgrc/**,
			// uri 只會異動為 "dgrc"、"tsmpc", 當然 uri 也可能沒有任何異動
			request = rewriteRequestURI(request, uri);
			// 計算API Req每秒轉發吞吐量
			setApiReqThroughput();

		} catch (DgrException e) {
			if (e.getHttpCode() == 0) {
				TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
			} else {
				TPILogger.tl.info(StackTraceUtil.logTpiShortStackTrace(e));
			}
			checkResponse(request, response, entry, getLocale(request), e.getHttpCode(), e.getiCheck());
			return;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} catch (Throwable e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【before end】--\n");

		// add Last Resp Header
		HttpServletResponse myResponse = (HttpServletResponse) response;
		CusContentCachingResponseWrapper responseWrapper = new CusContentCachingResponseWrapper(myResponse);

		// 加上 CSP 標題
		String dgrCspVal = getTsmpSettingService().getVal_DGR_CSP_VAL();// 例如: "*" 或 "https://10.20.30.88:18442
																		// https://10.20.30.88:28442"
		boolean isAddCsp = isAddCsp(request.getRequestURI());// 是否要加上 CSP 標題
		if (isAddCsp) {
			/*
			 * Content-Security-Policy(CSP,內容安全策略)的值, 例如:
			 * "default-src https://10.20.30.88:18442 https://10.20.30.88:28442 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
			 * "default-src * 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
			 */
//			String cspVal = String.format(cspDefaultVal, dgrCspVal);
			if (StringUtils.hasText(dgrCspVal) && !"*".equals(dgrCspVal.trim())) //如果是星號全不加  否則依照填入內容加上
				responseWrapper.setHeaderByForce("Content-Security-Policy", dgrCspVal);
		}

		responseWrapper.setHeaderByForce("Access-Control-Allow-Origin", getTsmpSettingService().getVal_DGR_CORS_VAL()); // CORS
																														// 改從
																														// Setting
																														// 取值
		// responseWrapper.setHeaderByForce("Access-Control-Allow-Headers",
		// "Content-Type, Authorization, SignCode, Language"); // CORS
		responseWrapper.setHeaderByForce("Access-Control-Allow-Headers", corsAllowHeaders); // "2. corsAllowHeaders = "
																							// + corsAllowHeaders
		responseWrapper.setHeaderByForce("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,PATCH,DELETE"); // CORS

		responseWrapper.setHeaderByForce("X-Frame-Options", "sameorigin");
		responseWrapper.setHeaderByForce("X-Content-Type-Options", "nosniff");
		responseWrapper.setHeaderByForce("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
		responseWrapper.setHeaderByForce("Referrer-Policy", "strict-origin-when-cross-origin");
		responseWrapper.setHeaderByForce("Cache-Control", "no-cache");
		responseWrapper.setHeaderByForce("Pragma", "no-cache");

		// 將取得的 DGR_CORS_VAL 放入 ServletContext 裡面供其他類取用,例如:AllowCorsFilterConfig
		request.getServletContext().setAttribute("Access-Control-Allow-Origin",
				getTsmpSettingService().getVal_DGR_CORS_VAL());

		// 將取得的 DGR_CSP_VAL 放入 ServletContext 裡面供其他類取用,例如:AllowCorsFilterConfig
		request.getServletContext().setAttribute("Content-Security-Policy", dgrCspVal);

		// GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH

		try {
			// ... 原有邏輯 ...
			filterChain.doFilter(request, response);
//			filterChain.doFilter(request, responseWrapper); //response 空白一片
	    } catch (Exception e) {
	    	if (response.getStatus() == 200) {
	    		// 為了解決高併發時, 2個 worker thread 搶 filter 的問題
	    		// jakarta.servlet.ServletException: Request processing failed: java.lang.IllegalStateException: AsyncWebRequest must not be null
	    		StringBuilder sb = new StringBuilder();
	    		sb.append("\nresponse.getStatus()::" + response.getStatus());
	    		sb.append("\nrequest.getRequestURI()::" + request.getRequestURI());
	    		sb.append("\nmsg::" + e.getLocalizedMessage());
	    		TPILogger.tl.warn(sb.toString());
	    		return ;
	    	}
	    	TPILogger.tl.error("response.getStatus(): " + response.getStatus());
	    	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
	        throw e;  // 重新拋出異常，確保錯誤能被正確處理
	    } finally {
	        // 清理資源
	        //cleanup();
	    }

		// You can't delete headers afterwards by the standard Servlet API. Your best
		// bet is to just prevent the header from being set.

		// System.out.println(".....filter.....2");
		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【after end】--\n");
	}

	/** Before
	 * 高併發網路流量快速通關分派裝置(控制單元)
	 * 記錄 Req 進入時的 startTime , URI , method
	 *   */
	private static void fetchUriHistoryBefore(HttpServletRequest request) {
		long startTime = System.currentTimeMillis();
		
		// 以此 Request 記錄它的 startTime
		request.setAttribute(GatewayFilter.startTime, startTime);
//		request.setAttribute(GatewayFilter.endTime, endTime);
		
		// 以此 Request 記錄它的 URI Key: [GET] /aa/bb , 沒有含 QueryString
		String keyUri = getUriKeyString(request.getMethod(), request.getRequestURI());
		
		// 對特定 uri 的值進行計算並更新
		// 如果 keyUri 不存在，設為 -1
		// 如果 keyUri 存在，也設為 v(原來的值)
		apiRespTimeMap.compute(keyUri, (k, v) -> v == null ? 999999 : v);
	}

	/**
	 * 高併發網路流量快速通關分派裝置(控制單元)
	 * 依據 method URI
	 * 截取上一次 Request 的往返時間 elapsed time
	 * */
	private static long fetchUriHistoryElapsed(HttpServletRequest request) {
		String method01 = request.getMethod();
		String keyUri = getUriKeyString(method01, request.getRequestURI());
		long ms = apiRespTimeMap.get(keyUri);
		return ms;
	}
	
	/** After  
	 * 高併發網路流量快速通關分派裝置(控制單元)
	 * 記錄 Response 的時間
	 * 計算 elapsed time 並放在 (記憶單元)
	 * */
	public static void fetchUriHistoryAfter(HttpServletRequest request) {
		long endTime = System.currentTimeMillis();
		
		// 以此 Request 記錄它的 URI Key: [GET] /aa/bb , 沒有含 QueryString
		long startTime = (long)request.getAttribute(GatewayFilter.startTime);
		String method01 = request.getMethod();

		String afterUri = request.getRequestURI(); // 這裡被轉換過 '/dgrc' 了
		String srcUri = afterUri.substring(afterUri.indexOf("/", 1)); // 去除第一個 path (/dgrc)

		// 計算 '耗時'
		long elapsed = endTime - startTime;
		String keyUri = getUriKeyString(method01, srcUri);
		apiRespTimeMap.put(keyUri, elapsed);
		
		// print
		// fetchUriHistoryList(); //不需要輸出以免影響 CPU 使用率
	}
	
	/**
	 * print list
	 * 高併發網路流量快速通關分派裝置(輸出單元)
	 * @return
	 */
	public static String fetchUriHistoryList() {
		StringBuilder sb = sbBuilderHolder.get();
		sb.append(String.format("%40s", "").replace(' ', '-')+"\n");
		apiRespTimeMap.forEach((key, value) -> {
			if(key.length() > 40) {
				for (int i = 0; i < key.length(); i += 40) {
		            if (i + 40 < key.length()) {
		            	sb.append(key, i, i + 40).append("\n");
		            } else {
		            	sb.append(String.format("%-40s", key.substring(i)));
		            }
		        }				
			} else {
				sb.append(String.format("%-40s", key));				
			}
			sb.append(" "+value +"ms\n");			
		});
		String result = sb.toString();
		sb.setLength(0); // 清空
//	System.err.println(result);
		return result;
	}
	
	public void unload() {
		sbBuilderHolder.remove();
	}
	
	/**
	 * 高併發網路流量快速通關分派裝置(運算單元)
	 * @param method01
	 * @param uri
	 * @return
	 */
	private static String getUriKeyString(String method01, String uri) {
		String key = "[" + method01 + "] (" + uri + ")";
		return key;
	}
	
	
	/**
	 * 高併發網路流量快速通關分派裝置(運算單元)+(控制單元)
	 * @param dgrcUri
	 * @return newUri
	 */
	private void fetchUriHistoryRouterControl(HttpServletRequest request) {

		// 開始記錄 URI 的資訊, 未來做為 '高併發網路流量快速通關分派裝置' 轉發的依據
		fetchUriHistoryBefore(request);
		
		long lastTime = fetchUriHistoryElapsed(request);
//	System.err.println("last Time=" + lastTime);
	
		if (lastTime < tsmpSettingService.getVal_HIGHWAY_THRESHOLD()) {
			request.setAttribute(GatewayFilter.setWorkThread, GatewayFilter.fast);
		} else {
			request.setAttribute(GatewayFilter.setWorkThread, GatewayFilter.slow);
		}
	}
	
	private void showSrcURI(String uri, String remoteAddr, String remoteHost, 
			String method01, String qString) {
		boolean reqUriFlag = getTsmpSettingService().getVal_REQUEST_URI_ENABLED();
		if (reqUriFlag) {
			//...request.getRequestURI():[GET] (/dgrv4/ImGTW/refreshGTW)
			//...request.getRemoteAddr():(remoteAdd)
			//...request.getRemoteHost():(remoteHost)
			StringBuilder sb = sbBuilderHolder.get(); //從 Thread 中取一個 sb 物件來用
			sb.append("\n...request.getRequestURI():[" + method01 + "] (" + uri + ")");
			sb.append("\n...request.getQueryString():(" + qString + ")");
			sb.append("\n...request.getRemoteAddr():(" + remoteAddr + ")");
			sb.append("\n...request.getRemoteHost():(" + remoteHost + ")\n");
			TPILogger.tl.debug(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * 
	 * @param request
	 * @param uri     可能包含有 /tsmpc/、/dgrc/、、、、等
	 * @return
	 */
	private HttpServletRequest rewriteRequestURI(HttpServletRequest request, String uri) {
		String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();

		if (isDGRCorTSMPC(uri)) {
			fetchUriHistoryRouterControl( request); //dgrc uri 要再經過 '高併發網路流量快速通關分派裝置'
			request = GatewayFilter.changeRequestURI(request, uri);
		} else if (uri.contains("dgrv4")) {
			return request;
			// 要同時符合 1. 是要走 /kibana2 以及url 內沒有 /kibana2 的才加入 預防/kibana2/login 被二次加入/kibana2
		} else if (isGCPkibana(request, kibanaPrefix) && !uri.contains("login")) {
			// 導向 KibanaController.java "/kibana/**"
			request = GatewayFilter.changeRequestURI(request, kibanaPrefix + uri);
		} else {
			// 沒有要變動 uri 就不用 new 物件
			return request;
		}
		return request;
	}

	/**
	 * 開頭是 dgrc 或是 tsmpc 的 path
	 * @param request 
	 */
	private boolean isDGRCorTSMPC( String uri) {
		boolean b1 = (uri.length() >= 6 && (uri.substring(0, 6).equals("/dgrc/")));
		boolean b2 = (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpc/")));
		// 因為v3的noAuth要用tsmpg
		boolean b3 = (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpg/")));
		return (b1 || b2 || b3);
	}


	private boolean isGCPkibana(HttpServletRequest request, String kibanaPrefix) {

		String uri = request.getRequestURI();
		// 先確認是不是路由2在決跑判斷
		if (gcpRouting.equals(kibanaPrefix)) {

			// 因第一次轉導不能再進入前就加入kibana 所以寫死
			if (uri.contains("/app/dashboards")) {
				return true;
			}
			if (uri.contains("/kibana")) {
				return true;
			}
			// check req header "referer" =
			// "https://10.20.30.88:18442/kibana/app/dashboards"
			// "/kibana/app/dashboards"
			// "/kibana/app/discover"
			String referer = request.getHeader("Referer");
//		boolean hasReferer = false;
			if (StringUtils.hasLength(referer)) {
				// 報表管理和監控管理也列入
				if (referer.contains("/app/") || referer.contains("/ac09/") || referer.contains("/ac05/")) {
					return true;
				}

			}

			// "7.17.5" 應該會有 GCP 的值

			String kbn = "kbn";
			String Kbn = "Kbn";
			Enumeration<String> headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String key = headers.nextElement();
				if (key.contains(kbn) || key.contains(Kbn)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 重新設定uri 轉發到 newUri
	 */
	public static HttpServletRequest changeRequestURI(HttpServletRequest request, String newUri) {
		request = new HttpServletRequestWrapper((HttpServletRequest) request) {
			@Override
			public String getRequestURI() {
				return newUri;
			}
		};
		return request;
	}

	/**
	 * 將內容輸出
	 */
	private void responseFlush(HttpServletResponse httpResponse, ResponseEntity<?> respEntity) throws Exception {
		Integer httpStatus = respEntity.getStatusCodeValue();
		String errorRespStr = null;
		ObjectMapper mapper = new ObjectMapper();
		Object bodyObj = respEntity.getBody();
		if (bodyObj instanceof OAuthTokenErrorResp) {
			OAuthTokenErrorResp errorResp = (OAuthTokenErrorResp) bodyObj;
			errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResp);

		} else if (bodyObj instanceof OAuthTokenErrorResp2) {
			OAuthTokenErrorResp2 errorResp = (OAuthTokenErrorResp2) bodyObj;
			errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResp);

		}

		responseFlush(httpResponse, httpStatus, errorRespStr);
	}

	/**
	 * 將內容輸出
	 */
	private void responseFlush(HttpServletResponse httpResponse, Integer httpStatus, String errorRespStr) {
		httpResponse.setHeader("Content-Type", "application/json; charset=utf-8");
		httpResponse.setStatus(httpStatus);
		try {
			//checkmarx, Missing HSTS Header
			httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
			httpResponse.getWriter().append(errorRespStr);
			httpResponse.getWriter().flush();
			httpResponse.flushBuffer();
		} catch (JsonProcessingException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		} catch (IOException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * 是否要加上 CSP 標頭, <br>
	 * 當 URL 的 path 不是 "/kibana" 和 "/_dashboards" 和 "/website" 開頭的, <br>
	 * 才加入 CSP 標頭 <br>
	 */
	private boolean isAddCsp(String reqURI) {
		boolean isKibana = (reqURI.indexOf("/kibana") == 0) ? true : false;// 是否為 "/kibana" 開頭
		boolean isDashboards = (reqURI.indexOf("/_dashboards") == 0) ? true : false;// 是否為 "/_dashboards" 開頭
		boolean isWebsite = (reqURI.indexOf("/website") == 0) ? true : false;// 是否為 "/website" 開頭
		boolean isKibana2 = (reqURI.indexOf("/kibana2") == 0) ? true : false;// 是否為 "/kibana" 開頭

		if (!isKibana && !isDashboards && !isWebsite && !isKibana2) {
			return true;
		}

		return false;
	}

	private String addDgrcToUri(String uri) {
		return "/dgrc" + uri;
	}

	private String addTsmpcToUri(String uri) {
		return "/tsmpc" + uri;
	}

	private void pathsSelector(HttpServletRequest request, String reqUrl, int paths, boolean isComposerAPI) {

		if (paths == tsmpcRouting) {
			setModuleNameAndApiIdToReq(request, reqUrl, false);
		}

		if (paths == dgrcRouting) {
			if (isComposerAPI) {
				setModuleNameAndApiIdToReq(request, reqUrl, false);
			} else {
				setModuleNameAndApiIdToReq(request, reqUrl, true);
			}
		}

		boolean isTsmpc = reqUrl.substring(0, 7).equals("/tsmpc/");
		if (!isTsmpc) {
			isTsmpc = reqUrl.substring(0, 7).equals("/tsmpg/");// 因為v3的noAuth要用tsmpg
		}
		if (paths == tsmp_dgrcRouting) {
			if (isTsmpc) {
				setModuleNameAndApiIdToReq(request, reqUrl, false);
			} else {
				setModuleNameAndApiIdToReq(request, reqUrl, true);
			}
		}
	}

	/**
	 * dgrc only專用<br>
	 * 
	 * 這是例外狀況。用來判斷是不是composer，若是composer就走tsmpc，不是就走dgrc。
	 * 
	 * @return
	 */
	private boolean isComposerAPI(String reqUrl) {

		String keyword = "tsmpc/";
		int flag = reqUrl.indexOf(keyword);
		if (flag == -1) {
			keyword = "dgrc/";
			flag = reqUrl.indexOf(keyword);
		}

		String urlStr = reqUrl.substring(flag + keyword.length());

		String[] urlArr = urlStr.split("/");
		String moduleName = urlArr[0];
		String apiKey = urlArr[1];

		TsmpApiId apiId = new TsmpApiId();
		apiId.setApiKey(apiKey);
		apiId.setModuleName(moduleName);
		Optional<TsmpApi> opt_tsmpApi = getTsmpApiCacheProxy().findById(apiId);

		if (!opt_tsmpApi.isEmpty()) {
			// 看TSMP_API的API_SRC是C ==> Composer
			// 看TSMP_API的API_SRC是R ==> 註冊
			TsmpApi tsmpApi = opt_tsmpApi.get();
			if ("C".equals(tsmpApi.getApiSrc())) {
				return true;
			}
		}

		return false;
	}

	public String getStringBody(HttpServletRequest request) {

		byte[] byteBody = null;
		try {
			byteBody = StreamUtils.copyToByteArray(request.getInputStream());
		} catch (IOException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}

		String stringBody = new String();
		String method = request.getMethod();
		String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();

		if (HttpMethod.GET.matches(method)) {

			// 處理 QueryString
			stringBody = getQueryStringAndFormData(request);
		} else {

			// 處理 QueryString + RAW
			if (contentType.indexOf(MediaType.TEXT_PLAIN_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.APPLICATION_JSON_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.TEXT_HTML_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.APPLICATION_XML_VALUE.toLowerCase()) > -1) {
				if (byteBody.length != 0) {
					stringBody = new String(byteBody);
				}

				if (StringUtils.hasText(stringBody)) {
					stringBody = stringBody + " " + getQueryStringAndFormData(request);
				} else {
					stringBody = getQueryStringAndFormData(request);
				}
			}

			// 處理 QueryString + FormData
			if (contentType.indexOf(MediaType.MULTIPART_FORM_DATA_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE.toLowerCase()) > -1) {
				stringBody = getQueryStringAndFormData(request);
			}

			// 若沒有content Type，預設當作text處理
			if ("".equals(contentType)) {
				if (byteBody.length != 0) {
					stringBody = new String(byteBody);
				}

				if (StringUtils.hasText(stringBody)) {
					stringBody = stringBody + " " + getQueryStringAndFormData(request);
				} else {
					stringBody = getQueryStringAndFormData(request);
				}
			}
		}

		String body = stringBody == null ? "" : stringBody;

		return body;
	}

	private OAuthTokenErrorResp checkList(HttpServletResponse response, HttpServletRequest request, String uri) {
		String apiId = request.getAttribute(GatewayFilter.apiId).toString();
		String moduleName = request.getAttribute(GatewayFilter.moduleName).toString();
		String body = getStringBody(request);
		// String locale = getLocale(request);
		
		if (dotBotDetectionCheck.check(request)) {
			int status = HttpServletResponse.SC_FORBIDDEN;// 403
//			throw new DgrException(status, dotBotDetectionCheck);
			return getCheckErrorResp("Bot Detection", "Permission denied (User-Agent is not allowed.)",
					request.getRequestURI(), status);
		}

		// API開關
		// API狀態:啟用 / 停用(TSMP_API.api_status) 啟用為true,否則就false
		if (!apiStatusCheck.check(apiId, moduleName)) {
			return getCheckErrorResp("disabled", "API was disabled", request.getRequestURI(),
					HttpStatus.SERVICE_UNAVAILABLE.value());
		}

		// SQL Injection
		// 字符: '
		// 字符: ;
		// 有符合就 true, 否則就 false

		if (sqlInjectionCheck.check(body, true)) {
			return getCheckErrorResp("Sql Injection", "Invalid parameter", request.getRequestURI(),
					HttpStatus.BAD_REQUEST.value());
		}

		// XSS檢查器
		// <script
		// </script>
		// eval(
		// expression(
		// javascript:
		// vbscript:
		// onload=
		// src=
		// 有符合就 true, 否則就 false
		if (xssCheck.check(body, true)) {
			return getCheckErrorResp("xss", "Invalid parameter", request.getRequestURI(),
					HttpStatus.BAD_REQUEST.value());
		}

		// XXE檢查器
		// <!DOCTYPE
		// <\\!DOCTYPE
		// <!ENTITY
		// <\\!ENTITY
		// 有符合就 true, 否則就 false
		if (xxeCheck.check(body, true)) {
			return getCheckErrorResp("xxe", "Invalid parameter", request.getRequestURI(),
					HttpStatus.BAD_REQUEST.value());
		}

		// Traffic
		// 取得流量上限(TSMP_CLIENT.tps)超過上限就 true, 否則就 false
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, moduleName);
		Optional<TsmpApiReg> tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
		if (tsmpApiReg.isPresent()) {
			String noAuth = tsmpApiReg.get().getNoOauth();
			if ("1".equals(noAuth)) {
				// 若有勾 no auth, 則不檢查
			} else {
				// 否則, 檢查
				String cID = getCID(request); // 取得 client ID
				if (StringUtils.hasText(cID) && trafficCheck.check(cID)) {
					return getCheckErrorResp("tps", "Client requests exceeds TPS limit", request.getRequestURI(),
							HttpStatus.TOO_MANY_REQUESTS.value());
				}
			}
		}

		return null;
	}

	private OAuthTokenErrorResp getCheckErrorResp(String checkError, String msg, String path, int status) {
		OAuthTokenErrorResp resp = new OAuthTokenErrorResp();
		resp.setError(checkError);
		resp.setMessage(msg);
		resp.setPath(path);
		resp.setStatus(status);
		resp.setTimestamp(System.currentTimeMillis() + "");
		return resp;
	}

	private void checkResponse(HttpServletRequest request, HttpServletResponse response, //
			String entry, String locale, int status, ICheck check) {
		ResHeader vo = ControllerUtil.tsmpCheckResponseBaseObj(check, locale);

		ObjectMapper mapper = new ObjectMapper();
		response.setHeader("Content-Type", "application/json; charset=utf-8");
		response.setStatus(status);
		try {
			// 檢核失敗時寫API Log
			String simpleName = check.getClass().getSimpleName();
			if (!"ModeCheck".equals(simpleName))
				writeLog(request, response, entry);

			//checkmarx, Missing HSTS Header
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
			response.getWriter().append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(vo));
			response.getWriter().flush();
			response.flushBuffer();
		} catch (JsonProcessingException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		} catch (IOException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private String getLocale(HttpServletRequest request) {
		String locale = "";
		for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
			String nextHeaderName = (String) e.nextElement();
			String headerValue = request.getHeader(nextHeaderName);
			if ("locale".toLowerCase().equals(nextHeaderName.toLowerCase())) {
				locale = headerValue;
				return locale;
			}
		}
		return locale;
	}

	private String getQueryStringAndFormData(HttpServletRequest request) {
		Enumeration keys = request.getParameterNames();
		StringBuffer strBuf = new StringBuffer();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String[] valueArray = request.getParameterValues(key);
			if (valueArray.length >= 2) {
				for (int i = 0; valueArray.length > i; i++) {
					strBuf.append(key + " " + valueArray[i] + " ");
				}
			} else {
				String value = request.getParameter(key);
				strBuf.append(key + " " + value + " ");
			}

		}
		return strBuf.toString();
	}

	/**
	 * 取得 cid, 依表頭 Authorization 的各種格式 <br>
	 * cid 為 v3 底層的名稱, 即 client ID <br>
	 */
	private String getCID(HttpServletRequest httpReq) {
		// 解析資料
		String auth = httpReq.getHeader(CommForwardProcService.AUTHORIZATION);
		String cid = tokenCheck.getCid(auth);
		return cid;
	}

	private void setModuleNameAndApiIdToReq(HttpServletRequest request, String reqUrl, boolean paths) {

		String moduleName = null;
		String apiKey = null;

		if (!paths) {
			String keyword = "tsmpc/";
			int flag = reqUrl.indexOf(keyword);
			if (flag == -1) {
				keyword = "tsmpg/";
				flag = reqUrl.indexOf(keyword);
			}
			String urlStr = reqUrl.substring(flag + keyword.length());

			String[] urlArr = urlStr.split("/");
			moduleName = urlArr[0];
			apiKey = urlArr[1];

			if (!isTsmpcApiExist(moduleName, apiKey)) {
				moduleName = null;
				apiKey = null;
				return;
			}

			// 判斷是否有勾path parameter，若沒勾，且path又有待參數，則moduleName和apiKey都塞null
			boolean isURLRID = isURLRID(moduleName, apiKey);
			if (!isURLRID && urlArr.length > 2) {
				moduleName = null;
				apiKey = null;
				return;
			}

		} else {
			TsmpApiReg apiReg = getDgrcRoutingHelper().calculateRoute(reqUrl);
			if (apiReg != null) {
				apiKey = apiReg.getApiKey();
				moduleName = apiReg.getModuleName();
			} else {
				// 找出為什麼是 null, 因為 request.getRequestURI()中間可能被 LB 轉換過
				logger.error("reqUrl:" + reqUrl + ", request.getRequestURI():" + request.getRequestURI() + ", moduleName=null and apiID=null");
			}
		}

		request.setAttribute(GatewayFilter.apiId, apiKey);
		request.setAttribute(GatewayFilter.moduleName, moduleName);
	}

	public Boolean isURLRID(String moduleName, String apiKey) {
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApiReg> tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
		if (tsmpApiReg.isEmpty())
			return false;

		String urlRid = tsmpApiReg.get().getUrlRid();
		if ("1".equals(urlRid))
			return true;

		return false;
	}

	boolean isTsmpcApiExist(String moduleName, String apiKey) {
		TsmpApiId apiId = new TsmpApiId();
		apiId.setApiKey(apiKey);
		apiId.setModuleName(moduleName);
		Optional<TsmpApi> tsmpApi = getTsmpApiCacheProxy().findById(apiId);
		if (tsmpApi.isEmpty())
			return false;
		return true;
	}

	protected void writeLog(HttpServletRequest httpReq, HttpServletResponse httpResp, //
			String entry) {
		try {
			String uuid = UUID.randomUUID().toString();
			String mbody = getCommForwardProcService().getReqMbody(httpReq);
			String aType = getAType(httpReq);

			// ES
			TsmpApiLogReq logReqVo_es = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, mbody, entry,
					aType);
			getCommForwardProcService().addEsTsmpApiLogResp1(httpResp, logReqVo_es, mbody, 0);
			// RDB
			TsmpApiLogReq logReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, mbody, entry,
					aType);
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpResp, logReqVo_rdb, mbody, 0);
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}
	}

	private String getAType(HttpServletRequest request) {
		String aType = "R";
		try {
			String apiId = (String) request.getAttribute(GatewayFilter.apiId);
			String moduleName = (String) request.getAttribute(GatewayFilter.moduleName);
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
			if (cApiKeySwitch) {
				aType = "C";
			}
		} catch (Exception e) {
			String eMsg = "\nrequest.getRequestURI():  " + request.getRequestURI() + "\n";
			eMsg += StackTraceUtil.logTpiShortStackTrace(e);
			TPILogger.tl.debug(eMsg);
		}
		return aType;
	}

	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}

	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return this.commForwardProcService;
	}

	public WebsiteService getWebsiteService() {
		return websiteService;
	}

	public static void setApiRespThroughput() {
		// 獲取目前秒數
		long currentTimeMillis = System.currentTimeMillis();
		int currentSeconds = (int) (currentTimeMillis / 1000);

		// 檢查currentSeconds目前時間點有沒有資料，若沒有currentSeconds就初始值1，若有currentSeconds就累加1
		synchronized (GatewayFilter.throughputObjLock) {
			if (GatewayFilter.apiRespThroughput.containsKey(currentSeconds)) {
				Integer throughputSize = GatewayFilter.apiRespThroughput.get(currentSeconds);
				GatewayFilter.apiRespThroughput.put(currentSeconds, throughputSize + 1);
			} else {
				GatewayFilter.apiRespThroughput.put(currentSeconds, 1);
			}
		}
	}
	
	public static void setApiRespThroughput(HttpServletRequest httpReq) {
		setApiRespThroughput();
	}

	public static void setApiReqThroughput() {
		// 獲取目前秒數
		long currentTimeMillis = System.currentTimeMillis();
		int currentSeconds = (int) (currentTimeMillis / 1000);

		// 檢查currentSeconds目前時間點有沒有資料，若沒有currentSeconds就初始值1，若有currentSeconds就累加1
		synchronized (GatewayFilter.throughputObjLock) {
			if (apiReqThroughput.containsKey(currentSeconds)) {
				Integer throughputSize = apiReqThroughput.get(currentSeconds);
				apiReqThroughput.put(currentSeconds, throughputSize + 1);
			} else {
				apiReqThroughput.put(currentSeconds, 1);
			}
		}
	}

	/**
	 * 是否為 dgrv4 特權 API (不驗 token 之API) <br>
	 * 例如: version、logger、online、AA0325、AA0326 <br>
	 * 
	 * @return true: 是; false: 不是
	 */
	public static boolean isSpecialApi(String uri) {
		// dgrv4 特權、不驗 token 之API,
		boolean isSpecial = false;
		List<String> list = Arrays.asList("/dgrv4/version", "/dgrv4/logger", "/dgrv4/onlineConsole2",
				"/dgrv4/11/AA0325", "/dgrv4/11/AA0326", "/dgrv4/shutdown");
		for (String list001 : list) {
			if (uri.contains(list001)) {
				isSpecial = true;
				break;
			}
		}

		return isSpecial;
	}

	/**
	 * 是否為 dgR AC API
	 * 
	 * @return true: 是; false: 不是
	 */
	public static boolean isDgrUrl(String uri) {
		boolean isDgrUrl = (uri.length() >= 10 && (uri.substring(0, 10).equals("/dgrv4/11/")));
		isDgrUrl = isDgrUrl || (uri.length() >= 10 && (uri.substring(0, 10).equals("/dgrv4/17/")));

		return isDgrUrl;
	}
	
	/**
	 * 兩個方法的作用和為什麼會在高併發時出現問題 當這兩個方法保持預設值（true）時，在高併發情況下可能會出現問題：
	 * 
	 * 1.請求進入系統，第一次經過 filter 
	 * 2.開始非同步處理 
	 * 3.因為 shouldNotFilterAsyncDispatch = true，非同步分發時不會再次執行 filter 
	 * 4.此時如果有其他 filter 或處理邏輯修改了 request 的屬性
	 * 5.當非同步操作完成時，Spring 嘗試獲取 AsyncWebRequest，但可能已經被清除或變為 null
	 */
//    @Override
//    protected boolean shouldNotFilterAsyncDispatch() {
//        return false; //true, 在高併發時出現問題
//    }
//
//    @Override
//    protected boolean shouldNotFilterErrorDispatch() {
//        return false; //true, 在高併發時出現問題
//    }
}
