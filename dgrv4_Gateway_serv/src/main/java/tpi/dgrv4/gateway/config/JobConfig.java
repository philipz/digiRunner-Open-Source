package tpi.dgrv4.gateway.config;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import tpi.dgrv4.gateway.component.cache.core.CacheValueAdapter;
import tpi.dgrv4.gateway.component.job.DeferrableJobManager;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.component.job.RefreshCacheJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Configuration
@EnableScheduling
public class JobConfig {

	@Autowired
	private TPILogger logger;
	
	@Bean
	public JobManager mainJobManager() {
		return new JobManager(logger);
	}

	@Bean
	public DeferrableJobManager deferrableJobManager() {
		return new DeferrableJobManager(logger);
	}

	@Bean
	public DeferrableJobManager refreshCacheJobManager() {
		return new DeferrableJobManager(logger);
	}

	@Bean
	@Scope(value = "prototype")
	public RefreshCacheJob refreshCacheJob(String key, Supplier<?> supplier, CacheValueAdapter adapter) {
		return new RefreshCacheJob(key, supplier, adapter, logger);
	}

}