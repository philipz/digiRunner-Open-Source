package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class DgrWebsiteVo {

	private Long dgrWebsiteId;

	private String websiteName;

	private String remark;
	
	private String websiteStatus;

	@Override
	public String toString() {
		return "DgrWebsite [dgrWebsiteId=" + dgrWebsiteId + ", websiteName=" + websiteName + ", websiteStatus= " + websiteStatus +", remark=" + remark + "]\n";
	}

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

	public String getWebsiteName() {
		return websiteName;
	}

	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWebsiteStatus() {
		return websiteStatus;
	}

	public void setWebsiteStatus(String websiteStatus) {
		this.websiteStatus = websiteStatus;
	}
	

	
}
