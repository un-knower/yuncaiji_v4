package cn.uway.ucloude.uts.tasktracker.cmd;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

public class JobTerminateCmd implements HttpCmdProcessor {

	private TaskTrackerContext context;
	
	
	
	public JobTerminateCmd(TaskTrackerContext context) {
		super();
		this.context = context;
	}

	@Override
	public String nodeIdentity() {
		// TODO Auto-generated method stub
		return this.context.getConfiguration().getIdentity();
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return HttpCmdNames.HTTP_CMD_JOB_TERMINATE;
	}

	@Override
	public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
		// TODO Auto-generated method stub
		 String jobId = request.getParam("jobId");
	        if (StringUtil.isEmpty(jobId)) {
	            return HttpCmdResponse.newResponse(false, "jobId can't be empty");
	        }

	        if (!context.getRunnerPool().getRunningJobManager().running(jobId)) {
	            return HttpCmdResponse.newResponse(false, "jobId dose not running in this TaskTracker now");
	        }

	        context.getRunnerPool().getRunningJobManager().terminateJob(jobId);

	        return HttpCmdResponse.newResponse(true, "Execute terminate Command success");
	}

}
