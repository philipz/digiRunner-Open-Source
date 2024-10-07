package tpi.dgrv4.dpaa.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0219Req;
import tpi.dgrv4.dpaa.vo.AA0219Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0219Service {

	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	private OauthClientDetailsDao oauthClientDetailsDao;
	
	@Autowired
	private TsmpClientDao tsmpClientDao;
	
	/**
	 * 1.查詢oauth_client_details資料表，條件 client_id = AA0219Req.clientID，將查詢出來的資料放進AA0219Resp。
	 * */
	public AA0219Resp getTokenSettingbyClient(TsmpAuthorization authorization, AA0219Req req) {
		AA0219Resp resp = new AA0219Resp();;

		try {
			String clientId = req.getClientID();
			
			checkParam(req);
			
			OauthClientDetails oauthClientDetails = getOauthClientDetailsDao().findById(clientId).orElse(null);
			resp.setClientID(clientId);
			Set<String> typeSet = null;
			if(StringUtils.isEmpty(oauthClientDetails.getAuthorizedGrantTypes())) {
				typeSet = new LinkedHashSet<>();
			}else {
				String[] arrType = oauthClientDetails.getAuthorizedGrantTypes().split(",");
				typeSet = new LinkedHashSet(Arrays.asList(arrType));
			}
			
			resp.setAuthorizedGrantType(typeSet);
			resp.setAccessTokenValidity(oauthClientDetails.getAccessTokenValidity());
			resp.setRaccessTokenValidity(oauthClientDetails.getRefreshTokenValidity());
			resp.setWebServerRedirectUri(oauthClientDetails.getWebServerRedirectUri());
			resp.setWebServerRedirectUri1(oauthClientDetails.getWebServerRedirectUri1());
			resp.setWebServerRedirectUri2(oauthClientDetails.getWebServerRedirectUri2());
			resp.setWebServerRedirectUri3(oauthClientDetails.getWebServerRedirectUri3());
			resp.setWebServerRedirectUri4(oauthClientDetails.getWebServerRedirectUri4());
			resp.setWebServerRedirectUri5(oauthClientDetails.getWebServerRedirectUri5());
			
			Optional<TsmpClient> opt_tsmpClient = getTsmpClientDao().findById(clientId);
			if (opt_tsmpClient.isPresent()) {
				TsmpClient tsmpClient  = opt_tsmpClient.get();
				resp.setAccessTokenQuota(tsmpClient.getAccessTokenQuota()==null?0:tsmpClient.getAccessTokenQuota());
				resp.setRefreshTokenQuota(tsmpClient.getRefreshTokenQuota()==null?0:tsmpClient.getRefreshTokenQuota());
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(AA0219Req req) {
		String clientId = req.getClientID();
		
		if(StringUtils.isEmpty(clientId)) {
			//1343:用戶端帳號:必填參數
			throw TsmpDpAaRtnCode._1343.throwing();
		}
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
}
