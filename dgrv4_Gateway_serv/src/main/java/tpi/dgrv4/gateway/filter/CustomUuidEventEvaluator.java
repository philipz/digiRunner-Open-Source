package tpi.dgrv4.gateway.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class CustomUuidEventEvaluator extends EventEvaluatorBase<ILoggingEvent> {
	public static boolean IS_EXEC = true;
	private boolean isExecPrintFirst = true;
	@Override
    public boolean evaluate(ILoggingEvent event) {
		if(IS_EXEC) {
	        String message = event.getFormattedMessage();
	        
	        return message.contains("LOGUUID");
		}else {
			if(isExecPrintFirst) {
				isExecPrintFirst = false;
				TPILogger.tl.info("logback filter not executed ");
			}
			return false;
		}
    }
}