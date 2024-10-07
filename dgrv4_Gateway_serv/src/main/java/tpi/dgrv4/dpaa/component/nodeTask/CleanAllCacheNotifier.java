package tpi.dgrv4.dpaa.component.nodeTask;

import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;

@Configuration
//@EnableNodeTaskClient
public class CleanAllCacheNotifier extends TsmpNodeTaskNotifier<Void> {

	@Override
	public Class<Void> eventType() {
		return Void.class;
	}

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.Clean_AllCache;
	}

}
