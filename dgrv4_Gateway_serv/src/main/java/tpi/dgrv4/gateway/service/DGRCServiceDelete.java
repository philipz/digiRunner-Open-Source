package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.component.cache.proxy.ProxyMethodServiceCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.gateway.vo.AutoCacheRespVo;
import tpi.dgrv4.gateway.vo.FixedCacheVo;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class DGRCServiceDelete implements IApiCacheService{
	
	@Autowired
	private CommForwardProcService commForwardProcService;
	
	@Autowired
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	
	@Autowired
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	
	@Autowired
	private DgrcRoutingHelper dgrcRoutingHelper;
	
	@Autowired
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	
	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockApiTestService mockApiTestService;
	
	private  Map<String, String> maskInfo ;

	public ResponseEntity<?> forwardToDelete(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
			String payload) throws Exception {
		try {
			
			if(payload == null) {
				payload = "";
			}
			
			String reqUrl = httpReq.getRequestURI();
			
			TsmpApiReg apiReg = null;
			String dgrcDel_moduleName = httpReq.getAttribute(GatewayFilter.moduleName).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.apiId).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, dgrcDel_moduleName);
			Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);		
			if (opt_tsmpApiReg.isPresent()) {
				apiReg = opt_tsmpApiReg.get(); 
				maskInfo = new HashMap<>();
				maskInfo.put("bodyMaskPolicy", apiReg.getBodyMaskPolicy());
				maskInfo.put("bodyMaskPolicySymbol", apiReg.getBodyMaskPolicySymbol());
				maskInfo.put("bodyMaskPolicyNum", String.valueOf( apiReg.getBodyMaskPolicyNum()));
				maskInfo.put("bodyMaskKeyword", apiReg.getBodyMaskKeyword());
				
				maskInfo.put("headerMaskPolicy", apiReg.getHeaderMaskPolicy());
				maskInfo.put("headerMaskPolicySymbol", apiReg.getHeaderMaskPolicySymbol());
				maskInfo.put("headerMaskPolicyNum", String.valueOf( apiReg.getHeaderMaskPolicyNum()));
				maskInfo.put("headerMaskKey", apiReg.getHeaderMaskKey());
		
			}else {
				throw new Exception("TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + dgrcDel_moduleName);
			}
			
			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();
			//判斷是否需要cAikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(dgrcDel_moduleName, apiId);
			String aType = "R";
			if(cApiKeySwitch) {
				aType = "C";
			}
			
			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());
			
			// 檢查資料
			ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg, payload, false);
			
			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq dgrcDelDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload, "dgrc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq dgrcDelDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, payload, "dgrc", aType);
						
			// JWT 資料驗證有錯誤
			if(errRespEntity != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + getCommForwardProcService().getLogResp(errRespEntity, maskInfo).toString());
				//第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}
			
			//轉換 Request Body 格式
			JwtPayloadData jwtPayloadData = getCommForwardProcService().convertRequestBody(httpRes, httpReq, payload, false);
			errRespEntity = jwtPayloadData.errRespEntity;
			if(errRespEntity != null) {//資料有錯誤	
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + getCommForwardProcService().getLogResp(errRespEntity, maskInfo).toString());
				//第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}
			payload = jwtPayloadData.payloadStr;
			
			List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);
			// 沒有目標URL,則回覆錯誤訊息
			if (CollectionUtils.isEmpty(srcUrlList)) {
				ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);
				
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcDelDgrReqVo_rdb, respMbody);
				return srcUrlListErrResp;
			}
					
			int tokenPayload = apiReg.getFunFlag();
			
			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcDelDgrReqVo,
						dgrcDelDgrReqVo_rdb, cApiKeySwitch);
			}
			
			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToByPolicy(httpHeaders, httpReq, httpRes, apiReg, uuid,
					tokenPayload, cApiKeySwitch, dgrcDelDgrReqVo, dgrcDelDgrReqVo_rdb, srcUrlList, payload);

			byte[] httpArray = null;
			String httpRespStr = null;
			if (convertResponseBodyMap != null) {
				httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
				httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");
			}
			
			int content_Length = 0;
			if (httpArray != null) {
				content_Length = httpArray.length;
				ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
				//http InputStream copy into Array
				IOUtils.copy(bi, httpRes.getOutputStream());
			}
			
			// 印出第四道log
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length,
					maskInfo);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + resLog.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcDelDgrReqVo, httpRespStr, content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcDelDgrReqVo_rdb, httpRespStr,
					content_Length);

			return null;
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	/**
	 * 依失敗處置策略,決定只調用API一次或API失敗時重試
	 */
	private Map<String, Object> forwardToByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
			TsmpApiLogReq dgrcDelDgrReqVo, TsmpApiLogReq dgrcDelDgrReqVo_rdb, List<String> srcUrlList, String payload)
			throws Exception {
		// 失敗判定策略
		String failDiscoveryPolicy = apiReg.getFailDiscoveryPolicy();
		// 失敗處置策略, 0: 無重試; 1: 當調用目標URL失敗時,自動重試下一個目標URL
		String failHandlePolicy = apiReg.getFailHandlePolicy();
		
		Map<String, Object> convertResponseBodyMap = null;
		if ("1".equals(failHandlePolicy)) {// 1: 當調用目標URL失敗時,自動重試下一個目標URL
			TPILogger.tl.debug("srcUrl size:" + srcUrlList.size());
			for (int i = 0; i < srcUrlList.size(); i++) {
				String srcUrl = srcUrlList.get(i);
				String tryNumWord = (i + 1) + "/" + srcUrlList.size();
				TPILogger.tl.debug("Src Url(" + tryNumWord + "):" + srcUrl);
				convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, payload, srcUrl, uuid, tokenPayload,
						dgrcDelDgrReqVo, dgrcDelDgrReqVo_rdb, cApiKeySwitch, tryNumWord);
 
				int httpStatus = httpRes.getStatus();
				boolean isStopReTry = getCommForwardProcService().isStopReTry(failDiscoveryPolicy, httpStatus);// 是否停止重試
				if (isStopReTry) {// 停止重試
					break;
				}
			}

		} else {// 0: 無重試
			String srcUrl = srcUrlList.get(0);// 只取第一個URL執行
			TPILogger.tl.debug("Src Url:" + srcUrl);
			convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, payload, srcUrl, uuid, tokenPayload,
					dgrcDelDgrReqVo, dgrcDelDgrReqVo_rdb, cApiKeySwitch, null);
		}

		return convertResponseBodyMap;
	}
	
	protected Map<String, Object> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq dgrcDelDgrReqVo, TsmpApiLogReq dgrcDelDgrReqVo_rdb, Boolean cApiKeySwitch, String tryNumWord)
			throws Exception {

		// 2. tsmpc req header / body
		// 3. tsmpc resp header / body / code
		
		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);
		
		// 2,3道是否走cache
		HttpRespData dgrcDel_respObj = new HttpRespData();
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcDelDgrReqVo, srcUrl, reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcDelDgrReqVo, srcUrl, reqMbody);
		if(StringUtils.hasText(autoCacheId)) {//自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcDelDgrReqVo);
			paramVo.setUuid(uuid);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this, paramVo);
			if(apiCacheRespVo != null) {//走cache
				dgrcDel_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(), apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				//此行因為httpRes不能放在callback,所以移到外層
			}else {//cache發生未知錯誤,call api
				dgrcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid, false,
						tryNumWord);
			}
		}else if(StringUtils.hasText(fixedCacheId)) {//固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if(cacheVo != null) {//走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcDelDgrReqVo);
				if(isUpdate) {//更新紀錄
					dgrcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid,
							true, tryNumWord);
					//statusCode大於等於200 且 小於400才更新紀錄
					if(dgrcDel_respObj.statusCode >= 200 && dgrcDel_respObj.statusCode < 400) {
						cacheVo.setData(dgrcDel_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(dgrcDel_respObj.respHeader);
						cacheVo.setStatusCode(dgrcDel_respObj.statusCode);
						cacheVo.setRespStr(dgrcDel_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					}else {//否則就取上次紀錄
						dgrcDel_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
					}
				}else {//取得cache資料
					dgrcDel_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
				}
			}else {//call api
				dgrcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid, true,
						tryNumWord);
				//statusCode大於等於200 且 小於400才更新紀錄
				if(dgrcDel_respObj.statusCode >= 200 && dgrcDel_respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(dgrcDel_respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(dgrcDel_respObj.respHeader);
					cacheVo.setStatusCode(dgrcDel_respObj.statusCode);
					cacheVo.setRespStr(dgrcDel_respObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}
			
		}else {//call api
			dgrcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid, false,
					tryNumWord);
		}
		
		httpRes = getCommForwardProcService().getConvertResponse(dgrcDel_respObj.respHeader, dgrcDel_respObj.statusCode, httpRes);

		//轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq, dgrcDel_respObj.httpRespArray, dgrcDel_respObj.respStr);
 
		return convertResponseBodyMap;
	}
	
	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start DGRC-to-Bankend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End DGRC-from-Bankend For Cache】--\n");
			
			//第二組ES REQ
			TsmpApiLogReq dgrcDelBankendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
			
			HttpRespData respObj = getHttpRespData(vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString()); 
	 
			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcDelBankendReqVo, contentLength);
			
			return respObj;

		}catch(Exception dgrcDel_e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(dgrcDel_e));
			return null;
		}
	}
	
	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletResponse httpRes, String srcUrl,
			TsmpApiLogReq dgrReqVo, String payload, String uuid, boolean isFixedCache, String tryNumWord)
			throws Exception {
		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		} 
		
		StringBuffer dgrcDel_sb = new StringBuffer();
		if (isFixedCache) {
			dgrcDel_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Bankend For Fixed Cache】" + tryNumLog + "--");
			dgrcDel_sb
					.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Bankend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			dgrcDel_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Bankend】" + tryNumLog + "--");
			dgrcDel_sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Bankend】" + tryNumLog + "--\n");
		}
		
		//第二組ES REQ
		TsmpApiLogReq dgrcDelBankendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl, payload);
		HttpRespData respObj = getHttpRespData(header, srcUrl, payload);
		respObj.fetchByte(maskInfo); // because Enable inputStream
		dgrcDel_sb.append(respObj.getLogStr());
		TPILogger.tl.debug(dgrcDel_sb.toString()); 
		
		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes);
 
		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;
		
		//第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcDelBankendReqVo, contentLength);
		
		return respObj;
	}
	
	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String reqUrl) throws IOException {
		StringBuffer dgrcDel_log = new StringBuffer();
		
		// print
		writeLogger(dgrcDel_log, "--【URL】--");
		writeLogger(dgrcDel_log, httpReq.getRequestURI());
		writeLogger(dgrcDel_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcDel_log, "【" + httpReq.getMethod() + "】\r\n");
		
		// print header
		writeLogger(dgrcDel_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			String tmpValue = valueList.toString();
			//[ ] 符號總是位於 String 的第一個和最後一個字符，則可以使用 substring() 方法更有效地去除它們。
			tmpValue = tmpValue.substring(1, tmpValue.length() - 1);
			String value = getCommForwardProcService().convertAuth(key, tmpValue, maskInfo);
			writeLogger(dgrcDel_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcDel_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		return dgrcDel_log;
	}
		
	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}
	
	private HttpRespData getHttpRespData(Map<String, List<String>> header, 
			String reqUrl, String payload) throws Exception {
		HttpRespData dgrcDel_httpRespData = HttpUtil.httpReqByRawDataList(reqUrl, "DELETE", payload,header, true, false, maskInfo);
		
		return dgrcDel_httpRespData;
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}
	
	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			HttpHeaders httpHeaders, String srcUrl, String uuid, 
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVo_rdb, cApiKeySwitch);
	}
	
	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}
	
	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}
	
	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}
}
