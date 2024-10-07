package tpi.dgrv4.dpaa.service;

import java.io.InputStream;
import java.util.Iterator;

import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0118Resp;
import tpi.dgrv4.dpaa.vo.DPB9922Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ComposerWebSocketClientConn;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9922Service {
	private TPILogger logger = TPILogger.tl;

	@Autowired
	private TsmpSettingDao tsmpSettingDao;
	
	@Autowired
	private DPB0118Service dpb0118Service;
	
	@Autowired
    private ComposerWebSocketClientConn composerWebSocketClientConn;
	
	@Transactional
	public DPB9922Resp importTsmpSetting(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
			
			checkParam(mFile.getOriginalFilename());

     		importData(mFile.getInputStream());
		    
     		return new DPB9922Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	protected void importData(InputStream inputStream) throws Exception {
		try (Workbook workbook = new XSSFWorkbook(inputStream);){
	
			 Sheet sheet = workbook.getSheetAt(0);
			 boolean isFirst = true;
			 DataFormatter formatter = new DataFormatter();
		     Iterator<Row> rows = sheet.iterator();
		     while (rows.hasNext()) {
		    	 if(isFirst) {
		    		 isFirst = false;
		    		 Row row = rows.next();
		    		 if(!"id".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"value".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"memo".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 Row row = rows.next();
		    		 TsmpSetting vo = new TsmpSetting();
		    		 
		    		 vo.setId(formatter.formatCellValue(row.getCell(0)));
		    		 vo.setValue(formatter.formatCellValue(row.getCell(1)));
		    		 vo.setMemo(formatter.formatCellValue(row.getCell(2)));

		    		 getTsmpSettingDao().saveAndFlush(vo);
		    		 
		    		//因為composer address被更新,websocket要重連
		 			if("TSMP_COMPOSER_ADDRESS".equals(vo.getId())) {
		 				restartWs();
		 			}
		    	 }

		     }
		    
		}
	}
	
	protected void restartWs() {
		getComposerWebSocketClientConn().restart();
	}
	
	protected void checkParam(String fileName) {
		
		int fileNameIndex = fileName.lastIndexOf(".");
		if(fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}
		
		String fileExtension = fileName.substring(fileNameIndex + 1);
		if(!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
		
		String[] arrFileName = fileName.substring(0, fileNameIndex).split("_");
		if(!(arrFileName.length == 4 || arrFileName.length == 3)) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}
		
		DPB0118Resp dpb0118Resp = getDpb0118Service().queryModuleVersion();
        String version = dpb0118Resp.getMajorVersionNo() == null ? "unknown" : dpb0118Resp.getMajorVersionNo();
        if(!version.equalsIgnoreCase(arrFileName[arrFileName.length-1])) {
        	throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
        }
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected DPB0118Service getDpb0118Service() {
		return dpb0118Service;
	}

	protected ComposerWebSocketClientConn getComposerWebSocketClientConn() {
		return composerWebSocketClientConn;
	}
	
	

}
