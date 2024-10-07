package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.DPB0119Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0119Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/***
 * 取得客製包URL
 * 
 * @author min
 *
 */
@RestController
public class DPB0119Controller {

	@Autowired
	private DPB0119Service service;

	/**
	 * 
	 * @param DPB0119Resp
	 * @return
	 */
	@CrossOrigin
	@GetMapping(value = "/dgrv4/11/DPB0119", params = { "1" }, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0119Resp> queryCusUrl(HttpServletRequest httpReq, @RequestHeader HttpHeaders headers) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0119Resp resp = null;
		
		try {
			//ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryCusUrl(httpReq, tsmpHttpHeader.getAuthorization());
		} catch (Exception e) {	
			
			// 因為Querystring沒有locale資訊，所以前端將locale資訊放入HttpHeaders內
			String locale = LocaleType.EN_US;
			if (headers.get("locale")!=null && headers.get("locale").isEmpty() == false) {
				locale = headers.get("locale").get(0);
			}
			ReqHeader reqHeader = new ReqHeader();
			reqHeader.setLocale(locale);
			throw new TsmpDpAaException(e, reqHeader);
		}

		return ControllerUtil.tsmpResponseBaseObj(headers, resp);

	}
}
