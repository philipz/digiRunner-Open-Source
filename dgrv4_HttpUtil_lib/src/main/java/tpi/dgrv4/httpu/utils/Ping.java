package tpi.dgrv4.httpu.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ping {
	public static boolean ping(String ipAddress) throws Exception {
		int timeOut = 3000; // 超時應該在3鈔以上
		boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut); // 当返回值是true时，说明host是可用的，false则不可。
		return status;
	}

	public static void ping02(String ipAddress) throws Exception {
		String line = null;
		try {
			Process pro = Runtime.getRuntime().exec("ping " + ipAddress);
			BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while ((line = buf.readLine()) != null)
				System.out.println(line);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public static boolean ping(String ipAddress, int pingTimes, int timeOut) {
		BufferedReader in = null;
		Runtime r = Runtime.getRuntime(); // 將要執行的ping命令,此命令是windows格式的命令
		String pingCommand = "ping " + ipAddress + " -n " + pingTimes + " -w " + timeOut;
		try { // 執行命令並獲取輸出
			System.out.println(pingCommand);
			Process p = r.exec(pingCommand);
			if (p == null) {
				return false;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream())); // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
			int connectedCount = 0;
			String line = null;
			while ((line = in.readLine()) != null) {
				connectedCount += getCheckResult(line);
			} // 如果出現類似=23ms TTL=62這樣的字樣,出現的次數=測試次數則返回 true
			return connectedCount == pingTimes;
		} catch (Exception ex) {
			ex.printStackTrace(); // 出現異常則返回 false
			return false;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 若line含有=18ms TTL=16字樣,說明已經ping通,返回1,否則返回0.
	private static int getCheckResult(String line) { // System.out.println("控制台输出的结果为:"+line);
		Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);
		if (line==null) {line="";}
		Matcher matcher = pattern.matcher(line);// 不接受 null 
		while (matcher.find()) {
			return 1;
		}
		return 0;
	}

	/** Lib 中不能有 main */
//	public static void main(String[] args) throws Exception {
//		String ipAddress = "127.0.0.1";
//		System.out.println(ping(ipAddress));
//		ping02(ipAddress);
//		System.out.println(ping(ipAddress, 5, 5000));
//	}
}
