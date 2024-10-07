package tpi.dgrv4.dpaa.component.nodeTask;

import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;

@Configuration
//@EnableNodeTaskClient
public class EnableDCNotifier extends TsmpNodeTaskNotifier<String> {

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.DEPLOY_CONTAINER_ENABLE;
	}

	@Override
	public Class<String> eventType() {
		return String.class;
	}

}
