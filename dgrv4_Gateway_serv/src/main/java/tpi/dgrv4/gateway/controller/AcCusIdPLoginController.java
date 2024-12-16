package tpi.dgrv4.gateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.gateway.service.AcCusIdPLoginService;

@RestController
public class AcCusIdPLoginController {

	@Autowired
	private AcCusIdPLoginService acCusIdPLoginService;

	@GetMapping(value = { "/dgrv4/ssotoken/acCusIdp/login/{idpId}", "/dgrv4/ssotoken/acCusIdp/login" })
	public void preProcessCusAcLogin(HttpServletRequest httpReq, HttpServletResponse httpResp, //
			@RequestHeader HttpHeaders httpHeaders, @RequestParam Map<String, String> queryParams,
			@PathVariable(name = "idpId", required = false) String idpId) {

		acCusIdPLoginService.preProcessCusAcLogin(httpHeaders, httpReq, httpResp, idpId, queryParams);
	}
}
