package tpi.dgrv4.gateway.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.codec.utils.JWKcodec.JWKVerifyResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.GtwIdPVerifyResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * 驗證 ID token 並取得 User 個人資料
 * 
 * @author Mini
 */

@Service
public class GtwIdPVerifyService {

	@Autowired
	private TokenHelper tokenHelper;

	@Autowired
	private TsmpSettingService tsmpSettingService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private GtwIdPJwksService gtwIdPJwksService;

	public ResponseEntity<?> verify(HttpServletRequest httpReq, HttpServletResponse httpResp, HttpHeaders headers) {
		String reqUri = httpReq.getRequestURI();

		ResponseEntity<?> respEntity = null;
		try {
			String idTokenJwtstr = httpReq.getParameter("id_token");
			
			return verify(idTokenJwtstr);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.Internal_Server_Error;
			TPILogger.tl.error(errMsg);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}

	protected ResponseEntity<?> verify(String idTokenJwtstr) throws Exception {
		
		// 沒有 id_token 值
		if (!StringUtils.hasLength(idTokenJwtstr)) {
			String errMsg = TokenHelper.Missing_required_parameter + "id_token";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
		
		// 取得 ID token 中的值
		IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);
		String iss = idTokenData.iss;
		
		// 檢查資料
		ResponseEntity<?> errRespEntity = checkReqParam(idTokenJwtstr, iss);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}
 
		// 取得 JWKS 的公鑰 JSON
		String jwksJsonStr = getGtwIdPJwksService().getJwksJsonStr();

		// 驗證 ID token
		boolean isVerify = false;
		JWKVerifyResult jwkRs = JWKcodec.verifyJWStokenByJsonString(idTokenJwtstr, iss, jwksJsonStr);
		isVerify = jwkRs.verify;
//		TPILogger.tl.debug("ID token verify : " + isVerify);

		if (isVerify) {// 驗證 ID token 成功
			String userName = idTokenData.userName;
			String userAlias = idTokenData.userAlias;
			String userEmail = idTokenData.userEmail;
			String userPicture = idTokenData.userPicture;
			Long iat = idTokenData.iat;
			Long exp = idTokenData.exp;
			String aud = idTokenData.aud;
			
			GtwIdPVerifyResp gtwIdPVerifyResp = new GtwIdPVerifyResp();
			gtwIdPVerifyResp.setIss(iss);
			gtwIdPVerifyResp.setSub(userName);
			gtwIdPVerifyResp.setAud(aud);
			gtwIdPVerifyResp.setExp(exp);
			gtwIdPVerifyResp.setIat(iat);
			gtwIdPVerifyResp.setName(userAlias);
			gtwIdPVerifyResp.setEmail(userEmail);
			gtwIdPVerifyResp.setPicture(userPicture);

			String respJsonStr = getObjectMapper().writeValueAsString(gtwIdPVerifyResp);
			respJsonStr = JWKcodec.toPrettyJson(respJsonStr);
			return new ResponseEntity<String>(respJsonStr, HttpStatus.OK);

		} else {
			String errMsg = jwkRs.errorMessg;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.Forbidden, errMsg),
					HttpStatus.FORBIDDEN);// 403
		}
	}
	
	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idTokenJwtstr, String iss) throws Exception {
		// ID token 沒有 iss
		if (!StringUtils.hasLength(iss)) {
			String errMsg = "Missing ID token parameter: iss";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();
		
		String schemeAndDomainAndPort = GtwIdPWellKnownService.getSchemeAndDomainAndPort(dgrPublicDomain,
				dgrPublicPort);

		String matchIssuer = null;
		List<String> supportGtwIdPTypeList = GtwIdPHelper.getSupportGtwIdPType();// 目前 GTW IdP 支援的 IdP Type
		for (String idPType : supportGtwIdPTypeList) {
			String issuer = GtwIdPWellKnownService.getIssuer(schemeAndDomainAndPort, idPType);
			if (iss.equals(issuer)) {
				matchIssuer = issuer;
				break;
			}
		}

		// iss 不正確
		if (!StringUtils.hasLength(matchIssuer)) {
			String errMsg = "Invalid ID token issuer. iss: " + iss;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.Forbidden, errMsg),
					HttpStatus.FORBIDDEN);// 403
		}
		
		return null;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected GtwIdPJwksService getGtwIdPJwksService() {
		return gtwIdPJwksService;
	}
}
