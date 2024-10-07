package tpi.dgrv4.gateway.keeper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tpi.dgrv4.common.utils.StackTraceUtil;

public class TPIFileLoggerQueue {
	private static Logger logger = LoggerFactory.getLogger(TPIFileLoggerQueue.class);
	
	//ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
	public static final int TRACE = 0;
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	
	private final static int bufferSize = 100000; // 11,574
	public static BlockingQueue<TPIFileLoggerQueue> fileLoggerQueue = new ArrayBlockingQueue<TPIFileLoggerQueue>(bufferSize);
	public int level = -10;
	public String msg = null;

	public static boolean flag = true; //由 'LoggerFlagController' 控制是否不寫入 File
	
	public TPIFileLoggerQueue(int level , String msg) {
		this.level = level;
		this.msg = msg;
	}
	
	public static void put(int level , String msg) throws InterruptedException {
		// ChatGPT: 生產者執行緒嘗試在阻塞佇列中加入兩個元素，但佇列的容量只有1。
		// 因此，當它嘗試添加第二個元素時，它會被阻塞。
		// 稍後，消費者執行緒從佇列中取出元素，並中斷生產者執行緒，導致生產者執行緒在 put 方法中拋出 InterruptedException。
		// 試著解決因為 Queue 滿了而丟出的 Exception.
		try {
			fileLoggerQueue.put(new TPIFileLoggerQueue(level, msg));
		} catch (InterruptedException e) {
			logger.error("fileLoggerQueue.size()= " + fileLoggerQueue.size() 
			+ "\n, Size maybe is FULL.  " + StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
	}
	
	public static void startThread() {
		new Thread() {
			public void run() {
				processLogFileOut();
			}
		}.start();
	}
	
	public static void processLogFileOut(){
		try {
			while (fileLoggerQueue.remainingCapacity() > -1) {
				TPIFileLoggerQueue o = fileLoggerQueue.take();
				o.procFlush();
				
				// 不要每次都印出來, 偶數秒才印, 觀察使用量
//				if (System.currentTimeMillis() / 1000 % 5 == 0) {
//					System.out.println("TPI Logger Queue size:" + fileLoggerQueue.size());
//				}
			}
		} catch (InterruptedException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
	}
	
	private void procFlush() {
		// 不產生任何寫入 Logger 的動作
		if (flag == false) {
			return;
		} 
		
		switch (this.level) {
		case TPIFileLoggerQueue.TRACE:
			logger.trace(msg);
			break;
		case TPIFileLoggerQueue.DEBUG:
			logger.debug(msg);
			break;
		case TPIFileLoggerQueue.INFO:
			logger.info(msg);
			break;
		case TPIFileLoggerQueue.WARN:
			logger.warn(msg);
			break;
		case TPIFileLoggerQueue.ERROR:
			logger.error(msg);
			break;
		default:
			logger.error(msg);
			break;
		}
	}
}
