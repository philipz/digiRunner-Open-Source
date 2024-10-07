package tpi.dgrv4.gateway.cache;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import tpi.dgrv4.gateway.component.job.DummyJob;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.RefreshCacheJob;
import tpi.dgrv4.gateway.keeper.TPILogger;


@Service
@CacheConfig(cacheNames = {"dao"})
public class DaoCacheService {

	public static final String CACHE_NAME = "dao";

	@Autowired
	private JobHelper jobHelper;
	
	@Autowired
	private TPILogger tpiLogger;

	@Cacheable(key = "#cacheKey")
	public <ReturnType> ReturnType executeCache(String cacheKey, Supplier<ReturnType> supplier) {
		final String groupId = RefreshCacheJob.GROUP_ID.concat("-").concat(cacheKey);
		DummyJob job = new DummyJob(groupId, 0, tpiLogger);
		getJobHelper().add(job);

		return supplier.get();
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

}
