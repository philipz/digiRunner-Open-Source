package tpi.dgrv4.dpaa.component.req;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType.ItemContainer;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DpReqServiceFactory {

	private TPILogger logger = TPILogger.tl;

	@Autowired
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;

	@Autowired
	@Qualifier("dpReqServiceImpl_D1")
	private DpReqServiceIfs d1ReqService;

	@Autowired
	@Qualifier("dpReqServiceImpl_D2")
	private DpReqServiceIfs d2ReqService;

	@Autowired
	@Qualifier("dpReqServiceImpl_D3")
	private DpReqServiceIfs d3ReqService;

	@Autowired
	@Qualifier("dpReqServiceImpl_D4")
	private DpReqServiceIfs d4ReqService;
	
	@Autowired
	@Qualifier("dpReqServiceImpl_D5")
	private DpReqServiceIfs d5ReqService;

	/**
	 * 使用時機: 草稿(申請單)建立後
	 * @param reqOrdermId
	 * @param exceptionSupplier
	 * @return
	 * @throws X
	 */
	public <X extends TsmpDpAaException> DpReqServiceIfs getDpReqService( //
			Long reqOrdermId, Supplier<? extends X> exceptionSupplier) throws X {
		return getDpReqService(reqOrdermId, null, exceptionSupplier);
	}

	/**
	 * 使用時機: 草稿(申請單)建立前
	 * @param reqType
	 * @param exceptionSupplier
	 * @return
	 * @throws X
	 */
	public <X extends TsmpDpAaException> DpReqServiceIfs getDpReqService( //
			String reqType, Supplier<? extends X> exceptionSupplier) throws X {
		return getDpReqService(null, reqType, exceptionSupplier);
	}

	/**
	 * @param reqOrdermId
	 * @param reqType
	 * @param exceptionSupplier
	 * @return
	 * @throws X 有傳入 reqOrdermId, 但找不到申請單時所拋出的錯誤
	 */
	public <X extends TsmpDpAaException> DpReqServiceIfs getDpReqService( //
			Long reqOrdermId, String reqType, Supplier<? extends X> exceptionSupplier) throws X {
		if (reqOrdermId != null && StringUtils.isEmpty(reqType)) {
			Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
			if (opt_m.isPresent()) {
				reqType = opt_m.get().getReqType();
			} else {
				throw exceptionSupplier.get();
			}
		}
		
		if (TsmpDpReqReviewType.API_APPLICATION.isValueEquals(reqType)) {
			return getD1ReqService();
		} else if (TsmpDpReqReviewType.API_ON_OFF.isValueEquals(reqType)) {
			return getD2ReqService();
		} else if (TsmpDpReqReviewType.CLIENT_REG.isValueEquals(reqType)) {
			return getD3ReqService();
		} else if (TsmpDpReqReviewType.THINKPOWER_ARTICLE.isValueEquals(reqType)) {
			return getD4ReqService();
		} else if (TsmpDpReqReviewType.OPEN_API_KEY.isValueEquals(reqType)) {
			return getD5ReqService();
		}
		this.logger.error("Unknown reqType: " + reqType);
		throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
	}

	public boolean isReviewTypeEquals(ItemContainer rt, DpReqServiceIfs ifs) {
		if (ifs instanceof DpReqServiceImpl_D1) {
			return TsmpDpReqReviewType.API_APPLICATION.equals(rt);
		} else if (ifs instanceof DpReqServiceImpl_D2) {
			return TsmpDpReqReviewType.API_ON_OFF.equals(rt);
		} else if (ifs instanceof DpReqServiceImpl_D3) {
			return TsmpDpReqReviewType.CLIENT_REG.equals(rt);
		} else if (ifs instanceof DpReqServiceImpl_D4) {
			return TsmpDpReqReviewType.THINKPOWER_ARTICLE.equals(rt);
		} else if (ifs instanceof DpReqServiceImpl_D5) {
			return TsmpDpReqReviewType.OPEN_API_KEY.equals(rt);
		}
		return false;
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected DpReqServiceIfs getD1ReqService() {
		return this.d1ReqService;
	}

	protected DpReqServiceIfs getD2ReqService() {
		return this.d2ReqService;
	}

	protected DpReqServiceIfs getD3ReqService() {
		return this.d3ReqService;
	}

	protected DpReqServiceIfs getD4ReqService() {
		return this.d4ReqService;
	}
	
	protected DpReqServiceIfs getD5ReqService() {
		return this.d5ReqService;
	}

}
