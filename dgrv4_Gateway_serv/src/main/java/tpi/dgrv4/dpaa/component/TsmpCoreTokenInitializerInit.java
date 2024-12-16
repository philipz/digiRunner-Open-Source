package tpi.dgrv4.dpaa.component;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.component.ITsmpCoreTokenInitializerInit;
import tpi.dgrv4.entity.component.IVersionService;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpCoreTokenInitializerInit implements ITsmpCoreTokenInitializerInit {

	@Autowired
	private IVersionService versionService;

	@Override
	public StringBuffer init(ContextRefreshedEvent e, StringBuffer info) {

	    if (info == null) {
	        info = new StringBuffer();
	    }
		
		String s = "\n      _       ____                                      _  _   \r\n"
				+ "   __| | __ _|  _ \\ _   _ _ __  _ __   ___ _ __  __   __ || |  \r\n"
				+ "  / _` |/ _` | |_) | | | | '_ \\| '_ \\ / _ \\ '__| \\ \\ / / || |_ \r\n"
				+ " | (_| | (_| |  _ <| |_| | | | | | | |  __/ |     \\ V /|__   _|\r\n"
				+ "  \\__,_|\\__, |_| \\_\\\\__,_|_| |_|_| |_|\\___|_|      \\_/    |_|  \r\n"
				+ "        |___/                                                  ";
		info.append(s);
		Environment env = e.getApplicationContext().getEnvironment();
		String webServerPort = env.getProperty("server.port");
		String context_path = env.getProperty("server.servlet.context-path");
		String sslFlag = env.getProperty("server.ssl.enabled");
		info.append("\n========== dgRv4 web server info ============");
		info.append("\n ...dgR VERSION = " + versionService.getVersion().strVersion);
		info.append("\n ...server.port = " + webServerPort);
		info.append("\n ...server.servlet.context-path = " + context_path);
		info.append("\n ...server.ssl.enabled = " + sslFlag);
		info.append("\n ...spring.profiles.active = " + env.getProperty("spring.profiles.active"));
		TPILogger.lc.param.put("server.port", webServerPort);
		TPILogger.lc.param.put("server.servlet.context-path", context_path);
		TPILogger.lc.param.put("server.ssl.enabled", sslFlag);
		TPILogger.lc.param.put("spring.profiles.active", env.getProperty("spring.profiles.active"));
		info.append("\n ...NodeName = " + TPILogger.lc.userName);

		LinkedList<String> logStartingMsg = tpi.dgrv4.gateway.keeper.TPILogger.logStartingMsg;
		for (String msg : logStartingMsg) {
			info.append("\n ..." + msg);
		}
	    info.append("\n_____________________________________________");
	    info.append("\n");

		return info;
	}

}
