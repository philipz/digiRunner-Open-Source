package tpi.dgrv4.entity.component.cipher;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.function.Function;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.entity.component.IFileHelper;
import tpi.dgrv4.entity.component.IFileHelperCacheProxy;
import tpi.dgrv4.entity.component.ITsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;


/**
 * 利用 digiRunner 底層核發 Token 所使用的密碼金鑰對 (KeyPair) 實現加、解密
 * 
 * @author Kim
 */
@Component
public class TsmpCoreTokenEntityHelper {

	@Autowired
	private ITPILogger logger;

	@Autowired
	private IFileHelperCacheProxy fileHelperCacheProxy;

	@Autowired
	private ITsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
		
	private final int MAX_ENCRYPT_BLOCK = 117;

	private int MAX_DECRYPT_BLOCK = 128;
	
	private boolean loggerFlag = true;
	
	public TsmpCoreTokenEntityHelper(ITPILogger logger) {
		this.logger = logger;
	}

	public String encrypt(String originalString) throws DgrException {
		PublicKey publicKey = getPublicKey();
		if (publicKey == null) {
			this.logger.debug("Public key is null");
			throw DgrRtnCode._1433.throwing( TsmpCoreTokenInitializer.DEFAULT_ALGORITHM );
		}

		byte[] originalByte = originalString.getBytes();
		byte[] encodedByte = codec(originalByte, Cipher.ENCRYPT_MODE, publicKey, this.MAX_ENCRYPT_BLOCK);
		String encodedString = Base64.getEncoder().encodeToString(encodedByte);
		return encodedString;
	}

	public String decrypt(String encodedString) throws DgrException {
		PrivateKey privateKey = getPrivateKey();
		if (privateKey == null) {
			this.logger.debug("Private key is null");
			throw DgrRtnCode._1434.throwing( TsmpCoreTokenInitializer.DEFAULT_ALGORITHM );
		}
		
		// 調整 RSA decrypt block size
		if (privateKey != null && privateKey instanceof RSAPrivateKey) {
			RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
			int keySize = rsaPrivateKey.getModulus().bitLength();
			if (keySize >= 2048) {
				this.MAX_DECRYPT_BLOCK = 256;
			}
		}
		
		byte[] encodedByte = Base64.getDecoder().decode(encodedString);
		byte[] decodedByte = codec(encodedByte, Cipher.DECRYPT_MODE, privateKey, this.MAX_DECRYPT_BLOCK);
		String decodedString = new String(decodedByte);
		return decodedString;
	}

