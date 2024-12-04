package tpi.dgrv4.gateway.controller;

import java.util.LinkedList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.TCP.Packet.ChangeLoggerLevelPacket;
import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OnlineConsole2Service;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.AllClientInfoData;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.ComposerInfoData;
import tpi.dgrv4.gateway.vo.CurrentLogReq;
import tpi.dgrv4.gateway.vo.OnlineConsole;

@RestController
@RequestMapping("/dgrv4")

public class OnlineConsole2Controller {

	public static Object lockObj = new Object();
	
	@Autowired
	private TsmpSettingService tsmpSettingService; 
	
	@Autowired
	private OnlineConsole2Service onlineConsole2Service;

	@Autowired
	private TPILogger logger;
	
	@PostMapping(value = "/onlineConsole2/api")
	public ResponseEntity<?> onlineconsole2(HttpServletRequest request, @RequestBody CurrentLogReq req) {

		List<OnlineConsole> data = null;

		if (tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			data = onlineConsole2Service.outputlog(req);
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN...");
		}

		return ResponseEntity.ok(data);
	}

	@PostMapping(value = "/onlineConsole2/showAllClient")
	public ResponseEntity<?> showAllClient(HttpServletRequest request) {
		
		AllClientInfoData allClientInfoData = new AllClientInfoData();
		
		if (!tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return ResponseEntity.ok(allClientInfoData);
		}
		
		if (TPILogger.lc != null) {
			TPILogger.lc.send(new RequireAllClientListPacket());
			myWaitLock();
			allClientInfoData.setAllClientList((LinkedList<ClientKeeper>)TPILogger.lc.paramObj.get("allClientList"));
			allClientInfoData.setAllComposerList((LinkedList<ComposerInfoData>)TPILogger.lc.paramObj.get(RequireAllClientListPacket.allComposerListStr));
			// lc.paramObj.put("CPU..etc", DpaaSystemInfo);
		} else {
			TPILogger.tl.error("<Keeper Server> Lost Connection" );
		}
		
		
		return ResponseEntity.ok(allClientInfoData);
	}
	
	@GetMapping(value = "/onlineConsole2/changeLoggerLevel", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String changeLoggerLevel(HttpServletRequest request) {
		String loggerLevel = request.getParameter("loggerLevel");
		
		if (tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			logger.loggerLevel = loggerLevel;
			onlineConsole2Service.changeLoggerLevel(loggerLevel);
			
			// error 才能一直印出來
			//logger.error("<font size=18>切換logger Level為: " + loggerLevel + " </font>");
			
			// 傳送出去通知
			ChangeLoggerLevelPacket packet = new ChangeLoggerLevelPacket();
			packet.loggerLevel = loggerLevel;
			//TPILogger.tl.info("[Contrller Start] change log level: " + loggerLevel);
			TPILogger.lc.send(packet);
			//TPILogger.tl.info("[Contrller End] change log level: " + loggerLevel);
			return "success";
		} else {
			return "FORBIDDEN...";
		}		
	}
	
	@GetMapping(value = "/onlineConsole2/currentLoggerLevel", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String currentLoggerLevel(HttpServletRequest request) {
		if (tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {			
			return onlineConsole2Service.currentLoggerLevel();
		} else {
			return "FORBIDDEN...";
		}		
	}
	
	private void myWaitLock() {
		synchronized (RequireAllClientListPacket.waitKey) {
			try {
				RequireAllClientListPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			    Thread.currentThread().interrupt();
			}
		}
	}
	
}