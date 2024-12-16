package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.BotDetectionRuleValidator;
import tpi.dgrv4.gateway.component.BotDetectionRuleValidator.BotDetectionRuleValidateResult;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;

@Component
public class BotDetectionCheck implements ICheck {

	@Autowired
	private TsmpRtnCodeService tsmpRtnCodeService;

	@Autowired
	private BotDetectionRuleValidator botDetectionRuleValidator;

	public boolean check(HttpServletRequest request) {

		BotDetectionRuleValidateResult result = getBotDetectionRuleValidator().validate(request);
		if (result.isPrintingLog()) {
			TPILogger.tl.debug(result.getMsg());
		}
		return !result.isPassed();
	}

	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._1219;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (tsmpRtnCode != null) {
			return tsmpRtnCode.getTsmpRtnMsg() + " (User-Agent is not allowed.)";
		} else {
			return getRtnCode().getDefaultMessage() + " (User-Agent is not allowed.)";
		}
	}

	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return tsmpRtnCodeService;
	}

	protected BotDetectionRuleValidator getBotDetectionRuleValidator() {
		return botDetectionRuleValidator;
	}

}
