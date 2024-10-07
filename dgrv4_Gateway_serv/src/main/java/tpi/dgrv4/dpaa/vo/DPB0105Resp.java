package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0105Resp {

	/** 週期排程UID */
	private String apptRjobId;

	/** 週期排程Version */
	private Long lv;

	/** 排程表達式 */
	private String cronExpression;

	/** 狀態代碼 */
	private String status;

	/** 狀態名稱 */
	private String statusName;
	
	/** 下次執行時間	yyyy/MM/dd HH:mm:ss */
	private String nextDateTime;

	/** 排程項目ID */
	private List<Map<Long, Long>> apptRjobDIds;

	public String getApptRjobId() {
		return apptRjobId;
	}
	
	public Long getLv() {
		return lv;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public void setNextDateTime(String nextDateTime) {
		this.nextDateTime = nextDateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	
	public String getNextDateTime() {
		return nextDateTime;
	}

	public List<Map<Long, Long>> getApptRjobDIds() {
		return apptRjobDIds;
	}

	public void setApptRjobDIds(List<Map<Long, Long>> apptRjobDIds) {
		this.apptRjobDIds = apptRjobDIds;
	}

}