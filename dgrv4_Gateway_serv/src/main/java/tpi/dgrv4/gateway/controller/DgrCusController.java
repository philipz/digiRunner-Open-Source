package tpi.dgrv4.gateway.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParser;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParserFactory;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@RestController
public class DgrCusController {

	public final static String KEY_PATH = "/dgrv4/cus";

	public final static String KEY_OF_TOKEN_COOKIE = "_authorization";

	public final static String KEY_OF_RID_COOKIE = "_embedded_link_reportId";

	@Autowired
	private TsmpReportUrlDao tsmpReportUrlDao;
	
	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	@GetMapping(KEY_PATH + "/{cusAppCode}/noauth/login")
	public void resource(
		@PathVariable("cusAppCode") String cusAppCode,	// 客製包代碼
		@RequestHeader HttpHeaders httpHeaders,		
		HttpServletRequest request,
		HttpServletResponse response
	) throws Throwable {
		
		try {
			String loginURL = getTsmpSettingService().getVal_CUS_LOGIN_URL();

			Map<String, List<String>> headers = httpHeaders.toSingleValueMap().entrySet().stream().map(entry -> {
				return Map.entry(entry.getKey(), List.of(entry.getValue()));
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));			

			HttpRespData respData = HttpUtil.httpReqByGetList(loginURL, headers, true, false);
			respData.fetchByte(); // Because inputStream is enabled

			response.setStatus(respData.statusCode);
			if (!CollectionUtils.isEmpty(respData.respHeader)) {
				respData.respHeader.forEach((k, vs) -> {
					vs.forEach((v) -> {
						if (k != null) {
							if (!k.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
								response.addHeader(k, v);
							}
						}
					});
				});
			}
			
			if (respData.httpRespArray == null || respData.httpRespArray.length == 0) {
				sendError(response, HttpStatus.NOT_FOUND, "No data responded from " + loginURL + "\n" + respData.getLogStr());
				return;
			}

			ByteArrayInputStream bi = new ByteArrayInputStream(respData.httpRespArray);
			IOUtils.copy(bi, response.getOutputStream());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, StackTraceUtil.logStackTrace(e));
		}
	}

	@RequestMapping(KEY_PATH + "/{cusAppCode}/**")
	public void resource(
		@PathVariable("cusAppCode") String cusAppCode,	// 客製包代碼
		@RequestHeader HttpHeaders httpHeaders,
		@CookieValue(value = KEY_OF_TOKEN_COOKIE, required = false) String authorization,
		@CookieValue(value = KEY_OF_RID_COOKIE, required = false) String reportId,	// 功能(報表)代碼
		@RequestBody(required = false) String payload,
		HttpServletRequest request,
		HttpServletResponse response
	) throws Throwable {
		if(!StringUtils.hasLength(authorization)) {
			TPILogger.tl.error(TokenHelper.invalid_token);
			sendError(response, HttpStatus.UNAUTHORIZED, (TokenHelper.invalid_token));
			return;
		}
		if(!StringUtils.hasLength(reportId)) {
			TPILogger.tl.error(TokenHelper.invalid_request + ":reportId is null");
			sendError(response, HttpStatus.BAD_REQUEST, (TokenHelper.invalid_request + ":reportId is null"));
			return;
		}
		

		// Check if token was expired
		boolean isValid = checkAccessTokenExp(authorization, response);
		if (!isValid) {
			return;
		}

		try {
			String targetPath = getTargetPath(request, cusAppCode);
			String targetCompleteUrl = getTargetCompleteUrl(reportId, targetPath);

			TPILogger.tl.debug(String.format("%s → %s", request.getRequestURL(), targetCompleteUrl));

			Map<String, List<String>> headers = httpHeaders.toSingleValueMap().entrySet().stream().map(entry -> {
				return Map.entry(entry.getKey(), List.of(entry.getValue()));
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// cApiKey
			addCApiKey(headers);

			HttpRespData respData;			
			if("GET".equals(request.getMethod())) {
				respData = HttpUtil.httpReqByGetList(targetCompleteUrl, headers, true, false);
			}else {
				respData = HttpUtil.httpReqByRawDataList(targetCompleteUrl, "POST", payload, headers, true, false);
			}

			respData.fetchByte(); // Because inputStream is enabled

			response.setStatus(respData.statusCode);
			if (!CollectionUtils.isEmpty(respData.respHeader)) {
				respData.respHeader.forEach((k, vs) -> {
					vs.forEach((v) -> {
						if (k != null) {
							if (!k.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
								response.addHeader(k, v);
							}
						}
					});
				});
			}
			
			if (respData.httpRespArray == null || respData.httpRespArray.length == 0) {
				sendError(response, HttpStatus.NOT_FOUND, "No data responded from " + targetCompleteUrl + "\n" + respData.getLogStr());
				return;
			}

			ByteArrayInputStream bi = new ByteArrayInputStream(respData.httpRespArray);
			IOUtils.copy(bi, response.getOutputStream());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, StackTraceUtil.logStackTrace(e));
		}
	}

	private boolean checkAccessTokenExp(String authorization, HttpServletResponse response) throws Throwable {
		authorization = new String(Base64.getDecoder().decode(authorization), StandardCharsets.UTF_8);
		try {
			TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory().getParser(authorization);
			TsmpAuthorization auth = authorizationParser.parse();
			Long exp = auth.getExp();

			if (exp == null || exp.equals(0L)) {
				sendError(response, HttpStatus.BAD_REQUEST, "Invalid value of exp of access token");
				return false;
			}

			// access token 過期
			long nowTime = System.currentTimeMillis() / 1000;// 去掉亳秒
			if (exp < nowTime) {
				sendError(response, HttpStatus.UNAUTHORIZED, (TokenHelper.Access_token_expired + exp));
				return false;
			}
			
			return true;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, StackTraceUtil.logStackTrace(e));
		}
		return false;
	}

	private String getTargetPath(HttpServletRequest request, String cusAppCode) {
		String requestUri = request.getRequestURI();
		Pattern p = Pattern.compile("^" + KEY_PATH + "/" + cusAppCode + "(.*)$");
		Matcher m = p.matcher(requestUri);
		if (m.matches()) {
			requestUri = m.group(1);
		}
		String queryStr = request.getQueryString();
		if (StringUtils.hasLength(queryStr)) {
			requestUri += "?" + queryStr;
		}
		return requestUri;
	}

	private String getTargetCompleteUrl(String reportId, String targetPath) {
		String targetBaseUrl = getTsmpReportUrlDao()
			.findByReportId(reportId)
			.stream().map(TsmpReportUrl::getReportUrl).findAny()
			.orElseThrow(DgrRtnCode._1241::throwing);
		
		// 若有指定路徑, 則移除目標位址的最後一段路徑, 剩下的當作 basePath, 再串上 targetPath
		// ex: /website/AC0099/AC0019 → /website/AC0099/static/css/style.css
		if (StringUtils.hasLength(targetPath) && !"/".equals(targetPath)) {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(targetBaseUrl);
	        List<String> pathSegments = new ArrayList<>(builder.build().getPathSegments());
	        if (CollectionUtils.isEmpty(pathSegments)) {
	        	pathSegments.add(targetPath);
	        } else {
	        	pathSegments.set(pathSegments.size() - 1, targetPath);
	        }
	        return builder.replacePath(String.join("/", pathSegments)).build().toString();
		// 如果 targetPath 是空或是只有 "/", 表示是由 AA0506 過來, 要直接進入註冊在 TSMP_REPORT_URL.report_url 的位址
		} else {
			return targetBaseUrl;
		}
	}

	private void addCApiKey(Map<String, List<String>> headers) {
		String uuid = UUID.randomUUID().toString().toUpperCase();
		String cApiKey = CApiKeyUtils.signCKey(uuid);
		headers.put("cuuid", List.of(uuid));
		headers.put("capi-key", List.of(cApiKey));
	}

	private void sendError(HttpServletResponse response, HttpStatus status, String errMsg) throws IOException {
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.setStatus(status.value());
		response.getOutputStream().write(errMsg.getBytes());
	}	

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
