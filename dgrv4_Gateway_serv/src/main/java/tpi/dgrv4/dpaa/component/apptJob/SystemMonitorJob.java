package tpi.dgrv4.dpaa.component.apptJob;

import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class SystemMonitorJob extends ApptJob {

	public SystemMonitorJob(TsmpDpApptJob tsmpDpApptJob) {
		super(tsmpDpApptJob, TPILogger.tl);
	}

	@Override
	public String runApptJob() throws Exception {
		// 尚未實作
		TPILogger.tl.trace("[SystemMonitorJob] Not implemented yet");
		return "END_OF_CALL";
	}

}
