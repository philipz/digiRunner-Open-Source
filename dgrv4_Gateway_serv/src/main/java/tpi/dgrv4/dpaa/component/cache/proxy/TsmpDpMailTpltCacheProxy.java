package tpi.dgrv4.dpaa.component.cache.proxy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;

import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpDpMailTpltCacheProxy extends DaoCacheProxy {

	@Autowired
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;

	public List<TsmpDpMailTplt> findByCode(String code) {
		Supplier<List<TsmpDpMailTplt>> supplier = () -> {
			return getTsmpDpMailTpltDao().findByCode(code);
		};
		return getList("findByCode", supplier, code);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpDpMailTpltDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpDpMailTplt.class);
	}

	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
