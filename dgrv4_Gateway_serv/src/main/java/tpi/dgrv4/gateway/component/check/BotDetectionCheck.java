package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.BotDetectionRuleValidator;
import tpi.dgrv4.gateway.component.BotDetectionCache;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import org.springframework.util.StringUtils;

@Component
public class BotDetectionCheck implements ICheck {

    @Autowired
    private TsmpRtnCodeService tsmpRtnCodeService;

    @Autowired
    private BotDetectionRuleValidator botDetectionRuleValidator;
    
    @Autowired
    private BotDetectionCache botDetectionCache;

    public boolean check(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        
        // 快速路徑：檢查是否為忽略的 API
        // Fast path: Check if API should be ignored
        if (BotDetectionRuleValidator.isIgnoreApi(request.getRequestURI())) {
            return false;
        }
        
        // 先檢查開關狀態
        // Check switch status first
        if (!botDetectionRuleValidator.isValidationEnabled()) {
            return false;
        }
        
        // 檢查快取
        if (StringUtils.hasText(userAgent)) {
            Boolean cachedResult = botDetectionCache.get(userAgent);
            if (cachedResult != null) {
                // 如果是使用快取的結果，也要執行一次驗證以取得日誌訊息
                BotDetectionRuleValidator.BotDetectionRuleValidateResult logResult = 
                    getBotDetectionRuleValidator().validate(request);
                if (logResult.isPrintingLog()) {
                    TPILogger.tl.debug(logResult.getMsg());
                }
                return !cachedResult;
            }
        }

        // 執行完整驗證
        BotDetectionRuleValidator.BotDetectionRuleValidateResult result = 
            getBotDetectionRuleValidator().validate(request);
        
        // 只在開關開啟時才快取結果
        if (StringUtils.hasText(userAgent) && botDetectionRuleValidator.isValidationEnabled()) {
            botDetectionCache.put(userAgent, result.isPassed());
        }
        
        // 輸出日誌
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
        return tsmpRtnCode != null 
            ? tsmpRtnCode.getTsmpRtnMsg() + " (User-Agent is not allowed.)"
            : getRtnCode().getDefaultMessage() + " (User-Agent is not allowed.)";
    }

    protected TsmpRtnCodeService getTsmpRtnCodeService() {
        return tsmpRtnCodeService;
    }

    protected BotDetectionRuleValidator getBotDetectionRuleValidator() {
        return botDetectionRuleValidator;
    }
}