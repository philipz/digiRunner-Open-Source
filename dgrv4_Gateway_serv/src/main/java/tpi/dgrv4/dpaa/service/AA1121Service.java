package tpi.dgrv4.dpaa.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.AnalyzeClientRelatedDataStatus;
import tpi.dgrv4.dpaa.vo.AA1120DgrApikeyUseApi;
import tpi.dgrv4.dpaa.vo.AA1120ExportData;
import tpi.dgrv4.dpaa.vo.AA1120ImportClientRelated;
import tpi.dgrv4.dpaa.vo.AA1121Client;
import tpi.dgrv4.dpaa.vo.AA1121Group;
import tpi.dgrv4.dpaa.vo.AA1121GroupAuth;
import tpi.dgrv4.dpaa.vo.AA1121LackApi;
import tpi.dgrv4.dpaa.vo.AA1121RdbConnection;
import tpi.dgrv4.dpaa.vo.AA1121Resp;
import tpi.dgrv4.dpaa.vo.AA1121SecurityLevel;
import tpi.dgrv4.dpaa.vo.AA1121Vgroup;
import tpi.dgrv4.entity.entity.DgrImportClientRelatedTemp;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.DgrImportClientRelatedTempDao;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientHostDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1121Service {
	private TPILogger logger = TPILogger.tl;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TsmpClientDao tsmpClientDao;
	@Autowired
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	@Autowired
	private TsmpGroupDao tsmpGroupDao;
	@Autowired
	private TsmpVgroupDao tsmpVgroupDao;
	@Autowired
	private TsmpGroupApiDao tsmpGroupApiDao;
	@Autowired
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	@Autowired
	private TsmpSecurityLevelDao tsmpSecurityLevelDao;
	@Autowired
	private DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao;
	@Autowired
	private DgrRdbConnectionDao dgrRdbConnectionDao;
	@Autowired
	private TsmpClientHostDao tsmpClientHostDao;
	
	public AA1121Resp importClientRelated(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			
			
			checkParam(mFile);
			
			//將json轉bean
			AA1120ExportData exportData = getExportData(mFile.getInputStream());
			
			//解析資料
			AA1121Resp resp = analyzeData(exportData);
			
			//暫存資料,供給其他API使用,並給resp.longId
			saveTempData(resp, exportData, tsmpAuthorization);
		    
     		return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	private AA1120ExportData getExportData(InputStream inputStream) {
		try (inputStream){
			AA1120ExportData exportData = getObjectMapper().readValue(inputStream, AA1120ExportData.class);
			return exportData;
		}catch(Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1291.throwing();
		}
	}
	
	private AA1121Resp analyzeData(AA1120ExportData exportData)  {
		
		AA1121Resp resp = new AA1121Resp();
		AA1120ImportClientRelated importVo = exportData.getImportClientRelated();
		resp.setClientList(this.getClientList(importVo.getTsmpClientList(), importVo.getTsmpClientHostList()));
		resp.setGroupAuthList(this.getGroupAuthList(importVo.getTsmpGroupAuthoritiesList()));
		resp.setGroupList(this.getGroupList(importVo.getTsmpGroupList()));
		resp.setLackApiList(this.getLackApiList(importVo));
		resp.setSecurityLevelList(this.getSecurityLevelList(importVo.getTsmpSecurityLevelList()));
		resp.setVgroupList(this.getVgroupList(importVo, resp.getLackApiList()));
		resp.setRdbConnectionList(this.getRdbConnectionList(importVo.getDgrRdbConnectionList()));

		return resp;
		
	}
	
	private void saveTempData(AA1121Resp resp, AA1120ExportData exportData, TsmpAuthorization tsmpAuthorization) throws Exception{
		byte[] arrImport = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData.getImportClientRelated());
		byte[] arrAnalyze = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(resp);
		DgrImportClientRelatedTemp tempVo = new DgrImportClientRelatedTemp();
		tempVo.setAnalyzeClientRelated(arrAnalyze);
		tempVo.setImportClientRelated(arrImport);
		tempVo.setCreateUser(tsmpAuthorization.getUserName());
		tempVo = getDgrImportClientRelatedTempDao().save(tempVo);
		
		resp.setLongId(tempVo.getTempId().toString());
	}
	
	private List<AA1121LackApi> getLackApiList(AA1120ImportClientRelated importVo) {
		List<AA1121LackApi> lackApiList = new ArrayList<>();
		//tsmpGroupApi
		List<TsmpGroupApi> tsmpGroupApiList = importVo.getTsmpGroupApiList();
		tsmpGroupApiList.forEach(vo->{
			Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equals(vo.getModuleName()) 
					&& f.getApiId().equals(vo.getApiKey())).findAny();
			if(lackApiOpt.isEmpty()) {
				TsmpApiId idVo = new TsmpApiId(vo.getApiKey(), vo.getModuleName());
				boolean isExists = getTsmpApiCacheProxy().existsById(idVo);
				if(!isExists) {
					AA1121LackApi lackApiVo = new AA1121LackApi();
					lackApiVo.setApiId(vo.getApiKey());
					lackApiVo.setModuleName(vo.getModuleName());
					lackApiList.add(lackApiVo);
				}
			}
		});
		
		List<AA1120DgrApikeyUseApi> dgrApikeyUseApiList = importVo.getDgrApikeyUseApiList();
		
		for(AA1120DgrApikeyUseApi vo :dgrApikeyUseApiList){
			Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equals(vo.getModuleName()) 
																			&& f.getApiId().equals(vo.getApiId())).findAny();
			if(lackApiOpt.isPresent()) {
				continue;
			}
			TsmpApiId idVo = new TsmpApiId(vo.getApiId(), vo.getModuleName());
			boolean isExists = getTsmpApiCacheProxy().existsById(idVo);
			if(!isExists) {
				AA1121LackApi lackApiVo = new AA1121LackApi();
				lackApiVo.setApiId(vo.getApiId());
				lackApiVo.setModuleName(vo.getModuleName());
				lackApiList.add(lackApiVo);
			}
		}
		
		lackApiList.sort(new Comparator<AA1121LackApi>() {
			@Override
			public int compare(AA1121LackApi o1, AA1121LackApi o2) {
				return o1.getModuleName().compareToIgnoreCase(o2.getModuleName());
			}
		});
		
		return lackApiList;
		
	}
	
	private List<AA1121Client> getClientList(List<TsmpClient> tsmpClientList, List<TsmpClientHost> tsmpClientHostList) {
		List<AA1121Client> retList = new ArrayList<>();
		tsmpClientList.forEach(vo ->{
			AA1121Client clientVo = new AA1121Client();
			clientVo.setClientAlias(vo.getClientAlias());
			clientVo.setClientId(vo.getClientId());
			clientVo.setClientName(vo.getClientName());
			
			//設定資料異動狀態(dataStatus)
			TsmpClient dbClientVo = getTsmpClientDao().findById(vo.getClientId()).orElse(null);
			if(dbClientVo != null) {//clientId存在
				//查看clientName和clientAlias存不存在
				boolean isClientNameExist = getTsmpClientDao().existsByClientNameAndClientIdNot(vo.getClientName(), vo.getClientId());
				boolean isClientAliasExist = false;
				if(!isClientNameExist && StringUtils.hasLength(vo.getClientAlias())) {
					isClientAliasExist = getTsmpClientDao().existsByClientAliasAndClientIdNot(vo.getClientAlias(), vo.getClientId());
				}
				if(isClientNameExist || isClientAliasExist || !vo.getSecurityLevelId().equalsIgnoreCase(dbClientVo.getSecurityLevelId())) {
					this.logger.info("isClientIdExist=true"+", id="+vo.getClientId());
					this.logger.info("isClientNameExist="+isClientNameExist+", name="+vo.getClientName());
					this.logger.info("isClientAliasExist="+isClientAliasExist+", alias="+vo.getClientAlias());
					this.logger.info("importSecurityLevel="+vo.getSecurityLevelId()+", dbSecurityLevel="+dbClientVo.getSecurityLevelId());
					clientVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
				}else {
					List<TsmpClientHost> importHostList = tsmpClientHostList.stream().filter(f->vo.getClientId().equalsIgnoreCase(f.getClientId())).collect(Collectors.toList());
					if(!CollectionUtils.isEmpty(importHostList)) {
						boolean isR = true;
						List<TsmpClientHost> dbHostList = this.getTsmpClientHostDao().findByClientId(vo.getClientId());
						if(!CollectionUtils.isEmpty(dbHostList)) {
							for(TsmpClientHost importHostVo: importHostList) {
								List<TsmpClientHost> filterDbHostList = dbHostList.stream().filter(f->importHostVo.getHostIp().equalsIgnoreCase(f.getHostIp()) 
										|| importHostVo.getHostName().equalsIgnoreCase(f.getHostName())).collect(Collectors.toList());
								if(filterDbHostList.size() >= 2) {
									isR = false;
									this.logger.info("isClientIdExist=true"+", id="+vo.getClientId());
									this.logger.info("importHostVo="+importHostVo);
									this.logger.info("filterDbHostList="+filterDbHostList);
									break;
								}
							}
						}
						if(isR) {
							clientVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());
						}else {
							clientVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
						}
					}else {
						clientVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());
					}
				}
			}else {//clientId不存在
				//查看clientName和clientAlias存不存在
				boolean isClientNameExist = getTsmpClientDao().existsByClientName(vo.getClientName());
				boolean isClientAliasExist = false;
				if(!isClientNameExist && StringUtils.hasLength(vo.getClientAlias())) {
					isClientAliasExist = getTsmpClientDao().existsByClientAlias(vo.getClientAlias());
				}
				if(isClientNameExist || isClientAliasExist) {
					this.logger.info("isClientIdExist=false"+", id="+vo.getClientId());
					this.logger.info("isClientNameExist="+isClientNameExist+", name="+vo.getClientName());
					this.logger.info("isClientAliasExist="+isClientAliasExist+", alias="+vo.getClientAlias());
					clientVo.setDataStatus(AnalyzeClientRelatedDataStatus.CA.name());
				}else {
					clientVo.setDataStatus(AnalyzeClientRelatedDataStatus.A.name());
				}
			}
			retList.add(clientVo);
		});
		
		retList.sort(new Comparator<AA1121Client>() {
			@Override
			public int compare(AA1121Client o1, AA1121Client o2) {
				return o1.getDataStatus().compareTo(o2.getDataStatus());
			}
		});
		
		return retList;
	}
	
	private List<AA1121Group> getGroupList(List<TsmpGroup> tsmpGroupList) {
		List<AA1121Group> retList = new ArrayList<>();
		tsmpGroupList = tsmpGroupList.stream().filter(f->"0".equals(f.getVgroupFlag())).collect(Collectors.toList());
		tsmpGroupList.forEach(vo ->{
			AA1121Group groupVo = new AA1121Group();
			groupVo.setGroupAlias(vo.getGroupAlias());
			groupVo.setGroupName(vo.getGroupName());
			
			//設定資料異動狀態(dataStatus)
			boolean isGroupNameExist = getTsmpGroupDao().existsByGroupNameAndVgroupFlag(vo.getGroupName(), vo.getVgroupFlag());
			if(isGroupNameExist) {
				boolean isGroupAliasExist = false;
				if(StringUtils.hasLength(vo.getGroupAlias())) {
					isGroupAliasExist = getTsmpGroupDao().existsByGroupAliasAndVgroupFlagAndGroupNameNot(vo.getGroupAlias(), vo.getVgroupFlag(), vo.getGroupName());
				}
				
				if(isGroupAliasExist) {
					this.logger.info("isGroupNameExist="+isGroupNameExist+", name="+vo.getGroupName());
					this.logger.info("isGroupAliasExist="+isGroupAliasExist+", alias="+vo.getGroupAlias());
					groupVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
				}else {
					groupVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());
				}
			}else {
				boolean isGroupAliasExist = false;
				if(StringUtils.hasLength(vo.getGroupAlias())) {
					isGroupAliasExist = getTsmpGroupDao().existsByGroupAliasAndVgroupFlag(vo.getGroupAlias(), vo.getVgroupFlag());
				}
				
				if(isGroupAliasExist) {
					this.logger.info("isGroupNameExist="+isGroupNameExist+", name="+vo.getGroupName());
					this.logger.info("isGroupAliasExist="+isGroupAliasExist+", alias="+vo.getGroupAlias());
					groupVo.setDataStatus(AnalyzeClientRelatedDataStatus.CA.name());
				}else {
					groupVo.setDataStatus(AnalyzeClientRelatedDataStatus.A.name());
				}
			}
			
			retList.add(groupVo);
		});
		
		retList.sort(new Comparator<AA1121Group>() {
			@Override
			public int compare(AA1121Group o1, AA1121Group o2) {
				return o1.getDataStatus().compareTo(o2.getDataStatus());
			}
		});
		
		return retList;
	}
	
	private List<AA1121Vgroup> getVgroupList(AA1120ImportClientRelated importVo, List<AA1121LackApi> lackApiList) {
		List<AA1121Vgroup> retList = new ArrayList<>();
		List<TsmpVgroup> tsmpVgroupList = importVo.getTsmpVgroupList();
		tsmpVgroupList.forEach(vo ->{
			AA1121Vgroup vgroupVo = new AA1121Vgroup();
			vgroupVo.setVgroupAlias(vo.getVgroupAlias());
			vgroupVo.setVgroupName(vo.getVgroupName());
			
			//設定資料異動狀態(dataStatus)
			boolean isVgroupNameExist = getTsmpVgroupDao().existsByVgroupName(vo.getVgroupName());
			if(isVgroupNameExist) {
				boolean isVgroupAliasExist = false;
				if(StringUtils.hasLength(vo.getVgroupAlias())) {
					isVgroupAliasExist = getTsmpVgroupDao().existsByVgroupAliasAndVgroupNameNot(vo.getVgroupAlias(), vo.getVgroupName());
				}
				
				if(isVgroupAliasExist) {
					vgroupVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
					this.logger.info("isVgroupNameExist="+isVgroupNameExist+", name="+vo.getVgroupName());
					this.logger.info("isVgroupAliasExist="+isVgroupAliasExist+", alias="+vo.getVgroupAlias());
				}else {
					//檢查API是否會超過186支
					List<String> groupIdList = importVo.getTsmpVgroupGroupList().stream().filter(f->f.getVgroupId().equals(vo.getVgroupId()))
												.map(obj->obj.getGroupId()).collect(Collectors.toList());
					List<TsmpGroupApi> groupApiList = importVo.getTsmpGroupApiList().stream().filter(f->groupIdList.contains(f.getGroupId())).collect(Collectors.toList());
					TsmpVgroup dbVgroupVo = getTsmpVgroupDao().findFirstByVgroupName(vo.getVgroupName());
					List<TsmpGroupApi> dbGroupApiList = getTsmpGroupApiDao().query_aa0237Service(null, dbVgroupVo.getVgroupId(), null, null, null);
					int importSize = 0;
					for(TsmpGroupApi importApiVo : groupApiList) {
						boolean isIgnore = false;
						for(TsmpGroupApi dbApiVo : dbGroupApiList) {
							if(importApiVo.getModuleName().equalsIgnoreCase(dbApiVo.getModuleName()) && importApiVo.getApiKey().equalsIgnoreCase(dbApiVo.getApiKey())){
								isIgnore = true;
								continue;
							}
						}
						if(!isIgnore) {
							for(AA1121LackApi lackApiVo : lackApiList) {
								if(importApiVo.getModuleName().equalsIgnoreCase(lackApiVo.getModuleName()) && importApiVo.getApiKey().equalsIgnoreCase(lackApiVo.getApiId())){
									isIgnore = true;
									continue;
								}
							}
							if(!isIgnore) {
								importSize++;
							}
						}
					}
					//oauth_client_details.scope長度為2048,id會有10碼所以186個=1860+185(逗號)=2045
					if((dbGroupApiList.size() + importSize) > 186) {
						this.logger.info("Exceed 186 api, vgroupName is " + vgroupVo.getVgroupName());
						vgroupVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
					}else {
						vgroupVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());
					}

				}
			}else {
				boolean isVgroupAliasExist = false;
				if(StringUtils.hasLength(vo.getVgroupAlias())) {
					isVgroupAliasExist = getTsmpVgroupDao().existsByVgroupAlias(vo.getVgroupAlias());
				}
				
				if(isVgroupAliasExist) {
					this.logger.info("isVgroupNameExist="+isVgroupNameExist+", name="+vo.getVgroupName());
					this.logger.info("isVgroupAliasExist="+isVgroupAliasExist+", alias="+vo.getVgroupAlias());
					vgroupVo.setDataStatus(AnalyzeClientRelatedDataStatus.CA.name());
				}else {
					vgroupVo.setDataStatus(AnalyzeClientRelatedDataStatus.A.name());
				}
			}
			
			retList.add(vgroupVo);
		});
		
		retList.sort(new Comparator<AA1121Vgroup>() {
			@Override
			public int compare(AA1121Vgroup o1, AA1121Vgroup o2) {
				return o1.getDataStatus().compareTo(o2.getDataStatus());
			}
		});
		
		return retList;
	}
	
	private List<AA1121GroupAuth> getGroupAuthList(List<TsmpGroupAuthorities> groupAuthList) {
		List<AA1121GroupAuth> retList = new ArrayList<>();
		groupAuthList.forEach(vo ->{
			AA1121GroupAuth authVo = new AA1121GroupAuth();
			authVo.setGroupAuthId(vo.getGroupAuthoritieId());
			authVo.setGroupAuthName(vo.getGroupAuthoritieName());
			
			//設定資料異動狀態(dataStatus)
			boolean isGroupAuthIdExist = getTsmpGroupAuthoritiesDao().existsByGroupAuthoritieId(vo.getGroupAuthoritieId());
			if(isGroupAuthIdExist) {
				boolean isGroupAuthNameExist = getTsmpGroupAuthoritiesDao().existsByGroupAuthoritieNameAndGroupAuthoritieIdNot(vo.getGroupAuthoritieName(), vo.getGroupAuthoritieId());
				
				if(isGroupAuthNameExist) {
					this.logger.info("isGroupAuthIdExist="+isGroupAuthIdExist+", id="+vo.getGroupAuthoritieId());
					this.logger.info("isGroupAuthNameExist="+isGroupAuthNameExist+", name="+vo.getGroupAuthoritieName());
					authVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
				}else {
					authVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());
				}
			}else {
				boolean isGroupAuthNameExist = getTsmpGroupAuthoritiesDao().existsByGroupAuthoritieName(vo.getGroupAuthoritieName());
				
				if(isGroupAuthNameExist) {
					this.logger.info("isGroupAuthIdExist="+isGroupAuthIdExist+", id="+vo.getGroupAuthoritieId());
					this.logger.info("isGroupAuthNameExist="+isGroupAuthNameExist+", name="+vo.getGroupAuthoritieName());
					authVo.setDataStatus(AnalyzeClientRelatedDataStatus.CA.name());
				}else {
					authVo.setDataStatus(AnalyzeClientRelatedDataStatus.A.name());
				}
			}
			
			retList.add(authVo);
		});
		
		retList.sort(new Comparator<AA1121GroupAuth>() {
			@Override
			public int compare(AA1121GroupAuth o1, AA1121GroupAuth o2) {
				return o1.getDataStatus().compareTo(o2.getDataStatus());
			}
		});
		
		return retList;
	}

	private List<AA1121SecurityLevel> getSecurityLevelList(List<TsmpSecurityLevel> slList) {
		List<AA1121SecurityLevel> retList = new ArrayList<>();
		slList.forEach(vo ->{
			AA1121SecurityLevel slVo = new AA1121SecurityLevel();
			slVo.setSecurityLevelId(vo.getSecurityLevelId());
			slVo.setSecurityLevelName(vo.getSecurityLevelName());
			
			//設定資料異動狀態(dataStatus)
			boolean isSecurityLevelIdExist = getTsmpSecurityLevelDao().existsBySecurityLevelId(vo.getSecurityLevelId());
			if(isSecurityLevelIdExist) {
				boolean isSecurityLevelNameExist = getTsmpSecurityLevelDao().existsBySecurityLevelNameAndSecurityLevelIdNot(vo.getSecurityLevelName(), vo.getSecurityLevelId());
				
				if(isSecurityLevelNameExist) {
					this.logger.info("isSecurityLevelIdExist="+isSecurityLevelIdExist+", id="+vo.getSecurityLevelId());
					this.logger.info("isSecurityLevelNameExist="+isSecurityLevelNameExist+", name="+vo.getSecurityLevelName());
					slVo.setDataStatus(AnalyzeClientRelatedDataStatus.CR.name());
				}else {
					slVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());
				}
			}else {
				boolean isSecurityLevelNameExist = getTsmpSecurityLevelDao().existsBySecurityLevelName(vo.getSecurityLevelName());
				
				if(isSecurityLevelNameExist) {
					this.logger.info("isSecurityLevelIdExist="+isSecurityLevelIdExist+", id="+vo.getSecurityLevelId());
					this.logger.info("isSecurityLevelNameExist="+isSecurityLevelNameExist+", name="+vo.getSecurityLevelName());
					slVo.setDataStatus(AnalyzeClientRelatedDataStatus.CA.name());
				}else {
					slVo.setDataStatus(AnalyzeClientRelatedDataStatus.A.name());
				}
			}
			
			retList.add(slVo);
		});
		
		retList.sort(new Comparator<AA1121SecurityLevel>() {
			@Override
			public int compare(AA1121SecurityLevel o1, AA1121SecurityLevel o2) {
				return o1.getDataStatus().compareTo(o2.getDataStatus());
			}
		});
		
		return retList;
	}
	
	private List<AA1121RdbConnection> getRdbConnectionList(List<DgrRdbConnection> rdbList) {
		List<AA1121RdbConnection> retList = new ArrayList<>();
		rdbList.forEach(vo ->{
			AA1121RdbConnection rdbVo = new AA1121RdbConnection();
			rdbVo.setConnectionName(vo.getConnectionName());
			
			//設定資料異動狀態(dataStatus)
			boolean isConnectionNameExist = getDgrRdbConnectionDao().existsById(vo.getConnectionName());
			if(isConnectionNameExist) {
				rdbVo.setDataStatus(AnalyzeClientRelatedDataStatus.R.name());

			}else {
				rdbVo.setDataStatus(AnalyzeClientRelatedDataStatus.A.name());
			}
			
			retList.add(rdbVo);
		});
		
		retList.sort(new Comparator<AA1121RdbConnection>() {
			@Override
			public int compare(AA1121RdbConnection o1, AA1121RdbConnection o2) {
				return o1.getDataStatus().compareTo(o2.getDataStatus());
			}
		});
		
		return retList;
	}
	
	private void checkParam(MultipartFile mFile) {
		
		if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
			throw TsmpDpAaRtnCode._1233.throwing();
		}
		
		String fileName = mFile.getOriginalFilename();
		int fileNameIndex = fileName.lastIndexOf(".");
		String fileExtension = fileName.substring(fileNameIndex + 1);
		if(!"json".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

	protected DgrImportClientRelatedTempDao getDgrImportClientRelatedTempDao() {
		return dgrImportClientRelatedTempDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}

	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}


}
