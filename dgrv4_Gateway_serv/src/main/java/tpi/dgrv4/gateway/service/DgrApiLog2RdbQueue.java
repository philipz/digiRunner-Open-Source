package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public abstract class DgrApiLog2RdbQueue {
	
	// 可配置參數
	@Value("${log.rdb.buffer.size:500}")
	private int configBufferSize;
	
	@Value("${log.rdb.memory.threshold.mb:256}")
	private int configMemoryThresholdMb;
	
	@Value("${log.rdb.flush.interval.ms:5000}")
	private int flushIntervalMs;
	
	@Value("${log.rdb.batch.size:100}")
	private int configBatchSize;
	
	@Value("${log.rdb.max.batch.size:500}")
	private int configMaxBatchSize;
	
	@Value("${log.rdb.min.batch.size:20}")
	private int configMinBatchSize;
	
	@Value("${log.rdb.adaptive.enabled:true}")
	private boolean adaptiveBatchingEnabled;
	
	// 靜態變數與原有代碼保持一致，但從配置中初始化
	private static int bufferSize = 500; // 設置默認值，避免初始化問題
	private static int memoryThresholdMb = 256;
	private static int batchSize = 100;
	private static int maxBatchSize = 500;
	private static int minBatchSize = 20;
	private static boolean isAdaptiveBatchingEnabled = true;
	public static BlockingQueue<DgrApiLog2RdbQueue> rdb_LoggerQueue = new ArrayBlockingQueue<>(bufferSize);
	
	// 監控指標
	private static final AtomicLong totalProcessedLogs = new AtomicLong(0);
	private static final AtomicLong totalDiscardedLogs = new AtomicLong(0);
	private static final AtomicInteger abortNum = new AtomicInteger(0);
	private static final AtomicLong totalDbWriteTime = new AtomicLong(0);
	private static final AtomicLong dbWriteCount = new AtomicLong(0);
	private static final AtomicLong lastBatchProcessTime = new AtomicLong(0);
	private static final AtomicInteger currentOptimalBatchSize = new AtomicInteger(100);
	
	// 定時刷新排程器
	private static ScheduledExecutorService scheduler;
	private static ScheduledExecutorService statsReporter;
	
	// 資源狀態控制
	private static volatile boolean isShuttingDown = false;
	private static boolean startFlag = false;
	
	// 原有功能變數
	public static List<TsmpReqLog> arrayListQ = new ArrayList<TsmpReqLog>();
	public static List<TsmpResLog> arrayListP = new ArrayList<TsmpResLog>();
	public static TsmpReqLogDao tsmpReqLogDaoObj;
	public static TsmpResLogDao tsmpResLogDaoObj;
	
	// 定時刷新實例
	private static DgrApiLog2RdbQueue instance;
	
	// 新增：為佇列和處理列表引入專用鎖定物件
	private static final Object queueLock = new Object();
	private static final Object pendingLock = new Object();
	
	public abstract void run();
	
	@PostConstruct
	public void init() {
		bufferSize = configBufferSize;
		memoryThresholdMb = configMemoryThresholdMb;
		batchSize = configBatchSize;
		maxBatchSize = configMaxBatchSize;
		minBatchSize = configMinBatchSize;
		isAdaptiveBatchingEnabled = adaptiveBatchingEnabled;
		currentOptimalBatchSize.set(batchSize);
		instance = this;
		
		// 如果rdb_LoggerQueue已經被靜態初始化，則重新創建以使用新的緩衝區大小
		if (bufferSize != 500 || rdb_LoggerQueue == null) {
			rdb_LoggerQueue = new ArrayBlockingQueue<DgrApiLog2RdbQueue>(bufferSize);
		}
		
		// 初始化定時刷新機制
		scheduler = new ScheduledThreadPoolExecutor(1);
		scheduler.scheduleAtFixedRate(() -> {
			try {
				flushLogs();
			} catch (Exception e) {
				TPILogger.tl.error("定時刷新日誌失敗: " + StackTraceUtil.logStackTrace(e));
			}
		}, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
		
		// 定期報告統計信息
		statsReporter = new ScheduledThreadPoolExecutor(1);
		statsReporter.scheduleAtFixedRate(() -> {
			try {
				reportStats();
			} catch (Exception e) {
				TPILogger.tl.error("報告統計信息失敗: " + StackTraceUtil.logStackTrace(e));
			}
		}, 60000, 60000, TimeUnit.MILLISECONDS);
		
		TPILogger.tl.info("日誌處理器初始化完成，緩衝區大小: " + bufferSize + 
				"，記憶體閾值: " + memoryThresholdMb + "MB，刷新間隔: " + flushIntervalMs + 
				"ms，批次大小: " + batchSize + "，自適應批次: " + isAdaptiveBatchingEnabled);
		
		// 啟動處理線程
		startThread();
	}
	
	private void reportStats() {
		long avgWriteTime = dbWriteCount.get() > 0 ? totalDbWriteTime.get() / dbWriteCount.get() : 0;
		
		TPILogger.tl.info("RDB日誌統計 - 隊列使用率: " + 
				String.format("%.2f", (double)rdb_LoggerQueue.size() / bufferSize * 100) + "%, " + 
				"已處理: " + totalProcessedLogs.get() + ", " + 
				"已丟棄: " + totalDiscardedLogs.get() + ", " + 
				"平均寫入時間: " + avgWriteTime + "ms, " +
				"最優批次大小: " + currentOptimalBatchSize.get());
	}
	
	@PreDestroy
	public void shutdown() {
		TPILogger.tl.info("開始關閉日誌處理器，等待所有日誌完成處理...");
		isShuttingDown = true;
		
		// 關閉排程器
		if (scheduler != null) {
			scheduler.shutdown();
			try {
				if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
					scheduler.shutdownNow();
				}
			} catch (InterruptedException e) {
				scheduler.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		
		if (statsReporter != null) {
			statsReporter.shutdown();
			try {
				if (!statsReporter.awaitTermination(5, TimeUnit.SECONDS)) {
					statsReporter.shutdownNow();
				}
			} catch (InterruptedException e) {
				statsReporter.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		
		// 最後一次刷新所有日誌
		flushLogs();
		
		TPILogger.tl.info("日誌處理器已關閉。處理日誌總數: " + totalProcessedLogs.get() + 
				"，丟棄日誌總數: " + totalDiscardedLogs.get());
	}
	
	// 定時刷新日誌的方法 (修改後)
	private void flushLogs() {
		List<TsmpReqLog> reqLogsToSave = null;
		List<TsmpResLog> resLogsToSave = null;
	
		// 在 synchronized 塊內複製列表並清除原列表
		synchronized (queueLock) {
			if (!arrayListQ.isEmpty()) {
				// 創建副本
				reqLogsToSave = new ArrayList<>(arrayListQ);
				arrayListQ.clear(); // 清除原列表
			}
		}
	
		synchronized (pendingLock) {
			if (!arrayListP.isEmpty()) {
				// 創建副本
				resLogsToSave = new ArrayList<>(arrayListP);
				arrayListP.clear(); // 清除原列表
			}
		}
	
		// 在鎖之外處理副本的保存
		long startTime = System.currentTimeMillis();
		int savedCount = 0;
	
		if (reqLogsToSave != null && !reqLogsToSave.isEmpty()) {
			try {
				// 過濾可能的 null 元素（增加健壯性）
				reqLogsToSave.removeIf(Objects::isNull);
				if (!reqLogsToSave.isEmpty()) {
					int size = reqLogsToSave.size();
					tsmpReqLogDaoObj.saveAll(reqLogsToSave);
					totalProcessedLogs.addAndGet(size);
					savedCount += size;
				}
			} catch (Exception e) {
				TPILogger.tl.error("批量保存請求日誌失敗: " + StackTraceUtil.logStackTrace(e));
				// 考慮是否需要處理保存失敗的日誌，例如重新放入隊列？
				// 目前的行為是失敗的日誌會丟失，因為原列表已被清除。
			}
		}
	
		if (resLogsToSave != null && !resLogsToSave.isEmpty()) {
			try {
				// 過濾可能的 null 元素（增加健壯性）
				resLogsToSave.removeIf(Objects::isNull);
				if (!resLogsToSave.isEmpty()) {
					int size = resLogsToSave.size();
					tsmpResLogDaoObj.saveAll(resLogsToSave);
					totalProcessedLogs.addAndGet(size);
					savedCount += size;
				}
			} catch (Exception e) {
				TPILogger.tl.error("批量保存回應日誌失敗: " + StackTraceUtil.logStackTrace(e));
				// 同上，考慮失敗處理
			}
		}
		
		// 只有實際保存了數據才記錄時間
		if (savedCount > 0) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			if (elapsedTime > 0) { // 避免除零錯誤或無意義的記錄
				totalDbWriteTime.addAndGet(elapsedTime);
				dbWriteCount.incrementAndGet();
			}
		}
	}
	
	// 提供靜態方法訪問非靜態方法
	private static void flushLogsStatic() {
		if (instance != null) {
			instance.flushLogs();
		}
	}
	
	public static void putByPoll(DgrApiLog2RdbQueue logObj) {
		while (rdb_LoggerQueue.offer(logObj) == false) {
			rdb_LoggerQueue.poll();
			totalDiscardedLogs.incrementAndGet();
			if (totalDiscardedLogs.get() % 100 == 0) {
				TPILogger.tl.warn("由於隊列已滿，已丟棄 " + totalDiscardedLogs.get() + " 條日誌");
			}
		}
	}
	
	public static void put(DgrApiLog2RdbQueue logObj) throws InterruptedException {
		// 檢查隊列是否初始化
		if (rdb_LoggerQueue == null) {
			TPILogger.tl.warn("rdb_LoggerQueue 未初始化，日誌將被丟棄");
			return;
		}
		
		// 正在關閉時不再接受新日誌
		if (isShuttingDown) {
			return;
		}
		
		// 隊列負載級別判斷
		double queueLoadFactor = (double)rdb_LoggerQueue.size() / bufferSize;
		
		// 隊列未滿三分之一直接加入
		if (queueLoadFactor < 0.3) {   
			putByPoll(logObj);
		} else {
			// 檢查記憶體狀況
			long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
			
			// 記憶體充足則繼續寫入
			if (freeMemory > memoryThresholdMb) {
				putByPoll(logObj);
				
				// 隊列負載過高時，嘗試主動刷新
				if (queueLoadFactor > 0.7) {
					flushLogsStatic();
				}
			} else {
				int currentAborts = abortNum.incrementAndGet();
				if (currentAborts % 100 == 0) {
					TPILogger.tl.warn("由於記憶體不足，已放棄寫入 " + currentAborts + " 條日誌, 可用記憶體: " + freeMemory + "MB");
					
					if (currentAborts > Integer.MAX_VALUE - 100000) {
						abortNum.set(10);
					}
					
					// 記憶體不足時，嘗試主動觸發GC並刷新現有日誌
					System.gc();
					flushLogsStatic();
				}
			}
		}
		
		startThread();
	}

	public static void startThread() {
		if (startFlag == false) {
			startFlag = true;
			new Thread("RDB-Log") {
				public void run() {
					try {
						processLogOut();
					} catch (OutOfMemoryError e) {
						startFlag = false;
						StringBuilder sb = new StringBuilder();
						sb.append("記憶體溢出錯誤: ").append(StackTraceUtil.logStackTrace(e));
						sb.append("\n\t隊列大小: ").append(rdb_LoggerQueue.size());
						sb.append("\n\t可用記憶體: ").append(Runtime.getRuntime().freeMemory() / 1024 / 1024).append("MB");
						
						TPILogger.tl.error(sb.toString());
						
						// 嘗試恢復而非直接退出
						try {
							rdb_LoggerQueue.clear();
							totalDiscardedLogs.addAndGet(rdb_LoggerQueue.size());
							flushLogsStatic();
							System.gc();
							
							// 重新啟動線程
							startFlag = false;
							startThread();
						} catch (Exception re) {
							TPILogger.tl.error("嘗試恢復失敗，系統將退出: " + re.getMessage());
							System.exit(1);
						}
					}
				}
			}.start();
			
			TPILogger.tl.debug("[#queueTrace#] \nRDB日誌隊列已啟動處理線程\n");
		}
	}

	// 自適應調整批次大小
	private static void adjustBatchSize(long processingTime, int processedItems) {
		if (!isAdaptiveBatchingEnabled || processedItems == 0) {
			return;
		}
		
		long timePerItem = processingTime / processedItems;
		int optimalBatch = currentOptimalBatchSize.get();
		
		// 如果處理時間過長，減小批次大小
		if (processingTime > 1000) { // 超過1秒
			optimalBatch = Math.max(minBatchSize, optimalBatch / 2);
		} 
		// 如果處理時間適中且單個項目處理快，增大批次大小
		else if (processingTime < 500 && timePerItem < 5) { // 少於0.5秒且每項少於5ms
			optimalBatch = Math.min(maxBatchSize, optimalBatch * 2);
		}
		
		currentOptimalBatchSize.set(optimalBatch);
	}

	public static void processLogOut() {
		TPILogger.tl.info("RDB日誌處理線程啟動...");
		long lastFlushTime = System.currentTimeMillis();
		try {
			int processedBatch = 0;
			
			while (!isShuttingDown || !rdb_LoggerQueue.isEmpty()) {
				DgrApiLog2RdbQueue o = rdb_LoggerQueue.take();
				o.run();
				
				processedBatch++;
				
				// 決定當前批次大小
				int currentBatchSize = isAdaptiveBatchingEnabled 
					? currentOptimalBatchSize.get() 
					: batchSize;
				
				// 處理隊列中累積的日誌
				int nowSize = Math.min(currentBatchSize - 1, rdb_LoggerQueue.size());
				int processedItems = 1; // 已處理一個
				
				long batchStartTime = System.currentTimeMillis();
				
				for (; nowSize > 0; nowSize--) {
					o = rdb_LoggerQueue.take();
					o.run();
					processedBatch++;
					processedItems++;
				}
				
				// 批次寫入資料庫
				long dbStartTime = System.currentTimeMillis();
				List<TsmpReqLog> reqLogsToSave = null;
            	List<TsmpResLog> resLogsToSave = null;

            	// 在 synchronized 塊內複製列表並清除原列表 (for arrayListQ)
            	synchronized (queueLock) {
                	if (!arrayListQ.isEmpty()) {
                    	reqLogsToSave = new ArrayList<>(arrayListQ);
                    	arrayListQ.clear();
                	}
            	}

            	// 在 synchronized 塊內複製列表並清除原列表 (for arrayListP)
            	synchronized (pendingLock) {
                	if (!arrayListP.isEmpty()) {
                    	resLogsToSave = new ArrayList<>(arrayListP);
                    	arrayListP.clear();
                	}
            	}

            	// 在鎖之外處理副本的保存
            	int savedCount = 0; // 跟蹤此批次實際保存的數量

            	if (reqLogsToSave != null && !reqLogsToSave.isEmpty()) {
                	try {
                    	reqLogsToSave.removeIf(Objects::isNull); // 過濾 null
                    	if (!reqLogsToSave.isEmpty()) {
                        	int size = reqLogsToSave.size();
                        	tsmpReqLogDaoObj.saveAll(reqLogsToSave);
                        	totalProcessedLogs.addAndGet(size);
                        	savedCount += size;
                    	}
                	} catch (Exception e) {
                    	TPILogger.tl.error("批量保存請求日誌失敗: " + StackTraceUtil.logStackTrace(e));
                	}
            	}

            	if (resLogsToSave != null && !resLogsToSave.isEmpty()) {
                	try {
                    	resLogsToSave.removeIf(Objects::isNull); // 過濾 null
                    	if (!resLogsToSave.isEmpty()) {
                        	int size = resLogsToSave.size();
                        	tsmpResLogDaoObj.saveAll(resLogsToSave);
                        	totalProcessedLogs.addAndGet(size);
                        	savedCount += size;
                    	}
                	} catch (Exception e) {
                    	TPILogger.tl.error("批量保存回應日誌失敗: " + StackTraceUtil.logStackTrace(e));
                	}
            	}

            	// 計算並記錄執行時間 (僅當此批次有保存數據時)
            	if (savedCount > 0) {
                 	long dbWriteTime = System.currentTimeMillis() - dbStartTime;
                 	if (dbWriteTime > 0) { // 避免無效記錄
                    	totalDbWriteTime.addAndGet(dbWriteTime);
                    	dbWriteCount.incrementAndGet();
                 	}
                 	// 計算批次處理總時間並調整批次大小
                 	long batchProcessTime = System.currentTimeMillis() - batchStartTime; // 包含 run() 調用的總時間
                 	lastBatchProcessTime.set(batchProcessTime);
                 	// 注意：adjustBatchSize 使用的是 processedItems (run() 調用次數)，而不是 savedCount (實際保存數量)
                 	// 這維持了之前的邏輯，但如果調整基於資料庫性能可能需要改用 savedCount。
                 	adjustBatchSize(batchProcessTime, processedItems);
            	}
				
				// 每處理1000批次記錄日誌統計
				if (processedBatch >= 1000) {
					TPILogger.tl.debug("日誌處理統計 - 已處理: " + totalProcessedLogs.get() + 
							", 已丟棄: " + totalDiscardedLogs.get() + 
							", 目前隊列大小: " + rdb_LoggerQueue.size() + 
							", 最優批次大小: " + currentOptimalBatchSize.get());
					processedBatch = 0;
				}
				
				// 如果隊列為空，短暫休眠避免CPU空轉
				if (rdb_LoggerQueue.isEmpty() && !isShuttingDown) {
					Thread.sleep(10);
				}
			}
		} catch (InterruptedException e) {
			TPILogger.tl.error("日誌處理線程被中斷: " + StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			TPILogger.tl.error("日誌處理發生錯誤: " + StackTraceUtil.logStackTrace(e));
		} finally {
			startFlag = false;
			
			// 確保所有剩餘日誌都被處理
			if (!isShuttingDown) {
				startThread();
			}
		}
	}
	
	// 取得監控資訊的方法
	public static String getMonitoringStats() {
		long avgWriteTime = dbWriteCount.get() > 0 ? totalDbWriteTime.get() / dbWriteCount.get() : 0;
		double queueUsage = rdb_LoggerQueue != null ? (double)rdb_LoggerQueue.size() / bufferSize * 100 : 0;
		
		return String.format(
			"日誌處理器統計 - 隊列使用率: %.2f%%, 已處理: %d, 已丟棄: %d, 平均寫入時間: %dms, 最優批次大小: %d, 可用記憶體: %dMB",
			queueUsage,
			totalProcessedLogs.get(),
			totalDiscardedLogs.get(),
			avgWriteTime,
			currentOptimalBatchSize.get(),
			Runtime.getRuntime().freeMemory() / 1024 / 1024
		);
	}
	
	// 提供公開方法獲取 abortNum 值
	public static int getAbortNum() {
		return abortNum.get();
	}
}
