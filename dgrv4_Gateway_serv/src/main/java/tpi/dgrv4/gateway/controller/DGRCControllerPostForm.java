package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.DGRCServicePostForm;

@RestController
public class DGRCControllerPostForm {
	
	@Autowired
	private DGRCServicePostForm service;
 
	@RequestMapping(value = "/dgrc/**", 
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, // 使用 Form Data 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Callable dispatch(HttpServletRequest httpReq, 
			HttpServletResponse httpRes, 
			@RequestHeader HttpHeaders headers) {
 
		return () -> {
			ResponseEntity<?> resp = service.forwardToPostFormData(headers, httpReq, httpRes);
			
			// 計算API每秒轉發吞吐量
			GatewayFilter.setApiRespThroughput();

			return resp;
		};
	}
}
