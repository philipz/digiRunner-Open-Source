package tpi.dgrv4.gateway.service;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.cache.DaoCacheService;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.util.CacheUtil;


@Service
public class TsmpRtnCodeService {
	
	@Autowired
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	
	@Autowired
	private JobHelper jobHelper;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private DaoCacheService daoCacheService;
	

	/**
	 * 由 TsmpRtnCodeId(rtnCode,locale) ,使用快取取得TsmpRtnCode
	 * 
	 * @param authoritiesId
	 * @return
	 */
	public TsmpRtnCode findById(String rtnCode, String locale) {
		String cacheKey = CacheUtil.genCacheKey(TsmpRtnCodeDao.class, "findById", rtnCode, locale);
		
		Supplier<TsmpRtnCode> supplier = () -> {
			TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode, locale);
			TsmpRtnCode obj = getTsmpRtnCodeDao().findById(id).orElse(null);
			return obj;
		};
		new CacheUtil().addRefreshCacheJob(getCtx(), getJobHelper(), DaoCacheService.CACHE_NAME, cacheKey, supplier);
		TsmpRtnCode tsmpRtnCode = getDaoCacheService().executeCache(cacheKey, supplier);
		
		return tsmpRtnCode;
	}
	
	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected DaoCacheService getDaoCacheService() {
		return this.daoCacheService;
	}
	
	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}
	
}