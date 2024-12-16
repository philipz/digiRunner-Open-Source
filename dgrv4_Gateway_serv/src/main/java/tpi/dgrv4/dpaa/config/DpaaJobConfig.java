package tpi.dgrv4.dpaa.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.HttpType;
import tpi.dgrv4.dpaa.component.apptJob.ApiApplicationJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiOffJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiOnJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiOnUpdateJob;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledDisable;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledEnable;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledLaunch;
import tpi.dgrv4.dpaa.component.apptJob.ApiScheduledRemoval;
import tpi.dgrv4.dpaa.component.apptJob.BroadcastJob;
import tpi.dgrv4.dpaa.component.apptJob.CallApiJob;
import tpi.dgrv4.dpaa.component.apptJob.CallTsmpApiJob;
import tpi.dgrv4.dpaa.component.apptJob.ClientRegJob;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertDetectorJobKeyword;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertDetectorJobSystemBasic;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertJob_Line;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertJob_RoleEmail;
import tpi.dgrv4.dpaa.component.apptJob.HandleReportDataJob;
import tpi.dgrv4.dpaa.component.apptJob.HousekeepingJob;
import tpi.dgrv4.dpaa.component.apptJob.NoticeExpCertJob;
import tpi.dgrv4.dpaa.component.apptJob.OakChkExpiJob;
import tpi.dgrv4.dpaa.component.apptJob.OpenApiKeyApplicaJob;
import tpi.dgrv4.dpaa.component.apptJob.OpenApiKeyRevokeJob;
import tpi.dgrv4.dpaa.component.apptJob.OpenApiKeyUpdateJob;
import tpi.dgrv4.dpaa.component.apptJob.SendMailApptJob;
import tpi.dgrv4.dpaa.component.apptJob.SyncTsmpdpapiToDpClientJob;
import tpi.dgrv4.dpaa.component.apptJob.SystemMonitorJob;
import tpi.dgrv4.dpaa.component.apptJob.TsmpInvokeJob;
import tpi.dgrv4.dpaa.component.job.AA0001Job;
import tpi.dgrv4.dpaa.component.job.AA0004Job;
import tpi.dgrv4.dpaa.component.job.AA0201Job;
import tpi.dgrv4.dpaa.component.job.AA0231Job;
import tpi.dgrv4.dpaa.component.job.ComposerServiceJob;
import tpi.dgrv4.dpaa.component.job.DPB0002Job;
import tpi.dgrv4.dpaa.component.job.DPB0005Job;
import tpi.dgrv4.dpaa.component.job.DPB0006Job;
import tpi.dgrv4.dpaa.component.job.DPB0046Job;
import tpi.dgrv4.dpaa.component.job.DPB0065Job;
import tpi.dgrv4.dpaa.component.job.DPB0067Job;
import tpi.dgrv4.dpaa.component.job.DPB0071MailJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredOpenApiKeyJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredTempFileJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredTempFileTsmpDpFileJob;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.component.job.NoticeDCEventsJob;
import tpi.dgrv4.dpaa.component.job.RefreshAuthCodeJob;
import tpi.dgrv4.dpaa.component.job.SaveEventJob;
import tpi.dgrv4.dpaa.component.job.SendOpenApiKeyExpiringMailJob;
import tpi.dgrv4.dpaa.component.job.SendOpenApiKeyMailJob;
import tpi.dgrv4.dpaa.component.job.SendReviewMailJob;
import tpi.dgrv4.dpaa.component.rjob.ThinkpowerArticleJob;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.dpaa.component.job.SendMailJob;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.gateway.component.job.appt.HttpUtilJob;
import tpi.dgrv4.gateway.config.BeanConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Configuration
@EnableScheduling
public class DpaaJobConfig {

	@Autowired
	private BeanConfig beanConfig;

//	@Bean
//	public JobManager mainJobManager() {
//		return new JobManager();
//	}
//
//	@Bean
//	public DeferrableJobManager deferrableJobManager() {
//		return new DeferrableJobManager();
//	}
//
//	@Bean
//	public DeferrableJobManager refreshCacheJobManager() {
//		return new DeferrableJobManager();
//	}
//
//	@Bean
//	@Scope(value = "prototype")
//	public RefreshCacheJob refreshCacheJob(String cacheName, String key, Supplier<Object> supplier) {
//		return new RefreshCacheJob(cacheName, key, supplier);
//	}

