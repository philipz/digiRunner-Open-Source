package tpi.dgrv4.gateway.keeper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.apache.hc.core5.http.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;

public class ProxyServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CloseableHttpClient proxyClient;

	private String targetHost;

	@Override
	public void init() throws ServletException {
		proxyClient = createHttpClient();
	}

	protected CloseableHttpClient createHttpClient() {
		try {
			return HttpUtil.getHttpClientBuilder().setDefaultRequestConfig(null).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return null;
	}

	protected HttpHost getTargetHost(HttpServletRequest servletRequest) {
		targetHost = servletRequest.getScheme() + "://" + servletRequest.getLocalAddr() + ":" + TPILogger.PORT;
		HttpHost host = HttpHost.create(targetHost);
		return host;
	}

	@Override
	protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
			throws ServletException, IOException {

		String method = servletRequest.getMethod();
		String proxyRequestUri = rewriteUrlFromRequest(servletRequest);

		HttpRequest proxyRequest;

		if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null
				|| servletRequest.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
			proxyRequest = newProxyRequestWithEntity(method, proxyRequestUri, servletRequest);
		} else {
			proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
		}

		copyRequestHeaders(servletRequest, proxyRequest);

		HttpResponse proxyResponse = null;
		try {
			// Execute the request
			proxyResponse = doExecute(servletRequest, servletResponse, proxyRequest);

			// Process the response:

			int statusCode = proxyResponse.getStatusLine().getStatusCode();
			servletResponse.setStatus(statusCode);

			copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

			if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {

				servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
			} else {

				copyResponseEntity(proxyResponse, servletResponse, proxyRequest, servletRequest);
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (proxyResponse != null)
				EntityUtils.consumeQuietly(proxyResponse.getEntity());
		}
	}

	protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse,
			HttpRequest proxyRequest, HttpServletRequest servletRequest) throws IOException {
		HttpEntity entity = proxyResponse.getEntity();
		if (entity != null) {
			if (entity.isChunked()) {
				InputStream is = entity.getContent();
				OutputStream os = servletResponse.getOutputStream();
				byte[] buffer = new byte[10 * 1024];
				int read;
				while ((read = is.read(buffer)) != -1) {
					os.write(buffer, 0, read);

					if (is.available() == 0) {
						os.flush();
					}
				}
			} else {
				OutputStream servletOutputStream = servletResponse.getOutputStream();
				entity.writeTo(servletOutputStream);
			}
		}
	}

	protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
			HttpRequest proxyRequest) throws IOException {
		TPILogger.tl.debug("proxy " + servletRequest.getMethod() + " uri: " + servletRequest.getRequestURI() + " -- "
				+ proxyRequest.getRequestLine().getUri());
		return proxyClient.execute(getTargetHost(servletRequest), proxyRequest);
	}

	protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
		StringBuilder uri = new StringBuilder(500);
		uri.append(servletRequest.getRequestURI());

		String queryString = servletRequest.getQueryString();
		String fragment = null;
		if (queryString != null) {
			int fragIdx = queryString.indexOf('#');
			if (fragIdx >= 0) {
				fragment = queryString.substring(fragIdx + 1);
				queryString = queryString.substring(0, fragIdx);
			}
		}
		if (queryString != null && queryString.length() > 0) {
			uri.append('?');
			uri.append(queryString);
		}

		if (fragment != null) {
			uri.append('#');
			uri.append(fragment);
		}
		return uri.toString();
	}

	static HeaderGroup hopByHopHeaders;
	static {
		hopByHopHeaders = new HeaderGroup();
		String[] headers = new String[] { HttpHeaders.CONNECTION, HttpHeaders.TE, HttpHeaders.PROXY_AUTHENTICATE,
				HttpHeaders.PROXY_AUTHORIZATION, HttpHeaders.TRAILER, HttpHeaders.TRANSFER_ENCODING,
				HttpHeaders.UPGRADE, HttpHeaders.KEEP_ALIVE };
		for (String header : headers) {
			hopByHopHeaders.addHeader(new BasicHeader(header, null));
		}
	}

	protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
		Enumeration<String> enumerationOfHeaderNames = servletRequest.getHeaderNames();
		while (enumerationOfHeaderNames.hasMoreElements()) {
			String headerName = enumerationOfHeaderNames.nextElement();
			Enumeration<String> headers = servletRequest.getHeaders(headerName);
			while (headers.hasMoreElements()) {// sometimes more than one value
				String headerValue = headers.nextElement();
				if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
					HttpHost host = getTargetHost(servletRequest);
					headerValue = host.getHostName();
					if (host.getPort() != -1)
						headerValue += ":" + host.getPort();
				}
				if (!headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
						&& !hopByHopHeaders.containsHeader(headerName))
					proxyRequest.addHeader(headerName, headerValue);
			}
		}
	}

	protected HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri,
			HttpServletRequest servletRequest) throws IOException {
		HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
		eProxyRequest
				.setEntity(new InputStreamEntity(servletRequest.getInputStream(), getContentLength(servletRequest)));
		return eProxyRequest;
	}

	private long getContentLength(HttpServletRequest request) {
		String contentLengthHeader = request.getHeader("Content-Length");
		if (contentLengthHeader != null) {
			return Long.parseLong(contentLengthHeader);
		}
		return -1L;
	}

	protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		for (Header header : proxyResponse.getAllHeaders()) {
			String headerName = header.getName();
			if (!hopByHopHeaders.containsHeader(headerName)) {
				String headerValue = header.getValue();
				if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
					servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
				} else {
					servletResponse.addHeader(headerName, headerValue);
				}
			}
		}
	}

	protected String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
		final String targetUri = servletRequest.getRequestURI();
		if (theUrl.startsWith(targetUri)) {
			StringBuffer curUrl = servletRequest.getRequestURL();// no query
			int pos;
			if ((pos = curUrl.indexOf("://")) >= 0) {
				if ((pos = curUrl.indexOf("/", pos + 3)) >= 0) {
					curUrl.setLength(pos);
				}
			}
			curUrl.append(servletRequest.getContextPath());
			curUrl.append(servletRequest.getServletPath());
			curUrl.append(theUrl, targetUri.length(), theUrl.length());
			return curUrl.toString();
		}
		return theUrl;
	}

}
