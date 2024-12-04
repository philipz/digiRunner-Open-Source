package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.AA0318Service;
import tpi.dgrv4.dpaa.service.AA0319Service;
import tpi.dgrv4.dpaa.service.AA1121Service;
import tpi.dgrv4.dpaa.service.AA1128Service;
import tpi.dgrv4.dpaa.service.AA1129Service;
import tpi.dgrv4.dpaa.service.DPB9922Service;
import tpi.dgrv4.dpaa.util.DpaaHttpUtil;
import tpi.dgrv4.dpaa.vo.AA0317Data;
import tpi.dgrv4.dpaa.vo.AA0317ReqItem;
import tpi.dgrv4.dpaa.vo.AA0318Resp;
import tpi.dgrv4.dpaa.vo.AA0319Req;
import tpi.dgrv4.dpaa.vo.AA0319ReqItem;
import tpi.dgrv4.dpaa.vo.AA1120ExportData;
import tpi.dgrv4.dpaa.vo.AA1121Resp;
import tpi.dgrv4.dpaa.vo.AA1128Req;
import tpi.dgrv4.dpaa.vo.AA1129Req;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;
import tpi.dgrv4.gateway.constant.DgrDeployRole;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.RefreshGTWReq;
import tpi.dgrv4.gateway.vo.RefreshGTWResp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class InMemoryGtwRefresh2LandingService {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AA0318Service aa0318Service;
	@Autowired
	private AA0319Service aa0319Service;
	@Autowired
	private AA1121Service aa1121Service;
	@Autowired
	private AA1128Service aa1128Service;
	@Autowired
	private AA1129Service aa1129Service;
	@Autowired
	private DPB9922Service dpb9922Service;
	@Autowired
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	@Autowired
	private SeqStoreService seqStoreService;
	
	@Value("${digiRunner.gtw.deploy.role}")
	private String deployRole;

	@Value("${digiRunner.gtw.deploy.landing.ip.port}")
	private String ipPort;

	@Value("${digiRunner.gtw.deploy.id}")
	private String deployId;


	@Value("${digiRunner.gtw.deploy.landing.scheme:https}")
	private String landingScheme;

	public void landingGtw(NodeInfoPacket nodeInfoPacket) {
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(getDeployRole())) {
			doMemoryRoleWork(nodeInfoPacket);
		} else {
			TPILogger.tl.info("My role is [" + getDeployRole() + "]");
		}
	}

	private void doMemoryRoleWork(NodeInfoPacket nodeInfoPacket) {
		if (StringUtils.hasText(getIpPort())) {
			doMemoryRoleHasIpPortWork(nodeInfoPacket);
		} else {
			TPILogger.tl.info("Missing properties [ip] & [port] ");
		}
	}

	private void doMemoryRoleHasIpPortWork(NodeInfoPacket nodeInfoPacket) {
		// 取得要調用 Landing 的 IP:port,若有多組會用逗號 "," 分隔
		String[] arrIpPort = getIpPort().split(",");
		int dataSize = arrIpPort.length;

		// 若打第一個 Landing API URL 失敗,就打下一個,直到成功則跳出
		int dataIndex = 1;// 為第 N 個 Landing URL
		for (String strIpPort : arrIpPort) {
			boolean hasBreak = doIpPortLoopWorks(strIpPort, nodeInfoPacket, dataIndex, dataSize);
			if (hasBreak) {
				break;
			}

			dataIndex++;
		}
	}

	private boolean doIpPortLoopWorks(String strIpPort, NodeInfoPacket nodeInfoPacket, int dataIndex, int dataSize) {
		String reqJson = null;
		String reqUrl = null;
		String schema = getLandingScheme();
		try {
			String keeperApi = schema + "://" + strIpPort;
			reqUrl = keeperApi + "/dgrv4/ImGTW/refreshGTW";

			// req header, 加上 CApiKey 做安全驗證
			Map<String, String> header = makeImGTWRefreshGTWHeader();

			// req body
			reqJson = makeImGTWRefreshGTWBody(keeperApi, nodeInfoPacket);

			// send to API
			HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, "POST", reqJson, header, false);
			TPILogger.tl.trace(resp.getLogStr());

			if (resp.statusCode > 0 && resp.statusCode < 400) {
				// 調用完API成功, 清空 TPILogger.tokenUsedMap
				TPILogger.tokenUsedMap.clear();
				
				// 調用完API成功, 清空 TPILogger.apiUsedMap
				TPILogger.apiUsedMap.clear();
				
				// 基本資訊
				TsmpBaseResp<RefreshGTWResp> gtwResp = getObjectMapper().readValue(resp.respStr,
						new TypeReference<TsmpBaseResp<RefreshGTWResp>>() {
						});
				RefreshGTWResp refreshGtwResp = gtwResp.getBody();
				String userName = "manager";
				String orgId = refreshGtwResp.getOrgId();
				TsmpAuthorization auth = new TsmpAuthorization();
				auth.setUserName(userName);
				auth.setOrgId(orgId);
				String locale = "en-US";// 固定用英文

				// import api data
				importAPIData(resp, refreshGtwResp, userName, orgId, auth, locale);

				// import client data
				importClientData(resp, refreshGtwResp, userName, auth);

				// import setting data
				importSettingTable(resp, refreshGtwResp, userName);

				// import Token data
				importTokenData(resp, refreshGtwResp, userName);
				
				return true;
				
			} else {
				if (dataIndex == dataSize) {// 當調用最後一個 Landing API 錯誤,才印出錯誤訊息
					TPILogger.tl.error(resp.getLogStr());
				}
				
				return false;
			}

		} catch (Exception e) {
			TPILogger.tl.error("reqUrl = " + reqUrl);
			TPILogger.tl.error("reqJson = " + reqJson);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return false;
		}
	}

	private void importTokenData(HttpRespData resp, RefreshGTWResp refreshGtwResp, String userName) {
		try {
			if (refreshGtwResp.getLandLastUpdateTimeToken().longValue() > -1) {
				// 先查詢 GTW 中 TSMP_TOKEN_HISTORY 所有 token_jti 放入 HashMap
				List<TsmpTokenHistory> gtwTokenHistoryList = getTsmpTokenHistoryDao().findAll();
				Map<String, TsmpTokenHistory> inMemJtiMap = new HashMap<>();
				gtwTokenHistoryList.forEach(l -> {
					inMemJtiMap.put(l.getTokenJti(), l);
				});
				
				// 依序取出 respTsmpTokenHistoryList 的 token_jti, 在 GTW 中 TSMP_TOKEN_HISTORY 查詢是否有值,
				// 若有值則 update 資料, 否則 create 資料,
				List<TsmpTokenHistory> respTsmpTokenHistoryList = refreshGtwResp.getTokenData();
				List<TsmpTokenHistory> tsmpTokenHistorySaveList = new ArrayList<>();
				if (!CollectionUtils.isEmpty(respTsmpTokenHistoryList)) {
					respTsmpTokenHistoryList.forEach(l -> {
						String jti = l.getTokenJti();
						if (inMemJtiMap.containsKey(jti)) {
							// update TsmpTokenHistory
							tsmpTokenHistorySaveList.add(updateOrCreateTokenHistory(inMemJtiMap.get(jti), l));

							inMemJtiMap.remove(jti);
						}else {// create TsmpTokenHistory
							tsmpTokenHistorySaveList.add(updateOrCreateTokenHistory(new TsmpTokenHistory(), l));
						}
					});
					getTsmpTokenHistoryDao().saveAllAndFlush(tsmpTokenHistorySaveList);
				}

				// 若 inMemJtiMap 仍有值,則刪除
				if (!inMemJtiMap.isEmpty()) {
					inMemJtiMap.keySet().forEach(m -> {
						getTsmpTokenHistoryDao().delete(inMemJtiMap.get(m));
					});
				}
				
				// 更新 GTW 的 Token 時間
				TPILogger.lastUpdateTimeToken.set(refreshGtwResp.getLandLastUpdateTimeToken());
			}
		} catch (Exception e) {
			TPILogger.tl.error("Import token data error");
			TPILogger.tl.error(resp.getLogStr());
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private TsmpTokenHistory updateOrCreateTokenHistory(TsmpTokenHistory tsmpTokenHistory,
			TsmpTokenHistory newTsmpTokenHistory) {
		// 如果無seqNo 表示要新增則重取流水號
		Long seq = tsmpTokenHistory.getSeqNo();
		if (tsmpTokenHistory.getSeqNo() == null) {

			seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_TOKEN_HISTORY_PK);
		}
		if (seq != null) {
			tsmpTokenHistory.setSeqNo(seq);
		}

		tsmpTokenHistory.setApiResp(newTsmpTokenHistory.getApiResp());
		tsmpTokenHistory.setClientId(newTsmpTokenHistory.getClientId());
		tsmpTokenHistory.setCreateAt(newTsmpTokenHistory.getCreateAt());
		tsmpTokenHistory.setExpiredAt(newTsmpTokenHistory.getExpiredAt());
		tsmpTokenHistory.setIdpType(newTsmpTokenHistory.getIdpType());
		tsmpTokenHistory.setIdTokenJwtstr(newTsmpTokenHistory.getIdTokenJwtstr());
		tsmpTokenHistory.setReexpiredAt(newTsmpTokenHistory.getReexpiredAt());
		tsmpTokenHistory.setRefreshTokenJwtstr(newTsmpTokenHistory.getRefreshTokenJwtstr());
		tsmpTokenHistory.setRetokenJti(newTsmpTokenHistory.getRetokenJti());
		tsmpTokenHistory.setRevokedAt(newTsmpTokenHistory.getRevokedAt());
		tsmpTokenHistory.setRevokedStatus(newTsmpTokenHistory.getRevokedStatus());
		tsmpTokenHistory.setRftQuota(newTsmpTokenHistory.getRftQuota());
		tsmpTokenHistory.setRftRevokedAt(newTsmpTokenHistory.getRftRevokedAt());
		tsmpTokenHistory.setRftRevokedStatus(newTsmpTokenHistory.getRftRevokedStatus());
		tsmpTokenHistory.setRftUsed(newTsmpTokenHistory.getRftUsed());
		tsmpTokenHistory.setScope(newTsmpTokenHistory.getScope());
		tsmpTokenHistory.setStime(newTsmpTokenHistory.getStime());
		tsmpTokenHistory.setTokenJti(newTsmpTokenHistory.getTokenJti());
		tsmpTokenHistory.setTokenQuota(newTsmpTokenHistory.getTokenQuota());
		tsmpTokenHistory.setTokenUsed(newTsmpTokenHistory.getTokenUsed());
		tsmpTokenHistory.setUserName(newTsmpTokenHistory.getUserName());
		tsmpTokenHistory.setUserNid(newTsmpTokenHistory.getUserNid());
		return tsmpTokenHistory;
	}

	private void importSettingTable(HttpRespData resp, RefreshGTWResp refreshGtwResp, String userName) {
		try {
			if (refreshGtwResp.getLastUpdateTimeSetting().longValue() > -1) {
				String settingData = refreshGtwResp.getSettingData();
				byte[] dataByte = Base64Util.base64Decode(settingData);// Excel 檔案由 String 轉回 byte
				InputStream inputStream = new ByteArrayInputStream(dataByte);
				getDpb9922Service().importDataForInMemoryInput(inputStream);

				// 更新setting時間
				TPILogger.lastUpdateTimeSetting.set(refreshGtwResp.getLastUpdateTimeSetting());
			}
		} catch (Exception e) {
			TPILogger.tl.error("Import setting data error");
			TPILogger.tl.error(resp.getLogStr());
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private void importClientData(HttpRespData resp, RefreshGTWResp refreshGtwResp, String userName,
			TsmpAuthorization auth) {
		try {
			if (refreshGtwResp.getLastUpdateTimeClient().longValue() > -1) {
				AA1120ExportData clientData = refreshGtwResp.getClientData();
				AA1121Resp aa1121Resp = getAa1121Service().importClientRelatedForInMemoryInput(clientData);

				AA1128Req aa1128Req = new AA1128Req();
				aa1128Req.setLongId(aa1121Resp.getLongId());
				getAa1128Service().importClientRelatedAllCover(auth, aa1128Req);

				AA1129Req aa1129Req = new AA1129Req();
				aa1129Req.setLongId(aa1121Resp.getLongId());
				getAa1129Service().importClientRelatedConfirm(auth, aa1129Req);

				// 更新client時間
				TPILogger.lastUpdateTimeClient.set(refreshGtwResp.getLastUpdateTimeClient());
			}
		} catch (Exception e) {
			TPILogger.tl.error("Import client data error");
			TPILogger.tl.error(resp.getLogStr());
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private void importAPIData(HttpRespData resp, RefreshGTWResp refreshGtwResp, String userName, String orgId,
			TsmpAuthorization auth, String locale) {
		try {
			if (refreshGtwResp.getLastUpdateTimeAPI().longValue() > -1) {
				AA0317Data aa0317Data = refreshGtwResp.getApiData();
				
				if (aa0317Data == null) {// 表示 Landing 端的資料全部刪除了,所以要匯入的資料為 null
					AA0319Req aa0319Req = new AA0319Req();
					aa0319Req.setBatchNo(null);
					aa0319Req.setApiList(null);
					getAa0319Service().importRegCompAPIs(auth, aa0319Req, locale, null);

				} else {
					AA0318Resp aa0318Resp = getAa0318Service().uploadRegCompAPIsForInMemoryInput(userName, orgId,
							aa0317Data);// 將資料寫 TSMP_API_IMP (TSMP API 匯入資料)

					Integer batchNo = aa0318Resp.getBatchNo();
					List<AA0317ReqItem> aa0317ReqItemList = refreshGtwResp.getAa0317ReqItemList();
					List<AA0319ReqItem> apiList = new ArrayList<>();
					aa0317ReqItemList.forEach(aa0317ReqItemVo -> {
						AA0319ReqItem aa0319ReqItemVo = new AA0319ReqItem();
						aa0319ReqItemVo.setApiKey(aa0317ReqItemVo.getApiKey());
						aa0319ReqItemVo.setModuleName(aa0317ReqItemVo.getModuleName());
						apiList.add(aa0319ReqItemVo);
					});
					
					AA0319Req aa0319Req = new AA0319Req();
					aa0319Req.setBatchNo(batchNo);
					aa0319Req.setApiList(apiList);
					getAa0319Service().importRegCompAPIs(auth, aa0319Req, locale, null);
					
				} 

				// 更新api時間
				TPILogger.lastUpdateTimeAPI.set(refreshGtwResp.getLastUpdateTimeAPI());
			}
		} catch (Exception e) {
			TPILogger.tl.error("Import API data error");
			TPILogger.tl.error(resp.getLogStr());
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private String makeImGTWRefreshGTWBody(String keeperApi, NodeInfoPacket nodeInfoPacket) {
		String reqJson = null;
		RefreshGTWReq req = new RefreshGTWReq();
		req.setKeeperApi(keeperApi);
		req.setDeployRole(getDeployRole());
		req.setGtwName(TPILogger.lc.userName);
		req.setGtwID(getDeployId());
		req.setGtwLastUpdateTimeAPI(TPILogger.lastUpdateTimeAPI.get());
		req.setGtwLastUpdateTimeClient(TPILogger.lastUpdateTimeClient.get());
		req.setGtwLastUpdateTimeSetting(TPILogger.lastUpdateTimeSetting.get());
		req.setGtwLastUpdateTimeToken(TPILogger.lastUpdateTimeToken.get());
		req.setNodeInfoPacket(nodeInfoPacket);
		req.setTokenUsedMap(TPILogger.tokenUsedMap);
		req.setApiUsedMap(TPILogger.apiUsedMap);
		reqJson = DpaaHttpUtil.toReqPayloadJson(req, "inMemory");// 因為不會驗証,所以cid不是重點
		return reqJson;
	}

	/**
	 * Request Header 加上 CApiKey 做安全驗證
	 */
	private Map<String, String> makeImGTWRefreshGTWHeader() {
		String uuidForCapiKey = UUID.randomUUID().toString();
		String cuuid = uuidForCapiKey.toUpperCase();
		String capiKey = CApiKeyUtils.signCKey(cuuid);
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("cuuid", cuuid);
		header.put("capi_key", capiKey);
		return header;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}
	
	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}

	protected String getLandingScheme() {
		return landingScheme;
	}
	protected String getDeployRole() {
		return deployRole;
	}

	protected String getIpPort() {
		return ipPort;
	}

	protected String getDeployId() {
		return deployId;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected AA0318Service getAa0318Service() {
		return aa0318Service;
	}

	protected AA0319Service getAa0319Service() {
		return aa0319Service;
	}

	protected AA1121Service getAa1121Service() {
		return aa1121Service;
	}

	protected AA1128Service getAa1128Service() {
		return aa1128Service;
	}

	protected AA1129Service getAa1129Service() {
		return aa1129Service;
	}

	protected DPB9922Service getDpb9922Service() {
		return dpb9922Service;
	}

}
