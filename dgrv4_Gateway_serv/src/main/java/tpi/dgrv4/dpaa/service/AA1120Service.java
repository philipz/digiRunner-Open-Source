package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.*;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AA1120Service {

	@Autowired
	private TsmpClientDao tsmpClientDao;
	@Autowired
	private TsmpClientHostDao tsmpClientHostDao;
	@Autowired
	private OauthClientDetailsDao oauthClientDetailsDao;
	@Autowired
	private TsmpDpClientextDao tsmpDpClientextDao;
	@Autowired
	private TsmpGroupDao tsmpGroupDao;
	@Autowired
	private TsmpClientGroupDao tsmpClientGroupDao;
	@Autowired
	private TsmpVgroupDao tsmpVgroupDao;
	@Autowired
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	@Autowired
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	@Autowired
	private TsmpGroupApiDao tsmpGroupApiDao;
	@Autowired
	private DgrXApiKeyDao dgrXApiKeyDao;
	@Autowired
	private DgrXApiKeyMapDao dgrXApiKeyMapDao;
	@Autowired
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	@Autowired
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	@Autowired
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	@Autowired
	private TsmpSecurityLevelDao tsmpSecurityLevelDao;
	@Autowired
	private DpAppDao dpAppDao;
	@Autowired
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	@Autowired
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	@Autowired
	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;
	@Autowired
	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;
	@Autowired
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;
	@Autowired
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;
	@Autowired
	private TsmpApiDao tsmpApiDao;
	@Autowired
	private DgrRdbConnectionDao dgrRdbConnectionDao; 
	@Autowired
	private ObjectMapper objectMapper;


	public AA1120Resp exportClientRelated(TsmpAuthorization auth, AA1120Req req) {
		AA1120Resp resp = new AA1120Resp();

		try {
			List<TsmpClient> tsmpClientList = getTsmpClientDao().findAll();
			List<TsmpClientHost> tsmpClientHostList = getTsmpClientHostDao().findAll();
			List<OauthClientDetails> oauthClientDetailsList = getOauthClientDetailsDao().findAll();
			List<TsmpDpClientext> tsmpDpClientextList = getTsmpDpClientextDao().findAll();
			List<TsmpGroup> tsmpGroupList = getTsmpGroupDao().findAll();
			List<TsmpClientGroup> tsmpClientGroupList = getTsmpClientGroupDao().findAll();
			List<TsmpVgroup> tsmpVgroupList = getTsmpVgroupDao().findAll();
			List<TsmpClientVgroup> tsmpClientVgroupList = getTsmpClientVgroupDao().findAll();
			List<TsmpVgroupGroup> tsmpVgroupGroupList = getTsmpVgroupGroupDao().findAll();
			List<TsmpGroupApi> tsmpGroupApiList = getTsmpGroupApiDao().findAll();
			List<DgrXApiKey> dgrXApiKeyList = getDgrXApiKeyDao().findAll();
			List<DgrXApiKeyMap> dgrXApiKeyMapList = getDgrXApiKeyMapDao().findAll();
			List<TsmpGroupAuthorities> tsmpGroupAuthoritiesList = getTsmpGroupAuthoritiesDao().findAll();
			List<TsmpGroupAuthoritiesMap> tsmpGroupAuthoritiesMapList = getTsmpGroupAuthoritiesMapDao().findAll();
			List<TsmpVgroupAuthoritiesMap> tsmpVgroupAuthoritiesMapList = getTsmpVgroupAuthoritiesMapDao().findAll();
			List<TsmpSecurityLevel> tsmpSecurityLevelList = getTsmpSecurityLevelDao().findAll();
			List<DpApp> dpAppList = getDpAppDao().findAll();
			List<TsmpOpenApiKey> tsmpOpenApiKeyList = getTsmpOpenApiKeyDao().findAll();
			List<TsmpOpenApiKeyMap> tsmpOpenApiKeyMapList = getTsmpOpenApiKeyMapDao().findAll();
			List<DgrGtwIdpInfoO> dgrGtwIdpInfoOList = getDgrGtwIdpInfoODao().findAll();
			List<DgrGtwIdpInfoL> dgrGtwIdpInfoLList = getDgrGtwIdpInfoLDao().findAll();
			List<DgrGtwIdpInfoA> dgrGtwIdpInfoAList = getDgrGtwIdpInfoADao().findAll();
			List<DgrGtwIdpInfoJdbc> dgrGtwIdpInfoJdbcList = getDgrGtwIdpInfoJdbcDao().findAll();
			List<DgrRdbConnection> dgrRdbConnectionList = getDgrRdbConnectionDao().findAll();
			
			
			//dgr api key使用的api
			List<AA1120DgrApikeyUseApi> useApiList = new ArrayList<>();
			for(TsmpOpenApiKeyMap vo : tsmpOpenApiKeyMapList) {
				Optional<AA1120DgrApikeyUseApi> useApiOpt = useApiList.stream().filter(f->f.getApiUid().equals(vo.getRefApiUid())).findAny();
				if(useApiOpt.isPresent()) {
					continue;
				}
				
				List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(vo.getRefApiUid());
				apiList.forEach(apiVo->{
					AA1120DgrApikeyUseApi useApiVo = new AA1120DgrApikeyUseApi();
					useApiVo.setApiId(apiVo.getApiKey());
					useApiVo.setApiUid(apiVo.getApiUid());
					useApiVo.setModuleName(apiVo.getModuleName());
					useApiList.add(useApiVo);
				});
			}
			
			//日期增加longtime格式
			tsmpDpClientextList.forEach(vo ->{
				if(vo.getResubmitDateTime() != null) {
					vo.setResubmitDateTimeForLongTime("" + vo.getResubmitDateTime().getTime());
					vo.setResubmitDateTime(null);
				}
			});
			
			//塞import資料
			AA1120ImportClientRelated importVo = new AA1120ImportClientRelated();
			importVo.setDgrGtwIdpInfoAList(dgrGtwIdpInfoAList);
			importVo.setDgrGtwIdpInfoJdbcList(dgrGtwIdpInfoJdbcList);
			importVo.setDgrGtwIdpInfoLList(dgrGtwIdpInfoLList);
			importVo.setDgrGtwIdpInfoOList(dgrGtwIdpInfoOList);
			importVo.setDgrXApiKeyList(dgrXApiKeyList);
			importVo.setDgrXApiKeyMapList(dgrXApiKeyMapList);
			importVo.setDpAppList(dpAppList);
			importVo.setOauthClientDetailsList(oauthClientDetailsList);
			importVo.setTsmpClientGroupList(tsmpClientGroupList);
		    importVo.setTsmpClientHostList(tsmpClientHostList);
		    importVo.setTsmpClientList(tsmpClientList);
		    importVo.setTsmpClientVgroupList(tsmpClientVgroupList);
		    importVo.setTsmpDpClientextList(tsmpDpClientextList);
		    importVo.setTsmpGroupApiList(tsmpGroupApiList);
		    importVo.setTsmpGroupAuthoritiesList(tsmpGroupAuthoritiesList);
		    importVo.setTsmpGroupAuthoritiesMapList(tsmpGroupAuthoritiesMapList);
		    importVo.setTsmpGroupList(tsmpGroupList);
		    importVo.setTsmpOpenApiKeyList(tsmpOpenApiKeyList);
		    importVo.setTsmpOpenApiKeyMapList(tsmpOpenApiKeyMapList);
		    importVo.setTsmpSecurityLevelList(tsmpSecurityLevelList);
		    importVo.setTsmpVgroupAuthoritiesMapList(tsmpVgroupAuthoritiesMapList);
		    importVo.setTsmpVgroupGroupList(tsmpVgroupGroupList);
		    importVo.setTsmpVgroupList(tsmpVgroupList);
		    importVo.setDgrApikeyUseApiList(useApiList);
		    importVo.setDgrRdbConnectionList(dgrRdbConnectionList);
		    
		    //移除create和update的日期和人員和初始版本,減少不要的資訊
		    ignoreUserAndDateAndVersion(importVo);
		    
		    AA1120ExportData exportData = new AA1120ExportData();
		    exportData.setImportClientRelated(importVo);
		    
		    String number = DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.西元年月日時分秒_4).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
		    String fileName = "exportClientRelated_" + number + ".json";
		    
		    resp.setData(exportData);
		    resp.setFileName(fileName);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	public void exportClientRelatedByFile(HttpServletResponse httpResp, AA1120Resp resp) {
		try (OutputStream out = httpResp.getOutputStream();){
			httpResp.setContentType("application/json");
			//httpResp.setContentType("application/octet-stream");
	        String headerKey = "Content-Disposition";
	        String headerValue = "attachment; filename=" + resp.getFileName();//匯出的檔名是前端控制的
	        httpResp.setHeader(headerKey, headerValue);
	        
	        getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, resp.getData());
	       
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	
	private void ignoreUserAndDateAndVersion(AA1120ImportClientRelated importVo) {
		importVo.getDgrGtwIdpInfoAList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
		importVo.getDgrGtwIdpInfoJdbcList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
		importVo.getDgrGtwIdpInfoLList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
		importVo.getDgrGtwIdpInfoOList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
		importVo.getDgrXApiKeyList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
		importVo.getDgrXApiKeyMapList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
		importVo.getDpAppList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1);});
	    importVo.getTsmpClientHostList().forEach(vo ->{vo.setCreateTime(null);});
	    importVo.getTsmpClientList().forEach(vo ->{vo.setCreateTime(null);vo.setCreateUser(null);vo.setUpdateTime(null);vo.setUpdateUser(null);});
	    importVo.getTsmpDpClientextList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
	    importVo.getTsmpGroupApiList().forEach(vo ->{vo.setCreateTime(null);});
	    importVo.getTsmpGroupList().forEach(vo ->{vo.setCreateTime(null);vo.setCreateUser(null);vo.setUpdateTime(null);vo.setUpdateUser(null);});
	    importVo.getTsmpOpenApiKeyList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
	    importVo.getTsmpOpenApiKeyMapList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
	    importVo.getTsmpVgroupGroupList().forEach(vo ->{vo.setCreateTime(null);});
	    importVo.getTsmpVgroupList().forEach(vo ->{vo.setCreateTime(null);vo.setCreateUser(null);vo.setUpdateTime(null);vo.setUpdateUser(null);});
	    importVo.getDgrRdbConnectionList().forEach(vo ->{vo.setCreateDateTime(null);vo.setCreateUser(null);vo.setUpdateDateTime(null);vo.setUpdateUser(null);vo.setVersion(1L);});
	    
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return tsmpVgroupGroupDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected DgrXApiKeyDao getDgrXApiKeyDao() {
		return dgrXApiKeyDao;
	}

	protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
		return dgrXApiKeyMapDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

	protected DpAppDao getDpAppDao() {
		return dpAppDao;
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return tsmpOpenApiKeyDao;
	}

	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return tsmpOpenApiKeyMapDao;
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {
		return dgrGtwIdpInfoADao;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}
	
	
}
