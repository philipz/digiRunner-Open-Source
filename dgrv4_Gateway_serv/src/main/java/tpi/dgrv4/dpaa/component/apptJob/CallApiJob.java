package tpi.dgrv4.dpaa.component.apptJob;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.dpaa.component.ApiHelperImpl;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class CallApiJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;;

	@Autowired
	private ApiHelperImpl apiHelper;

	public CallApiJob(TsmpDpApptJob tsmpDpApptJob) {
		super(tsmpDpApptJob, TPILogger.tl);
	}

	@Override
	public String runApptJob() throws Exception {
		// TODO 尚未實作
		// apiHelper.call(reqUrl, params, method);
		throw new Exception("尚未實作");
	}

	@Override
	public TsmpDpApptJob set(TsmpDpApptJob job) {
		// TODO
		return super.set(job);
	}

}
