package tpi.dgrv4.dpaa.component.nodeTask;

import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;

@Configuration
//@EnableNodeTaskClient
public class CleanCacheByKeyNotifier extends TsmpNodeTaskNotifier<String> {

	@Override
	public Class<String> eventType() {
		return String.class;
	}

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.Clean_CacheByKey;
	}

}
