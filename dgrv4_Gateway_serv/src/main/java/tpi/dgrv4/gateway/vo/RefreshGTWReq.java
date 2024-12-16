package tpi.dgrv4.gateway.vo;

import java.util.Map;

import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;

public class RefreshGTWReq {

	private String keeperApi;
	private String deployRole;
	private String gtwID;
	private String gtwName;
	private Long gtwLastUpdateTimeClient;
	private Long gtwLastUpdateTimeAPI;
	private Long gtwLastUpdateTimeSetting;
	/* GTW 的 token 使用量 */
	private Long gtwLastUpdateTimeToken;
	private NodeInfoPacket nodeInfoPacket;
	private Map<String, Long> tokenUsedMap;
	private Map<String, Integer> apiUsedMap;

	public String getKeeperApi() {
		return keeperApi;
	}

	public void setKeeperApi(String keeperApi) {
		this.keeperApi = keeperApi;
	}

	public String getDeployRole() {
		return deployRole;
	}

	public void setDeployRole(String deployRole) {
		this.deployRole = deployRole;
	}

	public String getGtwID() {
		return gtwID;
	}

	public void setGtwID(String gtwID) {
		this.gtwID = gtwID;
	}

	public String getGtwName() {
		return gtwName;
	}

	public void setGtwName(String gtwName) {
		this.gtwName = gtwName;
	}

	public Long getGtwLastUpdateTimeClient() {
		return gtwLastUpdateTimeClient;
	}

	public void setGtwLastUpdateTimeClient(Long gtwLastUpdateTimeClient) {
		this.gtwLastUpdateTimeClient = gtwLastUpdateTimeClient;
	}

	public Long getGtwLastUpdateTimeAPI() {
		return gtwLastUpdateTimeAPI;
	}

	public void setGtwLastUpdateTimeAPI(Long gtwLastUpdateTimeAPI) {
		this.gtwLastUpdateTimeAPI = gtwLastUpdateTimeAPI;
	}

	public Long getGtwLastUpdateTimeSetting() {
		return gtwLastUpdateTimeSetting;
	}

	public void setGtwLastUpdateTimeSetting(Long gtwLastUpdateTimeSetting) {
		this.gtwLastUpdateTimeSetting = gtwLastUpdateTimeSetting;
	}

	public NodeInfoPacket getNodeInfoPacket() {
		return nodeInfoPacket;
	}

	public void setNodeInfoPacket(NodeInfoPacket nodeInfoPacket) {
		this.nodeInfoPacket = nodeInfoPacket;
	}

	public Long getGtwLastUpdateTimeToken() {
		return gtwLastUpdateTimeToken;
	}

	public void setGtwLastUpdateTimeToken(Long gtwLastUpdateTimeToken) {
		this.gtwLastUpdateTimeToken = gtwLastUpdateTimeToken;
	}

	public Map<String, Long> getTokenUsedMap() {
		return tokenUsedMap;
	}

	public void setTokenUsedMap(Map<String, Long> tokenUsedMap) {
		this.tokenUsedMap = tokenUsedMap;
	}

	public Map<String, Integer> getApiUsedMap() {
		return apiUsedMap;
	}

	public void setApiUsedMap(Map<String, Integer> apiUsedMap) {
		this.apiUsedMap = apiUsedMap;
	}
}
