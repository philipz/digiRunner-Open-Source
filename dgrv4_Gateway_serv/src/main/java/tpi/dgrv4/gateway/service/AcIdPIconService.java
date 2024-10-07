package tpi.dgrv4.gateway.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoApiDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AcIdPIconService {

	private TPILogger logger = TPILogger.tl;

	@Autowired
	private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;

	@Autowired
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;

	@Autowired
	private DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao;

	public String getIcon(String idPType) {
		String iconFile = IdPHelper.DEFULT_ICON_FILE;
		try {
			if (DgrIdPType.LDAP.equals(idPType)) {// LDAP
				// 查詢狀態為 "Y", 且建立時間最新的
				DgrAcIdpInfoLdap dgrAcIdpInfoLdap = getDgrAcIdpInfoLdapDao()
						.findFirstByLdapStatusOrderByCreateDateTimeDesc("Y");
				if (dgrAcIdpInfoLdap != null && StringUtils.isNotBlank(dgrAcIdpInfoLdap.getIconFile())) {
					iconFile = dgrAcIdpInfoLdap.getIconFile();
				}

			} else if (DgrIdPType.MLDAP.equals(idPType)) {// MLDAP
				// 查詢狀態為 "Y", 且建立時間最新的
				DgrAcIdpInfoMLdapM dgrAcIdpInfoMLdapM = getDgrAcIdpInfoMLdapMDao()
						.findFirstByStatusOrderByCreateDateTimeDesc("Y");
				if (dgrAcIdpInfoMLdapM != null && StringUtils.isNotBlank(dgrAcIdpInfoMLdapM.getIconFile())) {
					iconFile = dgrAcIdpInfoMLdapM.getIconFile();
				}

			} else if (DgrIdPType.API.equals(idPType)) {// API
				// 查詢狀態為 "Y", 且建立時間最新的
				DgrAcIdpInfoApi dgrAcIdpInfoApi = getDgrAcIdpInfoApiDao()
						.findFirstByStatusOrderByCreateDateTimeDesc("Y");
				if (dgrAcIdpInfoApi != null && StringUtils.isNotBlank(dgrAcIdpInfoApi.getIconFile())) {
					iconFile = dgrAcIdpInfoApi.getIconFile();
				}
			}

			return iconFile;

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}

	protected DgrAcIdpInfoMLdapMDao getDgrAcIdpInfoMLdapMDao() {
		return dgrAcIdpInfoMLdapMDao;
	}

	protected DgrAcIdpInfoApiDao getDgrAcIdpInfoApiDao() {
		return dgrAcIdpInfoApiDao;
	}
}
