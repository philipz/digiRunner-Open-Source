package tpi.dgrv4.dpaa.component.nodeTask;

import java.util.List;

import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;

@Configuration
//@EnableNodeTaskClient
public class CleanCacheByTableNameNotifier extends TsmpNodeTaskNotifier<List> {

	@Override
	public Class<List> eventType() {
		return java.util.List.class;
	}

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.Clean_CacheByTableName;
	}

}
