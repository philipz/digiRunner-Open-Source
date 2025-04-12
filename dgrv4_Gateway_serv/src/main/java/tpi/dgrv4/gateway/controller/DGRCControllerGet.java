package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.DGRCServiceGet;

@RestController
public class DGRCControllerGet {
	
	@Autowired
	private DGRCServiceGet service;
	
	@GetMapping(value = "/dgrc/**")
	public DeferredResult<ResponseEntity<?>> dispatch(@RequestHeader HttpHeaders httpHeaders,
														 HttpServletRequest httpReq,
														 HttpServletResponse httpRes) throws Exception {
		
		DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(60000L); // 設置超時時間，例如 60 秒
		
		String selectWorkThread = (String) httpReq.getAttribute(GatewayFilter.SETWORK_THREAD);
		
		CompletableFuture<ResponseEntity<?>> future;
		if (GatewayFilter.FAST.equals(selectWorkThread)) {
			future = service.forwardToGetAsyncFast(httpHeaders, httpReq, httpRes);
		} else { // "slow" or null (default to slow)
			future = service.forwardToGetAsyncSlow(httpHeaders, httpReq, httpRes);
		}

		future.whenComplete((result, exception) -> {
			if (exception != null) {
				// 處理異常情況，例如返回錯誤響應
				deferredResult.setErrorResult(exception);
			} else {
				// 將異步執行的結果設置給 DeferredResult
				deferredResult.setResult(result);
			}
			// 計算 API 吞吐量，確保在請求完成後執行
			GatewayFilter.setApiRespThroughput();
		});

		// 返回 DeferredResult，Spring MVC 會處理異步響應
		return deferredResult;
	}
}
