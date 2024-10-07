package tpi.dgrv4.gateway.service;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.IVersionService;
import tpi.dgrv4.entity.vo.VersionInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;

@Service
public class VersionService implements IVersionService{

	/**
	 * 需要存取外界, 不加入 UT
	 */
	protected String getAllClientList() {
		if (TPILogger.lc == null) return null;
			
		String username = TPILogger.lc.userName;
		LinkedList<ClientKeeper> allClientList = null;
		allClientList = (LinkedList<ClientKeeper>)TPILogger.lc.paramObj.get("allClientList");

		List<ClientKeeper> clientKeeperList = null;
		ClientKeeper clientKeeper = null;
		if (allClientList != null) {
			clientKeeperList = allClientList.stream() //
					.filter(ck -> ck.getUsername().equals(username)) //
					.collect(Collectors.toList());
		}

		// 取 jar 檔的版本
		String version = null;			// version:dgrv4-gateway-1.0.0-20230420_1100.jar 
		if (clientKeeperList != null && clientKeeperList.size() == 1) {
			clientKeeper = clientKeeperList.get(0);
			version = clientKeeper.getVersion();
		}
		return version;
	}
	
	public String getProjectName() {
		return "digiRunner";
	}
	
	public VersionInfo getVersion() {
		// POJO Data
		VersionInfo v = new VersionInfo();
		v.strVersion = "0.0.0";
		v.MajorVersionNo = "0";

		try(var is = new ClassPathResource("version.txt").getInputStream()) {

			String version = new String(is.readAllBytes());

			v.strVersion = version;
			v.MajorVersionNo = version.split("-")[0];

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return v;
	}
}
