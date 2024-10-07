package tpi.dgrv4.gateway.util;

import java.awt.Toolkit;

import org.springframework.util.StringUtils;

public class BeepUtil {

	private static final Toolkit t = Toolkit.getDefaultToolkit();

	public static void beep(@SuppressWarnings("rawtypes") Class clz) throws InterruptedException {
		beep(clz, null);
	}

	public static void beep(
		@SuppressWarnings("rawtypes") Class clz,
		String msg
	) throws InterruptedException {
		if (!StringUtils.hasLength(msg)) {
			msg = "save OK !! " + clz;
		} else {
			msg = String.format("[%s] %s", clz.toString(), msg);
		}

		System.err.println(msg);
		t.beep();
		Thread.sleep(1000);
	}

	public static void beep() {
		System.err.println("beep OK !! ");
		t.beep();
	}

}