package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9903Req;
import tpi.dgrv4.dpaa.vo.DPB9903Resp;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.TCP.Packet.UpdateComposerTSPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ComposerWebSocketClientConn;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;


@Service
public class DPB9903Service {
    public enum EncrptionType{
        ENC,TAEASK,NONE
    }
	private TPILogger logger = TPILogger.tl;

	@Autowired
	private TsmpSettingDao tsmpSettingDao;
	
	@Autowired
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired(required = false)
	private TsmpCoreTokenBase tsmpCoreTokenBase;
    
    @Autowired
    private TsmpTAEASKHelper tsmpTAEASKHelper;

    @Autowired
    private DaoGenericCacheService daoGenericCacheService;
    
    @Autowired
    private ComposerWebSocketClientConn composerWebSocketClientConn;
    
	public DPB9903Resp updateTsmpSetting(TsmpAuthorization auth, DPB9903Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_TSMP_SETTING.value());
		
		checkParams(req);
		
		try {
			TsmpSetting oldSetting = findOldSetting(req);
			if (oldSetting == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			TsmpSetting newSetting = updateOldSetting(oldSetting, req, iip);

			/**
			 * 
			 * Composer專用邏輯，若更新的ID為COMPOSER_LOG_SWICTH、COMPOSER_LOG_SIZE、COMPOSER_LOG_INTERVAL、COMPOSER_LOG_MAX_FILES。
			 * 需要更新AA0325Controller.ts。
			 * 
			 */
			boolean isComposerFlag = false;
			String id = newSetting.getId().toUpperCase();
			isComposerFlag = "COMPOSER_LOG_SWICTH".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_LOG_SIZE".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_LOG_INTERVAL".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_LOG_MAX_FILES".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_REQUEST_TIMEOUT".equals(id);
			if (isComposerFlag) {
				TPILogger.lc.send(new UpdateComposerTSPacket());
			}

			refreshCache(newSetting);
			
			//因為composer address被更新,websocket要重連
			if("TSMP_COMPOSER_ADDRESS".equals(req.getId())) {
				restartWs();
			}
			
			return new DPB9903Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			throw TsmpDpAaRtnCode._1286.throwing();
		}
	}
	
	protected void restartWs() {
		getComposerWebSocketClientConn().restart();
	}

	protected void checkParams(DPB9903Req req) {
		String id = req.getId();
		
		if (!StringUtils.hasLength(id)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected TsmpSetting findOldSetting(DPB9903Req req) {
		String id = req.getId();
		String oldVal = req.getOldVal();
		TsmpSetting oldSetting = getTsmpSettingDao().findByIdAndValue(id, oldVal);
		return oldSetting;
	}

	protected TsmpSetting updateOldSetting(TsmpSetting setting, DPB9903Req req, InnerInvokeParam iip) throws Exception {
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, setting); //舊資料統一轉成 String
		
		String newVal = req.getNewVal();
		String memo = req.getMemo();
		if (req.getEncrptionType().equals(EncrptionType.ENC.toString())) {
            String encEncode=getENCEncode(newVal);
            setting.setValue(encEncode);
        }
        else if (req.getEncrptionType().equals(EncrptionType.TAEASK.toString())) {
           String TAEASKEncode=getTAEASKEncode(newVal);
            setting.setValue(TAEASKEncode);
        }else {
            setting.setValue(newVal);
        }
		
		
//		setting.setValue(newVal);
		setting.setMemo(memo);
		setting = getTsmpSettingDao().saveAndFlush(setting);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpSetting.class.getSimpleName(), TableAct.U.value(), oldRowStr, setting);// U
		
		return setting;
	}

    protected String getENCEncode(String value) throws Exception {
        String encoded = getTsmpCoreTokenBase().encrypt(value);
        encoded = String.format("ENC(%s)", encoded);
        logger.debug("ENC encode  " + encoded);
        return encoded;
    }

    protected String getTAEASKEncode(String value) {
        String encoded = null;
        encoded = getTsmpTAEASKHelper().encrypt(value);
        logger.debug("TAEASK encode   " + encoded);
        return encoded;
    }

    protected void refreshCache(TsmpSetting newSetting) {
    	/*
		String id = newSetting.getId();
		getTsmpSettingCacheProxy().findById(id);
		*/
    	getDaoGenericCacheService().clearAndNotify();
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpCoreTokenBase getTsmpCoreTokenBase() {
		return this.tsmpCoreTokenBase;
	}

    protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
        return this.tsmpTAEASKHelper;
    }

	protected ComposerWebSocketClientConn getComposerWebSocketClientConn() {
		return composerWebSocketClientConn;
	}

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}


}
