package tpi.dgrv4.gateway.util;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;

public enum CusGatewayLoginStateStore {

	INSTANCE;

	private final ConcurrentHashMap<String, Pair<Long, Map<String, String>>> store = new ConcurrentHashMap<>();

	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	private long expirationTime = 1;
	private TimeUnit timeUnit = TimeUnit.MINUTES;

	private ScheduledFuture<?> scheduledFuture;

	CusGatewayLoginStateStore() {
		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, expirationTime, expirationTime,
				timeUnit);
	}

	public String getState() {
		return UUID.randomUUID().toString();
	}

	public String putQueryParams(String state, Map<String, String> queryParams) {

		if (!StringUtils.hasText(state)) {
			state = getState();
		}

		long time = System.currentTimeMillis();
		store.put(state, Pair.of(time, queryParams));
		return state;
	}

	public Map<String, String> getQueryParams(String state) {

		if (!StringUtils.hasText(state)) {
			return Collections.emptyMap();
		}

		Pair<Long, Map<String, String>> pair = store.remove(state);
		if (pair == null) {
			return Collections.emptyMap();
		}

		return pair.getSecond();
	}

	public void setExpiration(long expirationTime, TimeUnit timeUnit) {
		this.expirationTime = expirationTime;
		this.timeUnit = timeUnit;

		if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
			scheduledFuture.cancel(true);
		}

		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, expirationTime, expirationTime,
				timeUnit);
	}

	private void evictExpiredEntries() {
		long currentTime = System.currentTimeMillis();
		store.entrySet().removeIf(entry -> currentTime - entry.getValue().getFirst() > TimeUnit.MILLISECONDS
				.convert(expirationTime, timeUnit));
	}
}
