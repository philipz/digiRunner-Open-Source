package tpi.dgrv4.dpaa.component.apptJob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.*;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CancellationException;

/**
 * 處理報表資料JOB
 * 1.ES_LOG_DISABLE =false,不管TSMP_APILOG_FORCE_WRITE_RDB的值(若為true會有紀錄但不做統計,直到執行RDB統計才做),就執行ES
 * 2.ES_LOG_DISABLE=true,而TSMP_APILOG_FORCE_WRITE_RDB=true就執行RDB
 * 3.dashboard熱冷門的計算就沒分ES/RDB, 因為來源是一樣的
 * 
 * @author tom
 *
 */
@SuppressWarnings("serial")
public class HandleReportDataJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	@Autowired
	private HandleReportDataByMinuteService handleReportDataByMinuteService;
	@Autowired
	private HandleReportDataByHourService handleReportDataByHourService;
	@Autowired
	private HandleReportDataByDayService handleReportDataByDayService;
	@Autowired
	private HandleReportDataByMonthService handleReportDataByMonthService;
	@Autowired
	private HandleReportDataByYearService handleReportDataByYearService;
	@Autowired
	private HandleDashboardLogDataService handleDashboardLogDataService;
	@Autowired
	private HandleDashboardDataService handleDashboardDataService;
	@Autowired
	private HandleDashboardDataByYearService handleDashboardDataByYearService;
	@Autowired
	private TsmpSettingService tsmpSettingService;
	@Autowired
	private HandleESApiDataService handleESApiDataService;
	@Autowired
	private TsmpDpApptJobDao tsmpDpApptJobDao;

	@Autowired
	private HandleESExpiredDataService handleESExpiredDataService;
	
	public HandleReportDataJob(TsmpDpApptJob tsmpDpApptJob) {
		super(tsmpDpApptJob, TPILogger.tl);
	}
	
//	 1.ES_LOG_DISABLE =false,不管TSMP_APILOG_FORCE_WRITE_RDB的值(若為true會有紀錄但不做統計,直到執行RDB統計才做),就執行ES
//	 2.ES_LOG_DISABLE=true,而TSMP_APILOG_FORCE_WRITE_RDB=true就執行RDB
//	 3.dashboard熱冷門的計算就沒分ES/RDB, 因為來源是一樣的 
	
	@Override
	public String runApptJob() throws Exception {
		Long id = this.getTsmpDpApptJob().getApptJobId();

		boolean hasReportRunning = getTsmpDpApptJobDao().existsByRefItemNoAndStatusAndApptJobIdNot("REPORT_BATCH", "R",
				id);
		if (hasReportRunning) {
			this.logger.info("REPORT_BATCH job status running id:" + id);
			throw new CancellationException();
		} else {
			boolean isEs = false;
			String rdbVal = "false";
			try {
				rdbVal = getTsmpSettingService().getVal_TSMP_APILOG_FORCE_WRITE_RDB();
				isEs = !getTsmpSettingService().getVal_ES_LOG_DISABLE();
				if("false".equalsIgnoreCase(rdbVal) && !isEs) {
					throw new CancellationException();
				}

				String strParam = this.getTsmpDpApptJob().getInParams();
				if(isEs) {
					this.logger.debug("--- Begin ES HandleReportDataJob ---");
					
					Date execDate = getExecuteDate();
					this.logger.debug(
							"execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));

					if(StringUtils.hasText(strParam)) {
						execDate = DateTimeUtil.stringToDateTime(strParam, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
						this.logger.debug(
								"inParams execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
						getHandleESExpiredDataService().exec(execDate, id);
						step("2/2P");
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}else {
						String stepMax = "/3";
						step(1 + stepMax);
						getHandleESExpiredDataService().exec(execDate, id);
						
						step(2 + stepMax);
						getHandleESApiDataService().exec(execDate, id);
						
						step(3 + stepMax);
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}
	
					

				}else {
					this.logger.debug("--- Begin RDB HandleReportDataJob ---");
	
					Date execDate = getExecuteDate();
					this.logger.debug(
							"execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
					String createUser = getTsmpDpApptJob().getCreateUser();
	
					String stepMax = "/6";
	
					if(StringUtils.hasText(strParam)) {
						execDate = DateTimeUtil.stringToDateTime(strParam, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
						this.logger.debug(
								"inParams execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
						step("6/6P");
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}else {
						step(1 + stepMax);
						getHandleReportDataByMinuteService().exec(execDate, createUser, id);
		
						step(2 + stepMax);
						getHandleReportDataByHourService().exec(execDate, createUser, id);
		
						step(3 + stepMax);
						getHandleReportDataByDayService().exec(execDate, createUser, id);
		
						step(4 + stepMax);
						getHandleReportDataByMonthService().exec(execDate, createUser, id);
		
						step(5 + stepMax);
						getHandleReportDataByYearService().exec(execDate, createUser, id);
		
						step(6 + stepMax);
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}
					
				}

			} catch(CancellationException e) {
				throw e;
			} catch(ObjectOptimisticLockingFailureException e) {
				this.logger.warn(StackTraceUtil.logTpiShortStackTrace(e));
				throw e;
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw e;
			} finally {
				if(isEs) {
					this.logger.debug("--- Finish ES HandleReportDataJob ---");
				}else if(!"false".equalsIgnoreCase(rdbVal)){
					this.logger.debug("--- Finish RDB HandleReportDataJob ---");
				}
				
			}
			return "SUCCESS";
		}

	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected HandleDashboardLogDataService getHandleDashboardLogDataService() {
		return handleDashboardLogDataService;
	}

	protected Date getExecuteDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	protected HandleReportDataByMinuteService getHandleReportDataByMinuteService() {
		return handleReportDataByMinuteService;
	}

	protected HandleReportDataByHourService getHandleReportDataByHourService() {
		return handleReportDataByHourService;
	}

	protected HandleReportDataByDayService getHandleReportDataByDayService() {
		return handleReportDataByDayService;
	}

	protected HandleReportDataByMonthService getHandleReportDataByMonthService() {
		return handleReportDataByMonthService;
	}

	protected HandleReportDataByYearService getHandleReportDataByYearService() {
		return handleReportDataByYearService;
	}

	protected HandleDashboardDataService getHandleDashboardDataService() {
		return handleDashboardDataService;
	}

	protected HandleDashboardDataByYearService getHandleDashboardDataByYearService() {
		return handleDashboardDataByYearService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected HandleESApiDataService getHandleESApiDataService() {
		return handleESApiDataService;
	}

	protected HandleESExpiredDataService getHandleESExpiredDataService() {
		return handleESExpiredDataService;
	}

	
}
