package tpi.dgrv4.gateway.component.job;

import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class DummyJob extends DeferrableJob {

	private TPILogger logger;

	private long interval;	// in million seconds

	public DummyJob(String groupId, long interval, TPILogger logger) {
		super(groupId);
		this.interval = interval;
		this.logger = logger;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.trace("Dummy job - start (" + this.getId() +")");
			Thread.sleep(this.interval);
			this.logger.trace("Dummy job - finish (" + this.getId() + ")");
		}catch (InterruptedException e) {
			this.logger.error("Dummy job exception!");
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		} catch (Exception e) {
			this.logger.error("Dummy job exception!");
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.interval = ((DummyJob) source).getInterval();
	}

	public long getInterval() {
		return this.interval;
	}

}
