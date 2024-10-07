package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@RestController
@CrossOrigin()
@RequestMapping("/dgrv4")
public class OnlineConsole2TestController {

	@Autowired
	private TPILogger logger;
	
	@Autowired
	private TsmpSettingService tsmpSettingService; 

	@GetMapping(value = "/onlineConsole2/testonlineconsole2", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String testonlineconsole2(HttpServletRequest request) {
		
		if (!tsmpSettingService.getVal_TSMP_ONLINE_CONSOLE()) {
			return "FORBIDDEN...";
		}
		
		String time = request.getParameter("time");
		logger.trace("I AM '<span id='userName'>" + TPILogger.lc.userName + "</span>'...");
		if (time != null) {
			for (int i = 1; i <= Integer.parseInt(time); i++) {
				logger.info("I AM '<span id='userName'>" + TPILogger.lc.userName + "</span>'..." + i);
			}
		} else {
			logger.debug("I AM '<span id='userName'>" + TPILogger.lc.userName + "</span>'");
		}
		return "success";
	}
}
