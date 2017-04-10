package cn.uway.ucloude.uts.jobtracker.cmd;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifier;

/**
 * 用来手动触发任务
 * 内部做的事情就是将某个任务加载到内存中
 * @author uway
 *
 */
public class TriggerJobManuallyHttpCmd implements HttpCmdProcessor {

	private static final ILogger logger = LoggerManager.getLogger(ClientNotifier.class);

	private JobTrackerContext context;
	
	public TriggerJobManuallyHttpCmd(JobTrackerContext context){
		this.context = context;
	}
	
	@Override
	public String nodeIdentity() {
		// TODO Auto-generated method stub
		return context.getConfiguration().getIdentity();
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return HttpCmdNames.HTTP_CMD_TRIGGER_JOB_MANUALLY;
	}

	@Override
	public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
		// TODO Auto-generated method stub
		String taskTrackerNodeGroup = request.getParam("nodeGroup");
        String jobId = request.getParam("jobId");

        if (StringUtil.isEmpty(taskTrackerNodeGroup)) {
            return HttpCmdResponse.newResponse(true, "nodeGroup should not be empty");
        }

        if (StringUtil.isEmpty(jobId)) {
            return HttpCmdResponse.newResponse(true, "jobId should not be empty");
        }

        context.getPreLoader().loadOne2First(taskTrackerNodeGroup, jobId);

        logger.info("Trigger Job jobId={} taskTrackerNodeGroup={}", jobId, taskTrackerNodeGroup);

        return HttpCmdResponse.newResponse(true, "trigger job succeed");
	}

}
