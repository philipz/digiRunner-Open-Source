package tpi.dgrv4.dpaa.component.apptJob;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.constant.TsmpDpItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Transactional
public class ApiScheduledEnable extends ApptJob {

	@Autowired
	private TsmpApiDao tsmpApiDao;

	public ApiScheduledEnable(TsmpDpApptJob tsmpDpApptJob) {
		super(tsmpDpApptJob, TPILogger.tl);
	}

	public ApiScheduledEnable(TsmpDpApptJob tsmpDpApptJob, TPILogger logger) {
		super(tsmpDpApptJob, logger);
	}

	@Override
	public String runApptJob() throws Exception {

		step(TsmpDpItem.API_SCH_WAKE_UP.getSubitemNo());

		step(TsmpDpItem.FISHING_SCH_EN_API.getSubitemNo());

		List<TsmpApi> ls = getApis();

		step(TsmpDpItem.CHANGE_EN_STATE.getSubitemNo());

		enableApis(ls);

		return "SUCCESS";
	}

	private void enableApis(List<TsmpApi> ls) {

		if (ls != null) {
			for (TsmpApi tsmpApi : ls) {
				tsmpApi.setApiStatus("1");
				tsmpApi.setEnableScheduledDate(0L);
				tsmpApi.setUpdateUser("SYSTEM");
				tsmpApi.setUpdateTime(DateTimeUtil.now());
			}
			getTsmpApiDao().saveAll(ls);
		}
	}

	private List<TsmpApi> getApis() {

		return getTsmpApiDao()
				.findByEnableScheduledDateLessThanEqualAndEnableScheduledDateNot(System.currentTimeMillis(), 0L);
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

}
