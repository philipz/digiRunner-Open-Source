package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;

@Service
public class KibanaService2 {

	private TPILogger logger = TPILogger.tl;

	@Autowired
	private TsmpSettingCacheProxy tsmpSettingCacheProxy;

	@Autowired
	private CApiKeyService capiKeyService;

	@Autowired
	private TsmpSettingService tsmpSettingService;

	public void login(HttpHeaders httpHeaders, String reportURL, HttpServletRequest request,
			HttpServletResponse response) {
		try {

			try {
				// 驗證CApiKey
				capiKeyService.verifyCApiKey(httpHeaders, false, false);
			} catch (Exception e) {
				ByteArrayInputStream bi = new ByteArrayInputStream(
						TsmpDpAaRtnCode._1522.getDefaultMessage().getBytes());
				response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
				IOUtils.copy(bi, response.getOutputStream());
				throw TsmpDpAaRtnCode._1522.throwing();
			}
			// 直接轉導 Kibana URL
			response.addHeader("kbn-xsrf", "true");
			response.setStatus(HttpServletResponse.SC_FOUND);
			response.sendRedirect(reportURL);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	protected String getKibanaURL() {

		String transferProtocol = getTsmpSettingService().getVal_KIBANA_TRANSFER_PROTOCOL();
		String kibanaHost = getTsmpSettingService().getVal_KIBANA_HOST();
		String kibanaPort = getTsmpSettingService().getVal_KIBANA_PORT();

		String strUrl = transferProtocol + "://" + kibanaHost + ":" + kibanaPort;
		URL url;
		try {
			url = new URL(strUrl);
			strUrl = HttpUtil.removeDefaultPort(url);
		} catch (MalformedURLException e1) {
			logger.error(StackTraceUtil.logStackTrace(e1));
		}
		return strUrl;
	}

	public void resource(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
			String payload) {
		StringBuffer sb = new StringBuffer();
		sb.append("\n ===============================================");

		try {
			String resourceURL = getKibanaURL() + request.getRequestURI();
			String querString = request.getQueryString();
			if (querString != null) {
				resourceURL = resourceURL + "?" + querString;
			}
			// 去掉 /kibana2
			String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();
			if (resourceURL.contains(kibanaPrefix)) {
				resourceURL = resourceURL.replaceFirst(kibanaPrefix, "");
			}
			sb.append("\nrequrli:　" + resourceURL);

			String method = request.getMethod();
			sb.append("\nmethod : " + method);

			// 請求Kibana URL

			URI targetUri = URI.create(resourceURL);
			HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().uri(targetUri).version(Version.HTTP_2);
			try {

				if (method.equalsIgnoreCase("GET")) {
					httpRequestBuilder.GET();
				} else {
					if (StringUtils.hasLength(payload)) {
						sb.append("\npayload : \n" + payload);
						httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));
					} else {
						httpRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());

					}

				}
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
			sb.append("\n ---- req Herder ---- ");
			Enumeration<String> httpHeaderKeys = request.getHeaderNames();
			//
			while (httpHeaderKeys.hasMoreElements()) {
				String key = httpHeaderKeys.nextElement();
				List<String> valueList = httpHeaders.get(key);
				if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && !key.equalsIgnoreCase(HttpHeaders.CONNECTION)
						&& !key.equalsIgnoreCase(HttpHeaders.HOST) && !key.equalsIgnoreCase("Keep-Alive")
						&& !key.equalsIgnoreCase("Transfer-Encoding")) {
					valueList.forEach(v -> {
						httpRequestBuilder.setHeader(key, v);
						sb.append("\n" + key + " :　" + v);
					});

				}

			}
			// 使用 basic auth
			String un = getTsmpSettingService().getVal_KIBANA_USER();
			String pw = getTsmpSettingService().getVal_KIBANA_PWD();
			String encodUNPW = Base64Util.base64Encode((un + ":" + pw).getBytes());
			httpRequestBuilder.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodUNPW);
			sb.append("\n ---- req Herder end ---- ");
			//不能自動轉導
			HttpClient httpClient = HttpClient.newBuilder().sslContext(getSSLContext()).followRedirects(Redirect.NEVER).build();

			HttpRequest httpRequest = httpRequestBuilder.build();

			HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
			sb.append("\nstatus : " + httpResponse.statusCode());
			sb.append("\nHTTP protocol : " + httpResponse.version());
			sb.append("\n ---- resp Herder ---- ");

			// 將請求完成的header複製一份到response
			java.net.http.HttpHeaders headerNames = httpResponse.headers();
			headerNames.map().entrySet().forEach(m -> {
				String key = m.getKey();
				if (m.getKey() != null) {
					m.getValue().forEach(v -> {
						if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && !":status".equals(key)
								&& !key.equalsIgnoreCase("Transfer-Encoding")) {
							response.addHeader(key, v);
							sb.append("\n" + key + " :　" + v);
						}

					});

				}
			});

			sb.append("\n ---- resp Herder end ---- ");
			response.setStatus(httpResponse.statusCode());
			response.setHeader("kbn-xsrf", "true");
			
			//將Kibana URL內容輸出
			OutputStream outputStream = response.getOutputStream();
			outputStream.write(httpResponse.body());
			outputStream.flush();
			sb.append("\n resp body len : " + httpResponse.body().length);

			sb.append("\n ===============================================");

			logger.trace(sb.toString());

			return;

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		    Thread.currentThread().interrupt();
		}
	}
	private SSLContext getSSLContext() {

		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new X509ExtendedTrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(final X509Certificate[] a_certificates, final String a_auth_type) {
				}

				public void checkServerTrusted(final X509Certificate[] a_certificates, final String a_auth_type) {
				}

				public void checkClientTrusted(final X509Certificate[] a_certificates, final String a_auth_type,
						final Socket a_socket) {
				}

				public void checkServerTrusted(final X509Certificate[] a_certificates, final String a_auth_type,
						final Socket a_socket) {
				}

				public void checkClientTrusted(final X509Certificate[] a_certificates, final String a_auth_type,
						final SSLEngine a_engine) {
				}

				public void checkServerTrusted(final X509Certificate[] a_certificates, final String a_auth_type,
						final SSLEngine a_engine) {
				}
			} }, null);
			return context;
		} catch (KeyManagementException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} catch (NoSuchAlgorithmException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return null;
	}
	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return tsmpSettingCacheProxy;
	}

	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
