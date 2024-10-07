package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.codec.utils.Base64Util;

@RestController
public class AcIdPMockJSMsgController {

	@CrossOrigin
	@RequestMapping(value = "/dgrv4/mockac/idpsso/errMsg")
	public ResponseEntity<?> showMsg(@RequestHeader HttpHeaders headers, @RequestParam String msg,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		msg = new String(Base64Util.base64URLDecode(msg));

		return new ResponseEntity<Object>(msg, HttpStatus.OK);
	}
}
