package tpi.dgrv4.dpaa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.filter.CustomEventEvaluator;
import tpi.dgrv4.gateway.filter.CustomUuidEventEvaluator;
import tpi.dgrv4.gateway.keeper.TPILogger;

@RestController
public class LogbackFilterController {
	
	
	@GetMapping(value = "/dgrv4/logback/sys-info/up") 
	public ResponseEntity<?> sysInfoUp(HttpServletRequest request) {
		String json = "[logback/sys-info/up]...OK";
		try {
			CustomEventEvaluator.IS_EXEC = true;
		} catch (Exception e) {
			TPILogger.tl.debug("...[logback/sys_info/up]...(error)");
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/dgrv4/logback/sys-info/down") 
	public ResponseEntity<?> sysInfoDown(HttpServletRequest request) {
		String json = "[logback/sys-info/down]...OK";
		try {
			CustomEventEvaluator.IS_EXEC = false;
		}catch (Exception e) {
			TPILogger.tl.debug("...[logback/sys-info/down]...(error)");
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}
	
	@GetMapping(value = "/dgrv4/logback/sys-info/status") 
	public ResponseEntity<?> sysInfoStatus(HttpServletRequest request) {
		String json = null;
		try {
			json = "sys-info executes as " + CustomEventEvaluator.IS_EXEC;
		}catch (Exception e) {
			TPILogger.tl.debug("...[logback/sys-info/status]...(error)");
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}
	
	@GetMapping(value = "/dgrv4/logback/uuid/up") 
	public ResponseEntity<?> uuidUp(HttpServletRequest request) {
		String json = "[logback/uuid/up]...OK";
		try {
			CustomUuidEventEvaluator.IS_EXEC = true;
		} catch (Exception e) {
			TPILogger.tl.debug("...[logback/uuid/up]...(error)");
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/dgrv4/logback/uuid/down") 
	public ResponseEntity<?> uuidDown(HttpServletRequest request) {
		String json = "[logback/uuid/down]...OK";
		try {
			CustomUuidEventEvaluator.IS_EXEC = false;
		}catch (Exception e) {
			TPILogger.tl.debug("...[logback/uuid/down]...(error)");
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}
	
	@GetMapping(value = "/dgrv4/logback/uuid/status") 
	public ResponseEntity<?> uuidStatus(HttpServletRequest request) {
		String json = null;
		try {
			json = "uuid executes as " + CustomUuidEventEvaluator.IS_EXEC;
		}catch (Exception e) {
			TPILogger.tl.debug("...[logback/uuid/status]...(error)");
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}
	
}
