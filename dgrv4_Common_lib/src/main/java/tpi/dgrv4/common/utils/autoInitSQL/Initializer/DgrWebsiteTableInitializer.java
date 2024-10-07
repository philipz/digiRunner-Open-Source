package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.DgrWebsiteVo;

@Service
public class DgrWebsiteTableInitializer {

	private List<DgrWebsiteVo> dgrWebsiteVolist = new LinkedList<>();

	public List<DgrWebsiteVo> insertDgrWebsite() {
		try {

			createDgrWebsiteVo(1L, "Y", "dp_api", "The API for the Developer Portal");
			createDgrWebsiteVo(2L, "Y", "dp", "The HTML for the Developer Portal");

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}

		return dgrWebsiteVolist;
	}


	protected void createDgrWebsiteVo(Long dgrWebsiteId, String websiteStatus, String websiteName, String remark) {
		DgrWebsiteVo dgrWebsiteVo = new DgrWebsiteVo();
		dgrWebsiteVo.setDgrWebsiteId(dgrWebsiteId);
		dgrWebsiteVo.setWebsiteStatus(websiteStatus);
		dgrWebsiteVo.setWebsiteName(websiteName);
		dgrWebsiteVo.setRemark(remark);
		dgrWebsiteVolist.add(dgrWebsiteVo);
	}

}
