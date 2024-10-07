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
import tpi.dgrv4.dpaa.service.DPB0172Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0172Req;
import tpi.dgrv4.dpaa.vo.DPB0172Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 更新GTW IdP資訊 (GOOGLE / MS)
 * 
 * @author Mini
 */
@RestController
public class DPB0172Controller {
	@Autowired
	private DPB0172Service service;

	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/DPB0172", params = { "before" }, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> updateGtwIdPInfo_oauth2_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new DPB0172Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/DPB0172", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0172Resp> updateGtwIdPInfo_oauth2(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0172Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0172Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.updateGtwIdPInfo_oauth2(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