	private byte[] codec(byte[] data, int mode, Key key, int maxBlockLength) throws DgrException {
		try {
			Cipher cipher = Cipher.getInstance( TsmpCoreTokenInitializer.DEFAULT_ALGORITHM );
			cipher.init(mode, key);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int inputLen = data.length;
			int offSet = 0;
			byte[] cache;
			int i = 0;

			while (inputLen - offSet > 0) {
				if (inputLen - offSet > maxBlockLength) {
					cache = cipher.doFinal(data, offSet, maxBlockLength);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * maxBlockLength;
			}
			byte[] encryptedData = out.toByteArray();

			return encryptedData;

		} catch (Throwable t) {
			if (Cipher.ENCRYPT_MODE == mode) {
				throw DgrRtnCode._1433.throwing( TsmpCoreTokenInitializer.DEFAULT_ALGORITHM );
			} else if (Cipher.DECRYPT_MODE == mode) {
				try {
					this.logger.error(new String(data, StandardCharsets.UTF_8));
				}catch(Exception e) {
					
				}
				throw DgrRtnCode._1434.throwing( TsmpCoreTokenInitializer.DEFAULT_ALGORITHM );
			} else {
				throw DgrRtnCode._1297.throwing();
			}
		}
	}

	/**
	 * 取得 PublicKey
	 * 
	 * @return Nullable
	 */
	private PublicKey getPublicKey() {
		return getKey((keyPair) -> {
			return keyPair.getPublic();
		});
	}

	/**
	 * 取得 PrivateKey
	 * 
	 * @return Nullable
	 */
	private PrivateKey getPrivateKey() {
		return getKey((keyPair) -> {
			return keyPair.getPrivate();
		});
	}

	private <R> R getKey(Function<KeyPair, R> func) {
		KeyPair keyPair = getKeyPair();
		if (keyPair == null) {
			return null;
		}
		return func.apply(keyPair);
	}

	/**
	 * 從資料庫讀取 KeyPair
	 * 
	 * @return
	 */
	public KeyPair getKeyPair() {
		byte[] content = getKeyPairContent();
		return deserializeKeyPair(content);
	}

	private byte[] getKeyPairContent() {
		TsmpDpFileType keyPairFileType = TsmpCoreTokenInitializer.KEY_PAIR_FILE_TYPE;
		Long keyPairRefId = TsmpCoreTokenInitializer.KEY_PAIR_REF_ID;
		String keyPairFileName = TsmpCoreTokenInitializer.KEY_PAIR_FILE_NAME;
		
		if (!StringUtils.hasText(keyPairFileName)) {
			this.logger.error("File name of KeyPair is empty! Was TsmpCoreTokenInitializer initialized successfully?");
			return null;
		}
		
		String tsmpDpFilePath = IFileHelper.getTsmpDpFilePath(keyPairFileType, keyPairRefId);

		//if (this.loggerFlag == true) {
			// 為了不要 RunLoopJob / ES 每 call 一次要解密時就 debugDelay2sec 一次, 導致整個畫面都是 "...Downloading KeyPair"
			//this.logger.debugDelay2sec("Downloading KeyPair from: " + tsmpDpFilePath + keyPairFileName);
		//}
		
		byte[] blobData = getFileHelperCacheProxy().downloadByPathAndName(tsmpDpFilePath, keyPairFileName);
		if (blobData == null || blobData.length < 1) {
			// 為了不要 RunLoopJob / ES 每 call 一次要解密時就 debugDelay2sec 一次, 導致整個畫面都是 "...Downloading KeyPair"
			this.loggerFlag = true;

			//例如在執行本機但DB連DEV
			if(tsmpDpFilePath != null && tsmpDpFilePath.indexOf("/") > -1) {
				String tsmpDir = tsmpDpFilePath.replaceAll("/", "\\\\");
				blobData = getFileHelperCacheProxy().downloadByPathAndName(tsmpDir, keyPairFileName);
				//this.logger.debugDelay2sec("change Downloading KeyPair from: " + tsmpDir + keyPairFileName);
			}else if(tsmpDpFilePath != null){
				String tsmpDir = tsmpDpFilePath.replaceAll("\\\\", "/");
				blobData = getFileHelperCacheProxy().downloadByPathAndName(tsmpDir, keyPairFileName);
				//this.logger.debugDelay2sec("change Downloading KeyPair from: " + tsmpDir + keyPairFileName);
			}
			
			if (blobData == null || blobData.length < 1) {
				this.logger.error(String.format("Fail to read blob from db: %s%s", tsmpDpFilePath, keyPairFileName));
			}
		} else {
			// 為了不要 RunLoopJob / ES 每 call 一次要解密時就 debugDelay2sec 一次, 導致整個畫面都是 "...Downloading KeyPair"
			this.loggerFlag = false;
		}
		return blobData;
	}

	/**
	 * KeyPair反序列化
	 * @param content
	 * @return
	 */
	public KeyPair deserializeKeyPair(byte[] content) {
		if (content == null || content.length == 0) {
			return null;
		}
		
		return getTsmpCoreTokenHelperCacheProxy().deserializeKeyPair(content);
	}
	
	protected IFileHelperCacheProxy getFileHelperCacheProxy() {
		return this.fileHelperCacheProxy;
	}

	protected ITsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
		return this.tsmpCoreTokenHelperCacheProxy;
	}

}