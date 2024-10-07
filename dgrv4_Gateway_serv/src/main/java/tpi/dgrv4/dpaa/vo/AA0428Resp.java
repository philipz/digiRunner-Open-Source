package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class AA0428Resp {
	/** 排序設定	*/
	private Map<String, String> sortBy;
	
	/** 資料清單	*/
	private List<AA0428Item> dataList;

	public Map<String, String> getSortBy() {
		return sortBy;
	}

	public void setSortBy(Map<String, String> sortBy) {
		this.sortBy = sortBy;
	}

	public List<AA0428Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA0428Item> dataList) {
		this.dataList = dataList;
	}
}
