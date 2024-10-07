package tpi.dgrv4.gateway.service;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.GtwIdPWellKnownResp;

@Service
public class GtwIdPWellKnownService {
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	TsmpSettingService tsmpSettingService;
	
	@Autowired
	TokenHelper tokenHelper;

	public ResponseEntity<?> getGtwIdPWellKnown(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> respEntity = null;
		try {
			// 檢查傳入的資料
			respEntity = checkReqParam(idPType);
			if (respEntity != null) {// 資料驗證有錯誤
				return respEntity;
			}
			
			String respJsonStr = getGtwIdPWellKnown(idPType);
			return new ResponseEntity<Object>(respJsonStr, HttpStatus.OK);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.Internal_Server_Error;
			TPILogger.tl.error(errMsg);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}
	
	private String getGtwIdPWellKnown(String idPType) throws Exception {
		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();
		
		String issuer = getIssuer(dgrPublicDomain, dgrPublicPort, idPType);
		String authorizationEndpoint = getAuthorizationEndpoint(dgrPublicDomain, dgrPublicPort, idPType);
		String tokenEndpoint = getTokenEndpoint(dgrPublicDomain, dgrPublicPort);
		String jwksUri = getJwksUri(dgrPublicDomain, dgrPublicPort);
		String callbackEndpoint = getCallbackEndpoint(dgrPublicDomain, dgrPublicPort, idPType);
		
		// 支援哪些 Scope
		List<String> scopesSupportedList = GtwIdPHelper.getSupportScopeList();
		
		GtwIdPWellKnownResp gtwIdPWellKnownResp = new GtwIdPWellKnownResp();
		gtwIdPWellKnownResp.setIssuer(issuer);
		gtwIdPWellKnownResp.setAuthorizationEndpoint(authorizationEndpoint);
		gtwIdPWellKnownResp.setTokenEndpoint(tokenEndpoint);
		gtwIdPWellKnownResp.setJwksUri(jwksUri);
		gtwIdPWellKnownResp.setIdTokenSigningAlgValuesSupported(Arrays.asList("RS256"));
		gtwIdPWellKnownResp.setScopesSupported(scopesSupportedList);
		
		if (DgrIdPType.GOOGLE.equals(idPType) || DgrIdPType.MS.equals(idPType)) {
			// OAuth 2.0 的 IdP type 才需要顯示 CallbackEndpoint
			gtwIdPWellKnownResp.setCallbackEndpoint(callbackEndpoint);
		}
		
		// 支援哪些 Code_challenge_method
		List<String> codeChallengeMethodsSupportedList = GtwIdPHelper.getCodeChallengeMethodsSupported();
		gtwIdPWellKnownResp.setCodeChallengeMethodsSupported(codeChallengeMethodsSupportedList);
		
		String respJsonStr = getObjectMapper().writeValueAsString(gtwIdPWellKnownResp);
		respJsonStr = JWKcodec.toPrettyJson(respJsonStr);
		return respJsonStr;
	}
	
	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idPType) {
		ResponseEntity<?> errRespEntity = getTokenHelper().checkSupportGtwIdPType(idPType);
		if (errRespEntity != null) {// idPType 資料驗證有錯誤
			return errRespEntity;
		}
		return null;
	}
 
	/**
	 * WellKnownUrl <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/{idPType}/.well-known/openid-configuration
	 * https://127.0.0.1:8080/dgrv4/ssotoken/GOOGLE/.well-known/openid-configuration
	 */
	public static String getWellKnownUrl(String dgrPublicDomain, String dgrPublicPort, String idPType) {
		String issuer = getIssuer(dgrPublicDomain, dgrPublicPort, idPType);
		String wellKnownUrl = String.format("%s/.well-known/openid-configuration", 
				issuer);
		
		return wellKnownUrl;
	}
	
	/**
	 * 1. issuer 的值 <br>
	 * 2. 核發 ID token 的 iss 值 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/{idPType} <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/GOOGLE <br>
	 */
	public static String getIssuer(String dgrPublicDomain, String dgrPublicPort, String idPType) {
		String issuer = String.format("https://%s:%s/dgrv4/ssotoken/%s", 
				dgrPublicDomain,
				dgrPublicPort,
				idPType);
		
		return issuer;
	}
	
	/**
	 * OpenID Connect 啟動身分驗證的入口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/{idPType}/authorization <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/GOOGLE/authorization <br>
	 */
	public static String getAuthorizationEndpoint(String dgrPublicDomain, String dgrPublicPort, String idPType) {
		String authorizationEndpoint = String.format("https://%s:%s/dgrv4/ssotoken/gtwidp/%s/authorization",
				dgrPublicDomain,
				dgrPublicPort,
				idPType);
		
		return authorizationEndpoint;
	}
	
	/**
	 * OpenID Connect 讓 Client 取得 token 的端口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/oauth/token <br>
	 */
	public static String getTokenEndpoint(String dgrPublicDomain, String dgrPublicPort) {
		String tokenEndpoint = String.format("https://%s:%s/oauth/token",
				dgrPublicDomain,
				dgrPublicPort);
		
		return tokenEndpoint;
	}
	
	/**
	 * digiRunner 的 callback URL <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/GOOGLE/gtwIdPCallback <br>
	 */
	public static String getCallbackEndpoint(String dgrPublicDomain, String dgrPublicPort, String idPType) {
		String tokenEndpoint = String.format("https://%s:%s/dgrv4/ssotoken/gtwidp/%s/gtwIdPCallback",
				dgrPublicDomain,
				dgrPublicPort,
				idPType);
		
		return tokenEndpoint;
	}
	
	/**
	 * 放 JWK Set 的公鑰內容 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/oauth2/certs <br>
	 */
	public static String getJwksUri(String dgrPublicDomain, String dgrPublicPort) {
		String jwksUri = String.format("https://%s:%s/dgrv4/ssotoken/oauth2/certs",
				dgrPublicDomain,
				dgrPublicPort);
		
		return jwksUri;
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
