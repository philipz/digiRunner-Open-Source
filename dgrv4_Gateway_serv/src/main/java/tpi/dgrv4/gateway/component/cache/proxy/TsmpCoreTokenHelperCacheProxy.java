package tpi.dgrv4.gateway.component.cache.proxy;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.security.KeyPair;
import java.util.Base64;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.ITsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.gateway.component.cache.core.AbstractCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpCoreTokenHelperCacheProxy extends AbstractCacheProxy implements ITsmpCoreTokenHelperCacheProxy {

	@Autowired
	private TPILogger logger;

	@Autowired
	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;

	public KeyPair deserializeKeyPair(byte[] content) {
		Supplier<KeyPair> supplier = () -> {
			KeyPair keyPair = null;
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				ObjectInputStream ois = new ObjectInputStream(bais);
				keyPair = (KeyPair) ois.readObject();
				ois.close();
				bais.close();
			} catch (Exception e) {
				this.logger.error("Fail to deserialize from blob\n" + StackTraceUtil.logStackTrace(e));
			}
			return keyPair;
		};
		String base64Content = Base64.getEncoder().encodeToString(content);
		return getOne("deserializeKeyPair", supplier, KeyPair.class, base64Content).orElse(null);
	}

	public String decrypt(String encodedString) {
		Supplier<String> supplier = () -> {
			try {
				return getTsmpCoreTokenHelper().decrypt(encodedString);
			} catch (Exception e) {
				return encodedString;
			}
		};
		return getOne("decrypt", supplier, String.class, encodedString).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpCoreTokenEntityHelper.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(KeyPair.class, new JavaSerializer());
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenHelper() {
		return this.tsmpCoreTokenHelper;
	}

}