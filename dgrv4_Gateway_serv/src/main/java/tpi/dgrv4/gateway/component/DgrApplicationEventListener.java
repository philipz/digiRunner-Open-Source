package tpi.dgrv4.gateway.component;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.filter.CustomEventEvaluator;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrApplicationEventListener {
	@EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
		new Thread() {
			public void run() {
				try {
					//因為tpi.dgrv4.dpaa.component.TsmpCoreTokenInitializerInit有用thread印出的LOG是sys_info所需要的LOG,所以等待1分
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					
				}
				//將它關閉
				CustomEventEvaluator.IS_EXEC = false;
				TPILogger.tl.info("auto logback sys-info executes as " + CustomEventEvaluator.IS_EXEC);
			}
		}.start();
    }

}
