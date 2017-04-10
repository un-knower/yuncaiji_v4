package cn.uway.ucloude.uts.jobtracker.cmd;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifier;

/**
 * 加载作业任务
 * @author uway
 *
 */
public class LoadJobHttpCmd implements HttpCmdProcessor {

	private static final ILogger logger = LoggerManager.getLogger(ClientNotifier.class);

	private JobTrackerContext context;
	
	public LoadJobHttpCmd(JobTrackerContext context){
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
		return HttpCmdNames.HTTP_CMD_LOAD_JOB;
	}

	@Override
	public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
		// TODO Auto-generated method stub
		String taskTrackerNodeGroup = request.getParam("nodeGroup");
        context.getPreLoader().load(taskTrackerNodeGroup);

        logger.info("load job succeed : nodeGroup={}", taskTrackerNodeGroup);

        return HttpCmdResponse.newResponse(true, "load job succeed");
	}

}
