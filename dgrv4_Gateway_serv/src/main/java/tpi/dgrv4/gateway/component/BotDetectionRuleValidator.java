package tpi.dgrv4.gateway.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.entity.entity.DgrBotDetection;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao.BotDetectionType;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class BotDetectionRuleValidator implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private DgrBotDetectionDao dgrBotDetectionDao;

	@Autowired
	private TsmpSettingService tsmpSettingService;

	private AtomicReferenceArray<Boolean> isPrintingLog = new AtomicReferenceArray<>(
			new Boolean[] { false, false, false });

	private AtomicReference<ConfigHolder> configHolder = new AtomicReference<>(
			new ConfigHolder(new ArrayList<>(), false));

	private final ExecutorService executorService;
	private final ExecutorService reloadExecutor;
	private final AtomicReference<ReloadConfig> pendingConfig;

	public BotDetectionRuleValidator() {

		int corePoolSize = Runtime.getRuntime().availableProcessors();
		int maximumPoolSize = corePoolSize * 2;
		long keepAliveTime = 60L;
		TimeUnit unit = TimeUnit.SECONDS;
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1000);
		CallerRunsPolicy policy = new ThreadPoolExecutor.CallerRunsPolicy();

		this.executorService = new ThreadPoolExecutor(//
				corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, policy);
		this.reloadExecutor = Executors.newSingleThreadExecutor(r -> {
		    Thread thread = new Thread(r);
		    thread.setName("bot-detection-reload-Thread");
		    return thread;
		});
		this.pendingConfig = new AtomicReference<>(null);
	}

	public BotDetectionRuleValidateResult validate(HttpServletRequest request) {

		ConfigHolder currentConfig = configHolder.get();

		if (request == null) {
			return new BotDetectionRuleValidateResult(true, "Input request is null.", isPrintingLog(2));
		}

		String userAgent = Objects.toString(request.getHeader("User-Agent"), "");

		Optional<BotDetectionRuleValidateResult> opt = verificationEnable(request, userAgent, currentConfig);
		if (opt.isPresent()) {
			return opt.get();
		}

		List<CompletableFuture<BotDetectionRuleValidateResult>> futures = new ArrayList<>();

		for (Pattern pattern : currentConfig.patterns) {
			CompletableFuture<BotDetectionRuleValidateResult> future = CompletableFuture.supplyAsync(() -> {
				if (pattern.matcher(userAgent).matches()) {
					String successMsg = successMessageGenerator(pattern, request);
					return new BotDetectionRuleValidateResult(true, successMsg, isPrintingLog(1));
				}
				return null;
			}, executorService);
			futures.add(future);
		}

		try {
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
			allOf.join();

			for (CompletableFuture<BotDetectionRuleValidateResult> future : futures) {
				BotDetectionRuleValidateResult result = future.join();
				if (result != null) {
					return result;
				}
			}

			return new BotDetectionRuleValidateResult(false, errorMessageGenerator(currentConfig.patterns, request),
					isPrintingLog(0));

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
			return new BotDetectionRuleValidateResult(false,
					"An error occurred during the bot detection process: " + e.getMessage());
		}
	}

	private Optional<BotDetectionRuleValidateResult> verificationEnable(HttpServletRequest request, String userAgent,
			ConfigHolder config) {

		String uri = request.getRequestURI();

		if (StringUtils.hasText(uri) && isIgnoreApi(uri)) {
			return Optional.of(new BotDetectionRuleValidateResult(true,
					"Incoming request URI is a DGR API, skipping bot detection validation. The URI is: " + uri,
					isPrintingLog(2)));
		}

		if (!config.isValidationEnabled) {
			return Optional.of(new BotDetectionRuleValidateResult(true, "Bot Detection Rule Validation is Disable.",
					isPrintingLog(2)));
		}

		if (!StringUtils.hasText(userAgent)) {
			StringBuilder errorMsg = new StringBuilder().append("Skipping bot detection validation.\n\n")
					.append("The following request's User-Agent is empty:\n\n").append("URI: ")
					.append(request.getRequestURI()).append("\n").append("User-Agent: ")
					.append(request.getHeader("User-Agent")).append("\n");
			return Optional.of(new BotDetectionRuleValidateResult(true, errorMsg.toString(), isPrintingLog(2)));
		}

		if (config.patterns.isEmpty()) {
			return Optional.of(new BotDetectionRuleValidateResult(true,
					"Skipping bot detection validation. Bot Detection Rule Validation's Rule patterns is empty.",
					isPrintingLog(2)));
		}

		return Optional.empty();
	}

	/**
	 * 是否為不檢查的 API <br>
	 */
	public static boolean isIgnoreApi(String uri) {
		boolean isIgnore = false;
		List<String> list = Arrays.asList("/dgrv4/", "/website/composer/", "/kibana/", "/http-api/",
				"/composer/swagger3.0/");
		for (String list001 : list) {
			if (uri.startsWith(list001)) {
				isIgnore = true;
				break;
			}
		}

		return isIgnore;
	}

	private String successMessageGenerator(Pattern pattern, HttpServletRequest request) {
		// Check if the input is null
		if (pattern == null || request == null) {
			return "Error: Input parameters are null.";
		}

		// Get the User-Agent
		String userAgent = request.getHeader("User-Agent");

		// Create the formatted message
		StringBuilder message = new StringBuilder();
		message.append("The following request passed the rules:\n\n");
		message.append("User-Agent:\n").append(userAgent).append("\n\n");
		message.append("Passed Pattern:\n").append(pattern.pattern()).append("\n\n");
		message.append("Request Details:\n\n");
		message.append("Method: ").append(request.getMethod()).append("\n");
		message.append("URI: ").append(request.getRequestURI()).append("\n");
		message.append("Protocol: ").append(request.getProtocol()).append("\n");
		message.append("Remote Address: ").append(request.getRemoteAddr()).append("\n\n");
		message.append("Request Headers:\n");

		// Get all header names
//		Enumeration<String> headerNames = request.getHeaderNames();
//		while (headerNames.hasMoreElements()) {
//			String headerName = headerNames.nextElement();
//			String headerValue = request.getHeader(headerName);
//			message.append(headerName).append(": ").append(headerValue).append("\n");
//		}

		return message.toString();
	}

	private String errorMessageGenerator(List<Pattern> patterns, HttpServletRequest request) {
		if (patterns == null || request == null) {
			return "Error: Input parameters are null.";
		}

		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null || userAgent.isEmpty()) {
			return "Error: User-Agent is missing or empty.";
		}

		StringBuilder message = new StringBuilder();
		message.append("The following request User-Agent did not pass the rules:\n\n");
		message.append("Request User-Agent:\n").append(userAgent).append("\n\n");

		message.append("Allow List patterns:\n");
		for (Pattern pattern : patterns) {
			message.append("- ").append(pattern.pattern()).append("\n");
		}
		message.append("\n");

		message.append("Request Details:\n\n");
		message.append("Method: ").append(request.getMethod()).append("\n");
		message.append("URI: ").append(request.getRequestURI()).append("\n");
		message.append("Protocol: ").append(request.getProtocol()).append("\n");
		message.append("Remote Address: ").append(request.getRemoteAddr()).append("\n\n");

