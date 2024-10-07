package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyExpiringMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@SuppressWarnings("serial")
public class SendOpenApiKeyExpiringMailJob extends Job {

//	private TPILogger logger = TPILogger.tl;

	@Autowired
	private SendOpenApiKeyExpiringMailService sendOpenApiKeyExpiringMailService;
	
	@Autowired
	private PrepareMailService prepareMailService;

	private final TsmpAuthorization auth;
	private final String sendTime;
	private final Long openApiKeyId;

	public SendOpenApiKeyExpiringMailJob(TsmpAuthorization auth, String sendTime, Long openApiKeyId) {
		this.auth = auth;
		this.sendTime = sendTime;
		this.openApiKeyId = openApiKeyId;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			TPILogger.tl.debug("--- Begin SendOpenApiKeyExpiringMailJob ---");
			if (this.auth == null) {
				throw new Exception("未傳入授權資訊, 無法寄出通知信");
			}
			if (this.openApiKeyId == null) {
				throw new Exception("未指定Open Api Key ID, 無法寄出通知信");
			}
			
			//identif 可寫入識別資料，ex: userName=mini 或 userName=mini,　reqOrdermId=17002	若有多個資料則以逗號和全型空白分隔
			String identif = "userName=" + auth.getUserName() 
						+ ",　mailType=OAK_EXPI"
						+ ",　openApiKeyId=" + openApiKeyId;
				
			// 效期快到的mail通知內容
			List<TsmpMailEvent> mailEvents = getSendOpenApiKeyExpiringMailService().getTsmpMailEvents(this.auth, this.openApiKeyId);
			if (mailEvents == null || mailEvents.isEmpty()) {
				throw new Exception("Unable to send E-mail, because failed to get E-mail parameters, may be clientId E-mail address was empty.");
			}
			
			/* 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信 */
			
			//準備好資料,以寫入排程
			getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(), sendTime);
			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} finally {
			TPILogger.tl.debug("--- Finish SendOpenApiKeyExpiringMailJob ---");
		}
	}
	
	protected SendOpenApiKeyExpiringMailService getSendOpenApiKeyExpiringMailService() {
		return sendOpenApiKeyExpiringMailService;
	}

	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}
}
