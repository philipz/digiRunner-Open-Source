package tpi.dgrv4.dpaa.constant;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 這是一個臨時文件的清理工具。 它提供了一個定期清理指定資料夾中文件的功能， 並根據資料夾大小進行清理。
 */
public enum TempFileCleaner {

	INSTANCE;

	// 資料夾路徑。設定在專案的根目錄下的 "tempFiles" 資料夾。
	private final String tempDirectoryPath = System.getProperty("user.dir") + File.separator + "tempFiles";

	// 用於排程的執行器服務
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
	    Thread thread = new Thread(r);
	    thread.setName("temp-file-cleaner");
	    return thread;
	});

	// 用於存儲排程的參考，以便稍後取消它
	private Future<?> scheduledFuture;

	// 用於存儲檔案路徑和其專屬的時間戳
	private final Map<String, Long> fileTimestamps = new ConcurrentHashMap<>();

	private long timeThreshold;

	private TPILogger logger = TPILogger.tl;

	// 初始化時設定預設每20分鐘自動清理並確保資料夾存在
	TempFileCleaner() {
		ensureDirectoryExists();
		startScheduledCleaner(20, TimeUnit.MINUTES);
	}

	/**
	 * 檢查資料夾底下沒有存在 fileTimestamps 裡面的檔案，並將其刪除。
	 */
	public void cleanUntrackedFiles() {
		File directory = new File(tempDirectoryPath);
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				String absolutePath = file.getAbsolutePath();
				if (!fileTimestamps.containsKey(absolutePath)) {
					deleteFileByCompleteFilePath(absolutePath);
				}
			}
		}
	}

	/**
	 * 設定清理器的間隔時間。
	 *
	 * @param interval 清理的間隔時間。
	 * @param timeUnit 時間的單位。如果沒有提供，預設為分鐘。
	 */
	public void setCleanerInterval(long interval, TimeUnit timeUnit) {
		// 如果之前有設定過排程，則先取消它
		if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
			scheduledFuture.cancel(true);
		}

		// 使用新的間隔時間重新啟動清理器
		startScheduledCleaner(interval, timeUnit != null ? timeUnit : TimeUnit.MINUTES);
	}

	/**
	 * 啟動定時清理器。
	 */
	private void startScheduledCleaner(long interval, TimeUnit timeUnit) {
		timeThreshold = timeUnit.toSeconds(interval);
		scheduledFuture = scheduler.scheduleAtFixedRate(this::cleanExpiredFiles, interval, interval, timeUnit);
	}

	/**
	 * 清理超過20分鐘的檔案。
	 */
	private void cleanExpiredFiles() {
		long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

		List<String> expiredFiles = fileTimestamps.entrySet() //
				.stream() //
				.filter(entry -> currentTime - entry.getValue() > timeThreshold) //
				.map(Map.Entry::getKey) //
				.collect(Collectors.toList());

		// 刪除這些檔案並從 fileTimestamps 中移除它們的時間戳
		expiredFiles.forEach(this::deleteFileByCompleteFilePath);
		cleanUntrackedFiles();
	}

	/**
	 * 確保臨時文件資料夾存在。
	 */
	private void ensureDirectoryExists() {
		File directory = new File(tempDirectoryPath);
		if (!directory.exists()) {
			directory.mkdir(); // 如果資料夾不存在，創建它
		}
	}

	/**
	 * 刪除指定的檔案。
	 *
	 * @param filePath 要刪除的檔案的路徑。
	 * @return 是否成功刪除檔案。
	 */
	public boolean deleteFileByCompleteFilePath(String filePath) {
		Path path = Paths.get(filePath);
		try {
			Files.delete(path);
			
			// 從 Map 中移除該檔案的時間戳
			if (fileTimestamps.containsKey(filePath)) {
				fileTimestamps.remove(filePath); 
			}
			return true;
		} catch (NoSuchFileException e) {
			logger.error(String.format("%s: no such file or directory", path));
			logger.error(StackTraceUtil.logStackTrace(e));
		} catch (DirectoryNotEmptyException e) {
			logger.error(String.format("%s not empty%n", path));
			logger.error(StackTraceUtil.logStackTrace(e));
		} catch (IOException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
		return false;
	}

	/**
	 * 儲存檔案並回傳其路徑。
	 *
	 * @param file 要儲存的檔案。
	 * @return 儲存的檔案路徑。
	 * @throws Exception 如果儲存檔-案時發生錯誤。
	 */
	public String saveFile(MultipartFile file) throws Exception {

		// 檢查檔案是否為空
		if (file == null || file.isEmpty()) {
			return null;
		}

		// 1. 保證檔名是唯一的
		String originalFilename = file.getOriginalFilename();

		if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
			originalFilename = "DefaultFileName.txt";
		} else {
			// 防止目錄遍歷攻擊
			originalFilename = originalFilename.replace("../", "");
		}

		String uniqueFilename = UUID.randomUUID().toString() + "" + originalFilename;
		
		// 2. 依照設定的路徑把檔案存入指定路徑上
		Path savedFilePath = Paths.get(tempDirectoryPath, uniqueFilename);
		ensureDirectoryExists();
		//checkmarx, Stored Absolute Path Traversal, Input Path Not Canonicalized
		CheckmarxCommUtils.sanitizeForCheckmarx(file, savedFilePath);

		// 3. 取得現在時間戳並依照檔案大小來增加
		long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		long fileSizeInMB = file.getSize() / (1024 * 1024);
		timestamp += fileSizeInMB * 60; // 每大 1 MB 時間戳就加上 1 分鐘

		// 4. 將新增檔案路徑與專屬的時間戳放入 Map
		String filePath = savedFilePath.toString();
		fileTimestamps.put(filePath, timestamp);

		// 5. 把該檔案路徑回傳
		return filePath;
	}
	
	private String sanitizePathTraversal(String filename) {
		Path p = Paths.get(filename);
		return p.getFileName().toString();
	}

}
