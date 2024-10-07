package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0301Item {

	/** 模組名稱 */
	private AA0301Trunc moduleName;

	/** API ID */
	private AA0301Trunc apiKey;

	/** 狀態 */
	private AA0301Pair apiStatus;

	/** API來源 */
	private AA0301Pair apiSrc;

	/** API名稱 */
	private AA0301Trunc apiName;

	/** API說明 */
	private AA0301Trunc apiDesc;

	/** JWT設定(Request) */
	private AA0301Pair jweFlag;

	/** JWT設定(Response) */
	private AA0301Pair jweFlagResp;

	/** 最近異動日期 */
	private String updateTime;

	/** 組織單位 */
	private AA0301Pair org;

	/** 預定啟用日期 */
	private long enableScheduledDate;

	/** 預定停用日期 */
	private long disableScheduledDate;

	private List<String> labelList;

	public AA0301Trunc getModuleName() {
		return moduleName;
	}

	public void setModuleName(AA0301Trunc moduleName) {
		this.moduleName = moduleName;
	}

	public AA0301Trunc getApiKey() {
		return apiKey;
	}

	public void setApiKey(AA0301Trunc apiKey) {
		this.apiKey = apiKey;
	}

	public AA0301Pair getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(AA0301Pair apiStatus) {
		this.apiStatus = apiStatus;
	}

	public AA0301Pair getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(AA0301Pair apiSrc) {
		this.apiSrc = apiSrc;
	}

	public AA0301Trunc getApiName() {
		return apiName;
	}

	public void setApiName(AA0301Trunc apiName) {
		this.apiName = apiName;
	}

	public AA0301Trunc getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(AA0301Trunc apiDesc) {
		this.apiDesc = apiDesc;
	}

	public AA0301Pair getJweFlag() {
		return jweFlag;
	}

	public void setJweFlag(AA0301Pair jweFlag) {
		this.jweFlag = jweFlag;
	}

	public AA0301Pair getJweFlagResp() {
		return jweFlagResp;
	}

	public void setJweFlagResp(AA0301Pair jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public AA0301Pair getOrg() {
		return org;
	}

	public void setOrg(AA0301Pair org) {
		this.org = org;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public long getEnableScheduledDate() {
		return enableScheduledDate;
	}

	public void setEnableScheduledDate(long enableScheduledDate) {
		this.enableScheduledDate = enableScheduledDate;
	}

	public long getDisableScheduledDate() {
		return disableScheduledDate;
	}

	public void setDisableScheduledDate(long disableScheduledDate) {
		this.disableScheduledDate = disableScheduledDate;
	}

	@Override
	public String toString() {
		return "AA0301Item [moduleName=" + moduleName + ", apiKey=" + apiKey + ", apiStatus=" + apiStatus + ", apiSrc="
				+ apiSrc + ", apiName=" + apiName + ", apiDesc=" + apiDesc + ", jweFlag=" + jweFlag + ", jweFlagResp="
				+ jweFlagResp + ", updateTime=" + updateTime + ", org=" + org + ", enableScheduledDate="
				+ enableScheduledDate + ", disableScheduledDate=" + disableScheduledDate + "]";
	}

}
