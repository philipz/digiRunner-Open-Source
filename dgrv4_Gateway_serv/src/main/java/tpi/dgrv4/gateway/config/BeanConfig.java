package tpi.dgrv4.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;

@Configuration
@ComponentScan({"tpi.dgrv4.common.utils"})	// Online Console
public class BeanConfig {

	/**
	 * 關於 jasypt 的設定值 (from application-*.properties)
	 */
	@Autowired
	private Singleton<JasyptEncryptorConfigurationProperties> configPropsSingleton;

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		// 關閉序列化空物件時的例外錯誤(允許序列化空物件)
		return objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}



}
