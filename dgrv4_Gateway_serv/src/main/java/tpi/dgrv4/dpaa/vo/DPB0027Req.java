package tpi.dgrv4.dpaa.vo;

public class DPB0027Req {

	/** PK	分頁使用 */
	private Long questionId;

	/** PK	分頁使用 */
	private Integer dataSort;

	/** 模糊搜尋	每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;

	/** 1：啟用，0：停用 （預設啟用），""：全部 */
	private String dataStatus;

	public DPB0027Req() {}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
}
