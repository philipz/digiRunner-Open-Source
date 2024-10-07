package tpi.dgrv4.entity.component.cipher;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.AESUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;

/**
 * 對稱式加解密工具
 */
@Component
public class TsmpTAEASKHelper {

	@Autowired
	private ITPILogger logger;

	private final int keySize = 128;

	private final Charset defaultCharset = StandardCharsets.UTF_8;

	private final String algorithm = "AES";

	private String secureKey;

	private Cipher encryptCipher;

	private Cipher decryptCipher;

	public TsmpTAEASKHelper(ITPILogger logger) {
		this.logger = logger;
	}

	@PostConstruct
	public void init() throws Exception {
		this.logger.debugDelay2sec("=== Begin TAEASK initialization ===");

		loadKey();

		/*
		 * 2022.05.10 改用 AESUtils 就不用初始化 Cipher 了 KeyGenerator kgen =
		 * KeyGenerator.getInstance(this.algorithm); SecureRandom secureRandom =
		 * SecureRandom.getInstance("SHA1PRNG");
		 * secureRandom.setSeed(getSecureKey().getBytes()); kgen.init(this.keySize,
		 * secureRandom);
		 * 
		 * SecretKey secretKey = kgen.generateKey(); byte[] enCodeFormat =
		 * secretKey.getEncoded(); SecretKeySpec key = new SecretKeySpec(enCodeFormat,
		 * this.algorithm);
		 * 
		 * this.encryptCipher = Cipher.getInstance(this.algorithm);
		 * this.encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		 * 
		 * this.decryptCipher = Cipher.getInstance(this.algorithm);
		 * this.decryptCipher.init(Cipher.DECRYPT_MODE, key);
		 */

		this.logger.debugDelay2sec("=== TAEASK initialize successfully! ===");
	}

	private void loadKey() throws Exception {
		String key = null;

		// 需要交付組在啟動v3的shell或bat中設定"TAEASK"的值，這邊才會抓得到
		try {
			key = System.getenv("TAEASK");
			if (!StringUtils.hasText(key)) {
				String errMsg = "\n\t...Could not find TAEASK Key\n";
				errMsg += "\t...Please add Eclipse 'Run as / Configurations... / Enviroment / Variable:TAEASK'\n";
				this.logger.error(errMsg);
			}
		} catch (Exception e) {
			this.logger.error("Unable to load TAEASK Key\n" + StackTraceUtil.logStackTrace(e));
		}

		// 如果找不到 secureKey 就自己產生一組
		if (!StringUtils.hasText(key)) {
			try {
				key = genKey();
				this.logger.debug("Generating TAEASK Key: " + key);
			} catch (Exception e) {
				this.logger.error("Error generating TAEASK Key\n" + StackTraceUtil.logStackTrace(e));
			}
		}

		if (!StringUtils.hasText(key)) {
			throw new Exception("Error loading TAEASK Key");
		}

		this.secureKey = key;

		this.logger.debugDelay2sec("TAEASK Key is loaded");
	}

	private String genKey() {
		long receptor = Instant.now().getEpochSecond();
		int radix = Character.MAX_RADIX;
		return Long.toString(receptor, radix);
	}

	public String encrypt(String content) {
		/*
		 * 2022.05.10 改用 AESUtils try { byte[] contentBytes =
		 * content.getBytes(this.defaultCharset); byte[] encryptBytes =
		 * getEncryptCipher().doFinal(contentBytes); String code =
		 * HexStringUtils.toString(encryptBytes); return code; } catch (Exception e) {
		 * logger.debug(StackTraceUtil.logStackTrace(e)); throw
		 * DgrRtnCode._1297.throwing(); }
		 */
		String code = encryptByAESUtils(content);
		return code;
	}

	public String decrypt(String content) {
		/*
		 * 2022.05.10 改用 AESUtils try { byte[] contentBytes =
		 * HexStringUtils.toBytes(content); byte[] decryptBytes =
		 * getDecryptCipher().doFinal(contentBytes);
		 * 
		 * String decryptContent = new String(decryptBytes); return decryptContent; }
		 * catch (Exception e) { logger.debug(StackTraceUtil.logStackTrace(e)); throw
		 * DgrRtnCode._1297.throwing(); }
		 */
		String decryptContent = decryptByAESUtils(content);
		return decryptContent;
	}

	public String encryptByAESUtils(String content) {
		try {
			byte[] seed = getSecureKey().getBytes();

			String enc = AESUtils.AesCipher(content, Cipher.ENCRYPT_MODE, this.keySize, this.algorithm, null, seed);

			// Base64 to Hex
			enc = HexStringUtils.toString(Base64Util.base64Decode(enc));

			return enc;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	public String decryptByAESUtils(String content) {
		try {
			// Hex to Base64
			content = Base64Util.base64Encode(HexStringUtils.toBytes(content));

			byte[] seed = getSecureKey().getBytes();

			String dec = AESUtils.AesCipher(content, Cipher.DECRYPT_MODE, this.keySize, this.algorithm, null, seed);

			return dec;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	protected String getSecureKey() {
		return this.secureKey;
	}

	protected Cipher getEncryptCipher() {
		return this.encryptCipher;
	}

	protected Cipher getDecryptCipher() {
		return this.decryptCipher;
	}

}