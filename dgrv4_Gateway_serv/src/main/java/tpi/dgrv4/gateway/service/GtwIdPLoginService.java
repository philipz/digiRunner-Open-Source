package tpi.dgrv4.gateway.service;

import java.net.URL;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPApiHelper.ApiUserInfoData;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.IdPJdbcHelper;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper.UserInfoData;
import tpi.dgrv4.gateway.component.LdapHelper;
import tpi.dgrv4.gateway.component.LdapHelper.LdapAdAuthData;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * 驗證User帳號、密碼 <br>
 * (LDAP / API / JDBC) <br>
 * 
 * @author Mini
 */
@Service
public class GtwIdPLoginService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TsmpSettingService tsmpSettingService;

	@Autowired
	private TokenHelper tokenHelper;

	@Autowired
	private IdPHelper idPHelper;

	@Autowired
	private GtwIdPCallbackService gtwIdPCallbackService;

	@Autowired
	private GtwIdPAuthService gtwIdPAuthService;

	@Autowired
	private LdapHelper ldapHelper;

	@Autowired
	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;

	@Autowired
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJDao;

	@Autowired
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;

	@Autowired
	private GtwIdPHelper gtwIdPHelper;

	@Autowired
	private TsmpTAEASKHelper tsmpTAEASKHelper;

	@Autowired
	private IdPJdbcHelper idPJdbcHelper;

	@Autowired
	private IdPApiHelper idPApiHelper;

	@Autowired
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;

	public static class LoginData {
		public ResponseEntity<?> errRespEntity;
		public String errMsg;
	}

	public ResponseEntity<?> gtwIdPLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		TPILogger.tl.debug("...idPType: " + idPType);

		String reqUri = httpReq.getRequestURI();
		String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");
		ResponseEntity<?> errRespEntity = null;
		try {
			String responseType = httpReq.getParameter("response_type");
			String dgrClientId = httpReq.getParameter("client_id");
			String oidcScopeStr = httpReq.getParameter("scope");
			String state = httpReq.getParameter("state");
			String reqUserName = httpReq.getParameter("username");
			String reqUserMima = httpReq.getParameter("password");
			String codeChallenge = httpReq.getParameter("code_challenge");
			String codeChallengeMethod = httpReq.getParameter("code_challenge_method");

			// 1.檢查傳入的資料
			errRespEntity = checkReqParam(idPType, responseType, dgrClientId, oidcScopeStr, dgrClientRedirectUri, state,
					reqUserName, reqUserMima, codeChallenge, codeChallengeMethod, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
				return null;
			}

			// 2.將 user 密碼做 AES 解密
			String userMima = null;
			try {
				userMima = getTsmpTAEASKHelper().decrypt(reqUserMima);
			} catch (Exception e) {
				TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			}

			if (!StringUtils.hasLength(userMima)) {
				// user 密碼解密失敗
				String errMsg = TokenHelper.User_password_decryption_failed;
				TPILogger.tl.debug(errMsg);
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
						HttpStatus.BAD_REQUEST);// 400
				getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
				return null;
			}

			// 3.驗證登入的資料
			if (DgrIdPType.LDAP.equals(idPType)) {
				errRespEntity = loginByLdap(httpReq, httpResp, idPType, responseType, dgrClientId, oidcScopeStr,
						dgrClientRedirectUri, state, reqUserName, userMima, codeChallenge, codeChallengeMethod);

			} else if (DgrIdPType.API.equals(idPType)) {
				String userIp = ServiceUtil.getIpAddress(httpReq);
				errRespEntity = loginByApi(httpReq, httpResp, idPType, responseType, dgrClientId, oidcScopeStr,
						dgrClientRedirectUri, state, reqUserName, userMima, userIp, codeChallenge, codeChallengeMethod);

			} else if (DgrIdPType.JDBC.equals(idPType)) {
				errRespEntity = loginByJdbc(httpReq, httpResp, idPType, responseType, dgrClientId, oidcScopeStr,
						dgrClientRedirectUri, state, reqUserName, userMima, codeChallenge, codeChallengeMethod);
				
			} else {
				// 無效的 IdP Type
				String errMsg = String.format(IdPHelper.MSG_INVALID_IDPTYPE, idPType);
				TPILogger.tl.debug(errMsg);
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}

			if (errRespEntity != null) {// 資料驗證有錯誤
				getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
				return null;
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.Internal_Server_Error;
			TPILogger.tl.error(errMsg);
			errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
			return null;
		}

		return null;
	}

	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idPType, String responseType, String dgrClientId,
			String oidcScopeStr, String dgrClientRedirectUri, String state, String reqUserName, String reqUserMima,
			String codeChallenge, String codeChallengeMethod, String reqUri) {
		ResponseEntity<?> errRespEntity = getTokenHelper().checkSupportGtwIdPType(idPType);
		if (errRespEntity != null) {// idPType 資料驗證有錯誤
			return errRespEntity;
		}

		errRespEntity = getTokenHelper().checkResponseType(responseType);
		if (errRespEntity != null) {// response_type 資料驗證有錯誤
			return errRespEntity;
		}

		// 沒有 state
		if (!StringUtils.hasLength(state)) {
			String errMsg = TokenHelper.Missing_required_parameter + "state";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 沒有 username
		if (!StringUtils.hasLength(reqUserName)) {
			String errMsg = TokenHelper.Missing_required_parameter + "username";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 沒有 password
		if (!StringUtils.hasLength(reqUserMima)) {
			String errMsg = TokenHelper.Missing_required_parameter + "password";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_request, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 檢查是否有 code_challenge 和 code_challenge_method
		errRespEntity = getGtwIdPHelper().checkCodeChallengeParam(codeChallenge, codeChallengeMethod);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}

		errRespEntity = getTokenHelper().checkOidcScope(oidcScopeStr);
		if (errRespEntity != null) {// scope 資料驗證有錯誤
			return errRespEntity;
		}

		errRespEntity = getTokenHelper().checkRedirectUri(dgrClientId, dgrClientRedirectUri, reqUri);
		if (errRespEntity != null) {// redirectUri 驗證有錯誤
			return errRespEntity;
		}

		errRespEntity = getTokenHelper().checkClientStatus(dgrClientId, reqUri);
		if (errRespEntity != null) {// client 資料驗證有錯誤
			return errRespEntity;
		}

		return null;
	}

	/**
	 * 以 JDBC 登入
	 */
	protected ResponseEntity<?> loginByJdbc(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			String responseType, String dgrClientId, String openIdScopeStr, String dgrClientRedirectUri, String state,
			String reqUserName, String userMima, String codeChallenge, String codeChallengeMethod) throws Exception {

		UserInfoData userInfoData = new UserInfoData();
		String reqUri = httpReq.getRequestURI();
		try {
			ResponseEntity<?> errRespEntity = null;

			// 1.取得 dgR client 對應的 JDBC 連線資料
			// 取得狀態為 "Y",且建立時間最新的
			String status = "Y";
			DgrGtwIdpInfoJdbc dgrGtwIdpInfoJdbc = getDgrGtwIdpInfoJdbcDao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(dgrClientId, status);

			if (dgrGtwIdpInfoJdbc == null) {
				// Table [DGR_GTW_IDP_INFO_JDBC] 查不到資料
				TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_JDBC] can't find data. dgrClientId: " + dgrClientId
						+ ", status: " + status);
				// 設定檔缺少參數
				String errMsg = TokenHelper.The_profile_is_missing_parameters + "GTW IdP(" + idPType + ") Info";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				return errRespEntity;
			}

			// 2.驗證 user 帳號 & 密碼, 並取得 user data
			String connName = dgrGtwIdpInfoJdbc.getConnectionName();
			String sqlPtmt = dgrGtwIdpInfoJdbc.getSqlPtmt();
			String sqlParams = dgrGtwIdpInfoJdbc.getSqlParams();
			String userMimaAlg = dgrGtwIdpInfoJdbc.getUserMimaAlg();
			String userMimaColName = dgrGtwIdpInfoJdbc.getUserMimaColName();
			
			String idtSubColName = dgrGtwIdpInfoJdbc.getIdtSub();
			String idtNameColName = dgrGtwIdpInfoJdbc.getIdtName();
			String idtEmailColName = dgrGtwIdpInfoJdbc.getIdtEmail();
			String idtPictureColName = dgrGtwIdpInfoJdbc.getIdtPicture();

			userInfoData = getIdPJdbcHelper().checkUserAuth(connName, sqlPtmt, sqlParams, reqUserName, userMima, userMimaAlg,
					userMimaColName, idtSubColName, idtNameColName, idtEmailColName, idtPictureColName, reqUri);
			errRespEntity = userInfoData.errRespEntity;
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 3.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
			// 若 state 已存在,顯示錯誤訊息
			errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 建立 DGR_GTW_IDP_AUTH_M
			String dgRcode = null;// 此時還沒有 dgRcode

			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId, idPType,
					dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);
			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();

			// 建立 DGR_GTW_IDP_AUTH_D, 寫入 scope
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, openIdScopeStr);

			// 4.將 user info 存入DB
			// 此時尚沒有 dgRcode, 先存 state 的值, 到期時間填0
			long expireDateTime = 0;
			String userName = userInfoData.userName;
			String userAlias = userInfoData.userAlias;
			String userEmail = userInfoData.userEmail;
			String userPicture = userInfoData.userPicture;
			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, expireDateTime,
					idPType, dgrClientId, userName, userAlias, userEmail, userPicture, null, null, null, null, null,
					null);

			// 5.轉導到 user 同意畫面
			String dgrConsentUiUrl = getDgrUserConsentUiUrl(idPType, responseType, dgrClientId, openIdScopeStr,
					dgrClientRedirectUri, state, reqUserName);

			// user 同意畫面
			TPILogger.tl.debug("Redirect to URL【User Consent UI URL】: " + dgrConsentUiUrl);
			httpResp.sendRedirect(dgrConsentUiUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.Internal_Server_Error;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 以 LDAP 登入
	 */
	protected ResponseEntity<?> loginByLdap(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			String responseType, String dgrClientId, String openIdScopeStr, String dgrClientRedirectUri, String state,
			String reqUserName, String userMima, String codeChallenge, String codeChallengeMethod) throws Exception {

		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> errRespEntity = null;
		try {

			// 1.取得 dgR client 對應的 LDAP 連線資料
			// 取得狀態為 "Y",且建立時間最新的
			String status = "Y";
			DgrGtwIdpInfoL dgrGtwIdpInfoL = getDgrGtwIdpInfoLDao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(dgrClientId, status);

			if (dgrGtwIdpInfoL == null) {
				// Table [DGR_GTW_IDP_INFO_L] 查不到資料
				TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_L] can't find data. client_id: " + dgrClientId
						+ ", status: " + status);

				// 設定檔缺少參數
				String errMsg = TokenHelper.The_profile_is_missing_parameters + "GTW IdP info(LDAP). client_id: "
						+ dgrClientId;
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				return errRespEntity;
			}

			String ldapUrl = dgrGtwIdpInfoL.getLdapUrl();// Ldap登入的URL
			String ldapDn = dgrGtwIdpInfoL.getLdapDn();// Ldap登入的使用者DN
			String ldapBaseDn = dgrGtwIdpInfoL.getLdapBaseDn();// Ldap基礎DN
			int ldapTimeout = dgrGtwIdpInfoL.getLdapTimeout();// Ldap登入的連線timeout,單位毫秒

			// 2.檢查 LDAP User 帳號 & 密碼是否正確
			// 3.取得 user data
			boolean isGetUserInfo = true;
			LdapAdAuthData ldapAdAuthData = getLdapHelper().checkLdapAuth(ldapUrl, ldapDn, ldapBaseDn, ldapTimeout,
					reqUserName, userMima, isGetUserInfo, null);
			String errMsg = ldapAdAuthData.errMsg;
			if (StringUtils.hasLength(errMsg)) {
				TPILogger.tl.debug(errMsg);
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_user, errMsg),
						HttpStatus.UNAUTHORIZED);// 401
				return errRespEntity;
			}

			// 4.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
			// 若 state 已存在,顯示錯誤訊息
			errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 建立 DGR_GTW_IDP_AUTH_M
			String dgRcode = null;// 此時還沒有 dgRcode
			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId, idPType,
					dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);
			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();

			// 建立 DGR_GTW_IDP_AUTH_D, 寫入 scope
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, openIdScopeStr);

			// 5.將 user info 存入DB
			// 此時尚沒有 dgRcode, 先存 state 的值, 到期時間填0
			long expireDateTime = 0;
			String userAlias = ldapAdAuthData.name;
			String userEmail = ldapAdAuthData.mail;
			String userPicture = null;
			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, expireDateTime,
					idPType, dgrClientId, reqUserName, userAlias, userEmail, userPicture, null, null, null, null, null,
					null);

			// 6.轉導到 user 同意畫面
			String dgrConsentUiUrl = getDgrUserConsentUiUrl(idPType, responseType, dgrClientId, openIdScopeStr,
					dgrClientRedirectUri, state, reqUserName);

			// user 同意畫面
			TPILogger.tl.debug("Redirect to URL【User Consent UI URL】: " + dgrConsentUiUrl);
			httpResp.sendRedirect(dgrConsentUiUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.Internal_Server_Error;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 以 API 登入
	 */
	protected ResponseEntity<?> loginByApi(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			String responseType, String dgrClientId, String openIdScopeStr, String dgrClientRedirectUri, String state,
			String reqUserName, String userMima, String userIp, String codeChallenge, String codeChallengeMethod)
			throws Exception {

		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> errRespEntity = null;
		try {

			// 1.取得 dgR client 對應的 API 連線資料
			// 取得狀態為 "Y",且建立時間最新的
			String status = "Y";

			DgrGtwIdpInfoA dgrGtwIdpInfoA = getDgrGtwIdpInfoADao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(dgrClientId, status);

			if (dgrGtwIdpInfoA == null) {
				// Table [DGR_GTW_IDP_INFO_A] 查不到資料
				TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_A] can't find data. client_id: " + dgrClientId
						+ ", status: " + status);

				// 設定檔缺少參數
				String errMsg = TokenHelper.The_profile_is_missing_parameters + "GTW IdP info(API). client_id: "
						+ dgrClientId;
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				return errRespEntity;
			}

			// 2.調用登入API, 檢查 user 帳號 & 密碼是否正確
			// 3.取得 user data
			ApiUserInfoData apiUserInfoData = getIdPApiHelper().callLoginApi(reqUserName, userMima, userIp,
					dgrGtwIdpInfoA, reqUri);
			String errMsg = apiUserInfoData.errMsg;
			if (StringUtils.hasLength(errMsg)) {
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.invalid_user, errMsg),
						HttpStatus.UNAUTHORIZED);// 401
				return errRespEntity;
			}

			// 4.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
			// 若 state 已存在,顯示錯誤訊息
			errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 建立 DGR_GTW_IDP_AUTH_M
			String dgRcode = null;// 此時還沒有 dgRcode
			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId, idPType,
					dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);
			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();

			// 建立 DGR_GTW_IDP_AUTH_D, 寫入 scope
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, openIdScopeStr);

			// 5.將 user info 存入DB
			// 此時尚沒有 dgRcode, 先存 state 的值, 到期時間填0
			long expireDateTime = 0;
			String userAlias = apiUserInfoData.userName;
			String userEmail = apiUserInfoData.userEmail;
			String userPicture = apiUserInfoData.userPicture;
			String apiResp = apiUserInfoData.apiResp;
			String	idtLightId=	apiUserInfoData.idtLightId ;
			String idtRoleName= apiUserInfoData.idtRoleName;
			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, expireDateTime,
					idPType, dgrClientId, reqUserName, userAlias, userEmail, userPicture, null, null, null, apiResp, idtLightId, idtRoleName);

			// 6.轉導到 user 同意畫面
			String dgrConsentUiUrl = getDgrUserConsentUiUrl(idPType, responseType, dgrClientId, openIdScopeStr,
					dgrClientRedirectUri, state, reqUserName);

			// user 同意畫面
			TPILogger.tl.debug("Redirect to URL【User Consent UI URL】: " + dgrConsentUiUrl);
			httpResp.sendRedirect(dgrConsentUiUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.Internal_Server_Error;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 取得 user 同意畫面 URL
	 */
	private String getDgrUserConsentUiUrl(String idPType, String responseType, String dgrClientId,
			String openIdScopeStr, String dgrClientRedirectUri, String state, String reqUserName) throws Exception {

		// 前端GTW IdP User同意畫面的URL
		// TODO, Mini,(要恢復)
		String dgrConsentUiUrl = getTsmpSettingService().getVal_GTW_IDP_CONSENT_URL();

		// TODO, Mini, test測試用
//		String dgrConsentUiUrl = "https://localhost:8080/dgrv4/ac4/gtwidp/{idPType}/consent";

		// TODO, Mini, test (前端未加入畫面前測試用)
//		String dgrConsentUiUrl = "https://localhost:8080/dgrv4/mockac/gtwidp/{idPType}/consentui";

		dgrConsentUiUrl = dgrConsentUiUrl.replace("{idPType}", idPType);

		URL urlObj = new URL(dgrConsentUiUrl);
		dgrConsentUiUrl = urlObj.getPath();// 使用相對路徑

		String redirectUrl = String.format(
				"%s" + "?response_type=%s" + "&client_id=%s" + "&scope=%s" + "&redirect_uri=%s" + "&state=%s"
						+ "&username=%s",
				dgrConsentUiUrl, IdPHelper.getUrlEncode(responseType), IdPHelper.getUrlEncode(dgrClientId),
				IdPHelper.getUrlEncode(openIdScopeStr), IdPHelper.getUrlEncode(dgrClientRedirectUri),
				IdPHelper.getUrlEncode(state), IdPHelper.getUrlEncode(reqUserName));
		return redirectUrl;
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

	protected IdPHelper getIdPHelper() {
		return idPHelper;
	}

	protected GtwIdPCallbackService getGtwIdPCallbackService() {
		return gtwIdPCallbackService;
	}

	protected GtwIdPAuthService getGtwIdPAuthService() {
		return gtwIdPAuthService;
	}

	protected LdapHelper getLdapHelper() {
		return ldapHelper;
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJDao() {
		return dgrGtwIdpInfoJDao;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {
		return dgrGtwIdpInfoADao;
	}

	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}

	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return tsmpTAEASKHelper;
	}

	protected IdPJdbcHelper getIdPJdbcHelper() {
		return idPJdbcHelper;
	}

	protected IdPApiHelper getIdPApiHelper() {
		return idPApiHelper;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}
}
