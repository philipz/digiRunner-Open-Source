package tpi.dgrv4.dpaa.service;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0200Req;
import tpi.dgrv4.dpaa.vo.DPB0200Resp;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0200Service {
	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	private TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
	
	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	private final static String SUCCESS = "Connect success";
	private final static String FAIL = "Connect fail";

	public DPB0200Resp testRdbConnection(TsmpAuthorization authorization, DPB0200Req req) {
		DPB0200Resp resp = new DPB0200Resp();
		
		String msg = null;
		Boolean success = false;
		String jdbcUrl = req.getJdbcUrl();
		String username = req.getUserName();
		String mima = req.getMima();
		try {
			if (!StringUtils.hasLength(jdbcUrl))
				throw TsmpDpAaRtnCode._1350.throwing("{{jdbcUrl}}");

			if (!StringUtils.hasLength(username)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{username}}");
			}

		} catch (TsmpDpAaException e) {
			throw e;
		}
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(getTsmpSettingService().getENCPlainVal(mima));

		try {

			HikariDataSource dataSource = new HikariDataSource(config);

			try (Connection connection = dataSource.getConnection()) {
				if (connection.isValid(5000)) {
					msg = SUCCESS;
					success = true;
				} else {
					msg = FAIL;
					success = false;
				}
			} catch (SQLException e) {
				msg = FAIL;;
				success = false;
				this.logger.error(StackTraceUtil.logStackTrace(e));
			} finally {
				dataSource.close();
			}
		} catch (Exception e) {
			msg = FAIL;
			success = false;
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		resp.setMsg(msg);
		resp.setSuccess(success);
		return resp;
	}
 
	protected TsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
		return this.tsmpCoreTokenHelperCacheProxy;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
