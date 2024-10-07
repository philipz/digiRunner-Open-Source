package tpi.dgrv4.gateway.TCP.Packet;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class SendDbInfoMapPacket implements Packet_i {
	Map<String, JsonNode> data;
	public JsonNode dbInfoJson;
	@Override
	public void runOnServer(LinkerServer ls) {
		if (ls.paramObj.containsKey(TPILogger.DBINFOMAP) == false) {
			ls.paramObj.put(TPILogger.DBINFOMAP, new HashMap<String, String>());
		}

		data = (Map<String, JsonNode>) ls.paramObj.get(TPILogger.DBINFOMAP);
		data.put(TPILogger.DBINFO, dbInfoJson);
		CommunicationServer.cs.sendToAll(ls, this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {

		lc.paramObj.put(TPILogger.DBINFOMAP, data);
		Map<String, Object> map = (Map<String, Object>) lc.paramObj.get(TPILogger.DBINFOMAP);
		TPILogger.dbInfoMap = map;
	}

}
