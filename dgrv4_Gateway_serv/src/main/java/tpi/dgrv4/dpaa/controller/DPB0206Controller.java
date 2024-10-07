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
import tpi.dgrv4.dpaa.service.DPB0206Service;
import tpi.dgrv4.dpaa.vo.DPB0206Req;
import tpi.dgrv4.dpaa.vo.DPB0206Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0206Controller {
	@Autowired
	private DPB0206Service service;

	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/DPB0206", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0206Resp> deleteGtwIdPInfo_jdbc(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0206Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0206Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.deleteGtwIdPInfo_jdbc(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
