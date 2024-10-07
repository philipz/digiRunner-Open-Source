package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpOrganizationCacheProxy;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0428Item;
import tpi.dgrv4.dpaa.vo.AA0428Pair;
import tpi.dgrv4.dpaa.vo.AA0428Req;
import tpi.dgrv4.dpaa.vo.AA0428Resp;
import tpi.dgrv4.dpaa.vo.AA0428Trunc;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.vo.AA0301SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0428Service {
	private TPILogger logger = TPILogger.tl;
	private Integer pageSize;

	@Autowired
	private TsmpApiDao tsmpApiDao;

	@Autowired
	private ServiceConfig serviceConfig;

	@Autowired
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	private TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy;

	@Autowired
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	public AA0428Resp queryAPIListByLabel(TsmpAuthorization authorization, AA0428Req req, ReqHeader reqHeader) {
		AA0428Resp resp = new AA0428Resp();
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			AA0301SearchCriteria cri = checkAndSetParm(authorization, req);
			List<TsmpApi> tspmApi = getTsmpApiDao().query_AA0428Service(cri);

			if (tspmApi.size() == 0) {
				// 1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<AA0428Item> itemList = AA0428Data(tspmApi, locale);
			Map<String, String> sortBy = new HashMap<>();
			sortBy.put("apiKey", "asc");
			resp.setSortBy(sortBy);

			resp.setDataList(itemList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private List<AA0428Item> AA0428Data(List<TsmpApi> tspmApi, String locale) {
		List<AA0428Item> dataList = new ArrayList<>();
		tspmApi.forEach((api) -> {

			AA0428Item item = new AA0428Item();

			String moduleName = nvl(api.getModuleName());
			item.setModuleName(setTrunc(moduleName, 15));

			String apiKey = nvl(api.getApiKey());
			item.setApiKey(setTrunc(apiKey, 20));

			String apiName = nvl(api.getApiName());
			item.setApiName(setTrunc(apiName, 20));

			String apiDesc = api.getApiDesc();
			if (apiDesc != null) {
				item.setApiDesc(setTrunc(apiDesc, 30));
			} else {
				AA0428Trunc trunc = new AA0428Trunc();
				trunc.setOri(apiDesc);
				trunc.setT(false);
				trunc.setVal(apiDesc);
			}
			String apiStatus = nvl(api.getApiStatus());
			TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale("ENABLE_FLAG", apiStatus,
					locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0428Pair pair = setPair(apiStatus, subitemName);
				item.setApiStatus(pair);
			} else {
				AA0428Pair pair = setPair(apiStatus, apiStatus);
				item.setApiStatus(pair);
			}

			String apiSrc = nvl(api.getApiSrc());
			dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_SRC", apiSrc, locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0428Pair pair = setPair(apiSrc, subitemName);
				item.setApiSrc(pair);
			} else {
				AA0428Pair pair = setPair(apiSrc, apiSrc);
				item.setApiSrc(pair);
			}

			String jweFlag = nvl(api.getJewFlag());
			if (StringUtils.isEmpty(jweFlag)) {
				jweFlag = "0";
			}
			dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_JWT_FLAG", jweFlag, locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0428Pair pair = setPair(jweFlag, subitemName);
				item.setJweFlag(pair);
			} else {
				AA0428Pair pair = setPair(jweFlag, jweFlag);
				item.setJweFlag(pair);
			}

			String jweFlagResp = nvl(api.getJewFlagResp());
			if (StringUtils.isEmpty(jweFlagResp)) {
				jweFlagResp = "0";
			}
			dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_JWT_FLAG", jweFlagResp, locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0428Pair pair = setPair(jweFlagResp, subitemName);
				item.setJweFlagResp(pair);
			} else {
				AA0428Pair pair = setPair(jweFlagResp, jweFlagResp);
				item.setJweFlagResp(pair);
			}

			// "TSMP_API.create_time / update_time，yyyy-MM-dd HH:mm:ss 優先使用
			// update_time，若無值才取 create_time"
			Date updateTiime = api.getUpdateTime();
			if (updateTiime == null) {
				updateTiime = api.getCreateTime();
			}
			Optional<String> opt = DateTimeUtil.dateTimeToString(updateTiime, DateTimeFormatEnum.西元年月日時分秒);
			if (opt.isPresent()) {
				item.setUpdateTime(opt.get());
			} else {
				item.setUpdateTime("");
			}

			// TSMP_API.org_id，對應文字 TSMP_ORGANIZATION.org_name
			String oId = api.getOrgId();
			if (null == oId) {
				item.setOrg(null);
			} else {
				TsmpOrganization org = getOrganization(oId);
				if (org != null) {
					String orgName = nvl(org.getOrgName());
					if (StringUtils.isEmpty(orgName)) {
						orgName = api.getOrgId();
					}
					AA0428Pair pair = setPair(api.getOrgId(), orgName);
					item.setOrg(pair);
				} else {
					AA0428Pair pair = setPair(oId, oId);
					item.setOrg(pair);
				}
			}
			String label1 = api.getLabel1();
			String label2 = api.getLabel2();
			String label3 = api.getLabel3();
			String label4 = api.getLabel4();
			String label5 = api.getLabel5();
			Set<String> set = new HashSet<>();
			set.add(label1);
			set.add(label2);
			set.add(label3);
			set.add(label4);
			set.add(label5);
			set.removeIf(value -> !StringUtils.hasLength(value));
			List<String> labeList = new ArrayList<>();
			set.forEach(s -> labeList.add(s.toLowerCase()));
			labeList.sort(null);
			item.setLabelList(labeList);

			Long disableScheduledDate = api.getDisableScheduledDate();
			Long enableScheduledDate = api.getEnableScheduledDate();

			item.setDisableScheduledDate(disableScheduledDate);
			item.setEnableScheduledDate(enableScheduledDate);

			dataList.add(item);
		});

		return dataList;
	}

	private AA0301SearchCriteria checkAndSetParm(TsmpAuthorization authorization, AA0428Req req) {
		String orgId = authorization.getOrgId();
		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing(); // 1273:組織單位ID:必填參數
		}

		String paging = nvl(req.getPaging());
		if (!StringUtils.isEmpty(paging)) {
			if (!"Y".equals(paging) && !"N".equals(paging)) {
				throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
			}
		} else {
			paging = "N";
		}
		List<String> labeList = req.getLabelList();
		if (CollectionUtils.isEmpty(labeList)) {
			throw TsmpDpAaRtnCode._2009.throwing("1");
		}

		String apiKey = nvl(req.getApiKey());
		String moduleName = nvl(req.getModuleName());
		TsmpApi lastTsmpApi = null;

		TsmpApiId id = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> optApi = getTsmpApiDao().findById(id);
		if (optApi.isPresent()) {
			lastTsmpApi = optApi.get();
		}
		AA0301SearchCriteria cri = new AA0301SearchCriteria();
		cri.setLastTsmpApi(lastTsmpApi);
		// 取得組織原則
		List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(),
				Integer.MAX_VALUE); // 組織與子組織的orgId
		cri.setOrgList(orgList);
		cri.setPaging(paging);
		cri.setPageSize(getPageSize());
		String reqKey = "apiKey";
		String reqValue = "asc";
		cri.setSortColumn(nvl(reqKey));
		cri.setSort(nvl(reqValue));
		cri.setLabeList(labeList);
		return cri;

	}

	private boolean isNeedTrunc(String value, int size) {
		boolean isNeedTrunc = false;
		if (value.length() > size) {
			isNeedTrunc = true;
		}
		return isNeedTrunc;
	}

	private AA0428Trunc setTrunc(String value, int size) {
		AA0428Trunc trunc = new AA0428Trunc();
		boolean isNeedTrunc = isNeedTrunc(value, size);
		if (isNeedTrunc) {
			trunc.setOri(value);
			trunc.setT(isNeedTrunc);
			if (value.length() > size) {
				value = value.substring(0, size);
				trunc.setVal(value);
			}
		} else {
			trunc.setT(isNeedTrunc);
			trunc.setVal(value);
		}
		return trunc;
	}

	private AA0428Pair setPair(String valueV, String valueN) {
		AA0428Pair pair = new AA0428Pair();
		pair.setV(valueV);
		pair.setN(valueN);
		return pair;
	}

	/**
	 * 由 orgId ,使用快取取得getTsmpOrganization
	 * 
	 * @param authoritiesId
	 * @return
	 */
	private TsmpOrganization getOrganization(String orgId) {
		TsmpOrganization i = getTsmpOrganizationCacheProxy().findById(orgId);
		return i;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0301");
		return pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpOrganizationCacheProxy getTsmpOrganizationCacheProxy() {
		return this.tsmpOrganizationCacheProxy;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
}
