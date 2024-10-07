package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@SuppressWarnings("serial")
public class SendReviewMailJob extends Job {

	private TPILogger logger = TPILogger.tl;

	@Autowired
	private SendReviewMailService service;
	
	@Autowired
	private PrepareMailService prepareMailService;

	private final TsmpAuthorization auth;

	private final Long reqOrdermId;

	private final String reqType;
	
	private final String sendTime;

	private final String reqOrderNo;
	
	private final String locale;

	public SendReviewMailJob(TsmpAuthorization auth, Long reqOrdermId, 
			String reqType, String sendTime, String reqOrderNo, String locale) {
		this.auth = auth;
		this.reqOrdermId = reqOrdermId;
		this.reqType = reqType;
		this.sendTime = sendTime;
		this.reqOrderNo = reqOrderNo;
		this.locale = locale;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin SendReviewMailJob ---");
			if (this.auth == null) {
				throw new Exception("未傳入授權資訊, 無法寄出簽核通知信");
			}
			if (this.reqOrdermId == null) {
				throw new Exception("未指定申請單主檔序號, 無法寄出簽核通知信");
			}
			if (!StringUtils.hasLength(this.reqType)) {
				throw new Exception("未指定申請單簽核類型, 無法寄出簽核通知信");
			}
			List<TsmpMailEvent> mailEvents = service.getTsmpMailEventList( //
					this.auth, this.reqOrdermId, this.reqType, this.locale);
			if (mailEvents == null || mailEvents.isEmpty()) {
				throw new Exception("取得信件參數失敗, 無法寄出簽核通知信");
			}
			
			/* 20200525; Mini; 寫入 APPT_JOB Table & 建立 Mail 檔案, 改由排程來寄信
			send(mailEvents);
			*/
			
			//準備好資料,以寫入排程
			//identif 可寫入識別資料，ex: userName=mini 或 userName=mini,　reqOrdermId=17002	若有多個資料則以逗號和全型空白分隔
			String identif = "userName=" + auth.getUserName() 
						+ ",　mailType=REVI" 
						+ ",　reqOrderNo=" + reqOrderNo 
						+ ",　reqType=" + reqType;
			prepareMailService.createMailSchedule(mailEvents, identif, 
					TsmpDpMailType.SAME.text(), sendTime);
			
			
		} catch (Exception e) {
			logger.debug("" + e);
		} finally {
			this.logger.debug("--- Finish SendReviewMailJob ---");
		}
	}
}
