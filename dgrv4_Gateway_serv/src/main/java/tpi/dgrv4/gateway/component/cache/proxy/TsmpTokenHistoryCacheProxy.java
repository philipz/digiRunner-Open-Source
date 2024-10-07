package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;

import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
@Component
public class TsmpTokenHistoryCacheProxy extends DaoCacheProxy {

	@Autowired
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;

	public TsmpTokenHistory findFirstByTokenJti(String jti) {
		Supplier<TsmpTokenHistory> supplier = () -> {
			return getTsmpTokenHistoryDao().findFirstByTokenJti(jti);
		};
		return getOne("findFirstByTokenJti", supplier, TsmpTokenHistory.class, jti).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpTokenHistoryDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpTokenHistory.class);
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return this.tsmpTokenHistoryDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}

}