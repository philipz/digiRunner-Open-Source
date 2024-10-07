package tpi.dgrv4.dpaa.component.job;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.component.nodeTask.DisableDCNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.EnableDCNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.RefreshDCNotifier;
import tpi.dgrv4.dpaa.component.nodeTask.TsmpNodeTaskNotifier;
import tpi.dgrv4.entity.entity.jpql.TsmpDc;
import tpi.dgrv4.entity.entity.jpql.TsmpDcNode;
import tpi.dgrv4.entity.repository.TsmpDcDao;
import tpi.dgrv4.entity.repository.TsmpDcNodeDao;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class NoticeDCEventsJob extends Job {
	
	@Autowired
	private DisableDCNotifier disableDCNotifier;
	
	@Autowired
	private EnableDCNotifier enableDCNotifier;
	
	@Autowired
	private RefreshDCNotifier refreshDCNotifier;
	
	@Autowired
	private TsmpDcDao tsmpDcDao;
	
	@Autowired
	private TsmpDcNodeDao tsmpDcNodeDao;
	
	private TPILogger logger = TPILogger.tl;

	private final Integer action;
	private final Long dcId;
	
	public NoticeDCEventsJob(Integer action, Long dcId) {
		this.action = action;
		this.dcId = dcId;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin NoticeDCEventsJob ---");
			
			//chk param
			
			if (StringUtils.isEmpty(action)){
				this.logger.debug("缺少必填參數 action");
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			if (StringUtils.isEmpty(dcId)){
				this.logger.debug("缺少必填參數 dcId");
				throw TsmpDpAaRtnCode._1296.throwing();
			}
 
			Optional<TsmpDc> opt_dc = getTsmpDcDao().findById(dcId);
			if(!opt_dc.isPresent()) {
				this.logger.debug("查無資料 TsmpDc, dcId: " + dcId);
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			TsmpDc tsmpDc = opt_dc.get();
			String dcCode = tsmpDc.getDcCode();
			if(StringUtils.isEmpty(dcCode)) {
				this.logger.debug("查無資料 dcCode");
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			String taskId = null;
			TsmpNodeTaskNotifier<String> notifier = null;
			if(action == 0) {//停用
				notifier = getDisableDCNotifier();
				 
			}else if(action == 1) {//啟用
				notifier = getEnableDCNotifier();
				
			}else if(action == 2) {//重整
				notifier = getRefreshDCNotifier();
				
			}else {
				this.logger.debug("action 參數錯誤: " + action);
				throw TsmpDpAaRtnCode._1290.throwing();
			}
			
			taskId = notifier.noticePublicEvent(dcCode);
			this.logger.debug("任務編號 TaskId: " + taskId);
			if(StringUtils.isEmpty(taskId)) {
				this.logger.debug("無法取得 TaskId");
				
			}else {
				//傳入任務編號(taskId)，取得TSMP_NODE_TASK.id							
				Long nodeTaskId = notifier.getTsmpNodeTaskId(taskId);
				this.logger.debug("TSMP_NODE_TASK.id: " + nodeTaskId);
				
				if(StringUtils.isEmpty(nodeTaskId)) {
					this.logger.debug("無法取得 TSMP_NODE_TASK.id");
					
				}else {
					//更新至所有與此 dcId 相關的 TSMP_DC_NODE.node_task_id (update TSMP_DC_NODE set NODE_TASK_ID = ? where DC_ID = ?)									
					List<TsmpDcNode> dcNodeList = getTsmpDcNodeDao().findByDcId(dcId);
					for (TsmpDcNode tsmpDcNode : dcNodeList) {
						tsmpDcNode.setNodeTaskId(nodeTaskId);
						tsmpDcNode = getTsmpDcNodeDao().saveAndFlush(tsmpDcNode);
					}
				}
			}
			
		} catch (Exception e) {
			logger.debug("" + e);
		} finally {
			this.logger.debug("--- Finish NoticeDCEventsJob ---");
		}
	}

	protected DisableDCNotifier getDisableDCNotifier() {
		return this.disableDCNotifier;
	}
	
	protected EnableDCNotifier getEnableDCNotifier() {
		return this.enableDCNotifier;
	}
	
	protected RefreshDCNotifier getRefreshDCNotifier() {
		return this.refreshDCNotifier;
	}
	
	protected TsmpDcDao getTsmpDcDao() {
		return this.tsmpDcDao;
	}
	
	protected TsmpDcNodeDao getTsmpDcNodeDao() {
		return this.tsmpDcNodeDao;
	}
}
