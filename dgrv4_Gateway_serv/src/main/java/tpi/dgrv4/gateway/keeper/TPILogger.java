package tpi.dgrv4.gateway.keeper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LoggerLevelConstant;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.LicenseType;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpaaSystemInfoHelper;
import tpi.dgrv4.dpaa.constant.LicenseEnvType;
import tpi.dgrv4.dpaa.service.ChangeDbConnInfoService;
import tpi.dgrv4.dpaa.vo.DpaaSystemInfo;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.DgrNodeLostContactDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.TCP.Packet.TPILogInfoPacket;
import tpi.dgrv4.gateway.TCP.Packet.WebsiteTargetThroughputPacket;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.server.CommunicationServerConfig;
import tpi.dgrv4.gateway.service.AwsApiService;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.DPB0059Service;
import tpi.dgrv4.gateway.service.DgrApiLog2ESQueue;
import tpi.dgrv4.gateway.service.DgrApiLog2RdbQueue;
import tpi.dgrv4.gateway.service.MonitorHostService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.service.WebsiteService;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.tcp.utils.communication.ClinetNotifier;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.Role;
import tpi.dgrv4.tcp.utils.packets.DoSetUserName;

@Component
public class TPILogger  extends ITPILogger{
	//ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
	public static boolean trace_flag = false; // 可由 LoggerFlagController 啟用/停用
	public static boolean debug_flag = false; // 可由 LoggerFlagController 啟用/停用
	public static boolean info_flag = true; // 可由 LoggerFlagController 啟用/停用
	public static boolean warn_flag = true; // 可由 LoggerFlagController 啟用/停用
	public static boolean error_flag = true; // 可由 LoggerFlagController 啟用/停用
	
	public final static String nodeInfo = "nodeInfo";

	private static Logger logger = LoggerFactory.getLogger(TPILogger.class);
	public static TPILogger tl;
	
	public static LinkerClient lc;
	public static LinkedList<String> logStartingMsg = new LinkedList<String>();
	
	private ClinetNotifier lcNofify;
	
	public String loggerLevel;
	
	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	// 用來載入 @PostConstruct init()
	@Autowired
	private CommunicationServerConfig communicationServerConfig;
	
	@Autowired(required = false)
	private LicenseUtilBase util;
	
	private String prifixUserName = "gateway";
	
	public static String uuid = UUID64Util.UUID64(UUID.randomUUID()).substring(0,4);
	
	@Value("${digi.instance.id}")
	private String instanceId;

	
	@Autowired
	DPB0059Service dPB0059Service;
	@Autowired
	private JobHelper jobHelper;
	
	@Autowired
	private MonitorHostService monitorHostService;
	
	@Autowired
	private GenericCache genericCache;
	@Autowired
	private DaoGenericCache daoGenericCache;
	@Autowired
    private ApptJobDispatcher apptJobDispatcher;
	
	@Autowired
	private DgrNodeLostContactDao dgrNodeLostContactDao;
	public static final String dgrNodeLostContactDaoStr = "dgrNodeLostContactDaoStr";

	@Autowired
	private ServiceConfig serviceConfig;
	private Thread scheduler_t_refresh = null;
	private Thread scheduler_t_check = null;
	
	private StringBuffer debugMsg = new StringBuffer();
	private String delayDEBUGLineNumberStr = "";

	private StringBuffer traceMsg = new StringBuffer();
	private String delayTRACELineNumberStr = "";
	
	public ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	
	private HashMap<String, String> paramBefore ;
	
	private long startTime = System.currentTimeMillis();

	@Autowired
	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	
	@Autowired
	private WebsiteService websiteService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private AwsApiService  awsApiService;
	@Autowired
	private HikariDataSource dataSource;
	
	
	@Autowired
	private ChangeDbConnInfoService changeDbConnInfoService;
	
	public static boolean dbConnByApi ;
	
	public static Map<String, Object> dbInfoMap = new HashMap<>();
	public static final String DBINFOMAP = "dbInfoMap";
	public static final String DBINFO = "dbInfo";
	@Value("${dbInfo.mask.keys}")
	private String maskKeys;
	
	public static List<String> maskKeysArr;
	
	
	static {
		tl = new TPILogger();
		ITPILogger.tl = tl;
		TPIFileLoggerQueue.startThread();
	}
	
	@PostConstruct
	public void init() {
		tl = this;
		ITPILogger.tl = tl;
		initKeeper();
		
		// init logger LEVEL
		initLoggerLevel(null);
		maskKeysArr =  Arrays.asList(maskKeys.split(",")) ;
		
	}
	
