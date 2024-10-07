package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.WebsiteService;

@RestController
public class WebsiteController {

	@Autowired
	private WebsiteService service;

	@RequestMapping(value = "/website/{websiteName}/**")
	public void resource(@PathVariable("websiteName") String websiteName, @RequestHeader HttpHeaders httpHeaders,
			HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) String payload) throws Throwable {

		service.resource(httpHeaders, request, response, websiteName, payload);

	}
	
	@RequestMapping(value = "/http-api/**")
	public void httpApiComposer(@RequestHeader HttpHeaders httpHeaders,
			HttpServletRequest request, HttpServletResponse response) throws Throwable {

		service.resource(httpHeaders, request, response, "httpApiComposer", null);

	}

}
