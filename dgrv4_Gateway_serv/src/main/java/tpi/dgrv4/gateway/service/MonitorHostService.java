package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpaaSystemInfoHelper;
import tpi.dgrv4.dpaa.vo.DpaaSystemInfo;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpMonitorLog;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class MonitorHostService {

	private final static String ES_URL = "ES_URL";

	private final static  String ES_HEADER = "ES_HEADER";
	
	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	private CommForwardProcService commForwardProcService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private JobHelper jobHelper;
	
	private DpaaSystemInfoHelper dpaaSystemInfoHelper = new DpaaSystemInfoHelper();
	
	public void execMonitor() {
		try {
			
			boolean isDisable = getTsmpSettingService().getVal_ES_MONITOR_DISABLE();
			if(isDisable) {
				return;
			}

			Map<String, Object> esInfo = getEsInfo();
			if (CollectionUtils.isEmpty(esInfo)) {
				return;
			}
			
			Long currentTimestamp = System.currentTimeMillis();
			DpaaSystemInfo infoVo = new DpaaSystemInfo();
			
			getDpaaSystemInfoHelper().setCpuUsedRateAndMem(infoVo);
			getDpaaSystemInfoHelper().setDiskInfo(infoVo);
			getDpaaSystemInfoHelper().setRuntimeInfo(infoVo);
			
			String type = getTsmpSettingService().getVal_ES_SYS_TYPE();
			String node = TPILogger.lc.param.get(TPILogger.nodeInfo);
			
			TsmpMonitorLog logVo = new TsmpMonitorLog();
			logVo.setCpu(infoVo.getCpu());
			logVo.setCreateTimestamp(currentTimestamp);
			logVo.setDavail(infoVo.getDavail());
			logVo.setDfs(infoVo.getDfs());
			logVo.setDtotal(infoVo.getDtotal());
			logVo.setDusage(infoVo.getDusage());
			logVo.setDused(infoVo.getDused());
			logVo.setHfree(infoVo.getHfree());
			logVo.setHmax(infoVo.getHmax());
			logVo.setHtotal(infoVo.getHtotal());
			logVo.setHused(infoVo.getHused());
			logVo.setMem(infoVo.getMem());
			logVo.setNode(node);
			logVo.setTs(DateTimeUtil.dateTimeToString(new Date(currentTimestamp), DateTimeFormatEnum.西元年月日T時分秒時區).get());
			logVo.setType(type);
			
			// [一級快取]的工作數量
			long main = getJobHelper().getJobQueueSize(1);
			
			// [二級快取]的工作數量
			long deferrable = getJobHelper().getJobQueueSize(2);
			
			// [二級快取]的工作數量
			long refresh = getJobHelper().getJobQueueSize(3);

			logVo.setMainJobSize(main);
			logVo.setDeferrableJobSize(deferrable);
			logVo.setRefreshJobSize(refresh);
			
			// ES Disk
			Map<String, Object> es = getEsLog(esInfo);
			if (!CollectionUtils.isEmpty(es)) {
				logVo.setEs(es);
			}
			
			this.insertEs(logVo, esInfo);
			
			TPILogger.tl.trace("monitor host success");
		}catch(Exception e) {
			TPILogger.tl.error("monitor host fail");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private Map<String, Object> getEsInfo() {
		// 檢查是否有ES_ID_PWD參數
		String esCredential = null;
		try {
			esCredential = getTsmpSettingService().getVal_ES_ID_PWD();
		} catch (DgrException e) {
			TPILogger.tl.debug(" please wait 'TsmpCoreTokenHelper loading...'/n" + StackTraceUtil.logStackTrace(e));
			return null;
		}
		if (!StringUtils.hasText(esCredential)) {
			TPILogger.tl.info("tsmp_setting ES_ID_PWD no value");
			return null;
		}

		// 檢查是否有ES_URL參數
		String esUrl = getTsmpSettingService().getVal_ES_URL();
		if (!StringUtils.hasText(esUrl)) {
			TPILogger.tl.info("tsmp_setting ES_URL no value");
			return null;
		}

		// 檢查ES_ID_PWD參數內容值個數和URL個數有沒有吻合
		String[] arrEsUrl = esUrl.split(",");
		String[] arrIdPwd = esCredential.split(",");
		if (arrIdPwd.length != arrEsUrl.length) {
			TPILogger.tl.info("ES connection info and id pwd is not mapping");
			return null;
		}

		// 測試連線
		int index = 0;
		boolean isConnection = false;
		for (; index < arrEsUrl.length; index++) {
			isConnection = getCommForwardProcService().checkConnection(arrEsUrl[index]);
			if (isConnection) {
				break;
			}
		}
		if (!isConnection) {
			TPILogger.tl.info("es connection is fail");
			return null;
		}
		
		Map<String, Object> esInfo = new HashMap<>();
		esInfo.put(ES_URL, arrEsUrl[index]);
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("Authorization", "Basic " + arrIdPwd[index]);
		esInfo.put(ES_HEADER, header);
		return esInfo;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getEsLog(Map<String, Object> esInfo) {
		Map<String, Object> es = new LinkedHashMap<>();
		// allocation
		try {
			HttpRespData resp = getEsResp(esInfo);

			JsonNode nodes = getObjectMapper().readTree(resp.respStr).required("nodes");
			if (nodes.size() > 0) {
				List<Map<String, Object>> allocations = new LinkedList<>();
				nodes.fields().forEachRemaining(entry -> {
					JsonNode node = entry.getValue();
					Map<String, Object> vo = new LinkedHashMap<>();
					vo.put("node.id", entry.getKey());
					vo.put("node.name", node.required("name").asText());
					if(node.has("host")) {
						vo.put("host", node.required("host").asText());
					}else {
						vo.put("host", "N/A"); //opensearch取不到值
					}
					if(node.has("ip")) {
						vo.put("ip", node.required("ip").asText());
					}else {
						vo.put("ip", "N/A");  //opensearch取不到值
					}
					JsonNode fs = node.required("fs");
					JsonNode fs_total = fs.required("total");
					long diskTotal = fs_total.required("total_in_bytes").longValue();
					long diskAvail = fs_total.required("available_in_bytes").longValue();
					vo.put("disk.total", diskTotal);
					vo.put("disk.avail", diskAvail);
					long diskUsed = diskTotal - diskAvail;
					vo.put("disk.used", diskUsed);
					BigDecimal diskUsage = BigDecimal.valueOf(diskUsed).divide(BigDecimal.valueOf(diskTotal), 2,
							RoundingMode.HALF_UP);
					vo.put("disk.usage_in_decimal", diskUsage.doubleValue()); // Stored as decimal. ex: 0.17
					vo.put("disk.usage_in_percent", diskUsage.multiply(BigDecimal.valueOf(100L))); // Stored as percentage.
					allocations.add(vo);
				});
				es.put("allocation", allocations);
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return es;
	}
	
	protected HttpRespData getEsResp(Map<String, Object> esInfo) throws Exception {
		String api = String.valueOf(esInfo.get(ES_URL)) + "_nodes/stats/fs";
		TPILogger.tl.trace("Call ES REST API: " + api);
		
		Map<String, String> header = (Map<String, String>) esInfo.get(ES_HEADER);
		HttpRespData resp = HttpUtil.httpReqByGet(api, header, false);
		TPILogger.tl.trace(resp.getLogStr());
		if (HttpStatus.OK.value() != resp.statusCode) {
			throw new Exception();
		}
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	private void insertEs(TsmpMonitorLog logVo, Map<String, Object> esInfo) throws IOException {
		String esUrl = String.valueOf(esInfo.get(ES_URL));
		
		String indexName = "tsmp_monitor_log_" + DateTimeUtil.dateTimeToString(new Date(logVo.getCreateTimestamp()), DateTimeFormatEnum.西元年月日_4).get();
		String strJson = getObjectMapper().writeValueAsString(logVo);
		String esReqUrl = esUrl + indexName + "/_doc";
		
		//call es add data
		Map<String, String> header = (Map<String, String>) esInfo.get(ES_HEADER);
		
		HttpRespData resp = HttpUtil.httpReqByRawData(esReqUrl, HttpMethod.POST.toString(), strJson, header, false);
		TPILogger.tl.trace(resp.getLogStr());
	}

	protected DpaaSystemInfoHelper getDpaaSystemInfoHelper() {
		return dpaaSystemInfoHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected JobHelper getJobHelper() {
		return jobHelper;
	}

	public CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	

}
