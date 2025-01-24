package tpi.dgrv4.httpu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

public class HttpUtil {

	public static final String PREFIX_Sha256_Hex = "Sha256_Hex=";
	public static final String PREFIX_UnZip_DATA = "UnZip data:\n";
	public static final String PREFIX_UnDeflate_DATA = "UnDeflate data:\n";
	/**
	 * don't return headers containing security sensitive information
	 */
	private static final String[] EXCLUDE_HEADERS = { "Proxy-Authorization", "Authorization", "Content-Length" };

	public static class HttpRespData {
		public int statusCode = 502; //因為 undertow, 不接受 -1, 學習 Nginx 改為 502
		public long startTime = System.currentTimeMillis();
		public long endTime = 0L;
		// public long endTime01 = 0L;
		private StringBuffer log = new StringBuffer();
		public String respStr = null;
		public InputStream respInputStreamObj;
		public byte[] httpRespArray;
		public Map<String, List<String>> respHeader;
		public HttpRequestBase httpRequestBase = null; // 用完 http connection 需要歸還 pool
		public  boolean isEnableInputStream = false;
		public void logger(String msg) {

			// 若是到了結尾, 則要計算共花費多時間
			appendElapsedTime(msg);

			msg = maskAuth(msg);
			msg += "\n";
			log.append("\n" + msg);
		}

		// 若是到了結尾, 則要計算共花費多時間
		private void appendElapsedTime(String msg) {
			endTime = System.currentTimeMillis();
			String payloadReturn = "--【Resp payload....Return....】--";
			if (payloadReturn.equals(msg)) {
				msg = "--【Http Elapsed Time】--";
				msg += "\n";
				// msg += (endTime01 - startTime) + " ms\n";
				msg += (endTime - startTime) + " ms\n";
				log.append("\n" + msg);
			}
		}

		public String getLogStr() {
//			if (isEnableInputStream == false) {
//				return log.toString() + "\n" + this.respStr;
//			} else {
				return log.toString() + "\n";
//			}
		}

		public void fetchByte() throws IOException {
			fetchByte(null);
		}

		public void fetchByte(Map<String, String> maskInfo) throws IOException {
			ByteArrayOutputStream byteoutStream = new ByteArrayOutputStream();
			if (respInputStreamObj == null) {
				this.logger("respInputStreamObj is null");
				return;
			}
			IOUtils.copy(respInputStreamObj, byteoutStream);
			httpRespArray = byteoutStream.toByteArray();
			// 用完 http connection 需要歸還 pool
			if (httpRequestBase != null) {
				httpRequestBase.releaseConnection();
			}
			if (isText()) {
				respStr = maskBody(maskInfo, new String(httpRespArray, "UTF-8"));
			} else {
				respStr = HttpUtil.PREFIX_Sha256_Hex + HexStringUtils.toString(SHA256Util.getSHA256(httpRespArray));
				if (hasFile()) {
					respHeader.put("__show_file_info", Arrays.asList("filesize=" + httpRespArray.length));
				}
			}

			this.logger(respStr);
		}

		public boolean isText() {
			return respHeader.get("Content-Encoding") == null && !hasFile(); // 若有此 header , 可能有壓縮格式
		}

		public boolean hasFile() {
			return respHeader.get("Content-Disposition") != null; // 若有此 header , 可能是 file
		}

		public boolean isFile() {
			return respHeader.get("__show_file_info") != null; // fetchByte() 之後, 若有此 header , 判定為 file
		}

		public void setRespData(int statusCode, String respStr, byte[] httpRespArray,
				Map<String, List<String>> respHeader) {
			this.statusCode = statusCode;
			this.respStr = respStr;
			this.httpRespArray = httpRespArray;
			this.respHeader = respHeader;
		}

		public String getLogStr_UnCompress() throws Exception {
			return HttpUtil.getLogStr_UnCompress(respHeader, httpRespArray);
		}

		private String maskAuth(String auth) {
			try {
				if (StringUtils.hasText(auth) && (auth.toLowerCase().indexOf("\tkey: authorization,") > -1
						|| auth.toLowerCase().indexOf("\tkey: tokenpayload,") > -1
						|| auth.toLowerCase().indexOf("\tkey: backauth,") > -1)) {

					// 擷取value的值
					String keyword = ", Value: [";
					int startIndex = auth.indexOf(keyword);
					int endIndex = auth.lastIndexOf("]");
					String value = null;
					int startIndex2 = -1;
					if (startIndex > -1 && endIndex > -1) {
						value = auth.substring(startIndex + keyword.length(), endIndex);
					} else {
						keyword = ", Value: ";
						startIndex2 = auth.indexOf(keyword);
						value = auth.substring(startIndex2 + keyword.length());
						if (startIndex2 == -1) {
							return auth;
						}
					}

					// 分割,前面可能是Bearer或其他
					String[] arrAuth = value.split(" ");
					StringBuilder sb = new StringBuilder();
					String perfix = null;
					if (arrAuth.length > 1) {
						perfix = arrAuth[0];
						if ("bearer".equalsIgnoreCase(perfix) || "basic".equalsIgnoreCase(perfix)
								|| "dgrk".equalsIgnoreCase(perfix)) {
							for (int i = 1; i < arrAuth.length; i++) {
								if (i == arrAuth.length - 1) {
									sb.append(arrAuth[i]);
								} else {
									sb.append(arrAuth[i]).append(" ");
								}
								value = sb.toString();
							}
						}
					}

					// 遮罩
					if (value.length() > 10) {
						value = value.substring(0, 5) + "***" + value.substring(value.length() - 5);
					}
					if (StringUtils.hasText(perfix)) {
						value = perfix + " " + value;
					}

					if (startIndex > -1 && endIndex > -1) {
						value = auth.substring(0, startIndex + keyword.length()) + value + "]";
					} else {
						value = auth.substring(0, startIndex2 + keyword.length()) + value;
					}

					return value;
				} else {
					return auth;
				}
			} catch (Exception e) {
				log.append("\n" + StackTraceUtil.logStackTrace(e));
				return auth;
			}
		}
	}

	// reqUrl = removeDefaultPort(new URL(reqUrl));
	public static String removeDefaultPort(String url) throws MalformedURLException {
		String strUrl = url;

		// 為了效能所以先檢查字串, 減少 new 物件, 若 443 或 80 沒有寫就可以不用 new
		boolean hasDefaultPort = strUrl.indexOf(":443") != -1 || strUrl.indexOf(":80") != -1;
		if (hasDefaultPort) {
			strUrl = removeDefaultPort(new URL(url));
		}
		return strUrl;
	}

	public static String removeDefaultPort(URL url) {
		String strUrl = null;
		if (url.getPort() == url.getDefaultPort() || url.getPort() == -1) {
			if (StringUtils.hasText(url.getQuery())) {
				strUrl = url.getProtocol() + "://" + url.getHost() + url.getPath() + "?" + url.getQuery();
			} else {
				strUrl = url.getProtocol() + "://" + url.getHost() + url.getPath();
			}

		} else {
			strUrl = url.toString();
		}
		return strUrl;
	}

