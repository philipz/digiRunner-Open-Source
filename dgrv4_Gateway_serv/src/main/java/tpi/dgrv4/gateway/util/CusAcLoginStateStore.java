package tpi.dgrv4.gateway.util;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.data.util.Pair;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;

public enum CusAcLoginStateStore {

	INSTANCE; // 單例實例

	// 存儲狀態和 DgrAcIdpInfoCus 對象的 ConcurrentHashMap
	private final ConcurrentHashMap<String, Pair<Long, DgrAcIdpInfoCus>> store = new ConcurrentHashMap<>();

	// 用於執行定期清理任務的執行器
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
	    Thread thread = new Thread(r);
	    thread.setName("CusAcLoginStateStoreThread");
	    return thread;
	});

	// 過期時間設置，默認為 1 分鐘
	private long expirationTime = 1;
	private TimeUnit timeUnit = TimeUnit.MINUTES;

	// 定期執行的任務
	private ScheduledFuture<?> scheduledFuture;

	// 構造函數：初始化定期清理任務
	CusAcLoginStateStore() {
		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, expirationTime, expirationTime,
				timeUnit);
	}

	// 生成唯一的狀態標識符
	public String getState() {
		return UUID.randomUUID().toString();
	}

	// 存儲 DgrAcIdpInfoCus 對象並返回對應的狀態標識符
	public String putDgrAcIdpInfoCus(DgrAcIdpInfoCus info) {

		if (info == null) {
			return "";
		}

		String state = getState();
		long time = System.currentTimeMillis();
		store.put(state, Pair.of(time, info));
		return state;
	}

	// 根據狀態標識符獲取並移除 DgrAcIdpInfoCus 對象
	public Optional<DgrAcIdpInfoCus> getDgrAcIdpInfoCus(String state) {

		if (state == null) {
			return Optional.empty();
		}

		Pair<Long, DgrAcIdpInfoCus> pair = store.remove(state);
		if (pair == null) {
			return Optional.empty();
		}

		return Optional.of(pair.getSecond());
	}

	// 設置新的過期時間和單位，並重新調度清理任務
	public void setExpiration(long expirationTime, TimeUnit timeUnit) {
		this.expirationTime = expirationTime;
		this.timeUnit = timeUnit;

		if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
			scheduledFuture.cancel(true);
		}

		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, expirationTime, expirationTime,
				timeUnit);
	}

	// 清理過期條目的方法
	private void evictExpiredEntries() {
		long currentTime = System.currentTimeMillis();
		store.entrySet().removeIf(entry -> currentTime - entry.getValue().getFirst() > TimeUnit.MILLISECONDS
				.convert(expirationTime, timeUnit));
	}
}
