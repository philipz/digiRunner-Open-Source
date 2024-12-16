package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.http.HttpMethod;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

public class DgrApiLog2ESQueue {
	private final static int BUFFER_SIZE = 300000; // 3分鐘壓測, DgrApiLog2ESQueue REV ES_LoggerQueue size:31,993
	public final static BlockingQueue<DgrApiLog2ESQueue> ES_LoggerQueue = new ArrayBlockingQueue<DgrApiLog2ESQueue>(BUFFER_SIZE);

	private static String[] arrEsUrl;
	private static String[] arrIdPwd;
	private String esReqUrl;
	private int timeout;
	private static String indexName;
	private String strJson;
	private String _id; // ES 批次寫 _id
	private static StringBuffer bulkDatas = new StringBuffer(); //積纍批次寫入的 Data

	public static void put(DgrApiLog2ESQueue logObj) throws InterruptedException {
		ES_LoggerQueue.put(logObj);
		DgrApiLog2ESQueue.startThread(); //雖然每次都 start 但要確保它只會做一次
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
			TPILogger.tl.debug("[#queueTrace#] \nES log Queue has bean START...\n" );
		}
	}
	
	// 自定義一個 ThreadPoolExecutor
//	private static int corePoolSize = 1; // 核心 Thread 的數量，基本上 Thread 數量不會低於此數字
//	private static int maxPoolSize = 1;  // Thread Pool 的最大數量
//	private static long keepAliveTime = 10;  // 當閒置時間超過此設定的時間的話，系統會開始回收 corePoolSize 以上多餘的 Thread
//	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(bufferSize);
//	private static RejectedExecutionHandler handler = new RejectedExecutionHandler() {
//		@Override
//		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//			//r.run(); // 以主Thread來執行它,所以會阻塞
//			TPILogger.tl.debug("ES Log Q already full....size=" + workQueue.size());
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

	public static void processLogOut() {
		try {
			while (ES_LoggerQueue.remainingCapacity() > -1) {
				
				DgrApiLog2ESQueue o = ES_LoggerQueue.take();
				o.run(); // 自己纍加 1次 ES Log Data
				int nowSize = ES_LoggerQueue.size();
				int writeRow = nowSize + 1;
				Thread.sleep( 5000 ); // wait 後面的 log 持續增加
				for (; nowSize > 0 ; nowSize--) {
					o = ES_LoggerQueue.take();
					o.run(); //add 後面積纍的 Log
				}
				o.writeES(bulkDatas.toString()); // 寫入一批 bulk ES log
				//System.out.println("模擬寫入筆數 = " + writeRow);
				
				bulkDatas = new StringBuffer(); //清空重建
				
				// 不要每次都印出來, 偶數秒才印, 觀察使用量
//				if (System.currentTimeMillis() / 1000 % 5 == 0) {
//					TPILogger.tl.debug("[#queueTrace#] ES Queue ES_LoggerQueue size:" + ES_LoggerQueue.size() );
//				}
			}
		} catch (InterruptedException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
	}
	
	public DgrApiLog2ESQueue(String[] arrEsUrl, String[] arrIdPwd, int timeout, String indexName, String strJson, String esReqUrl, String _id) {
		this.arrEsUrl = arrEsUrl;
		this.arrIdPwd = arrIdPwd;
		this.timeout = timeout;
		this.indexName = indexName;
		this.strJson = strJson;
		this.esReqUrl = esReqUrl;
		this._id = _id;
	}
	
	public DgrApiLog2ESQueue(int timeout, String strJson, String esReqUrl, String _id) {
		this.timeout = timeout;
		this.strJson = strJson;
		this.esReqUrl = esReqUrl;
		this._id = _id;
	}
	
	public void run() {
//		批次 bulk 寫入
//		每行數據對應 2 個json, 佔兩行, firstLine 是用來指明操作命令和元數據, secondLine 是自定義的數據.
//		刪除命令(delete)只佔一行, 後面不需要再跟數據
//		每條數據之間不需要多餘的換行
		String firstLine = "{\"index\":{\"_id\":\"" + _id + "\"}}";
		String secondLine = strJson;
		String bulkBody = firstLine + "\n" + secondLine + "\n";
		bulkDatas.append(bulkBody); //纍加起來
	}
	
	private void writeES(String bulkBody) {
		String esReqUrl =  getEsReqUrl(arrEsUrl, arrIdPwd, indexName, timeout);
		// 如是第一次連 ES or 連失敗則 workIndex = -1, 需要重新 find connection
//		if (workIndex == -1) {
//			esReqUrl = getEsReqUrl(arrEsUrl, arrIdPwd, indexName, timeout);
//		}
		
		if (esReqUrl == null) {
			TPILogger.tl.error("! ES URL has bean FAIL !");
			return;
		}
		
		// 到了這一步表示 ES 活著, 於是可以代入 Authorization
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("Authorization", "Basic " + arrIdPwd[workIndex]);
		
		HttpRespData resp = null;
		
		//TODO 要打開
		/*
		 */
		try {
			
			resp = HttpUtil.httpReqByRawData(esReqUrl, HttpMethod.POST.toString(), bulkBody, header, false);
			// 告知寫入成功或失敗
			if (resp.statusCode >= 200 && resp.statusCode < 400) {

				// TPILogger.tl.debug("[#queueTrace#] \n" + resp.getLogStr() + "\n");
			} else {
				TPILogger.tl.error("! Request ES API has ben FAIL !");
			}
		} catch (IOException e) {
			TPILogger.tl.error("\nesReqUrl = " + esReqUrl + "\n" + StackTraceUtil.logStackTrace(e));
		}

		
	}
	
	public static int workIndex = -1; // -1:不可用, 0~n:為可用URL
	
	public static String getEsReqUrl(String[] arrEsUrl, String[] arrIdPwd, String indexName, int timeout) {
		workIndex = -1;
		//測試連線, 找出可用的連線(可再優化)
		if (workIndex == -1) {
			int index = 0;
			boolean isConnection = false;
			for(;index < arrEsUrl.length; index++) {
				isConnection = checkConnection(arrEsUrl[index], timeout, arrIdPwd[index]);
//				isConnection = true;
				if(isConnection) {
					workIndex = index;
					break;
				}
			}
		}
		
		// 如果算完還是 -1 表示, 沒有可用的連線
		if(workIndex > -1) {
//			String esReqUrl = arrEsUrl[workIndex] + indexName + "/_doc";  // 這是單筆處理
			String esReqUrl = arrEsUrl[workIndex] + indexName + "/_bulk"; // 改為批次處理
			return esReqUrl;
		}
		
		// 沒有可用的 URL
		return null;
	}
	
	public static boolean checkConnection(String strUrl, int timeout, String idPwd) {
		String reqUrl = strUrl;
		// 到了這一步表示 ES 活著, 於是可以代入 Authorization
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("Authorization", "Basic " + idPwd);
		// <tpuser/tsMp888>
		try {
			HttpRespData resp = HttpUtil.httpReqByGet(reqUrl, header, false, false);
			if (resp.statusCode == 200) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
//		try (Socket socket = new Socket()){
////			URL url = new URL(strUrl);
////			socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), timeout);
//		} catch (Exception e) {
//			TPILogger.tl.error("URL="+strUrl + ", timeout=" + timeout + "\n" + StackTraceUtil.logStackTrace(e));
//			//try { Thread.sleep(2000); } catch (InterruptedException e1) {Thread.currentThread().interrupt();}
//			return false;
//		}
	}

}
