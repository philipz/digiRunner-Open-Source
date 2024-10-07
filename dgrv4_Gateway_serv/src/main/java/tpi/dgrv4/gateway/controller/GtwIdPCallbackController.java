package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPCallbackService;

@RestController
public class GtwIdPCallbackController {
	@Autowired
	GtwIdPCallbackService service;

	@CrossOrigin
	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/gtwIdPCallback")
	public ResponseEntity<?> gtwIdPCallback(@RequestHeader HttpHeaders headers, @PathVariable("idPType") String idPType,
			HttpServletRequest httpReq, HttpServletResponse httpResp) throws IOException {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			ResponseEntity<?> respEntity = service.gtwIdPCallback(headers, httpReq, httpResp, idPType);
			return respEntity;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}

