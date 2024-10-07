package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.TSMPCServicePostFormUrlEncoded;

@RestController
public class TSMPCControllerPostFormUrlEncode {
	
	@Autowired
	private TSMPCServicePostFormUrlEncoded service;
 
	@RequestMapping(value = {"/tsmpc/*/*/**", "/tsmpg/*/*/**"}, 
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 Form Data 格式
			produces = MediaType.ALL_VALUE)
	public Callable dispatch(HttpServletRequest httpReq, 
			HttpServletResponse httpRes, 
			@RequestHeader HttpHeaders headers,
			@RequestParam MultiValueMap< String, String > values){
 
		return () -> {
			ResponseEntity<?> resp = service.forwardToPostFormUrlEncoded(headers, httpReq, httpRes, values);
			
			// 計算API每秒轉發吞吐量
			GatewayFilter.setApiRespThroughput();

			return resp;
		};
	}
}
