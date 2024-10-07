package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0101Req;
import tpi.dgrv4.dpaa.vo.AA0101Resp;
import tpi.dgrv4.dpaa.vo.AA0101func;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0101Service {

	private TPILogger logger = TPILogger.tl;

	@Autowired
	private TsmpFuncDao tsmpFuncDao;

	public AA0101Resp queryAllFunc(TsmpAuthorization auth, AA0101Req req, ReqHeader reqHeader) {
		AA0101Resp resp = new AA0101Resp();

		resp.setFuncList(new ArrayList<AA0101func>());

		try {
			List<TsmpFunc> data = getTsmpFuncDao().findByLocaleOrderByFuncCodeAsc(reqHeader.getLocale());
			//該語系查不到資料就查英文語系
			if(CollectionUtils.isEmpty(data)) {
				data = getTsmpFuncDao().findByLocaleOrderByFuncCodeAsc(LocaleType.EN_US);
			}

			if (data != null && data.isEmpty() == false) {

				List<AA0101func> funcList = data.stream().map((func) -> {
					AA0101func f = new AA0101func();

					f.setFuncCode(func.getFuncCode());
					f.setFuncName(func.getFuncName());
					f.setFuncURL(func.getFuncUrl());

					return f;
				}).collect(Collectors.toList());

				resp.setFuncList(funcList);
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();

		}

		return resp;
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return tsmpFuncDao;
	}

}
