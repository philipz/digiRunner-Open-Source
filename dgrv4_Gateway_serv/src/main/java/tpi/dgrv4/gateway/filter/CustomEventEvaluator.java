package tpi.dgrv4.gateway.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class CustomEventEvaluator extends EventEvaluatorBase<ILoggingEvent> {
	public static boolean IS_EXEC = true;
	private boolean isExecPrintFirst = true;
	
	@Override
    public boolean evaluate(ILoggingEvent event) {
		if(IS_EXEC) {
			String message = event.getFormattedMessage();
			  // 檢查啟動標誌
		    boolean logStartFlag = message.matches(".*Starting DgrApplication( vv\\d+\\.\\d+\\.\\d+)? using Java.*")
			        || message.contains("Running with Spring Boot")
			        || message.contains("seconds") 
			        || message.contains("profile is active");
		        
		    // 如果是啟動消息，添加到 TPILogger
		    if (logStartFlag) {
		        TPILogger.logStartingMsg.add(message);
		    }
		    
		    return 
		    logStartFlag
		    || message.contains("dgr-keeper server [go LIVE]")
		    || message.contains("This Client Connect to dgr-keeper server")
		    || message.contains("dgRv4 web server info")
		    ;
			
		}else {
			if(isExecPrintFirst) {
				isExecPrintFirst = false;
				TPILogger.tl.info("logback filter not executed ");
			}
			return false;
		}
    }
	
}