	public static void initLoggerLevel(String loggerLevel) {
		if (loggerLevel == null) {
			loggerLevel = TPILogger.tl.currentLoggerLevel();
		}
		TPILogger.tl.loggerLevel = loggerLevel;
		
		// onlineConsole 切換到 trace 才要啟用, 但它無法適用 HA, 一次只能切換一台
		TPILogger.trace_flag = "TRACE".equalsIgnoreCase(loggerLevel);
		TPILogger.debug_flag = "TRACE".equalsIgnoreCase(loggerLevel) || "DEBUG".equalsIgnoreCase(loggerLevel) || "LOGUUID".equalsIgnoreCase(loggerLevel);
		TPILogger.info_flag =  "TRACE".equalsIgnoreCase(loggerLevel) || "DEBUG".equalsIgnoreCase(loggerLevel) || "INFO".equalsIgnoreCase(loggerLevel) || "LOGUUID".equalsIgnoreCase(loggerLevel);
		TPILogger.warn_flag =  "TRACE".equalsIgnoreCase(loggerLevel) || "DEBUG".equalsIgnoreCase(loggerLevel) || "INFO".equalsIgnoreCase(loggerLevel) || "WARN".equalsIgnoreCase(loggerLevel) || "LOGUUID".equalsIgnoreCase(loggerLevel);
		
		TPILogger.tl.error("\n<font size=18>logger level: " + loggerLevel + " </font>\n");
	}
	
