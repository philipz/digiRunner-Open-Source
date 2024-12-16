package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.entity.repository.TsmpUserDao;

@RestController

public class HealthCheckingController {

	@GetMapping(path = "/liveness")
	public ResponseEntity<?> liveness() {

		String json = "{liveness：" + System.currentTimeMillis() + "}";

		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}

	@GetMapping(path = "/readiness")
	public ResponseEntity<?> readiness() {

		String json = "{readiness：" + System.currentTimeMillis() + "}";

		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}
}
