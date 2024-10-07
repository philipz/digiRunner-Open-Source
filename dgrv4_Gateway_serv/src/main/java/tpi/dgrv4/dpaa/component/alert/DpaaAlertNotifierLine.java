package tpi.dgrv4.dpaa.component.alert;

import org.springframework.stereotype.Component;

@Component
public class DpaaAlertNotifierLine extends DpaaAlertNotifierAbstract {

	@Override
	protected String getRefItemNo() {
		return "DPAA_ALERT";
	}

	@Override
	protected String getRefSubitemNo() {
		return "LINE";
	}

}