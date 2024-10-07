package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.IFileHelperCacheProxy;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.cache.core.AbstractCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class FileHelperCacheProxy extends AbstractCacheProxy implements IFileHelperCacheProxy{
	@Autowired
	private TPILogger logger;
	
	@Autowired
	private FileHelper fileHelper;

	public byte[] downloadByPathAndName(String tsmpDpFilePath, String filename) {
		Supplier<byte[]> supplier = () -> {
			try {
				return getFileHelper().downloadByPathAndName(tsmpDpFilePath, filename);
			} catch (Exception e) {
				logger.debug(StackTraceUtil.logStackTrace(e));
				return null;
			}
		};
		return getOne("downloadByPathAndName", supplier, byte[].class, tsmpDpFilePath, filename).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return FileHelper.class;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
}