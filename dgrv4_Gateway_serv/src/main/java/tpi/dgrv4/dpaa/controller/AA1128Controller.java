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
import tpi.dgrv4.dpaa.service.AA1128Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA1116Resp;
import tpi.dgrv4.dpaa.vo.AA1128Req;
import tpi.dgrv4.dpaa.vo.AA1128Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * @author Tom
 */
@RestController
public class AA1128Controller {
	
	@Autowired
	private AA1128Service service;
	
	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/AA1128", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA1128Resp> importClientRelatedAllCover(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA1128Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA1128Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.importClientRelatedAllCover(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
