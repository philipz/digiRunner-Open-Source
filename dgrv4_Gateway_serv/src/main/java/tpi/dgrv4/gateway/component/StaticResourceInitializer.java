package tpi.dgrv4.gateway.component;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.validator.BCRIVFactory;
import tpi.dgrv4.common.component.validator.BCRIValidator;
import tpi.dgrv4.common.component.validator.BeforeControllerRespValueRtnCodeBuilder;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.dpaa.component.cache.proxy.DgrWebSocketMappingCacheProxy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.repository.BaseDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationBearerParser;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationDGRKParser;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParserFactory;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpOpenApiKeyCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ComposerWebSocketClientConn;
import tpi.dgrv4.gateway.service.DPB0115Service;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.service.WebSocketClientConn;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Component
public class StaticResourceInitializer {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DPB0115Service dpb0115Service;

	@Autowired
	private Environment env;

	@Autowired
	private TsmpOpenApiKeyCacheProxy tsmpOpenApiKeyCacheProxy;

	@Autowired
	private TsmpRtnCodeDao tsmpRtnCodeDao;

	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	private DgrWebSocketMappingCacheProxy dgrWebSocketMappingCacheProxy;
	
	@Autowired
	private TPILogger logger;
	
	@PostConstruct
	public void init() {
		ControllerUtil.setObjectMapper(getObjectMapper());
		ControllerUtil.setDPB0115Service(getDPB0115Service());
		TsmpAuthorizationDGRKParser.setTsmpOpenApiKeyCacheProxy(getTsmpOpenApiKeyCacheProxy());
		TsmpAuthorizationBearerParser.setEnv(getEnvironment());
		BeforeControllerRespValueRtnCodeBuilder.setTsmpRtnCodeDao(getTsmpRtnCodeDao());
		TsmpAuthorization.setTsmpSettingService(getTsmpSettingService());
		TsmpAuthorizationDGRKParser.setLogger(getLogger());
		TsmpAuthorizationBearerParser.setLogger(getLogger());
		TsmpAuthorizationParserFactory.setLogger(getLogger());
		FuzzyEntityListener.setLogger(getLogger());
		BCRIValidator.setLogger(getLogger());
		BCRIVFactory.setLogger(getLogger());
		ReqValidator.setLogger(getLogger());
		BaseDao.setLogger(getLogger());
		ControllerUtil.setLogger(getLogger());
		ServiceUtil.setLogger(getLogger());
		WebSocketClientConn.setDgrWebSocketMappingCacheProxy(getDgrWebSocketMappingCacheProxy());
		WebSocketClientConn.setTsmpSettingService(getTsmpSettingService());
		ComposerWebSocketClientConn.setEnv(getEnvironment());
	}

	public ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}
	
	protected DPB0115Service getDPB0115Service() {
		return this.dpb0115Service;
	}

	public Environment getEnvironment() {
		return this.env;
	}

	public TsmpOpenApiKeyCacheProxy getTsmpOpenApiKeyCacheProxy() {
		return this.tsmpOpenApiKeyCacheProxy;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	public TPILogger getLogger() {
		return logger;
	}

	protected DgrWebSocketMappingCacheProxy getDgrWebSocketMappingCacheProxy() {
		return dgrWebSocketMappingCacheProxy;
	}


	

}