	@Bean
	@Scope("prototype")
	public AA0001Job aa0001Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0001Job(auth, mailEvents, sendTime);
	}

	@Bean
	@Scope("prototype")
	public AA0004Job aa0004Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0004Job(auth, mailEvents, sendTime);
	}

	@Bean
	@Scope("prototype")
	public AA0201Job aa0201Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0201Job(auth, mailEvents, sendTime);
	}

	@Bean
	@Scope("prototype")
	public AA0231Job aa0231Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new AA0231Job(auth, mailEvents, sendTime);
	}

	@Bean
	@Scope("prototype")
	public DPB0006Job dpb0006Job() {
		return new DPB0006Job();
	}

	@Bean
	@Scope("prototype")
	public DPB0002Job dpb0002Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new DPB0002Job(auth, mailEvents, sendTime);
	}

	@Bean
	@Scope("prototype")
	public DPB0005Job dpb0005Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return new DPB0005Job(auth, mailEvents, sendTime);
	}

	@Bean
	@Scope("prototype")
	public DPB0046Job dpb0046Job() {
		return new DPB0046Job();
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredTempFileJob deleteExpiredTempFileJob() {
		return new DeleteExpiredTempFileJob();
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredTempFileTsmpDpFileJob deleteExpiredTempFileTsmpDpFileJob(boolean switchPatternFileName) {
		return new DeleteExpiredTempFileTsmpDpFileJob(switchPatternFileName);
	}

	@Bean
	@Scope("prototype")
	public SendReviewMailJob sendReviewMailJob(TsmpAuthorization auth, Long reqOrdermId, String reqType,
			String sendTime, //
			String reqOrderNo, String locale) {
		return new SendReviewMailJob(auth, reqOrdermId, reqType, sendTime, reqOrderNo, locale);
	}

	@Bean
	@Scope("prototype")
	public SendOpenApiKeyMailJob sendOpenApiKeyMailJob(TsmpAuthorization auth, String sendTime, Long openApiKeyId,
			String openApiKeyType, String reqOrderNo) {
		return new SendOpenApiKeyMailJob(auth, sendTime, openApiKeyId, openApiKeyType, reqOrderNo);
	}

	@Bean
	@Scope("prototype")
	public SendOpenApiKeyExpiringMailJob sendOpenApiKeyExpiringMailJob(TsmpAuthorization auth, String sendTime,
			Long openApiKeyId) {
		return new SendOpenApiKeyExpiringMailJob(auth, sendTime, openApiKeyId);
	}

	@Bean
	@Scope("prototype")
	public DPB0065Job dpb0065Job(String locale) {
		return new DPB0065Job(locale);
	}

	@Bean
	@Scope("prototype")
	public SaveEventJob saveEventJob(TsmpEvents tsmpEvents) {
		return new SaveEventJob(tsmpEvents);
	}

	@Bean
	@Scope("prototype")
	public NoticeClearCacheEventsJob noticeClearCacheEventsJob(Integer action, String cacheName,
			List<String> tableNameList) {
		return new NoticeClearCacheEventsJob(action, cacheName, tableNameList);
	}

	@Bean
	@Scope("prototype")
	public NoticeDCEventsJob noticeDCEventsJob(Integer action, Long dcId) {
		return new NoticeDCEventsJob(action, dcId);
	}

	@Bean
	@Scope("prototype")
	public RefreshAuthCodeJob refreshAuthCodeJob(Long expDay) {
		return new RefreshAuthCodeJob(expDay);
	}

	@Bean
	@Scope("prototype")
	public ComposerServiceJob composerServiceJob(Integer act, List<String> apiUUIDs) {
		return new ComposerServiceJob(act, apiUUIDs);
	}

	/** 排程 Job */
	@Bean
	@Scope("prototype")
	public ApiOnJob apptJob_API_ON_OFF_API_ON(TsmpDpApptJob job) {
		return new ApiOnJob(job);
	}

	@Bean
	@Scope("prototype")
	public ApiOffJob apptJob_API_ON_OFF_API_OFF(TsmpDpApptJob job) {
		return new ApiOffJob(job);
	}

	@Bean
	@Scope("prototype")
	public ApiOnUpdateJob apptJob_API_ON_OFF_API_ON_UPDATE(TsmpDpApptJob job) {
		return new ApiOnUpdateJob(job);
	}

	@Bean
	@Scope("prototype")
	public CallTsmpApiJob apptJob_A_SCHEDULE_CALL_API1(TsmpDpApptJob job) {
		return new CallTsmpApiJob(job);
	}

	@Bean
	@Scope("prototype")
	public CallApiJob apptJob_A_SCHEDULE_CALL_API2(TsmpDpApptJob job) {
		return new CallApiJob(job);
	}

	@Bean
	@Scope("prototype")
	public SendMailApptJob apptJob_SEND_MAIL(TsmpDpApptJob job) {
		return new SendMailApptJob(job);
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredMailJob deleteExpiredMailJob() {
		return new DeleteExpiredMailJob();
	}

	@Bean
	@Scope("prototype")
	public DeleteExpiredOpenApiKeyJob deleteExpiredOpenApiKeyJob() {
		return new DeleteExpiredOpenApiKeyJob();
	}

	@Bean
	@Scope("prototype")
	public ApiApplicationJob apptJob_API_APPLICATION(TsmpDpApptJob job) {
		return new ApiApplicationJob(job);
	}

	@Bean
	@Scope("prototype")
	public ClientRegJob apptJob_CLIENT_REG(TsmpDpApptJob job) {
		return new ClientRegJob(job);
	}

	@Bean
	@Scope("prototype")
	public OpenApiKeyApplicaJob apptJob_OPEN_API_KEY_OPEN_API_KEY_APPLICA(TsmpDpApptJob job) {
		return new OpenApiKeyApplicaJob(job);
	}

	@Bean
	@Scope("prototype")
	public OpenApiKeyUpdateJob apptJob_OPEN_API_KEY_OPEN_API_KEY_UPDATE(TsmpDpApptJob job) {
		return new OpenApiKeyUpdateJob(job);
	}

	@Bean
	@Scope("prototype")
	public OpenApiKeyRevokeJob apptJob_OPEN_API_KEY_OPEN_API_KEY_REVOKE(TsmpDpApptJob job) {
		return new OpenApiKeyRevokeJob(job);
	}

	@Bean
	@Scope("prototype")
	public OakChkExpiJob apptJob_OAK_CHK_EXPI(TsmpDpApptJob job) {
		return new OakChkExpiJob(job);
	}

	@Bean
	@Scope("prototype")
	public SyncTsmpdpapiToDpClientJob apptJob_SYNC_DATA1(TsmpDpApptJob job) {
		return new SyncTsmpdpapiToDpClientJob(job);
	}

	@Bean
	@Scope("prototype")
	public HandleReportDataJob apptJob_REPORT_BATCH(TsmpDpApptJob job) {
		return new HandleReportDataJob(job);
	}

	@Bean
	@Scope("prototype")
	public HousekeepingJob apptJob_HOUSEKEEPING_BATCH(TsmpDpApptJob job) {
		return new HousekeepingJob(job);
	}
	
	@Bean
	@Scope("prototype")
	public HttpUtilJob apptJob_HTTP_UTIL_CALL_C_APIKEY(TsmpDpApptJob job) {
		return new HttpUtilJob(job, HttpType.C_APIKEY);
	}
	
	@Bean
	@Scope("prototype")
	public HttpUtilJob apptJob_HTTP_UTIL_CALL_NO_AUTH(TsmpDpApptJob job) {
		return new HttpUtilJob(job, HttpType.NO_AUTH);
	}
	
	@Bean
	@Scope("prototype")
	public HttpUtilJob apptJob_HTTP_UTIL_CALL_BASIC(TsmpDpApptJob job) {
		return new HttpUtilJob(job, HttpType.BASIC);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledRemoval apptJob_API_SCHEDULED_API_REMOVAL(TsmpDpApptJob job) {
		return new ApiScheduledRemoval(job);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledLaunch apptJob_API_SCHEDULED_API_LAUNCH(TsmpDpApptJob job) {
		return new ApiScheduledLaunch(job);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledEnable apptJob_API_SCHEDULED_API_ENABLE(TsmpDpApptJob job) {
		return new ApiScheduledEnable(job);
	}

	@Bean
	@Scope("prototype")
	public ApiScheduledDisable apptJob_API_SCHEDULED_API_DISABLE(TsmpDpApptJob job) {
		return new ApiScheduledDisable(job);
	}

	@Bean
	@Scope("prototype")
	public NoticeExpCertJob apptJob_NOTICE_EXP_CERT_JWE(TsmpDpApptJob job) {
		return new NoticeExpCertJob(job);
	}

	@Bean
	@Scope("prototype")
	public NoticeExpCertJob apptJob_NOTICE_EXP_CERT_TLS(TsmpDpApptJob job) {
		return new NoticeExpCertJob(job);
	}

	@Bean
	@Scope("prototype")
	public SystemMonitorJob apptJob_RUNLOOP_SYS_MONITOR(TsmpDpApptJob job) {
		return new SystemMonitorJob(job);
	}

	@Bean
	@Scope("prototype")
	public BroadcastJob apptJob_BROADCAST_RESTART_DGR_MODULE(TsmpDpApptJob job) {
		return new BroadcastJob(job);
	}

	/*
	 * v4 不須此排程，但因 BcryptParams 專利關係，TSMP_DP_ITEMS 的項目不可刪除，否則會造成 Index 亂掉
	 * 
	 * @Bean
	 * 
	 * @Scope("prototype") public RestartDgrModuleJob
	 * apptJob_RESTART_DGR_MODULE(TsmpDpApptJob job) { return new
	 * RestartDgrModuleJob(job); }
	 */

	/**
	 * 此為開發範例，非正式簽核單排程
	 * 
	 * @param job
	 * @return
	 */
	@Bean
	@Scope("prototype")
	public ThinkpowerArticleJob apptJob_THINKPOWER_ARTICLE(TsmpDpApptJob job) {
		return new ThinkpowerArticleJob(job);
	}

	@Bean
	@Scope("prototype")
	public TsmpInvokeJob apptJob_TSMP_INVOKE(TsmpDpApptJob job) {
		return new TsmpInvokeJob(job);
	}

	@Bean
	@Scope("prototype")
	public DPB0067Job dpb0067Job(String locale) {
		return new DPB0067Job(locale);
	}

	@Bean
	@Scope("prototype")
	public DPB0071MailJob dpb0071MailJob(TsmpAuthorization auth, List<TsmpMailEvent> tsmpMailEvent, String sendTime,
			Long reqOrdermId) {
		return new DPB0071MailJob(auth, tsmpMailEvent, sendTime, reqOrdermId);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertJob_RoleEmail apptJob_DPAA_ALERT_ROLE_EMAIL(TsmpDpApptJob job) {
		return new DpaaAlertJob_RoleEmail(job);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertJob_Line apptJob_DPAA_ALERT_LINE(TsmpDpApptJob job) {
		return new DpaaAlertJob_Line(job);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertDetectorJobKeyword apptJob_RUNLOOP_ALERT_KEYWORD(TsmpDpApptJob job) throws Exception {
		ObjectMapper objectMapper = beanConfig.objectMapper();
		return new DpaaAlertDetectorJobKeyword(job, objectMapper);
	}

	@Bean
	@Scope("prototype")
	public DpaaAlertDetectorJobSystemBasic apptJob_RUNLOOP_ALERT_SYSTEM_BASIC(TsmpDpApptJob job) throws Exception {
		ObjectMapper objectMapper = beanConfig.objectMapper();
		return new DpaaAlertDetectorJobSystemBasic(job, objectMapper);
	}

	@Bean
	@Scope("prototype")
	public SendMailJob SendMailJob(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime,String identif) {
		return new SendMailJob(auth, mailEvents, sendTime ,identif);
	}

}