	public String currentLoggerLevel(){
		Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.LOGGER_LEVEL);
		if (opt_tsmpSetting.isPresent()) {
			TsmpSetting tsmpSetting = opt_tsmpSetting.get();
			return tsmpSetting.getValue();
		}
		return "";
	}
	
	
	private void testRun() {
		new Thread() {
			public void run() {
				while(true) {
					for (int i=0 ; i< 500 ; i++) {
						TPILogger.tl.trace(i + " = # .............................................. #");
					}
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						error(StackTraceUtil.logStackTrace(e));
					    Thread.currentThread().interrupt();
					}
				}
			}
		}.start();
	}

	public void initKeeper() {

		// keeper client connect to server with [Notifier]
		lcNofify = new ClinetNotifier() {
			@Override
			public void runDisconnect(LinkerClient conn) {
				// 表示至少連線過一次, 只是 keeper server 有斷線
				if (lc != null) {
					paramBefore = (HashMap<String, String>) TPILogger.lc.param.clone();
				}

				lc = null;
				TPILogger.tl.info("detected dgr-keeper is Disconnect....");

				
				// Re-connection
				new Thread() {
					public void run() {
						// connect to server each 10 sec
						conn.close();
						connect();
					}
				}.start();
			}
			
			@Override
			public void runConnection(LinkerClient conn) {
				StringBuffer msgbuf = new StringBuffer();
				String s = "\r\n"
						+ " __  ___ .______       ______  __       __  .___________.\r\n"
						+ "|  |/  / |   _  \\     /      ||  |     |  | |           |\r\n"
						+ "|  '  /  |  |_)  |   |  ,----'|  |     |  | `---|  |----`\r\n"
						+ "|    <   |   ___/    |  |     |  |     |  |     |  |     \r\n"
						+ "|  .  \\  |  |        |  `----.|  `----.|  |     |  |     \r\n"
						+ "|__|\\__\\ | _|         \\______||_______||__|     |__|     \r\n"
						+ "                                                         \r\n"
						+ ""
						;
				msgbuf.append(s);
				msgbuf.append("\n...This Client Connect to dgr-keeper server [Socket Connected OK !]");
				msgbuf.append("\n______________________________________________________");
				msgbuf.append("\n");
				TPILogger.tl.info(msgbuf.toString());
			}
		};

		// connect to server each 10 sec
		connect();
	}

	public void info(String logMsg) {
		if(info_flag == false) {return ;} //不寫檔 + 不送 keeper
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("INFO");
			log.getLogMsg().append(logMsg);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.INFO, "[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
			
		if (info_flag && getOnlineFlagByCache()) {
			//System.err.print("INFO...");
			sendLogPacket(log);
		}
	}

	public void debug(String logMsg) {
		if(debug_flag == false) {return ;} //不寫檔 + 不送 keeper
		
		// Level 設定不是 API Log 就離開
		if ( isNotAPILogInDebugMsg(logMsg)) {return;}
		
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("DEBUG");
			log.getLogMsg().append(logMsg);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.DEBUG, "[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		
		if (debug_flag && getOnlineFlagByCache()) {
			//System.err.print("DEBUG...");
			sendLogPacket(log);
		}
	}

	public void debugDelay2sec(String logMsg) {
		if(debug_flag == false) {return ;} //不寫檔 + 不送 keeper

		// Level 設定不是 API Log 就離開
		if ( isNotAPILogInDebugMsg(logMsg)) {return;}

		if (StringUtils.hasText(debugMsg) == false || traceMsg.length() >= 1024) {
			TPILogger.tl.executorService.schedule(() -> debug(), 2, TimeUnit.SECONDS); //延遲 2 秒後
		}
		String lineNumberStr = TPILogInfo.getLineNumber2();
		if (delayDEBUGLineNumberStr.equals(lineNumberStr)) {
			debugMsg.append("\t..."+logMsg + "\n");
		} else {
			delayDEBUGLineNumberStr = lineNumberStr;
			debugMsg.append("\n\t...[ "+ lineNumberStr + "] => \n\t..." + logMsg + "\n");
		}
	}

	public void debug() {
		if(debug_flag == false) {return ;} //不寫檔 + 不送 keeper
		if (StringUtils.hasText(debugMsg) == false) {
			return ;
		}
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("DEBUG");
			log.getLogMsg().append(debugMsg.toString()); // 載入 buffer
			debugMsg.delete(0, debugMsg.length()); //清空 buffer
			delayDEBUGLineNumberStr = "";
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.DEBUG, "[ "+log.getLine() + "]\n" + log.getLogMsg().toString());
			//logger.debug("[ "+log.getLine() + "]\n" + log.getLogMsg().toString());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		
		if (debug_flag && getOnlineFlagByCache()) {
			//System.err.print("DEBUG...");
			sendLogPacket(log);
		}
	}
	
	public void error(String logMsg) {
		if(error_flag == false) {return ;} //不寫檔 + 不送 keeper
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("ERROR");
			log.getLogMsg().append(logMsg);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.ERROR, "[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		//System.out.println(log.toString());
		//logger.error("[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		
		if (error_flag && getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}
	
	public void trace(String logMsg) {
		if(trace_flag == false) {return ;} //不寫檔 + 不送 keeper
		if (traceMsg.length() == 0 || traceMsg.length() >= 1) { // 原6000
			TPILogger.tl.executorService.schedule(() -> trace(), 4, TimeUnit.SECONDS); //延遲 4 秒後
		}

		String lineNumberStr = TPILogInfo.getLineNumber2();
		if (delayTRACELineNumberStr.equals(lineNumberStr)) {
			traceMsg.append("\t..."+logMsg + "\n");
		} else {
			delayTRACELineNumberStr = lineNumberStr;
			traceMsg.append("\n\t...[ "+ lineNumberStr + "] => \n\t..." + logMsg + "\n");
		}
		
		if (traceMsg.length() > 1000) {
			trace(); //大於字數就可以直印了
		}
	}
	
	public void trace() {
		if(trace_flag == false) {return ;} //不寫檔 + 不送 keeper
		if (StringUtils.hasLength(traceMsg) == false) {
			return ;
		}
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("TRACE");
			log.getLogMsg().append(traceMsg.toString()); // 載入 buffer
			traceMsg.delete(0, traceMsg.length()); //清空 buffer
			delayTRACELineNumberStr = "";
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.TRACE, "[ "+log.getLine() + "]\n" + log.getLogMsg().toString());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		
		if (trace_flag && getOnlineFlagByCache()) {
			//System.err.print("TRACE...");
			sendLogPacket(log);
		}
	}
	
	public void warn(String logMsg) {
		if(warn_flag == false) {return ;} //不寫檔 + 不送 keeper
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("WARN");
			log.getLogMsg().append(logMsg);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.WARN, "[ "+log.getLine() + "]\n\t" + log.getLogMsg().toString());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		
		if (warn_flag && getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}

	private void sendLogPacket(TPILogInfo log) {
		if (lc!=null && getTsmpSettingService()!=null) {

			//若loggerLevel沒有初始值就從資料庫取得。
			if (loggerLevel==null) {
				Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.LOGGER_LEVEL);
				if (opt_tsmpSetting.isPresent()) {
					loggerLevel = opt_tsmpSetting.get().getValue();
				}
			}
			
			// 當Value為 450 時是過濾特定關鍵字的Log訊息，例如：API的四道LOG。
//			if (LoggerLevelConstant.getValue(loggerLevel.toUpperCase()) == 450) {
//				String msg = log.getLogMsg().toString().toUpperCase();
//				if (msg.contains(loggerLevel.toUpperCase())) {
//					TPILogInfoPacket packet = new TPILogInfoPacket(log);
//					log.userName = lc.userName;
//					lc.send(packet);
//				}
//			} else {
//				if (LoggerLevelConstant.getValue(loggerLevel.toUpperCase()) >= LoggerLevelConstant
//						.getValue(log.getLevel())) {
//
//					TPILogInfoPacket packet = new TPILogInfoPacket(log);
//					log.userName = lc.userName;
//					lc.send(packet);
//				}
//			}
			
			TPILogInfoPacket packet = new TPILogInfoPacket(log);
			log.userName = lc.userName;
			lc.send(packet);
		}
	}
	
	private void connect() {
		while(true) {
			try {
				for (int i=0 ; i<10 ; i++) {
					if (paramBefore == null) break;
					Thread.sleep(1000);
					System.out.println("wait SQL_RDB or KeeperServer connection...." + i);
				}
				
				try {
					//網路斷掉, SQL 也會連不上
					String dgrKeeper_ip = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.DGRKEEPER_IP).get().getValue();
					String dgrKeeper_portStr = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.DGRKEEPER_PORT).get().getValue();
					//dgrKeeper_ip = "192.168.30.232";
					int dgrKeeper_port = Integer.parseInt(dgrKeeper_portStr);
					lc = new LinkerClient(dgrKeeper_ip, dgrKeeper_port, Role.admin, lcNofify);
				} catch (Exception e) {
					// 未連線前無法使用 TPILogger
					LoggerFactory.getLogger(TPILogger.class).error(StackTraceUtil.logStackTrace(e));
					mySleepByCount(20);
					continue;
				}
				
				TPILogger.lc.paramObj.put(dgrNodeLostContactDaoStr,  dgrNodeLostContactDao);
				
				// 重覆使用先前的 connection 資訊
				if (paramBefore != null) {
					System.out.println("重覆使用先前的 connection 資訊...paramBefore.size(): " + paramBefore.size());
//					paramBefore.forEach((k,v)->{
						//System.out.println("[k,v] = ["+ k +", "+ v +"]");
//					});
					TPILogger.lc.param = paramBefore;
					paramBefore = null;
				}
				
				break;
				
			}catch (InterruptedException ex) {
				// 未連線前無法使用 TPILogger
				LoggerFactory.getLogger(TPILogger.class).error(StackTraceUtil.logStackTrace(ex));
				
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			} catch (Exception e) {
				// 未連線前無法使用 TPILogger
				LoggerFactory.getLogger(TPILogger.class).error(StackTraceUtil.logStackTrace(e));
			}
		}

		//System.out.println("\n...0.Client LC 名稱設定 start... :" + prifixUserName  + uuid + "\n");
		lc.setUserName(prifixUserName + "(" + instanceId + ")-" + uuid);

