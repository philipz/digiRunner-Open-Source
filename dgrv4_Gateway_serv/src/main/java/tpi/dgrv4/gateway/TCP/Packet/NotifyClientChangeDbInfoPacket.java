package tpi.dgrv4.gateway.TCP.Packet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.thinkpower.tsmp.dpaa.TestUtils;

import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.ChangeDbConnInfoService;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class NotifyClientChangeDbInfoPacket implements Packet_i {
	private static final String CHANGEDBINFO = "changeDbInfo";
	private String cusIpPort;

	private String notifyConnectChanged;

	private String connect;
	private TsmpAuthorization auth;

	public NotifyClientChangeDbInfoPacket() {

	}

	public NotifyClientChangeDbInfoPacket(TsmpAuthorization auth, String connect, String cusIpPort,
			String notifyConnectChanged) {
		this.setConnect(connect);
		this.setAuth(auth);
		this.setCusIpPort(cusIpPort);
		this.setNotifyConnectChanged(notifyConnectChanged);
	}

	@Override
	public void runOnServer(LinkerServer ls) {
		if (ls.paramObj.containsKey(CHANGEDBINFO) == false) {
			ls.paramObj.put(CHANGEDBINFO, new ChangeDbConnInfoService());
		}
		CommunicationServer.cs.sendToAll(ls, this);

	}

	@Override
	public void runOnClient(LinkerClient lc) {
		try {
			synchronized (TPILogger.lc) {

				ChangeDbConnInfoService a = (ChangeDbConnInfoService) lc.paramObj.get(CHANGEDBINFO);
				if (a != null) {
					Map<String, Object> map = TPILogger.dbInfoMap;

					JsonNode dbInfoRespJson = (JsonNode) map.get(TPILogger.DBINFO);
					String username = "";
					String password = "";

					if ("1".equals(connect)) {
						username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbUsername1");
						password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbMima1");
					}
					if ("2".equals(connect)) {
						username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbUsername2");
						password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbMima2");
					}
					// 變更連線
					a.changeDbConnInfo(username, password);

					// call CUS0005
					callCUS0005(connect, auth);

				}
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private void callCUS0005(String connect, TsmpAuthorization auth) {
		Map<String, String> header = new HashMap<>();
		String uuidForCapiKey = UUID.randomUUID().toString();
		String cuuid = uuidForCapiKey.toUpperCase();
		String capi_key = CApiKeyUtils.signCKey(cuuid);
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("cuuid", cuuid);
		header.put("capi-key", capi_key);
		header.put("Authorization", auth.getTokenString());
		HttpRespData dbInfoResp = null;
		try {
			String url = getApiUrl();
			ConnectReq req = new ConnectReq();
			req.setConnect(connect);
			dbInfoResp = HttpUtil.httpReqByRawData(url, "POST", TestUtils.toReqPayloadJson(req), header, false);
			TPILogger.tl.debug(dbInfoResp.getLogStr());

		} catch (IOException e) {
			TPILogger.tl.info(StackTraceUtil.logTpiShortStackTrace(e));
			throw new TsmpDpAaException(e.getMessage());

		}
		if (dbInfoResp.statusCode <= 0 || dbInfoResp.statusCode >= 400) {
			throw new TsmpDpAaException("Error Status Code :" + dbInfoResp.statusCode);
		}

	}

	private String getApiUrl() {
		return "https://" + getCusIpPort() + getNotifyConnectChanged();

	}

	/**
	 * @return the connect
	 */
	public String getConnect() {
		return connect;
	}

	/**
	 * @param connect the connect to set
	 */
	public void setConnect(String connect) {
		this.connect = connect;
	}

	public TsmpAuthorization getAuth() {
		return auth;
	}

	public void setAuth(TsmpAuthorization auth) {
		this.auth = auth;
	}

	public String getCusIpPort() {
		return cusIpPort;
	}

	public void setCusIpPort(String cusIpPort) {
		this.cusIpPort = cusIpPort;
	}

	public String getNotifyConnectChanged() {
		return notifyConnectChanged;
	}

	public void setNotifyConnectChanged(String notifyConnectChanged) {
		this.notifyConnectChanged = notifyConnectChanged;
	}

}

class ConnectReq {
	private String connect;

	/**
	 * @return the connect
	 */
	public String getConnect() {
		return connect;
	}

	/**
	 * @param connect the connect to set
	 */
	public void setConnect(String connect) {
		this.connect = connect;
	}

}
