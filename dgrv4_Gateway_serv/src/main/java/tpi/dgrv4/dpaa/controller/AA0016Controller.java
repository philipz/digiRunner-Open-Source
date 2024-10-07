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
import tpi.dgrv4.dpaa.service.AA0016Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0016Req;
import tpi.dgrv4.dpaa.vo.AA0016Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * 建立登入角色的設定角色清單
 * 
 * @author tom
 *
 */
@RestController
public class AA0016Controller {
		
	@Autowired
	private AA0016Service service;
		
	
	/**
	 * 
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */
	@CrossOrigin
	@PostMapping(value = "/dgrv4/11/AA0016", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0016Resp> addTRoleRoleMap(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0016Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0016Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.addTRoleRoleMap(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
