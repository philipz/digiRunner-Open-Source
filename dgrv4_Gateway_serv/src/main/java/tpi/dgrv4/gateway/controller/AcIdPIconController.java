package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.AcIdPIconService;

@RestController
public class AcIdPIconController {
	@Autowired
	private AcIdPIconService service;

	@CrossOrigin
	@GetMapping(value = "/dgrv4/ssotoken/acidp/{idPType}/getIcon")
	public ResponseEntity<String> getIcon(@PathVariable(value = "idPType", required = true) String idPType) {

		String icon = service.getIcon(idPType);
		return ResponseEntity.ok(icon);
	}

}
