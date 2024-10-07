package tpi.dgrv4.dpaa.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0189Req;
import tpi.dgrv4.dpaa.vo.DPB0189Resp;
import tpi.dgrv4.dpaa.vo.DataSourceInfoVo;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;

@Service
public class DPB0189Service {
	private TPILogger logger = TPILogger.tl;
	@Autowired
	private DgrRdbConnectionDao dgrRdbConnectionDao;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TsmpSettingService tsmpSettingService;

	private Map<String, DataSourceInfoVo> dataSourceMap = new HashMap<>();

	@Autowired
	private CApiKeyService capiKeyService;

	public DPB0189Resp executeSql(DPB0189Req req, HttpHeaders headers) {
		return executeSql(req, headers, true, true);
	}
	
	public DPB0189Resp executeSql(DPB0189Req req, HttpHeaders headers, boolean isCheckComposerId,
			boolean isVerifyCApiKey) {
		DPB0189Resp resp = new DPB0189Resp();

		try {
			String strRs = checkParam(req);
			if (strRs != null) {
				resp.setResult(strRs);
				return resp;
			}

			if(isVerifyCApiKey) {
				// 驗證CApiKey
				getCapiKeyService().verifyCApiKey(headers, true, isCheckComposerId);
			}

			DgrRdbConnection connVo = getDgrRdbConnectionDao().findById(req.getConnName()).orElse(null);
			if (connVo != null) {
				DataSourceInfoVo dsVo = dataSourceMap.get(req.getConnName());
				if (dsVo == null || (dsVo != null && connVo.getVersion() > dsVo.getVersion())) {
					HikariConfig config = new HikariConfig();
					config.setJdbcUrl(connVo.getJdbcUrl());
					config.setUsername(connVo.getUserName());
					String mima = null;
					if ("ENC()".equals(connVo.getMima())) {
						mima = "";
					} else {
						mima = getTsmpSettingService().getENCPlainVal(connVo.getMima());
					}
					config.setPassword(mima);
					config.setMaximumPoolSize(connVo.getMaxPoolSize());
					config.setConnectionTimeout(connVo.getConnectionTimeout());
					config.setIdleTimeout(connVo.getIdleTimeout());
					config.setMaxLifetime(connVo.getMaxLifetime());
					if (StringUtils.hasText(connVo.getDataSourceProperty())) {
						List<Map<String, String>> list = getObjectMapper().readValue(connVo.getDataSourceProperty(),
								new TypeReference<List<Map<String, String>>>() {
								});
						list.forEach(map -> {
							map.forEach((k, v) -> {
								config.addDataSourceProperty(k, v);
							});
						});
					}

					if (dsVo != null) {
						dsVo.getHikariDataSource().close();
					}

					HikariDataSource dataSource = new HikariDataSource(config);
					dsVo = new DataSourceInfoVo();
					dsVo.setHikariDataSource(dataSource);
					dsVo.setVersion(connVo.getVersion());
					dataSourceMap.put(connVo.getConnectionName(), dsVo);

				}

				String rsJson = execJdbc(dsVo.getHikariDataSource(), req);
				resp.setResult(rsJson);
				resp.setResultJson(true);
				resp.setRtnCode("0000");

			} else {
				resp.setResult("connName is " + req.getConnName() + " not found");
				return resp;

			}

		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			resp.setResult(e.getMessage());
		}

		return resp;
	}

	private String execJdbc(HikariDataSource dataSource, DPB0189Req req) throws Exception {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(req.getStrSql().trim())) {

			String rsJson = null;
			boolean isQuery = false;
			String strSql = req.getStrSql().toLowerCase().trim();
			if (strSql.indexOf("select") == 0) {
				isQuery = true;
			}
			strSql = req.getStrSql().trim();

			List<String> paramList = req.getParamList();
			if (!CollectionUtils.isEmpty(paramList)) {
				for (int i = 1; i <= paramList.size(); i++) {
					preparedStatement.setString(i, paramList.get(i - 1));
				}
			}
			if (isQuery) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					ResultSetMetaData metadata = resultSet.getMetaData();
					int columnCount = metadata.getColumnCount();
					List<Map<String, String>> dataList = new ArrayList<>();
					while (resultSet.next()) {
						Map<String, String> dataMap = new HashMap<>();
						for (int i = 1; i <= columnCount; i++) {
							dataMap.put(metadata.getColumnName(i), resultSet.getString(i));
						}
						dataList.add(dataMap);
					}
					rsJson = getObjectMapper().writeValueAsString(dataList);
				}
			} else {
				int updatedRows = preparedStatement.executeUpdate();
				Map<String, Integer> dataMap = new HashMap<>();
				dataMap.put("updatedRows", updatedRows);
				rsJson = getObjectMapper().writeValueAsString(dataMap);
			}
			return rsJson;
		}
	}

	private String checkParam(DPB0189Req req) {
		if (!StringUtils.hasText(req.getConnName())) {
			return "connName is required parameters";
		}

		if (!StringUtils.hasText(req.getStrSql())) {
			return "strSql is required parameters";
		}

		return null;

	}

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}

	protected Map<String, DataSourceInfoVo> getDataSourceMap() {
		return dataSourceMap;
	}

}
