package tpi.dgrv4.dpaa.es;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 提供了連接池和 keep-alive 機制,適合高併發場景。
 * 
 * 
 * 使用方式:
 * EsHttpClient client = new EsHttpClient();
 * int httpCode = client.bulkWrite("http://es:9200/_bulk", bulkBody, headers);
 */
public class EsHttpClient {
    private final CloseableHttpClient httpClient;
    
    public EsHttpClient() {
    	SSLContextBuilder builder = new SSLContextBuilder();
    	Registry<ConnectionSocketFactory> registry = null;
        try {
			builder.loadTrustMaterial(null, (chain, authType) -> true);
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build(), NoopHostnameVerifier.INSTANCE);
			
			registry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslsf)
					.build();
		} catch (NoSuchAlgorithmException e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
		} catch (KeyStoreException e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
		} catch (KeyManagementException e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
        
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        
        httpClient = HttpClients.custom()
            .setConnectionManager(cm)
            .setKeepAliveStrategy((response, context) -> 30 * 1000) // 30秒
            .build();
    }
    
    public int bulkWrite(String esUrl, String bulkBody, Map<String, String> headers) throws IOException {
        HttpPost post = new HttpPost(esUrl);
        headers.forEach(post::addHeader);
        post.setEntity(new StringEntity(bulkBody));
        
        return httpClient.execute(post, response -> response.getStatusLine().getStatusCode());
    }
}


