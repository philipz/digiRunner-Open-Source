package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0188Req;
import tpi.dgrv4.dpaa.vo.DPB0188Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0188Service {
	private TPILogger logger = TPILogger.tl;
	@Autowired
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;

	public DPB0188Resp deleteGtwIdPInfo_api(TsmpAuthorization authorization, DPB0188Req req) {
		DPB0188Resp resp = new DPB0188Resp();

		try {
			String id = req.getId();
			if (!StringUtils.hasLength(id)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
			}
			long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoA a = getDgrGtwIdpInfoADao().findById(longId).orElse(null);
			if (a == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			getDgrGtwIdpInfoADao().delete(a);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		return resp;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {

		return dgrGtwIdpInfoADao;
	}
}
