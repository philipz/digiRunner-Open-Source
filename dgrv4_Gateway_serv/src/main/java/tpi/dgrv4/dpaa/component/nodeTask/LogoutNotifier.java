package tpi.dgrv4.dpaa.component.nodeTask;

import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;

@Configuration
//@EnableNodeTaskClient
public class LogoutNotifier extends TsmpNodeTaskNotifier<String> {

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.LOGOUT;
	}

	@Override
	public Class<String> eventType() {
		return String.class;
	}

}
