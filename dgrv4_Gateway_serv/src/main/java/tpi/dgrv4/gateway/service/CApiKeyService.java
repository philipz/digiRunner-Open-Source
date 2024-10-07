package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ComposerInfoData;

@Service
public class CApiKeyService {

	private TPILogger logger = TPILogger.tl;

	public void verifyCApiKey(HttpHeaders headers, boolean logs, boolean isCheckComposerId) {

		List<String> uuidList = headers.get("cuuid");
		// "_"符號會被底層擋掉，修改為"-"符號
		List<String> cApikeyList = headers.get("capi-key");
		if (logs) {
			headers.forEach((key, value) -> {
				logger.debugDelay2sec(String.format("Header '%s' = %s", key, value));
			});
		}

		if (CollectionUtils.isEmpty(uuidList)) {
			this.logger.error("uuid isEmpty");
			throw TsmpDpAaRtnCode._1522.throwing();
		}
		if (CollectionUtils.isEmpty(cApikeyList)) {
			this.logger.error("cApikey isEmpty");
			throw TsmpDpAaRtnCode._1522.throwing();
		}
		String uuid = uuidList.get(0);
		String cApikey = cApikeyList.get(0);
		checkParams(uuid, cApikey);
		String cuuid = uuid.toString().toUpperCase();
		boolean isValidate = false;
		List<String> composerIdList = null;
		if(isCheckComposerId) {
			composerIdList = getLiveComposerId();
			isValidate = CApiKeyUtils.verifyCKey(cuuid, cApikey, composerIdList);
		}else {
			isValidate = CApiKeyUtils.verifyCKey(cuuid, cApikey);
		}
		
		if (!isValidate) {
			this.logger.error("uuid :" + uuid);
			this.logger.error("cuuid :" + cuuid);
			this.logger.error("cApikey :" + cApikey);
			if(isCheckComposerId) {
				this.logger.error("composerIdList :" + composerIdList);
			}
			throw TsmpDpAaRtnCode._1522.throwing();
		}
	}

	private void checkParams(String uuid, String cApikey) {
		if (!StringUtils.hasText(uuid) || !StringUtils.hasText(cApikey)) {
			this.logger.error("uuid :" + uuid);
			this.logger.error("cApikey :" + cApikey);
			throw TsmpDpAaRtnCode._1522.throwing();
		}
	}
	
	protected List<String> getLiveComposerId() {
		if (TPILogger.lc != null) {
			TPILogger.lc.send(new RequireAllClientListPacket());
			
			synchronized (RequireAllClientListPacket.waitKey) {
				try {
					RequireAllClientListPacket.waitKey.wait();
				} catch (InterruptedException e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
					Thread.currentThread().interrupt();
				}
			}
			
			LinkedList<ComposerInfoData> composerList = (LinkedList<ComposerInfoData>)TPILogger.lc.paramObj.get(RequireAllClientListPacket.allComposerListStr);
			List<String> list = new ArrayList<>();
			for(ComposerInfoData composerVo : composerList) {
				String composerId = composerVo.getComposerID();
				byte shab[] = SHA256Util.getSHA256(composerId.getBytes());
				String encodeComposerId = Base64Util.base64URLEncode(shab);
				list.add(encodeComposerId);
			}
			
			return list;
		} else {
			TPILogger.tl.error("<Keeper Server> Lost Connection" );
			return new ArrayList<String>();
		}
	}
}