	public static void main(String[] args) throws Exception {

		String access_token = "";
		{ // POST x-www-form-urlencoded utf-8
			String reqUrl = "https://10.20.30.162:38452/tsmpdpaa/oauth/token";
			Map<String, String> formData = new HashMap<>();
			formData.put("grant_type", "client_credentials");

			Map<String, String> httpHeader = new HashMap<>();
			httpHeader.put("Authorization", "Basic WVdSdGFXNURiMjV6YjJ4bDpkSE50Y0RFeU13");
			HttpRespData resp = HttpUtil.httpReqByX_www_form_urlencoded_UTF8(reqUrl, "POST", formData, httpHeader,
					false);
			System.out.println(resp.getLogStr());
		}

//		{ // POST tpToken
//			String reqUrl = "https://data.kcg.gov.tw/dataset/82ef292a-5e96-4c99-a1ca-acae712e3805/resource/482fbc04-8666-4031-8dcf-13b5b4d35860/download/bustaxi";
//			Map<String, String> formData = new HashMap<>();
//			HttpRespData resp = null;
//			resp = HttpUtil.httpReqByGet(reqUrl, null, false);
//			System.err.println("\n<< Token Response Data >>\n" + resp.respStr);
//			resp = HttpUtil.httpReqByFormData(reqUrl, "PUT", formData, null, false);
//			System.err.println("\n<< Token Response Data >>\n" + resp.respStr);
//			resp = HttpUtil.httpReqByFile(reqUrl, "PUT", null, null, null, false);
//			System.err.println("\n<< Token Response Data >>\n" + resp.respStr);
//			resp = HttpUtil.httpReqByRawData(reqUrl, "POST", "", null, false);
//			System.err.println("\n<< Token Response Data >>\n" + resp.respStr);
//		}

//		String access_token = null;
//		{ // POST tpToken
//			String reqUrl = TestCommons.Token.URL;
//			Map<String, String> formData = new HashMap<>();
//			formData.put("grant_type", "password");
//			formData.put("username", "manager");
//			formData.put("password", "bWFuYWdlcjEyMw==");
//			HttpRespData resp = HttpUtil.httpReqByFormData(reqUrl, "POST", formData, null, false);
//			System.err.println("\n<< Token Response Data >>\n" + resp.respStr);
//			ObjectMapper om = new ObjectMapper();
//			JsonNode json = om.readTree(resp.respStr);
//			access_token = json.get("access_token").asText();
//		}

		{ // GET
			String reqUrl = "https://6295afc575c34f1f3b1f05b2.mockapi.io/get/api/getdata";
			HttpRespData resp = httpReqByGet(reqUrl, null, true);
			resp.fetchByte(null);
			System.out.println(resp.getLogStr());
		}
		{ // GET
//			String reqUrl = "https://10.20.30.88:18442/dgrv4/hello";
			String reqUrl = "https://6295afc575c34f1f3b1f05b2.mockapi.io/get/api/getdata";
			Map<String, List<String>> header = new HashMap<>();
			// header.put("Accept-Encoding", Arrays.asList("gzip", "deflate"));
			__httpReqByGet__http2(reqUrl, header, true, false);
//			HttpRespData resp = httpReqByGetList(reqUrl, header, true, false);
//			resp.fetchByte();
//			System.out.println(resp.getLogStr());
		}

//		String access_token = null;
//		{ // PSOT tpToken
//			String reqUrl = "https://10.20.30.162:38452/tsmpdpaa/tptoken/oauth/token";
//			Map<String, String> formData = new HashMap<>();
//			formData.put("grant_type", "password");
//			formData.put("username", "manager");
//			formData.put("password", "bWFuYWdlcjEyMw==");
//			HttpRespData resp = httpReqByFormData(reqUrl, "POST", formData, null, false);
//			System.out.println(resp.getLogStr());
//
//			ObjectMapper om = new ObjectMapper();
//			JsonNode json = om.readTree(resp.respStr);
//			access_token = json.get("access_token").asText();
//		}
//
//		{
//			String reqUrl = "https://10.20.30.216:8443/tsmpaa/00/aa0012";
//			String reqBody = "{\"ReqHeader\":{\"txSN\":\"1201218221530gADDxD\",\"txDate\":\"20201218T221530+0800\",\"txID\":\"AA0012\",\"cID\":\"YWRtaW5Db25zb2xl\"},\"Req_0012\":{\"funcFlag\":true}}";
//			Map<String, String> header = new HashMap<>();
//			header.put("Accept", "application/json");
//			header.put("Content-Type", "application/json");
//			header.put("Authorization", "Bearer " + access_token);
//			header.put("__Authorization", "Bearer " + access_token);
//			header.put("SignCode", generateSignCode(null, reqBody)); // null 使用 default signBlock
//			HttpRespData resp = httpReqByRawData(reqUrl, "POST", reqBody, header, false);
//			respObj.logger(resp.respStr);
//		}
	}

//	public static String httpReq(String reqUrl, String data, String method, String authorization //
//	) throws IOException {
//		return httpReq(reqUrl, data, method, authorization, authorization);
//	}
//
//	/**
//	 * @param reqUrlh
//	 * @param data
//	 * @param method
//	 * @param authorization 未經修改的授權, 用以取得 signCode
//	 * @param actualToken   修改後的授權, 會實際送到 API
//	 * @return
//	 */
//	public static String httpReq(String reqUrl, String data, String method, String authorization //
//			, String actualToken) throws IOException {
//		HttpURLConnection conn = null;
//
//		method = method.toUpperCase();
//		// GET 方法
//		if (GET.equals(method)) {
//			if (data != null && !data.isEmpty()) {
//				reqUrl += "?" + data;
//			}
//			
//			System.out.println(method + " URL: " + reqUrl);
//			System.out.println("REQUEST DATA:\n" + data);
//			URL url = new URL(reqUrl);
//			conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod(method);
//			if (actualToken != null && actualToken.length() > 0) {
//				conn.setRequestProperty("Authorization", actualToken);
//				conn.setRequestProperty("__Authorization", actualToken); // 因為基於安全理由 Authorization 取不出資料
//			}
//			conn.setRequestProperty("Content-Type", "application/json");
////			return read(conn.getInputStream());
//
//			// POST, PUT...etc
//		} else {
//			System.out.println(method + " URL: " + reqUrl);
//			System.out.println("REQUEST DATA:\n" + data);
//
//			boolean isHttps = reqUrl.startsWith("https://");
//			if (isHttps) {
//				// 取消 https 安全性驗證
//				disableCertificateValidation();
//			}
//
//			URL url = new URL(reqUrl);
//			conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod(method);
//			if (reqUrl.indexOf("oauth/token") == -1) {
//				conn.setRequestProperty("Content-Type", "application/json");
//			}
//			// 取 token
//			conn.setRequestProperty("Authorization", actualToken);
//			conn.setRequestProperty("__Authorization", actualToken); // 因為基於安全理由 Authorization 取不出資料
//
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			sendData(conn, data);
//		}
//
//		// 分類
//		InputStream is = null;
//		if (conn.getResponseCode() >= 400) {
//			is = conn.getErrorStream();
//		} else {
//			is = conn.getInputStream();
//		}
//
//		return toPrettyJson(read(is));
//	}

	public static String generateSignCode(String signBlock, String reqBody) throws NoSuchAlgorithmException {
		if (signBlock == null) {
			signBlock = "DzANBgNVBAgTBnRhaXdhbjEPMA0GA1UEBxMGdGFpcGVpMRMwEQYDVQQKEwp0aGlu";
		}

		String signCode = "";

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		digest.update((signBlock + reqBody).getBytes(StandardCharsets.UTF_8));
		signCode = byte2Hex(digest.digest());
		return signCode;
	}