//		mySleep(2000);
		myWait_DoSetUserName();
		
		// 傳送 node Info 給 Keeper Server, 這樣才能取得 version
		sendNodeInfo();
//		sendDbInfo();
		// 取得所有的 client monitor info
		lc.send(new RequireAllClientListPacket());
		
//		mySleep(3000);
		myWait_RequireAllClientListPacket();

		LinkedList<ClientKeeper> allClientList  = (LinkedList<ClientKeeper>)TPILogger.lc.paramObj.get("allClientList");
		
		// 更新自己的 clientInfo
		if (allClientList != null) {
			for (ClientKeeper clientKeeper : allClientList) {
				if (clientKeeper.getUsername().equals(lc.userName)) {
					TPILogger.lc.param.put(TPILogger.nodeInfo, String.format("%s / IP：%s / PORT：%s", clientKeeper.getUsername(), clientKeeper.getIp(), clientKeeper.getPort()));
				}
			}
		}
		
		StringBuffer msgbuf = new StringBuffer();
		String s = "\r\n"
				+ " __  ___ .______       ______  __       __  .___________.\r\n"
				+ "|  |/  / |   _  \\     /      ||  |     |  | |           |\r\n"
				+ "|  '  /  |  |_)  |   |  ,----'|  |     |  | `---|  |----`\r\n"
				+ "|    <   |   ___/    |  |     |  |     |  |     |  |     \r\n"
				+ "|  .  \\  |  |        |  `----.|  `----.|  |     |  |     \r\n"
				+ "|__|\\__\\ | _|         \\______||_______||__|     |__|     \r\n"
				+ "                                                         \r\n"
				+ ""
				;

		msgbuf.append(s);
		msgbuf.append("\n...This Client Connect to dgr-keeper server [Set UserName SUCCESS]");
		msgbuf.append("\n...Keeper Server IP = " + lc.serverIP);
		msgbuf.append("\n...Keeper Server port = " + lc.port);
		msgbuf.append("\n...Name = " + prifixUserName + "(" + instanceId + ")-" + uuid);
		msgbuf.append("\n...NodeInfo = " + lc.param.get(TPILogger.nodeInfo));
		msgbuf.append("\n______________________________________________________");
		msgbuf.append("\n");
		TPILogger.tl.info(msgbuf.toString());
		TPILogger.lc.paramObj.put("RefreshMem",  getApptJobDispatcher());
		lc.paramObj.put("GenericCache", genericCache);
		lc.paramObj.put("DaoGenericCache", daoGenericCache);
		TPILogger.lc.paramObj.put("changeDbInfo",  getChangeDbConnInfoService());
		TPILogger.lc.paramObj.put("dbInfoMap", dbInfoMap);


		// 定期跟Server回報主機的資訊。
		report2Keeper();
		
		// tsmp_monitor_log 定期寫入
		report2ESLog();
		
		// 排程器啟動程式
		startRunScheduler();
		
		// 定期跟Server回報website流量。
		report2KeeperByWebsiteThroughput();
		
		//判斷aws環境 呼叫aws 的計量API
		callAwsRegisterUsage();
		
	}

	private void callAwsRegisterUsage() {
		Boolean result = false;
		StringBuffer msgbuf = new StringBuffer();
		msgbuf.append("\n==================== [START]  call AWS RegisterUsage ====================");
		
		// 確認 Injection 狀態
		msgbuf.append("\n ... * AWS Lib IoC Object Status: " + getAwsApiService().getDgrAWSComponent() + " * ... \n");

		String productCode = null;
		// 判斷LICENSE中的 env 是否AWS 是才呼叫 AWS的計量服務
		String license = getTsmpSettingService().getVal_TSMP_LICENSE_KEY();
		if (StringUtils.hasLength(license)) {
			util.initLicenseUtil(license, null);
			String env = util.getValue(license, LicenseType.env);
			if (StringUtils.hasLength(env) && env.equals(LicenseEnvType.AWS.name())) {
				msgbuf.append("\nenv : " + env);
				try {
					// 取得系統變數內的PRODUCT_CODE
					productCode = System.getenv("PRODUCT_CODE");
					if (!StringUtils.hasText(productCode)) {
						TPILogger.tl.error("Could not find PRODUCT_CODE");
					}
					msgbuf.append("\nproductCode = " + productCode);
					Integer publicKeyVersion = null;
					// 取得系統變數內的publicKeyVersion
					String publicKeyVersionStr = System.getenv("publicKeyVersion");

					if (!StringUtils.hasText(publicKeyVersionStr)) {
						TPILogger.tl.error("Could not find publicKeyVersion");
					}
					msgbuf.append("\npublicKeyVersion = " + publicKeyVersionStr);
					try {
						publicKeyVersion = Integer.valueOf(publicKeyVersionStr);
					} catch (Exception e) {
						TPILogger.tl.error("Could not convert publicKeyVersion to integer");
					}

					// 取得系統變數內的nonce (可以沒有)
					String nonce = null;

					nonce = System.getenv("nonce");
					msgbuf.append("\nnonce = " + nonce);

					// 2024 / 6/ 15 完成 IoC 注入, git commit:"c3338a0", "9191cff"
					result = getAwsApiService().awsApi(productCode, publicKeyVersion, nonce);
					String call_reg_result = getAwsApiService().getRegisterUsageResult();
					msgbuf.append("\n call RegisterUsage result: " + call_reg_result);
					//msgbuf.append("\n call RegisterUsage result: " + AwsApiService.registerUsageResult.toString());

				} catch (Exception e) {
					result = false;
					TPILogger.tl.error("Unable to call RegisterUsage\n" + StackTraceUtil.logStackTrace(e));

				} finally {
					msgbuf.append("\n call RegisterUsage valid result: " + result);
				}
			} else {
				msgbuf.append("\n ... * No AWS * ...");
			}
		} else {
			msgbuf.append("\n ... # No AWS # ...");
		}
		msgbuf.append("\n==================== [END]  call AWS RegisterUsage ======================\n\n");
		TPILogger.tl.info(msgbuf.toString());
	}


	/**
	 * 排程器啟動程式
	 */
	private void startRunScheduler() {
		boolean isSchedulerEnabled = isSchedulerEnabled();
		if (isSchedulerEnabled
				&& (this.scheduler_t_refresh == null
						|| (this.scheduler_t_refresh != null && this.scheduler_t_refresh.isInterrupted()))
				&& (this.scheduler_t_check == null
						|| (this.scheduler_t_check != null && this.scheduler_t_check.isInterrupted()))) {
			
			// [排程]每 30 分撈 DB 排程到 MemList
			refreshDB2MemList();

			// [排程]每 1 秒檢查 MemList (原 ApptJobDispatcher)
			checkMemList();
		} else {
			StringBuffer sbf = new StringBuffer();
			//info("未啟用排程器, 請設定 service.scheduler.appt-job.enable=true");
			sbf.append("Scheduler is not enabled, please set 'service.scheduler.appt-job.enable=true'"); sbf.append("\n");
			sbf.append("isSchedulerEnabled: " + isSchedulerEnabled); sbf.append("\n");
			sbf.append("this.scheduler_t_refresh: " + this.scheduler_t_refresh); sbf.append("\n");
			if (this.scheduler_t_refresh != null) {
				sbf.append("this.scheduler_t_refresh: " + this.scheduler_t_refresh.isInterrupted()); sbf.append("\n");
			} 
			info(sbf.toString());
		}
	}

	/**
	 * '排程器' 是否有啟動
	 */
	private boolean isSchedulerEnabled() {
		boolean isSchedulerEnabled = false;
		
		String strVal = getServiceConfig().get("scheduler.appt-job.enable");
		try {
			isSchedulerEnabled = Boolean.valueOf(strVal);
		} catch (Exception e) {
			warn("Error value of " + strVal + ", set to default " + isSchedulerEnabled);
		}
		
		return isSchedulerEnabled;
	}

	/**
	 * [排程]每 30 分撈 DB 排程到 MemList
	 */
	private Object refreshDB2MemListLock = new Object();
	private void refreshDB2MemList() {
		class PeriodGetter {
			long get() {
				long period = 1800000L; // 30分
				String strVal = getServiceConfig().get("job-dispatcher.period.ms");
				try {
					period = Long.valueOf(strVal);
				} catch (Exception e) {
					warn("Error value of " + strVal + ", set to default " + period);
				}
				return period;
			}
		}

		final long period = new PeriodGetter().get();
		
		this.scheduler_t_refresh = new Thread(new Runnable() {
			@Override
			public void run() {
				//i這一段是為了sonarQube而修改,但要保證永遠跑迴圈
				int i = 0;
				while (i < Integer.MAX_VALUE) {
					try {
						i++;
						if(i > 1000) {
							i = 0;
						}
						getApptJobDispatcher().refreshJobCache();
						
//						Thread.sleep(period);
						synchronized (refreshDB2MemListLock) {
							refreshDB2MemListLock.wait(period); //default to 30 min(s)
						}
					} catch (InterruptedException e) {
						error(StackTraceUtil.logStackTrace(e));
					    // Restore interrupted state...
					    Thread.currentThread().interrupt();
					} catch (Exception e) {
						error(StackTraceUtil.logStackTrace(e));
					}
				}
			}
		}, "ApptJobDispatcher");
		this.scheduler_t_refresh.start();
	}

	/**
	 * [排程]每 1 秒檢查 MemList (原 ApptJobDispatcher)
	 */
	private Object checkMemListLock = new Object();
	private void checkMemList() {
		this.scheduler_t_check = new Thread(new Runnable() {
			@Override
			public void run() {
				//i這一段是為了sonarQube而修改,但要保證永遠跑迴圈
				int i = 0;
				while (i < Integer.MAX_VALUE) {
					try {
						i++;
						if(i > 1000) {
							i = 0;
						}
						getApptJobDispatcher().run();
//						Thread.sleep(1000); // 1 sec
						// 檢查是否因為 Sleep 導致Blocking, 目前測試有效
						synchronized (checkMemListLock) {
							checkMemListLock.wait(1000); // 1 sec 
				        }
					} catch (InterruptedException e) {
						error(StackTraceUtil.logStackTrace(e));
					    // Restore interrupted state...
					    Thread.currentThread().interrupt();
					} catch (Exception e) {
						error(StackTraceUtil.logStackTrace(e));
					}
				}
			}
		}, "ApptJobDispatcher");
		this.scheduler_t_check.start();
	}
	
	private Object report2ESLogLock = new Object();
	private void report2ESLog() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (lc != null) {
					try {
//						Thread.sleep(1000);
						synchronized (report2ESLogLock) {
							report2ESLogLock.wait(1000); //取代原來的 sleep(1000), 以免阻塞;
						}
						//監控Host,寫入ES
						getMonitorHostService().execMonitor();
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						 // Restore interrupted state...
					    Thread.currentThread().interrupt();
						return ;
					} catch(Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return ;
					}
				}
			}
		}).start();		
	}

	/**
	 * 定期跟Server回報主機的資訊。
	 */
	private Object report2KeeperLock = new Object();
	private void report2Keeper() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (lc != null) {
					try {
//						Thread.sleep(1000);
						synchronized (report2KeeperLock) {
							report2KeeperLock.wait(1000); //取代原來的 sleep(1000), 以免阻塞;
						}
						
						// 傳送 Node Info 給 Keeper server
						sendNodeInfo();
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						 // Restore interrupted state...
					    Thread.currentThread().interrupt();
						return ;
					} catch(Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return ;
					}
				}
			}

			
		}).start();
	}
	
	private void sendNodeInfo() {
		//監控Host CPU
		DpaaSystemInfo cpuInfo = getMetrics();
		
		// [一級快取]的工作數量
		long main = jobHelper.getJobQueueSize(1);
		
		// [二級快取]的工作數量
		long deferrable = jobHelper.getJobQueueSize(2);
		
		// [二級快取]的工作數量
		long refresh = jobHelper.getJobQueueSize(3);
		
		NodeInfoPacket nodeInfoPacket = new NodeInfoPacket();
		nodeInfoPacket.cpu = cpuInfo.getCpu() + "";
		nodeInfoPacket.mem = cpuInfo.getMem() + "";
		nodeInfoPacket.h_used = cpuInfo.getHused() + "";
		nodeInfoPacket.h_free = cpuInfo.getHfree() + "";
		nodeInfoPacket.h_total = cpuInfo.getHtotal() + "";
	
		Map<String, Object> map = dbInfoMap;
		String dbConnect = dataSource.getUsername();
		nodeInfoPacket.dbConnect = dbConnect + "";
		if (map != null) {
			if (map.get("dbInfo") != null) {
				nodeInfoPacket.dbInfo = ((JsonNode) map.get("dbInfo")).toPrettyString();
			}

		}
		
		// 獲取API每秒轉發吞吐量，發送到keeper server。
		// 獲取目前秒數，因為有可能在GatewayFilter設定Api Req Throughput的秒數為28，在TPILogger取出的秒數是27會有秒差問題，
		// 所以TPILogger取出秒數要減2秒。
		long currentTimeMillis = System.currentTimeMillis();
		int currentSeconds = ((int) (currentTimeMillis / 1000)) - 2;

		synchronized (GatewayFilter.throughputObjLock) {
			// 重置API發吞吐量
			nodeInfoPacket.api_ReqThroughputSize = 0 + "";
			nodeInfoPacket.api_RespThroughputSize = 0 + "";
			// 取得API發吞吐量
			if (GatewayFilter.apiReqThroughput.containsKey(currentSeconds)) {
				nodeInfoPacket.api_ReqThroughputSize = GatewayFilter.apiReqThroughput.get(currentSeconds) + "";
			}
			if (GatewayFilter.apiRespThroughput.containsKey(currentSeconds)) {
				nodeInfoPacket.api_RespThroughputSize = GatewayFilter.apiRespThroughput.get(currentSeconds) + "";
			}
		}
		
		nodeInfoPacket.main = main + "";
		nodeInfoPacket.deferrable = deferrable + "";
		nodeInfoPacket.refresh = refresh + "";
		nodeInfoPacket.updateTime = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒).get();
		nodeInfoPacket.startTime = startTime + "";
		nodeInfoPacket.serverPort = TPILogger.lc.param.get("server.port");
		nodeInfoPacket.serverServletContextPath = TPILogger.lc.param.get("server.servlet.context-path");
		nodeInfoPacket.serverSslEnalbed = TPILogger.lc.param.get("server.ssl.enabled");
		nodeInfoPacket.springProfilesActive = TPILogger.lc.param.get("spring.profiles.active");
		nodeInfoPacket.keeperServerIp = lc.serverIP;
		nodeInfoPacket.keeperServerPort = lc.port + "";
		nodeInfoPacket.rcdCacheSize = genericCache.getCacheMap().size() + "";
		nodeInfoPacket.daoCacheSize = daoGenericCache.getCacheMap().size() + "";
		nodeInfoPacket.fixedCacheSize = CommForwardProcService.fixedCacheMap.size() + "";
		nodeInfoPacket.webLocalIP = lc.getLocalIpAdress();
		nodeInfoPacket.fqdn = lc.getLocalIpFQDN();
		nodeInfoPacket.ES_Queue = DgrApiLog2ESQueue.ES_LoggerQueue.size() + "";
		nodeInfoPacket.RDB_Queue = DgrApiLog2RdbQueue.rdb_LoggerQueue.size() + "";
		
		// 取得目前執行的version資訊
		nodeInfoPacket.version = "0.0.0";
		for (String str : logStartingMsg) {
			if (str.indexOf("dgrv4-gateway-") != -1 && str.indexOf(".jar") != -1) {
				String version = str.substring(str.indexOf("dgrv4-gateway-"), str.indexOf(".jar") + 4);
				nodeInfoPacket.version = version;
			}
		}
		
		long endTime = System.currentTimeMillis();						
		nodeInfoPacket.upTime = DateTimeUtil.secondsToDaysHoursMinutesSeconds(endTime - startTime);
		lc.send(nodeInfoPacket);
	}
	
	/**
	 * 定期跟Server回報website的流量。
	 */
	private Object report2KeeperByWebsiteThroughputLock = new Object();
	private void report2KeeperByWebsiteThroughput() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (lc != null) {
					try {
//						Thread.sleep(1000);
						synchronized (report2KeeperByWebsiteThroughputLock) {
							report2KeeperByWebsiteThroughputLock.wait(1000); 
						}
						
						// 傳送 WebsiteThroughput 給 Keeper server
						sendWebsiteThroughput();
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						 // Restore interrupted state...
					    Thread.currentThread().interrupt();
						return ;
					} catch(Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return ;
					}
				}
			}
		}).start();
	}
	
	
	private void sendWebsiteThroughput() throws JsonProcessingException {
		if(this.websiteService.targetThroughputMap.size() > 0) {
			Map<Long, Map<String, Map<String, Map<String, Integer>>>> sendDataMap = null;
			try {
				long nowTimestampSec = System.currentTimeMillis() / 1000;
				//將N秒內將targetUrl最新的資料來傳送,目前只抓前1秒資料,所以迴圈只跑一次
				for(int i = 1 ; i < 2 ; i++) {
					nowTimestampSec = nowTimestampSec - i;
					Map<String, Map<String, Map<String, Integer>>> clientDataMap = this.websiteService.targetThroughputMap.get(nowTimestampSec);
					if(clientDataMap != null) {
						if(sendDataMap == null) {
							sendDataMap = new HashMap<>();
							sendDataMap.put(nowTimestampSec, clientDataMap);
						}else {
							//印象中在跑迴圈時,若對該物件有增刪動作(WebsiteService)會發生錯誤,所以copy給新的物件
							Map<String, Map<String, Map<String, Integer>>> websiteNameMap = new HashMap<>(clientDataMap);
							for(String websiteName : websiteNameMap.keySet()) {
								Map<String, Map<String, Integer>> targetUrlMap = websiteNameMap.get(websiteName);
								for(String targetUrl : targetUrlMap.keySet()) {
									Map<String, Integer> typeMap = targetUrlMap.get(targetUrl);
									
									Map<String, Map<String, Map<String, Integer>>> sendWebsiteNameMap = sendDataMap.get(nowTimestampSec + i);
									if(sendWebsiteNameMap != null) {//不應該會發生==null的情況,所以沒else
										Map<String, Map<String, Integer>> sendTargetUrlMap = sendWebsiteNameMap.get(websiteName);
										if(sendTargetUrlMap != null) {
											Map<String, Integer> sendTypeMap = sendTargetUrlMap.get(targetUrl);
											if(sendTypeMap == null) {
												sendTargetUrlMap.put(targetUrl, typeMap);
											}
										}else {
											sendWebsiteNameMap.put(websiteName, targetUrlMap);
										}
									}
								}
							}
						}
					}
				}
			} catch(Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
			WebsiteTargetThroughputPacket packet = new WebsiteTargetThroughputPacket();
			String strJson = null;
			if(sendDataMap != null) {
				strJson = objectMapper.writeValueAsString(sendDataMap);
			}
			packet.targetThroughputJson = strJson;
			lc.send(packet);
		}	
	}
	
	/**
	 * Get CPU / Mem info
	 * @return
	 */
	protected DpaaSystemInfo getMetrics() {
		DpaaSystemInfoHelper dpaaSystemInfoHelper = new DpaaSystemInfoHelper();
		DpaaSystemInfo infoVo = new DpaaSystemInfo();
		dpaaSystemInfoHelper.setCpuUsedRateAndMem(infoVo);
		dpaaSystemInfoHelper.setDiskInfo(infoVo);
		dpaaSystemInfoHelper.setRuntimeInfo(infoVo);
		return infoVo;
	}

	/* 由 cache 取 onlineConsole 值 */
	public boolean getOnlineFlagByCache() {
		boolean onlineFlagVal = false;
		if (getTsmpSettingService() == null) {
			return false; // 啟動階段還不能使用
		}
		
		// 為了避免 GatewayController 發生 504 中斷了流程, 所以強制接收後傳出去 Keeper
		try {
			onlineFlagVal = getTsmpSettingService().getVal_TSMP_ONLINE_CONSOLE();
		} catch (CannotCreateTransactionException e) {
			onlineFlagVal = true; // 強制傳輸到 OnlineConsole
			logger.error(StackTraceUtil.logStackTrace(e));
		}
		
		return onlineFlagVal;
	}

	// 設定值 450 < Msg 400, [450] ,500
	private boolean isNotAPILogInDebugMsg(String logMsg) {
		
		if (loggerLevel == null) {
			loggerLevel = TPILogger.tl.currentLoggerLevel();
		}
		
		int settingLevel = LoggerLevelConstant.getValue(loggerLevel.toUpperCase());
		int debugLevel = LoggerLevelConstant.getValue(LoggerLevelConstant.DEBUG.name());
		if (settingLevel < debugLevel) {
			String msg = logMsg.toUpperCase();
			if (msg.contains(LoggerLevelConstant.APILOG.text())) {
				return false;
			} else {
				return true; // Setting=APILog, but MsgLevel=Debug
			}
		}
		return false;
	}
	
	private void mySleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void mySleepByCount(int t) {
		try {
			for(int i=0 ; i < t ; i++) {
				Thread.sleep(1000);
				logger.debug(".");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		logger.debug(".\n\n");
	}

	private void myWait_DoSetUserName() {
		synchronized (DoSetUserName.waitKey) {
			try {
				DoSetUserName.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	private void myWait_RequireAllClientListPacket() {
		synchronized (RequireAllClientListPacket.waitKey) {
			try {
				RequireAllClientListPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
	}

	protected ChangeDbConnInfoService getChangeDbConnInfoService() {
		return this.changeDbConnInfoService;
	}

	protected DPB0059Service getDPB0059Service() {
	    return this.dPB0059Service;
	}
	protected ApptJobDispatcher getApptJobDispatcher() {
        return this.apptJobDispatcher;
    }
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	

	protected MonitorHostService getMonitorHostService() {
		return monitorHostService;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return this.tsmpSettingCacheProxy;
	}

	protected AwsApiService getAwsApiService() {
		return awsApiService;
	}
}
