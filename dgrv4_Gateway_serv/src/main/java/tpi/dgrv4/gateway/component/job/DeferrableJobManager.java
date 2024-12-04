package tpi.dgrv4.gateway.component.job;

import org.springframework.beans.factory.annotation.Autowired;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class DeferrableJobManager extends JobManager {

	public DeferrableJobManager(TPILogger logger) {
		super(logger);
	}

	@Autowired
	private ServiceConfig serviceConfig;

	private Integer poolSize;

	/** 負責把工作從 buff2nd 拿工作出來執行 */
	private ExecutorService executor2ndSingle;

	/** 負責執行工作 */
	public ExecutorService executor2nd;

	/**
	 * Will be called by @PostConstruct in {@link JobManager}
	 */
	@Override
	public void init() {
		this.poolSize = getValue("scheduler.deferrable.thread-pool-size", Integer::valueOf, 1);
		if (this.poolSize == null || this.poolSize < 1) {
			this.poolSize = 1;
		}
		for (int i = 0; i < this.poolSize; i++) {
			Thread t = new Thread(this);
			t.start();
		}
		this.executor2ndSingle = Executors.newSingleThreadExecutor();
		this.executor2nd = Executors.newFixedThreadPool(200);
		super.logger.debugDelay2sec("JobManager(" +this.getClass().getCanonicalName() + ") is initialized with thread-pool-size (" + this.poolSize + ")");
	}

	@Override
	public void run() {
		int i;
		while (true) {
			i = takeDeferrableJob();
			if (i == 0) {
				this.logger.error("DeferrableJobManager is terminated unexpectedly!");
				break;
			}
		}
	}

	public int takeDeferrableJob() {
		try {
			final DeferrableJob job = (DeferrableJob) take();
			
			this.jobLogs.put(job.getGroupId(), System.currentTimeMillis());
			String traceMsg = "[#JOB#][BEGIN]DeferrableJob: " + job.getGroupId();
			if (job instanceof ApptJob apptJob) {
				traceMsg += ", appt_id: " + apptJob.getTsmpDpApptJob().getApptJobId();
			}
			this.logger.trace(traceMsg);
			
			job.runAfter(getJobHelper(), this);
			job.setIsDeferrableJobDone();

			long start = this.jobLogs.remove(job.getGroupId());
			traceMsg = "[#JOB#][END]DeferrableJob: " + job.getGroupId();
			if (job instanceof ApptJob apptJob) {
				traceMsg += ", appt_id: " + apptJob.getTsmpDpApptJob().getApptJobId();
			}
			traceMsg += ", cost: " + (System.currentTimeMillis() - start) + "ms";
			this.logger.trace(traceMsg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			this.logger.warn("takeDeferrableJob was interrupted: " + StackTraceUtil.logStackTrace(e));
			return 0;
		} catch (Exception e) {
			this.logger.error("takeDeferrableJob error: " + StackTraceUtil.logStackTrace(e));
		}
		return 1;
	}

	@Override
	public void takeAllJobs() {
		synchronized (this.queueLock) {
			try {
				while (peek() != null) {
					takeDeferrableJob();
				}
			} finally {
				this.queueLock.notifyAll();
			}
		}
	}

	/**
	 * @param source
	 * @return 被取代的工作數量
	 */
	public int replaceByGroupId(DeferrableJob source) {
		AtomicInteger cnt = new AtomicInteger();
		
		final String srcGroupId = source.getGroupId();
		Iterator<Job> iterator = iterator();
		//放入二級之前
		while(iterator.hasNext()) {
			final DeferrableJob target = (DeferrableJob) iterator.next();
			if (srcGroupId.equals(target.getGroupId())) {
				target.replace(source);
				source.addRefJob(target);
				cnt.addAndGet(1);
			}
		}

		return cnt.get();
	}

	public void take2ndJob() {
		this.executor2ndSingle.execute(() -> {
			// 判斷要不要做
			buff2ndWait();

			// buff2nd 取一個, 並移除一個
			Map.Entry<String, Job> entry = null;
			synchronized (this.buff2nd) {
				Iterator<Map.Entry<String, Job>> itr = this.buff2nd.entrySet().iterator();
				if (itr.hasNext()) {
					entry = itr.next(); // NoSuchElementException
					itr.remove(); // ConcurrentModificationException
				}
			}
			
			if (entry == null) return ;
			
			Job valJob = entry.getValue();
			this.executor2nd.execute(() -> {
				// pause and run 2nd Job, 但要檢查 2nd 中是否有相同的 key
				((DeferrableJob) valJob).runAfter(getJobHelper(), this);
			});
			
			// 查看執行緒池的狀態
	        //getExecutor2ndQueueSize();

			// 再觸發一次 take2nd Job
			doAgaint2nd();
		});
	}
	
	public int getExecutor2ndQueueSize() {
		
		if (executor2nd==null)
			return -1; // UT 時不會啟動它
		
		int queueSize = ((ThreadPoolExecutor) executor2nd).getQueue().size();
//		System.out.println("Waiting tasks: " + queueSize);
		return queueSize;
	}
	
	public int getExecutor2ndAvalibleSize() {
		// 查看執行緒池的狀態
        int activeCount = ((ThreadPoolExecutor) executor2nd).getActiveCount();
        int poolSize = ((ThreadPoolExecutor) executor2nd).getPoolSize();
//        System.out.println("Active threads: " + activeCount);
//        System.out.println("Pool size: " + poolSize);
		return poolSize - activeCount;
	}

	public void doAgaint2nd() {
		if (!this.buff2nd.isEmpty()) {
			executor2ndSingle.execute(() -> {
				take2ndJob(); // 2nd 若有工作就一直 do Job
			});
		}
	}

	private void buff2ndWait() {
		if (this.buff2nd.isEmpty()) {
			try {
				synchronized (this.buff2nd) {
					this.buff2nd.wait(); // take1stJob 有做 notify
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/** This is only for unit test */
	public void take2ndJob(LinkedHashMap<String, Job> buff2nd) {
		// 2nd 若有工作就一直 do Job
		while (!buff2nd.isEmpty()) {
			Map.Entry<String, Job> entry = null;
			Iterator<Map.Entry<String, Job>> itr = buff2nd.entrySet().iterator();
			if (itr.hasNext()) {
				entry = itr.next(); // NoSuchElementException
				itr.remove(); // ConcurrentModificationException
			}
			if (entry == null) {
				return;
			}

			Job valJob = entry.getValue();

			// run Job
			((DeferrableJob) valJob).runAfter(getJobHelper(), this);
		}
	}

	private <T> T getValue(String key, Function<String, T> func, T defaultVal) {
		String strVal = getServiceConfig().get(key);
		try {
			return func.apply(strVal);
		} catch (Exception e) {
			this.logger.debug("Error value of " + strVal + ", set to default " + defaultVal);
		}
		return defaultVal;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

}