	/**
	 * 實驗 java 11 httpClient
	 * 
	 * @param reqUrl
	 * @param httpHeader
	 * @param isEnableInputStream
	 * @param isRedirect
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public static HttpRespData __httpReqByGet__http2(String reqUrl, Map<String, List<String>> httpHeader,
			boolean isEnableInputStream, boolean isRedirect) throws Exception {

		reqUrl = removeDefaultPort(reqUrl);

		boolean isHttps = reqUrl.startsWith("https://");
		if (isHttps) {
			// 取消 https 安全性驗證
			disableCertificateValidation();
		}

		HttpRespData respObj = new HttpRespData();
		// TODO

		HttpClient httpClt = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(180 * 1000L))
				.followRedirects(HttpClient.Redirect.ALWAYS).build();

		HttpRequest request = HttpRequest.newBuilder().uri(new URI(reqUrl)).GET().build();

		java.net.http.HttpResponse<String> response = httpClt.send(request, BodyHandlers.ofString());

		respObj.endTime = System.currentTimeMillis();
		// respObj.endTime01 = System.currentTimeMillis();

		respObj.statusCode = response.statusCode();
		System.out.println("=========================");
		System.out.println("\nHttpClient2:");
		System.out.println(response.body());
		System.out.println("01 elapsed=" + (respObj.endTime - respObj.startTime) + " ms");
		// System.out.println("02 elapsed=" + (respObj.endTime01 - respObj.startTime));

//		11
//		// print
//		// respObj.logger("");
//		respObj.logger("--【URL】--");
//		respObj.logger(reqUrl);
//		respObj.logger("--【End】--\r\n");
//		
//		URL url = new URL(reqUrl);
//		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setDoOutput(true);
//		conn.setUseCaches(false);
//		conn.setInstanceFollowRedirects(isRedirect);
//		conn.setRequestMethod("GET");
//		respObj.logger("【" + conn.getRequestMethod() + "】\r\n");
//		
//		// header
//		conn.setRequestProperty("Accept", "application/json");
//		Map<String, List<String>> httpHeaderNotFound = new HashMap<>();
//		if (httpHeader != null && httpHeader.size() > 0) {
//			httpHeader.forEach((k,vlist)->{
//				vlist.forEach((v)->{
//					conn.addRequestProperty(k, v);
//					handleNotFoundHttpHeaderList(httpHeaderNotFound, k, v);
//				});
//			});
//		}
//		
//		// print Req header
//		respObj.logger("--【Http Req Header】--");
//		Map<String, List<String>> reqHeaderMap = conn.getRequestProperties();
//		for (Map.Entry<String, List<String>> entry : reqHeaderMap.entrySet()) {
//			respObj.logger("\tKey: " + entry.getKey() + ", Value: " + entry.getValue());
//		}
//		printCantFoundHeaderNameList(httpHeaderNotFound, respObj); // can't found header name
//		respObj.logger("--【End】--\r\n");
//		// send
//		// sendData(conn, formBody.getBytes());
//
//		// print
//		respObj.logger("--【Http Resp Header】--");
//		respObj.logger("\tKey: http code, Value: " + conn.getHeaderField(null));
//		
//	respObj.endTime01 = System.currentTimeMillis();		
//	
//		Map<String, List<String>> map = conn.getHeaderFields();
//		respObj.respHeader = new HashMap<String, List<String>>(map);
//		for (Map.Entry<String, List<String>> entry : respObj.respHeader.entrySet()) {
//			respObj.logger("\tKey: " + entry.getKey() + ", Value: " + entry.getValue());
//		}
//		String server = conn.getHeaderField("Server");
//		if (server != null) {
//			respObj.logger("Server - " + server);
//		}
//		respObj.logger("--【End】--\r\n");
//		
//		// prepare Http Result
//		try {
//			respObj.statusCode = conn.getResponseCode();
//		} catch (Exception e) {
//			StackTraceElement[] elements = e.getStackTrace();
//			respObj.respStr = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
//			return respObj;
//		}
//		
//		// print
//		respObj.logger("--【Http status code】--");
//		respObj.logger("" + respObj.statusCode);
//		respObj.logger("--【End】--\r\n");
//		
//		// get Stream
//		InputStream is = null;
//		if (conn.getResponseCode() >= 400) {
//			is = conn.getErrorStream();
//		} else {
//			is = conn.getInputStream();
//		}
//		
//		// 分類
//		if (isEnableInputStream == true) {
//			respObj.respInputStreamObj = is;
//		} else {
//			respObj.respStr = toPrettyJson(read(is));
//		}
//		
//		// print
//		respObj.logger("--【Resp payload....Return....】--");
		return respObj;

		// TODO
	}

	public static HttpRespData httpReqByGet(String reqUrl, Map<String, String> httpHeader, boolean isEnableInputStream)
			throws IOException {
		return httpReqByGet(reqUrl, httpHeader, isEnableInputStream, false);
	}

	public static HttpRespData httpReqByGet(String reqUrl, Map<String, String> httpHeader, boolean isEnableInputStream,
			boolean isRedirect) throws IOException {
		return httpReqByGetList(reqUrl, tranferOne2List(httpHeader), isEnableInputStream, isRedirect, null);
	}

	public static HttpRespData httpReqByGetList(String reqUrl, Map<String, List<String>> httpHeader,
			boolean isEnableInputStream, boolean isRedirect) throws IOException {
		return httpReqByGetList(reqUrl, httpHeader, isEnableInputStream, isRedirect, null);
	}

	// 全局參數
	private static Object httpPoolLock = new Object();

	private static int httpClientBuilderNum = 1;
	// 請求參數
	private static RequestConfig configArr[] = new RequestConfig[httpClientBuilderNum];

	// builder
	private static HttpClientBuilder httpClientBuilderArr[] = new HttpClientBuilder[httpClientBuilderNum];

	private static void init(int index) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		synchronized (httpPoolLock) {
			if (httpClientBuilderArr[index] != null) {
				return;
			}

			// 建立一個信任所有證書的 SSLContext。
			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustAllStrategy()).build();
			// 信任所有的 Hostname
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
					NoopHostnameVerifier.INSTANCE);

			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();

			// 建立一個 PoolingHttpClientConnectionManager 實例
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);

			ConnectionConfig connConfig = ConnectionConfig.custom().build();
//			ConnectionConfig connConfig = ConnectionConfig.custom().setCharset(Charset.forName("utf-8")).build(); //只是增加了指 charset

			SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoTimeout(60000).build();
			connectionManager.setDefaultConnectionConfig(connConfig);
			connectionManager.setDefaultSocketConfig(socketConfig);
			// http connection pool 最大數
			connectionManager.setMaxTotal(5000);
			// 設定 route 最大連接數
			connectionManager.setDefaultMaxPerRoute(5000);
			int timeout = getTimeout();
			// 請求參數
			configArr[index] = RequestConfig.custom().setConnectTimeout(timeout) // 連接超時
					.setConnectionRequestTimeout(500) // 從 pool 中獲取連線超時
					.setSocketTimeout(60000) // 設置 socket 超時
					.build();
			// 創建 Builder
			httpClientBuilderArr[index] = HttpClients.custom();

			// 管理器是共享(shared)的, 它的 lifecycle 將由調用者管理, 且不會關閉
			// 否則可能會出現 Connection pool shutdown 異常
			httpClientBuilderArr[index].setConnectionManager(connectionManager).setConnectionManagerShared(true);
			// 長連接策略
			httpClientBuilderArr[index].setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
			httpClientBuilderArr[index].setDefaultRequestConfig(configArr[index]);
			// 取消 302
			httpClientBuilderArr[index].disableRedirectHandling();
			// 創建 httpClient
			// httpClient = httpClientBuilder.build();
			// TODO 定時回收 http connection
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// 關閉過期的連結
					connectionManager.closeExpiredConnections();
					// 關閉空閒30秒的連結
					connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
					// System.out.println("connection RELEASE! " );
				}
			}, 10 * 1000, 5 * 1000);
		}
	}



	private  static  int getTimeout() {
		// 從-D取 ，不能從properties 取
		String property = System.getProperty("httpClient.connection.timeout");
		//預設60秒
		if (property == null) {
			property = "30000";
		}
		return Integer.valueOf(property);
	}
	public static HttpClientBuilder getHttpClientBuilder()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		// 生成一個 1 到 3 之間的隨機整數（包括 1 和 3）
		int index = ThreadLocalRandom.current().nextInt(0, httpClientBuilderNum);

		if (httpClientBuilderArr[index] == null) {
			init(index);
		}

		return httpClientBuilderArr[index];
	}

	public static HttpRespData httpReqByGetList(String reqUrl, Map<String, List<String>> httpHeader,
			boolean isEnableInputStream, boolean isRedirect, Map<String, String> maskInfo) throws IOException {

		reqUrl = removeDefaultPort(reqUrl);

		boolean isHttps = reqUrl.startsWith("https://");
//		if (isHttps) {
		// 取消 https 安全性驗證
		// disableCertificateValidation(); //委由 `信任所有證書的 SSLContext` 做完了
//		}

		HttpRespData respObj = new HttpRespData();

		// print
		// respObj.logger("");
		respObj.logger("--【URL】--");
		respObj.logger(reqUrl);
		respObj.logger("--【End】--\r\n");
		HttpClientBuilder httpClientBuilder = null;
		CloseableHttpClient httpClient = null;

		try {
			// 無使用 http connection pool
//			httpClientBuilder = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
//					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build());

			// Using http connection pool
			httpClientBuilder = getHttpClientBuilder();
		} catch (Exception e) {
			respObj.respStr = StackTraceUtil.logStackTrace(e);
		}
		if (!isRedirect) {
			httpClientBuilder.disableRedirectHandling();
		}
		httpClient = httpClientBuilder.build();
//		URL url = new URL(reqUrl);
//		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setDoOutput(true);
//		conn.setUseCaches(false);
//		conn.setInstanceFollowRedirects(isRedirect);
//		conn.setRequestMethod("GET");
//		respObj.logger("【" + conn.getRequestMethod() + "】\r\n");

		HttpGet httpGet = new HttpGet(reqUrl);
//		HttpPost httpPost = new HttpPost(reqUrl);
		respObj.logger("【" + httpGet.getMethod() + "】\r\n");
		// header
		boolean isAddAccept = true;
		for (String key : httpHeader.keySet()) {
			String value = null;
			for (String v : httpHeader.get(key)) {
				if ("accept".equalsIgnoreCase(key)) {
					isAddAccept = false;
				}
				if (value == null) {
					value = v;
				} else {
					value += "," + v;
				}

			}
			httpGet.setHeader(changeMockTestHeader(key), value);
		}
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("keep-alive", "timeout=59");
		httpGet.removeHeaders("Content-Length");
		httpGet.removeHeaders("host");
		if (isAddAccept) {
			httpGet.setHeader("Accept", "application/json");
		}
		

		// print Req header
		respObj.logger("--【Http Req Header】--");
		for (Header h : httpGet.getAllHeaders()) {
			String keyString = h.getName();
			if (StringUtils.hasText(keyString) && (keyString.toLowerCase().indexOf("\tkey: authorization,") > -1
					|| keyString.toLowerCase().indexOf("\tkey: tokenpayload,") > -1
					|| keyString.toLowerCase().indexOf("\tkey: backauth,") > -1)) {
				// authorization, tokenpayload, backauth 有固定的 mask, 不受 API List 控制
				continue; 
			}
			// 執行 Header Policy mask, and 遮蔽機敏資料(password/pwd/mima)
			respObj.logger("\tKey: " + keyString + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
		}
//		printCantFoundHeaderNameList(httpHeaderNotFound, respObj); // can't found header name
		respObj.logger("--【End】--\r\n");
		// send
		// sendData(conn, formBody.getBytes());
		HttpResponse httpResponse = null;
		String eMessage = null;
		try {
			httpResponse = httpClient.execute(httpGet);
		} catch (Exception e) {
			respObj.respHeader = new HashMap<String, List<String>>();
			StackTraceElement[] elements = e.getStackTrace();
			eMessage = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
		}

		// print
		respObj.logger("--【Http Resp Header】--");
		if (httpResponse != null) {
			respObj.logger("\tKey: http code, Value: " + httpResponse.getStatusLine());
			// respObj.endTime01 = System.currentTimeMillis();

			Header[] headers = httpResponse.getAllHeaders();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			for (Header h : headers) {
				map.put(h.getName(), Arrays.asList(h.getValue()));
				respObj.logger("\tKey: " + h.getName() + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
			}
			respObj.respHeader = new HashMap<String, List<String>>(map);
			Header server = httpResponse.getFirstHeader("Server");
			if (server != null) {
				respObj.logger("Server - " + server);
			}
		} else {
			respObj.logger("null");
		}

		respObj.logger("--【End】--\r\n");

		// prepare Http Result
		HttpEntity entity = null;
		if (httpResponse != null) {
			entity = httpResponse.getEntity();
			respObj.statusCode = httpResponse.getStatusLine().getStatusCode();
		} else {
			respObj.statusCode = 502; //因為 undertow, 不接受 -1, 學習 Nginx 改為 502
		}

		// print
		respObj.logger("--【Http status code】--");
		respObj.logger("" + respObj.statusCode);
		respObj.logger("--【End】--\r\n");

		if (httpResponse != null) {
			// get Stream
			InputStream is = null;
			if (entity != null) {
				is = entity.getContent();
			}
			// print
			respObj.logger("--【Resp payload....Return....】--");
			// 分類
			if (isEnableInputStream == true) {
				respObj.isEnableInputStream = true;
				respObj.respInputStreamObj = is;
				respObj.httpRequestBase = httpGet; // Using http connection pool, 取完 byte 要releaseConnection
			} else {
				respObj.respStr = toPrettyJson(read(is));
				respObj.logger(maskBody(maskInfo, respObj.respStr));
				// httpClient 必需 release Connection, 不是 abort, 因為 release connection是還到
				// connection pool, 而 aboart 是抛棄這個 connection,而且還會佔用 pool
				httpGet.releaseConnection();
			}
		} else {
			respObj.respStr = eMessage;
			respObj.logger(respObj.respStr);
		}


		return respObj;
	}

	public static HttpRespData httpReqByFormData(String reqUrl, String method, Map<String, String> formData,
			Map<String, String> httpHeader, boolean isEnableInputStream) throws IOException {
		return httpReqByFormData(reqUrl, method, formData, httpHeader, isEnableInputStream, false,
				getPartMapByNull(formData), null);
	}

	public static HttpRespData httpReqByFormData(String reqUrl, String method, Map<String, String> formData,
			Map<String, String> httpHeader, boolean isEnableInputStream, Map<String, String> parts) throws IOException {
		return httpReqByFormData(reqUrl, method, formData, httpHeader, isEnableInputStream, false, parts, null);
	}

	public static HttpRespData httpReqByFormData(String reqUrl, String method, Map<String, String> formData,
			Map<String, String> httpHeader, boolean isEnableInputStream, Map<String, String> parts,
			Map<String, String> maskInfo) throws IOException {
		return httpReqByFormData(reqUrl, method, formData, httpHeader, isEnableInputStream, false, parts, maskInfo);
	}

	public static HttpRespData httpReqByFormData(String reqUrl, String method, Map<String, String> formData,
			Map<String, String> httpHeader, boolean isEnableInputStream, boolean isRedirect, Map<String, String> parts,
			Map<String, String> maskInfo) throws IOException {
		return httpReqByFormDataList(reqUrl, method, tranferOne2List(formData), tranferOne2List(httpHeader),
				isEnableInputStream, isRedirect, parts, maskInfo);
	}

	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, Map<String, List<String>> formData,
			Map<String, List<String>> httpHeader, boolean isEnableInputStream, boolean isRedirect) throws IOException {

		return httpReqByFormDataList(reqUrl, method, formData, httpHeader, isEnableInputStream, isRedirect,
				getPartMapByNull(formData), null);
	}

	/**
	 * Req key-value 為 list Resp Key-value 也為 list
	 */
	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, Map<String, List<String>> formData,
			Map<String, List<String>> httpHeader, boolean isEnableInputStream, boolean isRedirect,
			Map<String, String> parts, Map<String, String> maskInfo) throws IOException {

//		final String BOUNDARY = "JohnBoundary" + UUID.randomUUID().toString();

		reqUrl = removeDefaultPort(reqUrl);

		boolean isHttps = reqUrl.startsWith("https://");
		if (isHttps) {
			// 取消 https 安全性驗證
			disableCertificateValidation();
		}

		HttpRespData respObj = new HttpRespData();

		// print URL
		respObj.logger("--【URL】--");
		respObj.logger(reqUrl);
		respObj.logger("--【End】--\r\n");

		method = method.toUpperCase();
		CloseableHttpClient httpClient = null;
		HttpClientBuilder httpClientBuilder = null;

		try {
			httpClientBuilder = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build());
		} catch (Exception e) {
			respObj.respStr = StackTraceUtil.logStackTrace(e);
		}

		if (!isRedirect) {
			httpClientBuilder.disableRedirectHandling();
		}
		httpClient = httpClientBuilder.build();
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		entityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);

		formData.forEach((k, vList) -> {
			vList.forEach(v -> {
				String contentType = parts.get(k);

				if (contentType != null && StringUtils.hasLength(contentType)) {
					String mimeType = contentType;
					if (contentType.contains(";")) {
						mimeType = contentType.split(";")[0];
						Charset charset = Charset.forName(contentType.split(";")[1].split("=")[1]);
						entityBuilder.addTextBody(k, v, ContentType.create(mimeType, charset));
					} else {
						entityBuilder.addTextBody(k, v, ContentType.create(mimeType, Charset.defaultCharset()));
					}
				} else {
					entityBuilder.addTextBody(k, v,ContentType.create(HTTP.PLAIN_TEXT_TYPE, Charset.defaultCharset()));
				}
			});
		});
		HttpEntityEnclosingRequestBase httpBase = getRequestBase(method, reqUrl);
		HttpEntity entity = entityBuilder.build();
		httpBase.setEntity(entity);

		respObj.logger("【" + httpBase.getMethod() + "】\r\n");

		// Http Req Header
		for (String key : httpHeader.keySet()) {
			String value = null;
			for (String v : httpHeader.get(key)) {
				if (value == null) {
					value = v;
				} else {
					value += "," + v;
				}
			}
			httpBase.setHeader(changeMockTestHeader(key), value);

		}
		httpBase.setHeader("Connection", "keep-alive");
		httpBase.setHeader("keep-alive", "timeout=59");
		httpBase.removeHeaders("Content-Length");
		httpBase.removeHeaders("host");
		httpBase.removeHeaders("Content-Type");

		// print Req header
		respObj.logger("--【Http Req Header】--");
		for (Header h : httpBase.getAllHeaders()) {
			respObj.logger("\tKey: " + h.getName() + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
		}
		respObj.logger("--【End】--\r\n");

		// form body
//		String formBody = getFormBodyList(formData, BOUNDARY, parts);

		// print
		respObj.logger("--【Req payload / Form Data】--");
		formData.forEach((k, vList) -> {
			respObj.logger("\tKey: " + k + ", Value: " + maskBodyFromFormData(maskInfo, k, vList.toString())
					+ ", Content-Type: " + parts.get(k));
		});

		respObj.logger("--【End】--\r\n");

		// send
		HttpResponse httpResponse = null;
		String eMessage = null;
		try {
			httpResponse = httpClient.execute(httpBase);
		} catch (Exception e) {
			respObj.respHeader = new HashMap<String, List<String>>();
			StackTraceElement[] elements = e.getStackTrace();
			eMessage = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
		}

		// print
		respObj.logger("--【Http Resp Header】--");

		if (httpResponse != null) {
			respObj.logger("\tKey: http code, Value: " + httpResponse.getStatusLine());
			Map<String, List<String>> map = new HashMap<String, List<String>>();

			Header[] headers = httpResponse.getAllHeaders();
			for (Header h : headers) {
				map.put(h.getName(), Arrays.asList(h.getValue()));
				respObj.logger("\tKey: " + h.getName() + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
			}
			respObj.respHeader = new HashMap<String, List<String>>(map);
			Header server = httpResponse.getFirstHeader("Server");
			if (server != null) {
				respObj.logger("Server - " + server);
			}

		} else {
			respObj.logger("null");
		}

		respObj.logger("--【End】--\r\n");

		// prepare Http Result
		HttpEntity respEntity = null;
		if (httpResponse != null) {
			respEntity = httpResponse.getEntity();
			respObj.statusCode = httpResponse.getStatusLine().getStatusCode();
		} else {
			respObj.statusCode = 502; //因為 undertow, 不接受 -1, 學習 Nginx 改為 502
		}

		// print
		respObj.logger("--【Http status code】--");
		respObj.logger("" + respObj.statusCode);
		respObj.logger("--【End】--\r\n");

		if (httpResponse != null) {
			// get Stream
			InputStream is = null;
			if (respEntity != null) {
				is = respEntity.getContent();
			}

			// print
			respObj.logger("--【Resp payload....Return....】--");
			if (isEnableInputStream == true) {
				respObj.respInputStreamObj = is;
			} else {
				respObj.respStr = toPrettyJson(read(is));
				respObj.logger(maskBody(maskInfo, respObj.respStr));
			}
		} else {
			respObj.respStr = eMessage;
			respObj.logger(respObj.respStr);
		}
		return  respObj;
	}
	private static HttpEntityEnclosingRequestBase  getRequestBase(String method ,String url){
		HttpEntityEnclosingRequestBase request;

		if ("POST".equalsIgnoreCase(method)) {
			request = new HttpPost(url);
		} else if ("PUT".equalsIgnoreCase(method)) {
			request = new HttpPut(url);
		}else if ("PATCH".equalsIgnoreCase(method)) {
			request = new HttpPatch(url);
		}else if ("DELETE".equalsIgnoreCase(method)) {
			request = new HttpEntityEnclosingRequestBase() {
				@Override
				public String getMethod() {
					return "DELETE";
				}
			};
			request.setURI(URI.create(url));
		}
		else {
			throw new IllegalArgumentException("Invalid method: " + method);
		}
		return  request;
	}
	/**
	 * X_www_form_urlencoded UTF-8 FORM BODY 編碼方式
	 */
	public static String getDataString(Map<String, List<String>> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		params.forEach((k, vlist) -> {
			vlist.forEach((v) -> {
				try {
					result.append(URLEncoder.encode(k, "UTF-8"));
					result.append("=");
					result.append(URLEncoder.encode(v, "UTF-8"));
					result.append("&");
				} catch (UnsupportedEncodingException e) {
				}
			});
		});

		// 如果至少有一組 key/value, 最小長度一定是 "k=v&" , 那就去掉取後一個字
		if (result.length() >= 4) {
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}

	public static HttpRespData httpReqByX_www_form_urlencoded_UTF8(String reqUrl, String method,
			Map<String, String> formData, Map<String, String> httpHeader, boolean isEnableInputStream)
			throws IOException {
		return httpReqByX_www_form_urlencoded_UTF8(reqUrl, method, formData, httpHeader, isEnableInputStream, false,
				null);
	}

	public static HttpRespData httpReqByX_www_form_urlencoded_UTF8(String reqUrl, String method,
			Map<String, String> formData, Map<String, String> httpHeader, boolean isEnableInputStream,
			boolean isRedirect, Map<String, String> maskInfo) throws IOException {
		return httpReqByX_www_form_urlencoded_UTF8List(reqUrl, method, tranferOne2List(formData),
				tranferOne2List(httpHeader), isEnableInputStream, isRedirect, maskInfo);
	}

	public static HttpRespData httpReqByX_www_form_urlencoded_UTF8List(String reqUrl, String method,
			Map<String, List<String>> formData, Map<String, List<String>> httpHeader, boolean isEnableInputStream,
			boolean isRedirect) throws IOException {
		return httpReqByX_www_form_urlencoded_UTF8List(reqUrl, method, formData, httpHeader, isEnableInputStream,
				isRedirect, null);
	}

	public static HttpRespData httpReqByX_www_form_urlencoded_UTF8List(String reqUrl, String method,
			Map<String, List<String>> formData, Map<String, List<String>> httpHeader, boolean isEnableInputStream,
			boolean isRedirect, Map<String, String> maskInfo) throws IOException {


		reqUrl = removeDefaultPort(reqUrl);

		boolean isHttps = reqUrl.startsWith("https://");
		if (isHttps) {
			// 取消 https 安全性驗證
			disableCertificateValidation();
		}

		HttpRespData respObj = new HttpRespData();

		// print
		respObj.logger("--【URL】--");
		respObj.logger(reqUrl);
		respObj.logger("--【End】--\r\n");

		method = method.toUpperCase();
		CloseableHttpClient httpClient = null;
		HttpClientBuilder httpClientBuilder = null;
		try {

			httpClientBuilder = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build());
		} catch (Exception e) {
			respObj.respStr = StackTraceUtil.logStackTrace(e);
		}
		if (!isRedirect) {
			httpClientBuilder.disableRedirectHandling();
		}
		httpClient = httpClientBuilder.build();

		List<NameValuePair> params = new ArrayList<>();

		formData.forEach((k, vList) -> {

			vList.forEach(v -> {

				params.add(new BasicNameValuePair(k, v));
			});

		});
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

		HttpResponse response = null;
		String eMessage = null;
		HttpEntityEnclosingRequestBase requestBase = getRequestBase(method, reqUrl);
		requestBase.setEntity(entity);

		respObj.logger("【" + requestBase.getMethod() + "】\r\n");

		for (String key : httpHeader.keySet()) {
			String value = null;
			for (String v : httpHeader.get(key)) {
				if (value == null) {
					value = v;
				} else {
					value += "," + v;
				}
			}
			requestBase.setHeader(changeMockTestHeader(key), value);
		}
		requestBase.setHeader("Connection", "keep-alive");
		requestBase.setHeader("keep-alive", "timeout=59");
		requestBase.removeHeaders("Content-Length");
		requestBase.removeHeaders("host");
		// print Req header
		respObj.logger("--【Http Req Header】--");
		for (Header h : requestBase.getAllHeaders()) {
			respObj.logger("\tKey: " + h.getName() + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
		}
		respObj.logger("--【End】--\r\n");
		// form body
		// print
		respObj.logger("--【Req payload / Form Data】--");
		formData.forEach((k, vList) -> {
			respObj.logger("\tKey: " + k + ", Value: " + maskBodyFromFormData(maskInfo, k, vList.toString()));
		});
		respObj.logger("--【End】--\r\n");
		try {
			response = httpClient.execute(requestBase);
		} catch (Exception e) {
			respObj.respHeader = new HashMap<String, List<String>>();
			StackTraceElement[] elements = e.getStackTrace();
			eMessage = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
		}
		// print
		respObj.logger("--【Http Resp Header】--");

		if (response != null) {
			respObj.logger("\tKey: http code, Value: " + response.getStatusLine());
			Header[] headers = response.getAllHeaders();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			for (Header h : headers) {
				map.put(h.getName(), Arrays.asList(h.getValue()));
				respObj.logger("\tKey: " + h.getName() + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
			}

			respObj.respHeader = new HashMap<String, List<String>>(map);

			Header server = response.getFirstHeader("Server");
			if (server != null) {
				respObj.logger("Server - " + server);
			}
		} else {
			respObj.logger("null");
		}

		respObj.logger("--【End】--\r\n");

		// prepare Http Result
		HttpEntity respEntity = null;
		if (response != null) {
			respEntity = response.getEntity();
			respObj.statusCode = response.getStatusLine().getStatusCode();
		} else {
			respObj.statusCode = 502; //因為 undertow, 不接受 -1, 學習 Nginx 改為 502
		}

		// print
		respObj.logger("--【Http status code】--");
		respObj.logger("" + respObj.statusCode);
		respObj.logger("--【End】--\r\n");

		if (response != null) {
			// get Stream
			InputStream is = null;
			if (respEntity != null) {
				is = respEntity.getContent();
			}
			// print
			respObj.logger("--【Resp payload....Return....】--");
			// 分類
			if (isEnableInputStream == true) {
				respObj.isEnableInputStream = true;
				respObj.respInputStreamObj = is;
			} else {
				respObj.respStr = toPrettyJson(read(is));
				respObj.logger(maskBody(maskInfo, respObj.respStr));
			}
		} else {
			respObj.respStr = eMessage;
			respObj.logger(respObj.respStr);
		}
		return respObj;
	}

	public static HttpRespData httpReqByFile(String reqUrl, String method, Map<String, String> formTextData,
			Map<String, Path> formFileData, Map<String, String> httpHeader, boolean isEnableInputStream)
			throws IOException {
		boolean isRedirect = false;
		return httpReqByFormDataList(reqUrl, method, tranferOne2List(formTextData), tranferOnePath2List(formFileData),
				tranferOne2List(httpHeader), isEnableInputStream, isRedirect, null);
	}

	public static HttpRespData httpReqByRawData(String reqUrl, String method, String rawData,
			Map<String, String> httpHeader, boolean isEnableInputStream) throws IOException {
		return httpReqByRawData(reqUrl, method, rawData, httpHeader, isEnableInputStream, false, null);
	}

	public static HttpRespData httpReqByRawData(String reqUrl, String method, String rawData,
			Map<String, String> httpHeader, boolean isEnableInputStream, Map<String, String> maskInfo)
			throws IOException {
		return httpReqByRawData(reqUrl, method, rawData, httpHeader, isEnableInputStream, false, maskInfo);
	}

	public static HttpRespData httpReqByRawData(String reqUrl, String method, String rawData,
			Map<String, String> httpHeader, boolean isEnableInputStream, boolean isRedirect) throws IOException {
		return httpReqByRawDataList(reqUrl, method, rawData, tranferOne2List(httpHeader), isEnableInputStream,
				isRedirect, null);
	}

	public static HttpRespData httpReqByRawData(String reqUrl, String method, String rawData,
			Map<String, String> httpHeader, boolean isEnableInputStream, boolean isRedirect,
			Map<String, String> maskInfo) throws IOException {
		return httpReqByRawDataList(reqUrl, method, rawData, tranferOne2List(httpHeader), isEnableInputStream,
				isRedirect, maskInfo);
	}

	public static HttpRespData httpReqByRawDataList(String reqUrl, String method, String rawData,
			Map<String, List<String>> httpHeader, boolean isEnableInputStream, boolean isRedirect) throws IOException {
		return httpReqByRawDataList(reqUrl, method, rawData, httpHeader, isEnableInputStream, isRedirect, null);
	}

	public static HttpRespData httpReqByRawDataList(String reqUrl, String method, String rawData,
													Map<String, List<String>> httpHeader, boolean isEnableInputStream, boolean isRedirect,
													Map<String, String> maskInfo) throws IOException {

		reqUrl = removeDefaultPort(reqUrl);

		boolean isHttps = reqUrl.startsWith("https://");
		String eMessage = null;
//		if (isHttps) {
//			// 取消 https 安全性驗證
//			disableCertificateValidation(); //委由 `信任所有證書的 SSLContext` 做完了
//		}

		HttpResponse httpResponse = null;

		// 使用Apache HttpClinet
		HttpRespData respObj = new HttpRespData();
		method = method.toUpperCase();
		CloseableHttpClient httpClient = null;
		HttpClientBuilder httpClientBuilder = null;
		try {
			// 無使用 http connection pool
//			httpClientBuilder = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
//					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build());

			// Using http connection pool
			httpClientBuilder = getHttpClientBuilder();
		} catch (Exception e) {
			respObj.respStr = StackTraceUtil.logStackTrace(e);
		}

		if (!isRedirect) {
			httpClientBuilder.disableRedirectHandling();
		}

		httpClient = httpClientBuilder.build();
		HttpEntityEnclosingRequestBase httpReqBase = getRequestBase(method, reqUrl);
		if (rawData == null) {
			rawData = new String();
		}
		HttpEntity httpEntity = new ByteArrayEntity(rawData.getBytes());


			// print
			respObj.logger("--【URL】--");
			respObj.logger(reqUrl);
			respObj.logger("--【End】--\r\n");


			httpReqBase.setEntity(httpEntity);
			respObj.logger("【" + httpReqBase.getMethod() + "】\r\n");

			for (String key : httpHeader.keySet()) {
				String value = null;
				for (String v : httpHeader.get(key)) {
					if (value == null) {
						value = v;
					} else {
						value += "," + v;
					}
				}
				httpReqBase.setHeader(changeMockTestHeader(key), value);
			}
			httpReqBase.setHeader("Connection", "keep-alive");
			httpReqBase.setHeader("keep-alive", "timeout=59");
			httpReqBase.removeHeaders("Content-Length");
			httpReqBase.removeHeaders("host");
			// print Req header
			respObj.logger("--【Http Req Header】--");
			for (Header h : httpReqBase.getAllHeaders()) {
				respObj.logger("\tKey: " + h.getName() + ", Value: " + maskHeader(maskInfo, h.getName(), h.getValue()));
			}

			respObj.logger("--【End】--\r\n");

			// print
			respObj.logger("--【Req payload / Form Data】--");
			respObj.logger(maskBody(maskInfo, rawData));

			respObj.logger("--【End】--\r\n");

			try {
				httpResponse = httpClient.execute(httpReqBase);
			} catch (Exception e) {
				respObj.respHeader = new HashMap<String, List<String>>();
				StackTraceElement[] elements = e.getStackTrace();
				eMessage = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
			}

		// print
		respObj.logger("--【Http Resp Header】--");

		if (httpResponse != null) {
			respObj.logger("\tKey: http code, Value: " + httpResponse.getStatusLine());

			Header[] headers = httpResponse.getAllHeaders();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			for (Header h : headers) {
				List<String> valueList = new ArrayList<String>();
				valueList.add(h.getValue());
				map.put(h.getName(), valueList);
			}
			for (String key : map.keySet()) {
				List<String> valueList = map.get(key);
				String tmpValue = valueList.toString();
				//[ ] 符號總是位於 String 的第一個和最後一個字符，則可以使用 substring() 方法更有效地去除它們。
				tmpValue = tmpValue.substring(1, tmpValue.length() - 1);
				respObj.logger("\tKey: " + key + ", Value: " + maskHeader(maskInfo, key, tmpValue));
			}

			// prepare Http Result
			respObj.respHeader = new HashMap<String, List<String>>(map);

			Header server = httpResponse.getFirstHeader("Server");
			if (server != null) {
				respObj.logger("Server - " + server);
			}

		} else {
			respObj.logger("null");
		}

		respObj.logger("--【End】--\r\n");

		HttpEntity entity = null;
		if (httpResponse != null) {
			entity = httpResponse.getEntity();
			respObj.statusCode = httpResponse.getStatusLine().getStatusCode();
		} else {
			respObj.statusCode = 502; //因為 undertow, 不接受 -1, 學習 Nginx 改為 502
		}

		// print
		respObj.logger("--【Http status code】--");
		respObj.logger("" + respObj.statusCode);
		respObj.logger("--【End】--\r\n");

		if (httpResponse != null) {
			// get Stream
			InputStream is = null;
			if (entity != null) {
				is = entity.getContent();
			}
			// print
			respObj.logger("--【Resp payload....Return....】--");
			// 分類
			if (isEnableInputStream == true) {
				respObj.isEnableInputStream = true;
				respObj.respInputStreamObj = is;
				if (httpReqBase != null) {
					respObj.httpRequestBase = httpReqBase; // Using http connection pool, 取完 byte 要releaseConnection
				}

			} else {
				// 例如:ES查看index存不存在,不存在是為404但ErrorStream是null,會造成錯誤
				if (is != null) {
					respObj.respStr = toPrettyJson(read(is));
					respObj.logger(maskBody(maskInfo, respObj.respStr));
					// httpClient 必需 release Connection, 不是 abort, 因為 release connection是還到
					// connection pool, 而 aboart 是抛棄這個 connection,而且還會佔用 pool
					if (httpReqBase != null) {
						httpReqBase.releaseConnection();
					}
				}
			}

		} else {
			respObj.respStr = eMessage;
			respObj.logger(respObj.respStr);
		}


		return respObj;
	}
	
	public static String changeMockTestHeader(String key) {
		// 將前端傳來的 back...Test 改為正式的 Test
		if ("back-dgr-mock-test".equalsIgnoreCase(key)) {
			key = "dgr-mock-test";
		}
		return key;
	}

	public static String maskHeader(Map<String, String> maskInfo, String key, String header) {
		
		// 執行 Header Policy
		if (maskInfo != null) {
			String headerMaskPolicy = maskInfo.get("headerMaskPolicy");
			String headerMaskPolicySymbol = maskInfo.get("headerMaskPolicySymbol");
			String headerMaskKey = maskInfo.get("headerMaskKey");
			if (StringUtils.hasLength(headerMaskKey)) {
				String[] headerMaskKeyArr = headerMaskKey.split(",");
				int headerMaskPolicyNum = Integer.parseInt(maskInfo.get("headerMaskPolicyNum"));
				for (String headerKey : headerMaskKeyArr) {
					if (StringUtils.hasLength(key) && key.equalsIgnoreCase(headerKey)) {
						if (headerMaskPolicy.equals("1")) {
							if (header.length() > (headerMaskPolicyNum * 2)) {
								header = header.substring(0, headerMaskPolicyNum) + headerMaskPolicySymbol
										+ header.substring(header.length() - headerMaskPolicyNum);
							}
						}

						if (headerMaskPolicy.equals("2")) {
							if (header.length() > headerMaskPolicyNum) {
								header = header.substring(0, headerMaskPolicyNum) + headerMaskPolicySymbol;
							}
						}

						if (headerMaskPolicy.equals("3")) {
							if (header.length() > headerMaskPolicyNum) {
								header = headerMaskPolicySymbol + header.substring(headerMaskPolicyNum);
							}
						}

					}
				}
			}
		}
		
		// 遮蔽機敏資料(password/pwd/mima)
		// ex: 保留前5個字, 後方面部為 6 個星 ******
		// ServiceUtil.dataMask(header, httpClientBuilderNum, httpClientBuilderNum);
		if (hasSecretKeyWord(key)) {
			header = ServiceUtil.dataMask(header, 5, 5);
		}
		return header;
	}

	public static String maskBodyFromFormData(Map<String, String> maskInfo, String key, String value) {
		if (maskInfo != null) {

			String bodyMaskPolicySymbol = maskInfo.get("bodyMaskPolicySymbol");
			String bodyMaskKeyword = maskInfo.get("bodyMaskKeyword");
			if (StringUtils.hasLength(bodyMaskKeyword)) {
				String[] bodyMaskKeywordArr = bodyMaskKeyword.split(",");
				for (String keyword : bodyMaskKeywordArr) {
					if (key.equals(keyword)) {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < value.length(); i++) {
							sb.append(bodyMaskPolicySymbol);
						}
						return sb.toString();
					}
				}
			}
		}
		
		// 遮蔽機敏資料(password/pwd/mima)
		// ex: 保留前5個字, 後方面部為 6 個星 ******
		if (hasSecretKeyWord(key)) {
			value = ServiceUtil.dataMask(value, 5, 5);
		}
		
		return value;
	}

	private static String maskBodyProc(Map<String, String> maskInfo, String mbody) {
		// 遮蔽機敏資料(password/pwd/mima)
		// ex: pwd="12345" 替換為 pwd="123******
		// ex: <pwd>12345</pwd> 替換為 <pwd>12345********
		// keywowrd 只留右方 5 碼之後的字取代 6 個字元
		if (hasSecretKeyWord(mbody) && maskInfo == null) {
			maskInfo = new HashMap<String, String>();
			maskInfo.put("bodyMaskPolicy", "3");
			maskInfo.put("bodyMaskPolicySymbol", "***");
			maskInfo.put("bodyMaskPolicyNum", "6");
			maskInfo.put("bodyMaskKeyword", "password,pwd,mima");

		}

		if (maskInfo != null) {
			String bodyMaskPolicy = maskInfo.get("bodyMaskPolicy");
			String bodyMaskPolicySymbol = maskInfo.get("bodyMaskPolicySymbol");
			String bodyMaskKeyword = maskInfo.get("bodyMaskKeyword");

			if (StringUtils.hasLength(bodyMaskKeyword)) {

				String[] bodyMaskKeywordArr = bodyMaskKeyword.split(",");
				String bodyMaskPolicyNumStr = maskInfo.get("bodyMaskPolicyNum");
				if (bodyMaskPolicyNumStr == null) {
					bodyMaskPolicyNumStr = "0";
				}
				int bodyMaskPolicyNum = Integer.parseInt(bodyMaskPolicyNumStr);
				if (mbody.length() > (bodyMaskPolicyNum * 2)) {
					// 遮罩前後 bodyMaskPolicyNum 碼
					if ("1".equals(bodyMaskPolicy)) {
						for (String key : bodyMaskKeywordArr) {
							int startIndex = 0;
							while (mbody.indexOf(key, startIndex) >= 0) {
								int matchIndex = mbody.indexOf(key, startIndex);

								int startindex = matchIndex - bodyMaskPolicyNum;
								if (startindex < 0) {
									startindex = 0;
								}
								int endindex = matchIndex + key.length() + bodyMaskPolicyNum;
								if (endindex > mbody.length() - 1) {
									endindex = mbody.length() - 1;
								}

								mbody = mbody.substring(0, startindex) + bodyMaskPolicySymbol
										+ mbody.substring(matchIndex, matchIndex + key.length())
										+ bodyMaskPolicySymbol + mbody.substring(endindex);

								startIndex = matchIndex + key.length() + 1;
							}
						}
						return mbody;
					}
					// 遮罩前 bodyMaskPolicyNum 碼
					if ("2".equals(bodyMaskPolicy)) {
						for (String key : bodyMaskKeywordArr) {
							int startIndex = 0;
							while (mbody.indexOf(key, startIndex) >= 0) {
								int matchIndex = mbody.indexOf(key, startIndex);

								int startindex = matchIndex - bodyMaskPolicyNum;
								if (startindex < 0) {
									startindex = 0;
								}

								mbody = mbody.substring(0, startindex) + bodyMaskPolicySymbol
										+ mbody.substring(matchIndex);

								startIndex = matchIndex + key.length() + 1;
							}
						}
						return mbody;

					}
					// 遮罩後 bodyMaskPolicyNum 碼
					if ("3".equals(bodyMaskPolicy)) {
						String lowerMbody = mbody.toLowerCase();
						for (String key : bodyMaskKeywordArr) {
							String lowerKey = key.toLowerCase();
							int startIndex = 0;
							while (lowerMbody.indexOf(lowerKey, startIndex) >= 0) {
								int matchIndex = lowerMbody.indexOf(lowerKey, startIndex);

								int endindex = matchIndex + lowerKey.length() + bodyMaskPolicyNum;
								if (endindex > lowerMbody.length() - 1) {
									endindex = lowerMbody.length();
								}
								lowerMbody = lowerMbody.substring(0, matchIndex + lowerKey.length()) + bodyMaskPolicySymbol
										+ lowerMbody.substring(endindex);

								startIndex = matchIndex + lowerKey.length() + 1;
								// 要記得接回來
								mbody = lowerMbody;
							}
						}
						return mbody;
					}
					// 遮罩後 bodyMaskPolicyNum 碼 正規表示法
					if ("4".equals(bodyMaskPolicy)) {
						String regex = "(?<jsonField>\\\"(" + bodyMaskKeyword
								+ ")\\\"\\s*?:)\\s*?(?<jsonvalue>(true|false|\\d+|\\\"\\{.*?\\}\\\")|\\\".*?\\\"|\\[.*?\\])|(?<xmlField><(?<fieldname>"
								+ bodyMaskKeyword + ")>)\\s*?(?<xmlvalue>.*?)\\s*?(?<xmlEnd><\\/\\k<fieldname>>)";
						Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

						Matcher matcher = pattern.matcher(mbody);// 不接受 null

						// 遍歷所有匹配的字段，進行替換
						StringBuffer result = new StringBuffer();
						while (matcher.find()) {
							String field = matcher.group("jsonField");

							if (!StringUtils.hasLength(field)) {
								field = matcher.group("xmlField");
							}
							String fieldValue = matcher.group("jsonvalue");

							if (!StringUtils.hasLength(fieldValue))
								fieldValue = matcher.group("xmlvalue");

							if (StringUtils.hasLength(fieldValue)) {
								String maskedField = maskField(fieldValue, bodyMaskPolicyNum, bodyMaskPolicySymbol);
								String xmlEnd = StringUtils.hasLength(matcher.group("xmlEnd"))
										? matcher.group("xmlEnd")
										: new String();

								matcher.appendReplacement(result, (field + maskedField + xmlEnd));
							}
						}
						matcher.appendTail(result);
						mbody = result.toString();

					}

				}
			}
		}

		return mbody;
	}
	public static String maskBody(Map<String, String> maskInfo, String mbody) {
		// API List 有設定
		if (maskInfo != null) {
			mbody = maskBodyProc(maskInfo, mbody);
		}
		
		// 統一無腦做 "password,pwd,mima"
		mbody = maskBodyProc(null, mbody);
		return mbody;
	}

	private static String maskField(String fieldValue, int bodyMaskPolicyNum, String bodyMaskPolicySymbol) {

		if (fieldValue.length() > bodyMaskPolicyNum) {

			return fieldValue.substring(0, bodyMaskPolicyNum) + bodyMaskPolicySymbol
					+ fieldValue.charAt(fieldValue.length() - 1);

		}
		return fieldValue;
	}

	@SuppressWarnings("unused")
	private static String getFormBody(Map<String, String> formData, String boundary,
			Map<String, String> partContentTypes) {
		return getFormBodyList(tranferOne2List(formData), boundary, partContentTypes);
	}

	private static String getFormBodyList(Map<String, List<String>> formData, String boundary,
			Map<String, String> partContentTypes) {
		StringBuffer strBf = new StringBuffer();
		// handle map to String
		formData.forEach((k, vlist) -> {
			vlist.forEach((v) -> {
				// 起界
				strBf.append(String.format("--%s\r\n", boundary));

				// name
				strBf.append(String.format("Content-Disposition: form-data; name=\"%s\"\r\n", k));

				// non "Content-Type" , ex: Content-Type: applicaiton/json
				strBf.append(String.format("Content-Type: %s \r\n", partContentTypes.get(k)));

				// 換行分隔符
				strBf.append("\r\n");

				// value
				strBf.append(v);

				// 換行分隔符
				strBf.append("\r\n");
			});
		});
		// 終界
		strBf.append(String.format("--%s--", boundary));
		return strBf.toString();
	}

	public static final String toPrettyJson(String json) {
		if (!json.isEmpty()) {
			ObjectMapper om = new ObjectMapper();
			try {
				Object resp = om.readValue(json, Object.class);
				json = om.writerWithDefaultPrettyPrinter().writeValueAsString(resp);
			} catch (Exception e) {
				System.out.println("Not JSON Format...");
			}
		}

		return json;
	}

	private static void sendData(HttpURLConnection conn, byte[] data) throws IOException {
		try (OutputStream os = conn.getOutputStream(); DataOutputStream wr = new DataOutputStream(os);) {
			byte[] outputInBytes = data;
			// 傳送資料
			// os.write(outputInBytes);
			wr.write(outputInBytes);
		}
	}

	public static String get(String reqUrl, String data, Consumer<HttpURLConnection> consumer) {
		try {
			if (data != null && !data.isEmpty()) {
				reqUrl += "?" + data;
			}
			URL url = new URL(reqUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if (consumer != null) {
				consumer.accept(conn);
			}

			return read(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String post(String reqUrl, String data, Consumer<HttpURLConnection> consumer) {
		try {
			URL url = new URL(reqUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			if (consumer != null) {
				consumer.accept(conn);
			}
			conn.setDoOutput(true);
			conn.setDoInput(true);

			DataOutputStream wr = null;
			try {
				byte[] outputInBytes = data.getBytes("UTF-8");
				OutputStream os = conn.getOutputStream();
				// 傳送資料
				os.write(outputInBytes);
				os.close();
			} catch (IOException exception) {
				exception.printStackTrace();
				throw exception;
			} finally {
				closeQuietly(wr);
			}

			// 分類
			InputStream is = null;
			if (conn.getResponseCode() >= 400) {
				is = conn.getErrorStream();
			} else {
				is = conn.getInputStream();
			}

			return read(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String read(InputStream is) throws IOException {
		StringBuilder body = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
			CharBuffer buffer = CharBuffer.allocate(800);
			int nRead = -1;
			while ((nRead = in.read(buffer)) > 0) {
				buffer.flip();
				body.append(buffer.toString());
			}
			buffer.clear();
		}
		return body.toString();
	}

	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ex) {
		}
	}

	public static void disableCertificateValidation() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new TrustAllManager() };

		// Ignore differences between given hostname and certificate hostname
		HostnameVerifier hv = new TrustAllHostnameVerifier();

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception e) {
		}
	}

	public static SSLContext disableWssValidation() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustmanagers = new TrustManager[] { new X509ExtendedTrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
					throws CertificateException {
				// TODO Auto-generated method stub

			}
		} };

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustmanagers, new SecureRandom());
		SSLContext.setDefault(sslContext);

		return sslContext;
	}

	private static class TrustAllManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	}

	private static class TrustAllHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static String byte2Hex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++)
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		return result;
	}

	/**
	 * 提供檔案上傳功能
	 */
	public static HttpRespData httpReqByFormData(String reqUrl, String method, Map<String, String> textMap,
			Map<String, String> fileMap, Map<String, String> httpHeader, boolean isEnableInputStream,
			boolean isRedirect) throws IOException {
		return httpReqByFormDataList(reqUrl, method, tranferOne2List(textMap), tranferOne2List(fileMap),
				tranferOne2List(httpHeader), isEnableInputStream, isRedirect, getPartMapByNull(textMap));
	}

	/**
	 * 提供檔案上傳功能
	 */
	public static HttpRespData httpReqByFormData(String reqUrl, String method, Map<String, String> textMap,
			Map<String, String> fileMap, Map<String, String> httpHeader, boolean isEnableInputStream,
			boolean isRedirect, Map<String, String> partContentTypes) throws IOException {
		return httpReqByFormDataList(reqUrl, method, tranferOne2List(textMap), tranferOne2List(fileMap),
				tranferOne2List(httpHeader), isEnableInputStream, isRedirect, partContentTypes);
	}

	/**
	 * 提供檔案上傳功能
	 */
	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, Map<String, List<String>> textMap,
			Map<String, List<String>> fileMap, Map<String, List<String>> httpHeader, boolean isEnableInputStream,
			boolean isRedirect) throws IOException {

		return httpReqByFormDataList(reqUrl, method, textMap, fileMap, httpHeader, isEnableInputStream, isRedirect,
				getPartMapByNull(textMap), null);
	}

	/**
	 * 提供檔案上傳功能
	 * 
	 * @param maskInfo
	 */
	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, Map<String, List<String>> textMap,
			Map<String, List<String>> fileMap, Map<String, List<String>> httpHeader, boolean isEnableInputStream,
			boolean isRedirect, Map<String, String> maskInfo) throws IOException {

		return httpReqByFormDataList(reqUrl, method, textMap, fileMap, httpHeader, isEnableInputStream, isRedirect,
				getPartMapByNull(textMap), maskInfo);
	}

	/**
	 * 提供檔案上傳功能
	 */
	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, Map<String, List<String>> textMap,
			Map<String, List<String>> fileMap, Map<String, List<String>> httpHeader, boolean isEnableInputStream,
			boolean isRedirect, Map<String, String> partContentTypes, Map<String, String> maskInfo) throws IOException {

		final String BOUNDARY = "JohnBoundary" + UUID.randomUUID().toString();
		// form body
//		byte[] formBody = getFormBody(textMap, fileMap, BOUNDARY);
		Map<String, Object> formBodyMap = getFormBody(textMap, fileMap, BOUNDARY, partContentTypes, maskInfo);
		byte[] formBody = (byte[]) formBodyMap.get("data");
		byte[] formBody2Hex = (byte[]) formBodyMap.get("logData");
		return httpReqByFormDataList(reqUrl, method, BOUNDARY, formBody, formBody2Hex, httpHeader, isEnableInputStream,
				isRedirect, maskInfo);
	}

	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, String BOUNDARY, byte[] formBody,
			byte[] formBody2Hex, //
			Map<String, List<String>> httpHeader, boolean isEnableInputStream, boolean isRedirect) throws IOException {
		return httpReqByFormDataList(reqUrl, method, BOUNDARY, formBody, formBody2Hex, httpHeader, isEnableInputStream,
				isRedirect, null);
	}

	/**
	 * 提供檔案上傳功能
	 */
	public static HttpRespData httpReqByFormDataList(String reqUrl, String method, String BOUNDARY, byte[] formBody,
			byte[] formBody2Hex, //
			Map<String, List<String>> httpHeader, boolean isEnableInputStream, boolean isRedirect,
			Map<String, String> maskInfo) throws IOException {

		reqUrl = removeDefaultPort(reqUrl);

		boolean isHttps = reqUrl.startsWith("https://");
		if (isHttps) {
			// 取消 https 安全性驗證
			disableCertificateValidation();
		}

		HttpRespData respObj = new HttpRespData();

		// print
		respObj.logger("--【URL】--");
		respObj.logger(reqUrl);
		respObj.logger("--【End】--\r\n");

		URL url = new URL(reqUrl);
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		method = method.toUpperCase();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setInstanceFollowRedirects(isRedirect);
		conn.setRequestMethod(method);
		respObj.logger("【" + conn.getRequestMethod() + "】\r\n");

		// Http Req Header
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Cache-Control", "no-cache");
		Map<String, List<String>> httpHeaderNotFound = new HashMap<>();
		if (httpHeader != null && httpHeader.size() > 0) {
			httpHeader.forEach((k, list) -> {
				list.forEach((v) -> {
					conn.addRequestProperty(changeMockTestHeader(k), v);
					handleNotFoundHttpHeaderList(httpHeaderNotFound, k, v);
				});
			});
		}
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

		// print Req header
		respObj.logger("--【Http Req Header】--");
		Map<String, List<String>> reqHeaderMap = conn.getRequestProperties();
		reqHeaderMap.forEach((k, list) -> {
			respObj.logger("\tKey: " + k + ", Value: " + maskHeader(maskInfo, k, list.toString()));
		});
		printCantFoundHeaderNameList(httpHeaderNotFound, respObj); // print can't found header name
		respObj.logger("--【End】--\r\n");

		// print
		respObj.logger("--【Req payload / Form Data】--");
		if (formBody != null && formBody.length > 0) {
			if (formBody2Hex != null && formBody2Hex.length > 0) {
				String strFormBody = new String(formBody2Hex); // File 變為 Hex String
				respObj.logger(strFormBody);
			} else {
				String strFormBody = new String(formBody); // 原始 File RAW byte
				respObj.logger(strFormBody);
			}
		}

		respObj.logger("--【End】--\r\n");

		// send
		String eMessage = null;
		try {
			sendData(conn, formBody);
		} catch (Exception e) {
			respObj.respHeader = new HashMap<String, List<String>>();
			StackTraceElement[] elements = e.getStackTrace();
			eMessage = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
		}

		// print
		respObj.logger("--【Http Resp Header】--");
		if (eMessage == null) {
			respObj.logger("\tKey: http code, Value: " + conn.getHeaderField(null));
			Map<String, List<String>> map = conn.getHeaderFields();
			respObj.respHeader = new HashMap<String, List<String>>(map); // deep copy

			respObj.respHeader.forEach((k, list) -> {
				respObj.logger("\tKey: " + k + ", Value: " + maskHeader(maskInfo, k, list.toString()));
			});

			String server = conn.getHeaderField("Server");
			if (server != null) {
				respObj.logger("Server - " + server);
			}

			// prepare Http Result
			respObj.statusCode = conn.getResponseCode();

		} else {
			respObj.logger("null");
			respObj.statusCode = 502; //因為 undertow, 不接受 -1, 學習 Nginx 改為 502
		}

		respObj.logger("--【End】--\r\n");

		// print
		respObj.logger("--【Http status code】--");
		respObj.logger("" + respObj.statusCode);
		respObj.logger("--【End】--\r\n");

		if (eMessage == null) {
			// get Stream
			InputStream is = null;
			if (conn.getResponseCode() >= 400) {
				is = conn.getErrorStream();
			} else {
				is = conn.getInputStream();
			}
			// print
			respObj.logger("--【Resp payload....Return....】--");
			// 分類
			if (isEnableInputStream == true) {
				respObj.isEnableInputStream = true;
				respObj.respInputStreamObj = is;
			} else {
				respObj.respStr = toPrettyJson(read(is));
				respObj.logger(maskBody(maskInfo, respObj.respStr));
			}

		} else {
			respObj.respStr = eMessage;
			respObj.logger(respObj.respStr);
		}



		return respObj;
	}

	private static Map<String, Object> getFormBody(Map<String, List<String>> textMap, Map<String, List<String>> fileMap,
			String boundary, Map<String, String> partContentTypes, Map<String, String> maskInfo) throws IOException {

		List<byte[]> formBodyParts = new ArrayList<>();
		List<byte[]> formBodyParts_File2Hex = new ArrayList<>();
		if (textMap != null) {
			// Form Text k-v Data
			String k;
			List<String> vlist;
			for (Map.Entry<String, List<String>> entries : textMap.entrySet()) {
				k = entries.getKey();
				vlist = entries.getValue();
				for (String v : vlist) {
					Map<String, Object> data = getFormBodyPart(k, null, v.getBytes(), boundary, null,
							partContentTypes.get(k), maskInfo);

					ObjectMapper objectMapper = new ObjectMapper();
//					formBodyParts.add((byte[]) data.get("data"));
//					formBodyParts_File2Hex.add((byte[]) data.get("logData"));
					if (data.get("data") instanceof byte[]) {
						formBodyParts.add((byte[]) data.get("data"));
					} else {
						formBodyParts.add(objectMapper.writeValueAsBytes(data.get("data")));
					}

					formBodyParts_File2Hex.add(objectMapper.writeValueAsBytes(data.get("logData")));

				}
			}
		}
		if (fileMap != null) {
			// Form File k-v Data
			String k;
			List<String> vlist;
			for (Map.Entry<String, List<String>> entries : fileMap.entrySet()) {
				k = entries.getKey();
				vlist = entries.getValue();
				for (String v : vlist) {
					// 從 Path 讀取 File 內容
					File file = new File(v);
					String filename = file.getName();
					byte[] data = getFormBodyPart(k, filename, file, boundary, null, null, null);
					formBodyParts.add(data);
					byte[] hexData = (HttpUtil.PREFIX_Sha256_Hex + HexStringUtils.toString(SHA256Util.getSHA256(data)))
							.getBytes();
					formBodyParts_File2Hex.add(hexData);
//					byte[] data2 = getFormBodyPart(k, filename, file, boundary, null, null, maskInfo);
//					formBodyParts_File2Hex.add(data2);
				}
			}
		}
		byte inBuf[] = getFormBody(formBodyParts, boundary, partContentTypes);
		byte inBuf2Hex[] = getFormBody(formBodyParts_File2Hex, boundary, partContentTypes);

		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("data", inBuf);
		dataMap.put("logData", inBuf2Hex);
		return dataMap;
	}

	public static byte[] getFormBody(List<byte[]> formBodyParts, String boundary, Map<String, String> partContentTypes)
			throws IOException {
		if (CollectionUtils.isEmpty(formBodyParts)) {
			return null;
		}

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Iterator<byte[]> it = formBodyParts.iterator();
			byte[] formBodyPart;
			while (it.hasNext()) {
				formBodyPart = it.next();
				if (formBodyPart != null) {
					out.write(formBodyPart);
				}
				if (!it.hasNext()) {
					byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
					out.write(endData);
				}
			}
			out.flush();
			return out.toByteArray();
		}
	}

	public static byte[] getFormBodyPart(String name, String filename, File file, String boundary, StringBuffer logger,
			String eachContentType, Map<String, String> maskInfo) throws IOException {
		byte[] content = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataInputStream in = new DataInputStream(new FileInputStream(file));) {
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				baos.write(bufferOut, 0, bytes);
			}
			content = baos.toByteArray();
		}
		return (byte[]) getFormBodyPart(name, filename, content, boundary, logger, eachContentType, maskInfo)
				.get("data");
	}

	public static Map<String, Object> getFormBodyPart(String name, String filename, byte[] content, String boundary,
			StringBuffer logger, String eachContentType) throws IOException {
		return getFormBodyPart(name, filename, content, boundary, logger, eachContentType, null);
	}

	public static Map<String, Object> getFormBodyPart(String name, String filename, byte[] content, String boundary,
			StringBuffer logger, String eachContentType, Map<String, String> maskInfo) throws IOException {
		Map<String, Object> dataMap = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append("\r\n");
		sb.append("--" + boundary + "\r\n");
		String contentType = "";
		if (StringUtils.hasLength(filename)) {
			MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
			if (!StringUtils.hasLength(eachContentType)) {
				contentType = fileTypeMap.getContentType(filename);
			} else {
				contentType = eachContentType;
			}

			sb.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
			sb.append("Content-Type:" + contentType + "\r\n");
		} else {
			sb.append("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
			if (eachContentType != null) {
				sb.append("Content-Type:" + eachContentType + "\r\n");

			}
		}
		sb.append("\r\n");
		Map<String, byte[]> logDataMap = new HashMap<>();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(sb.toString().getBytes());

			// write header to logger
			if (logger != null) {
				logger.append(sb);
			}
			if (content != null && content.length > 0) {
				baos.write(content);
				// write content to logger

				String contentStr = "";
				try {
					if (StringUtils.hasLength(filename)) {
						contentStr = HttpUtil.PREFIX_Sha256_Hex
								+ HexStringUtils.toString(SHA256Util.getSHA256(content));
					} else {
						contentStr = new String(content);

					}

				} catch (Exception e1) {
					try {
						contentStr = HexStringUtils.toString(content);
					} catch (Exception e2) {
						contentStr = "[Unreadble content]";
					}
				}
				contentStr = maskBodyFromFormData(maskInfo, name, contentStr);

				if (contentStr.length() > 100) {
					contentStr = contentStr.substring(0, 100) + "...";
				}

				if (logger != null) {
					logger.append(contentStr);
				}
				logDataMap.put("contentD", sb.toString().getBytes());
				logDataMap.put("content", contentStr.getBytes());
				dataMap.put("data", baos.toByteArray());
				dataMap.put("logData", logDataMap);

			}
			baos.flush();

			return dataMap;
		}
	}

	private static void allowMethods(String... methods) {
		try {
			Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

			methodsField.setAccessible(true);

			String[] oldMethods = (String[]) methodsField.get(null);
			Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
			methodsSet.addAll(Arrays.asList(methods));
			String[] newMethods = methodsSet.toArray(new String[0]);

			methodsField.set(null/* static field */, newMethods);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 保留下來 http header 特殊字, ex: authorization
	 */
	private static void handleNotFoundHttpHeader(Map<String, String> httpHeaderNotFound, String k, String v) {
		handleNotFoundHttpHeaderList(tranferOne2List(httpHeaderNotFound), k, v);
	}

	private static void handleNotFoundHttpHeaderList(Map<String, List<String>> httpHeaderNotFound, String k, String v) {
		for (int i = 0; i < EXCLUDE_HEADERS.length; i++) {
			String headerv = EXCLUDE_HEADERS[i];
			if (headerv.equalsIgnoreCase(k)) {
				List<String> vlist = httpHeaderNotFound.get(k);
				if (vlist == null) {
					vlist = new LinkedList<String>();
					httpHeaderNotFound.put(k, vlist);
				}
				vlist.add(v);
				break;
			}
		}
	}

	/**
	 * 自動加入 Java 無法印出來的 header name
	 */
	private static void printCantFoundHeaderName(Map<String, String> httpHeaderNotFound, HttpRespData respObj) {
		printCantFoundHeaderNameList(tranferOne2List(httpHeaderNotFound), respObj);
	}

	private static void printCantFoundHeaderNameList(Map<String, List<String>> httpHeaderNotFound,
			HttpRespData respObj) {
		httpHeaderNotFound.forEach((k, vlist) -> {
			respObj.logger("\tKey: " + k + ", Value: " + vlist.toString());
		});
	}

	private static <T> Map<String, String> getPartMapByNull(Map<String, T> formData) {

		if (formData == null) {
			return Collections.emptyMap();
		}

		Map<String, String> partContentTypes = new HashMap<>();
		formData.forEach((key, value) -> {
			partContentTypes.put(key, "");
		});
		return partContentTypes;

	}

	/**
	 * map tranfer to mapList
	 */
	private static Map<String, List<String>> tranferOne2List(Map<String, String> map) {
		Map<String, List<String>> newMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(map)) {
			map.forEach((k, v) -> {
				newMap.put(k, Arrays.asList(v));
			});
		}
		return newMap;
	}

	private static Map<String, List<String>> tranferOnePath2List(Map<String, Path> map) {
		Map<String, List<String>> newMap = new HashMap<>();
		map.forEach((k, p) -> {
			newMap.put(k, Arrays.asList(p.toAbsolutePath().toString()));
		});
		return newMap;
	}

	public static String deflate_UnCompress(byte httpRespArray[]) throws IOException, DataFormatException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		Inflater decompressor = new Inflater();
		try {
			decompressor.setInput(httpRespArray);
			final byte[] buf = new byte[1024];
			while (!decompressor.finished()) {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			}
		} finally {
			decompressor.end();
		}
		// Decode the bytes into a String
		String outputString = new String(bos.toByteArray(), "UTF-8");
		return outputString;
	}

	public static byte[] deflate_compress(byte datas[]) throws IOException {
		// Compress the bytes
		Deflater zipDeflater = new Deflater();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			zipDeflater.setInput(datas);
			zipDeflater.finish();
			byte[] buffer = new byte[1024];
			int count = 0;
			while (!zipDeflater.finished()) {
				count = zipDeflater.deflate(buffer);
				stream.write(buffer, 0, count);
			}
		} finally {
			stream.close();
			zipDeflater.end();
		}
		return stream.toByteArray();
	}

	public static String gzip_UnCompress(byte httpRespArray[]) throws IOException {
		InputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(httpRespArray));
		Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader buffered = new BufferedReader(decoder);
		StringBuffer respStrBuf = new StringBuffer();
		String thisLine = null;
		while ((thisLine = buffered.readLine()) != null) {
			respStrBuf.append(thisLine);
		}
		return respStrBuf.toString();
	}

	public static byte[] gzip_compress(byte datas[]) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			OutputStream out = new GZIPOutputStream(baos);
			out.write(datas);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}

	public static String getUnComporessRawString(String UnCompressMsg) {
		UnCompressMsg = UnCompressMsg.replaceFirst(HttpUtil.PREFIX_UnDeflate_DATA, UnCompressMsg);
		UnCompressMsg = UnCompressMsg.replaceFirst(HttpUtil.PREFIX_UnZip_DATA, UnCompressMsg);
		return UnCompressMsg;
	}

	public static String getLogStr_UnCompress(String compressMehtod, byte[] httpRespArray) throws Exception {
		if (compressMehtod == null || compressMehtod.length() == 0) {
			return null;
		}
		if (compressMehtod.contains("deflate")) {
			return HttpUtil.PREFIX_UnDeflate_DATA + HttpUtil.deflate_UnCompress(httpRespArray);
		} else if (compressMehtod.contains("gzip")) {
			return HttpUtil.PREFIX_UnZip_DATA + HttpUtil.gzip_UnCompress(httpRespArray);
		}
		return null;
	}

	public static String getLogStr_UnCompress(Map<String, List<String>> respHeader, byte[] httpRespArray)
			throws Exception {
		if (respHeader.get("Content-Encoding") == null) {
			return null;
		}
		if (respHeader.get("Content-Encoding").contains("deflate")) {
			return HttpUtil.PREFIX_UnDeflate_DATA + HttpUtil.deflate_UnCompress(httpRespArray);
		} else if (respHeader.get("Content-Encoding").contains("gzip")) {
			return HttpUtil.PREFIX_UnZip_DATA + HttpUtil.gzip_UnCompress(httpRespArray);
		}
		return null;
	}

	// 遮蔽機敏資料(password/pwd/mima)
	public static boolean hasSecretKeyWord(String dataString) {
		if (StringUtils.hasLength(dataString)) {
			String lower = dataString.toLowerCase();
			return lower.contains("pwd") | lower.contains("password") | lower.contains("mima");
		}
		return false;
	}
}
