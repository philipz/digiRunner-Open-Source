package tpi.dgrv4.gateway.service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

public abstract class DgrApiLog2RdbQueue  {
	
	private final static int bufferSize = 300000; // 3分鐘壓測
	public static BlockingQueue<DgrApiLog2RdbQueue> rdb_LoggerQueue = new ArrayBlockingQueue<DgrApiLog2RdbQueue>(bufferSize);

	public abstract void run();
	
	public static void put(DgrApiLog2RdbQueue logObj) throws InterruptedException {
		rdb_LoggerQueue.put(logObj);
		DgrApiLog2RdbQueue.startThread(); //雖然每次都 start 但要確保它只會做一次
	}

	// 確保只能做一次
	private static boolean startFlag = false;
	public static void startThread() {
		if (startFlag == false) {
			startFlag = true; //表示已啟動, 只能做一次
			new Thread() {
				public void run() {
					processLogOut();
				}
			}.start();
			
			// 為了證明只有做一次
			TPILogger.tl.debug("[#queueTrace#] \nRDB log Queue has bean START...\n" );
		}
	}

	
	// 自定義一個 ThreadPoolExecutor
//	private static int corePoolSize = 1; // 核心 Thread 的數量，基本上 Thread 數量不會低於此數字
//	private static int maxPoolSize = 20;  // Thread Pool 的最大數量
//	private static long keepAliveTime = 10;  // 當閒置時間超過此設定的時間的話，系統會開始回收 corePoolSize 以上多餘的 Thread
//	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(bufferSize);
//	private static RejectedExecutionHandler handler = new RejectedExecutionHandler() {
//		@Override
//		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//			// r.run(); // 以主Thread來執行它,所以會阻塞
//			TPILogger.tl.debug("RDB Q already full....size=" + workQueue.size());
//		}
//	};
//	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
//		    corePoolSize,
//		    maxPoolSize,
//		    keepAliveTime,
//		    TimeUnit.SECONDS,
//		    workQueue,
//		    handler
//		);
	public static List<TsmpReqLog> arrayListQ = new LinkedList<TsmpReqLog>();
	public static List<TsmpResLog> arrayListP = new LinkedList<TsmpResLog>();
	public static TsmpReqLogDao tsmpReqLogDaoObj;
	public static TsmpResLogDao tsmpResLogDaoObj;
	private static Object processLogOutLock = new Object();
	public static void processLogOut() {
		try {
			while (rdb_LoggerQueue.remainingCapacity() > -1) {
				DgrApiLog2RdbQueue o = rdb_LoggerQueue.take();
				o.run(); //add self Entity (Req/Res)
//				Thread.sleep(5000); // wait 後面的 log
				synchronized (processLogOutLock) {
					processLogOutLock.wait(5000);// wait 後面的 log 
				}
				
				int nowSize = rdb_LoggerQueue.size();
				for (; nowSize > 0 ; nowSize--) {
					o = rdb_LoggerQueue.take();
					o.run(); //add 後面積纍的 Entity
				}
				
				if (arrayListQ.size() > 0) {
					tsmpReqLogDaoObj.saveAll(arrayListQ); //寫入一批 Req
					arrayListQ.clear();
				}
				
				if (arrayListP.size() > 0) {
					tsmpResLogDaoObj.saveAll(arrayListP); //寫入一批 Rep
					arrayListP.clear();
				}
				
				// 不要每次都印出來, 偶數秒才印, 觀察使用量
				if (System.currentTimeMillis() / 1000 % 5 == 0) {
					TPILogger.tl.debug("[#queueTrace#] RDB_LoggerQueue size:" + rdb_LoggerQueue.size());
				}
			}
		} catch (InterruptedException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
	}
	
}