//		message.append("Request Headers:\n");
//		Enumeration<String> headerNames = request.getHeaderNames();
//		while (headerNames.hasMoreElements()) {
//			String headerName = headerNames.nextElement();
//			String headerValue = request.getHeader(headerName);
//			message.append(headerName).append(": ").append(headerValue).append("\n");
//		}

		return message.toString();
	}

	public CompletableFuture<Void> reloadAsync(List<Pattern> newPatterns, boolean newIsValidationEnabled) {
		return reloadAsyncInternal(BotDetectionUpdateType.UPDATE, Optional.of(newPatterns),
				Optional.of(newIsValidationEnabled));
	}

	public CompletableFuture<Void> reloadAsync(List<Pattern> newPatterns) {
		return reloadAsyncInternal(BotDetectionUpdateType.UPDATE, Optional.of(newPatterns), Optional.empty());
	}

	public CompletableFuture<Void> reloadAsync(boolean newIsValidationEnabled) {
		return reloadAsyncInternal(BotDetectionUpdateType.UPDATE, Optional.empty(),
				Optional.of(newIsValidationEnabled));
	}

	private CompletableFuture<Void> reloadAsyncInternal(BotDetectionUpdateType type,
			Optional<List<Pattern>> newPatterns, Optional<Boolean> newIsValidationEnabled) {

		pendingConfig.set(new ReloadConfig(newPatterns, newIsValidationEnabled));

		return CompletableFuture.runAsync(() -> {

			ReloadConfig config = pendingConfig.getAndSet(null);
			if (config != null) {
				updateConfig(config);

				switch (type) {
				case INITIALIZATION -> {
					if (Boolean.TRUE.equals(configHolder.get().isValidationEnabled)) {
						TPILogger.tl.debug("\n\nBot Detection Initialization Completed Successfully\n"
								+ configHolder.get().toString());
					}
				}
				case UPDATE -> TPILogger.tl.debug("\n\nBot Detection Update Completed Successfully\n" //
						+ configHolder.get().toString() //
						+ getPrintingLogMsg(isPrintingLog));
				default -> {
					/* 不做任何處理 */ }
				}
			}

		}, reloadExecutor);
	}

	private void updateConfig(ReloadConfig config) {

		List<Pattern> newPatterns = config.patterns.isPresent() ? new ArrayList<>(config.patterns.get())
				: new ArrayList<>(configHolder.get().patterns);

		boolean newIsValidationEnabled = config.isValidationEnabled.isPresent() ? config.isValidationEnabled.get()
				: configHolder.get().isValidationEnabled;

		ConfigHolder newConfig = new ConfigHolder(newPatterns, newIsValidationEnabled);
		configHolder.set(newConfig);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		updateFromDb(BotDetectionUpdateType.INITIALIZATION);
	}

	public void updateFromDb(BotDetectionUpdateType type) {
		boolean isValidationEnabledInit = false;

		String checkBotDetection = tsmpSettingService.getVal_CHECK_BOT_DETECTION();

		if (StringUtils.hasText(checkBotDetection) && "true".equalsIgnoreCase(checkBotDetection)) {
			isValidationEnabledInit = true;
		}

		String botDetectionLog = tsmpSettingService.getVal_BOT_DETECTION_LOG();
		setBotDetectionLogAsync(botDetectionLog);

		List<DgrBotDetection> list = dgrBotDetectionDao
				.findByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(BotDetectionType.WHITELIST.getType());

		List<Pattern> patternsInit = getPatterns(list);

		reloadAsyncInternal(type, Optional.of(patternsInit), Optional.of(isValidationEnabledInit));
	}

	private List<Pattern> getPatterns(List<DgrBotDetection> list) {
		if (list == null || list.isEmpty()) {
			return new ArrayList<>();
		}

		List<Pattern> patterns = new ArrayList<>();
		List<String> invalidRules = new ArrayList<>();

		for (DgrBotDetection detection : list) {
			String rule = detection.getBotDetectionRule();

			try {
				Pattern pattern = Pattern.compile(rule);
				patterns.add(pattern);
			} catch (PatternSyntaxException e) {
				invalidRules.add(String.format("Rule: [%s] - Error: %s", rule, e.getDescription()));
			}
		}

		if (!invalidRules.isEmpty()) {
			TPILogger.tl.error("\n\nBot Detection Rule Validation Error\n\n"
					+ "The following rules contain invalid regular expression syntax:\n\n"
					+ formatInvalidRules(invalidRules));
		}

		return patterns;
	}

	@PreDestroy
	public void destroy() {
		executorService.shutdown();
		reloadExecutor.shutdown();
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
			if (!reloadExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
				reloadExecutor.shutdownNow();
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	private String formatInvalidRules(List<String> invalidRules) {
		return invalidRules.stream().map(rule -> " • " + rule).collect(Collectors.joining("\n")) + "\n";
	}

	public class BotDetectionRuleValidateResult {
		private boolean passed;
		private String msg;
		private boolean printingLog;

		public boolean isPrintingLog() {
			return printingLog;
		}

		public void setPrintingLog(boolean printingLog) {
			this.printingLog = printingLog;
		}

		public BotDetectionRuleValidateResult(boolean passed, String msg) {
			this.passed = passed;
			this.msg = msg;
			this.printingLog = false;
		}

		public BotDetectionRuleValidateResult(boolean passed, String msg, boolean printingLog) {
			this.passed = passed;
			this.msg = msg;
			this.printingLog = printingLog;
		}

		public boolean isPassed() {
			return passed;
		}

		public void setPassed(boolean passed) {
			this.passed = passed;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
	}

	private static class ConfigHolder {
		final List<Pattern> patterns;
		final boolean isValidationEnabled;

		ConfigHolder(List<Pattern> patterns, boolean isValidationEnabled) {
			this.patterns = List.copyOf(patterns);
			this.isValidationEnabled = isValidationEnabled;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			// Bot Detection status
			sb.append("\nBot Detection Status: ").append(isValidationEnabled ? "Enabled" : "Disabled").append("\n\n");

			// Bot Detection Allow List patterns
			sb.append("Bot Detection Allow List Rules:\n");
			if (patterns.isEmpty()) {
				sb.append("- No rules configured");
			} else {
				patterns.forEach(pattern -> sb.append("- ").append(pattern.pattern()).append("\n"));
			}

			return sb.toString();
		}
	}

	private String getPrintingLogMsg(AtomicReferenceArray<Boolean> isPrintingLog) {

		if (isPrintingLog.length() < 3) {
			return "Error: Invalid array length. Expected length: 3, Actual length: " + isPrintingLog.length();
		}

		StringBuilder message = new StringBuilder();
		message.append("\n\nBOT DETECTION LOG STATUS\n\n");

		message.append("Print rejected request logs: ").append(formatStatus(isPrintingLog.get(0))).append("\n");

		message.append("Print accepted request logs: ").append(formatStatus(isPrintingLog.get(1))).append("\n");

		message.append("Print additional info logs: ").append(formatStatus(isPrintingLog.get(2))).append("\n");

		return message.toString();
	}

	private String formatStatus(Boolean status) {
		if (status == null) {
			return "Unknown";
		}

		return status ? "Enabled" : "Disabled";
	}

	public boolean isValidationEnabled() {
		return configHolder.get().isValidationEnabled;
	}

	public List<Pattern> getPatterns() {
		return List.copyOf(configHolder.get().patterns);
	}

	private boolean isPrintingLog(int i) {
		return isPrintingLog.get(i);
	}

	public CompletableFuture<Void> setBotDetectionLogAsync(String input) {
		return CompletableFuture.runAsync(() -> {
			if (input == null || input.trim().isEmpty()) {
				for (int i = 0; i < 3; i++) {
					isPrintingLog.set(i, false);
				}
				return;
			}

			// 移除前後空格，然後以逗號分割字串
			String[] parts = input.trim().split(",");

			// 創建一個 ArrayList 來存儲 boolean 值
			List<Boolean> boolList = new ArrayList<>();

			for (String part : parts) {
				// 移除每個部分的前後空格
				String trimmedPart = part.trim();

				// 如果部分不為空，則解析它
				if (!trimmedPart.isEmpty()) {
					// 將字串轉換為小寫並比較
					if (trimmedPart.equalsIgnoreCase("true")) {
						boolList.add(true);
					} else if (trimmedPart.equalsIgnoreCase("false")) {
						boolList.add(false);
					}
					// 如果不是 "true" 或 "false"，忽略它
				}
			}

			// 將 ArrayList 轉換為 boolean 陣列
			boolean[] result = new boolean[boolList.size()];
			for (int i = 0; i < boolList.size(); i++) {
				result[i] = boolList.get(i);
			}

			for (int i = 0; i < 3; i++) {
				if (result.length > i) {
					isPrintingLog.set(i, result[i]);
				} else {
					isPrintingLog.set(i, false);
				}
			}
		}, reloadExecutor);
	}

	public enum BotDetectionUpdateType {
		INITIALIZATION, UPDATE;
	}
}

class ReloadConfig {
	final Optional<List<Pattern>> patterns;
	final Optional<Boolean> isValidationEnabled;

	ReloadConfig(Optional<List<Pattern>> patterns, Optional<Boolean> isValidationEnabled) {
		this.patterns = patterns;
		this.isValidationEnabled = isValidationEnabled;
	}
}
