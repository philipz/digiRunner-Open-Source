package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.AA0701Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0701Req;
import tpi.dgrv4.dpaa.vo.AA0701Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * 新增告警設定
 * 
 * @author tom
 *
 */
@RestController
public class AA0701Controller {
		
	@Autowired
	private AA0701Service service;
		
	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/AA0701", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> addAlarmSettings_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new AA0701Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}
	
	/**
	 * 
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */
	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/AA0701", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0701Resp> addAlarmSettings(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0701Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0701Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.addAlarmSettings(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
