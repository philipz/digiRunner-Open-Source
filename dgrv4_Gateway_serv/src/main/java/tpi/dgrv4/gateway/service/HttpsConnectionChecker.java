package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsConnectionChecker {
	/**
	 * sonar: 新增一個私有建構函式來隱藏隱式公共建構函式。 
	 * 實用程式類別不應該有公共建構函數. 
	 * sonar: Add a private constructor to hide the implicit public one.
	 * Utility classes should not have public constructors.
	 */
	private HttpsConnectionChecker() {
		throw new IllegalStateException("Utility class");
	}
    
    // 創建信任所有證書的 SSLContext
    // Create SSLContext that trusts all certificates
    private static SSLContext createTrustAllSSLContext() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// 信任所有證書的 SSLContext
				// An SSLContext that trusts all certificates
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				// 信任所有證書的 SSLContext
				// An SSLContext that trusts all certificates
			}
		} };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    /**
     * 簡單的連接檢查方法
     * Simple connection check method
     * @param strUrl 完整的URL（如 "https://example.com:443"）
     * @param strUrl Complete URL (e.g. "https://example.com:443")
     * @return 連接是否成功
     * @return Whether the connection was successful
     * @throws Exception 當連接失敗時拋出異常，包含完整的 stacktrace
     * @throws Exception When connection fails, throws exception with complete stacktrace
     */
    public static boolean checkConnection(String strUrl) throws Exception {
        // 創建SSLContext
        SSLContext sslContext = createTrustAllSSLContext();
    	
        // 創建SSLParameters並禁用主機名驗證
        SSLParameters sslParameters = sslContext.getDefaultSSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm(null);
        
        // 創建忽略證書的 HttpClient
        // Create HttpClient that ignores certificates
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .sslContext(sslContext)
            .sslParameters(sslParameters)  // 設置SSL參數, 不驗證 hostname
            .build();

        // 構建請求
        // Build request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(strUrl))
            .GET()
            .build();

     // 發送請求並獲取響應(不論是否為 200 , 只要有回應即可)
     // Send request and get response (any response is acceptable, regardless of whether it's 200 or not)
        client.send(request, HttpResponse.BodyHandlers.discarding());
        return true;
    }
}