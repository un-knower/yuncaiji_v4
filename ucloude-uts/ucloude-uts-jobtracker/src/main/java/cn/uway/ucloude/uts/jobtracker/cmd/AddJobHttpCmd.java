package cn.uway.ucloude.uts.jobtracker.cmd;

import java.util.Collections;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.protocol.command.JobSubmitRequest;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifier;

/**
 * 添加任务
 * @author uway
 *
 */
public class AddJobHttpCmd implements HttpCmdProcessor {
	private static final ILogger logger = LoggerManager.getLogger(ClientNotifier.class);

	private JobTrackerContext context;

	public AddJobHttpCmd(JobTrackerContext context) {
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
		return HttpCmdNames.HTTP_CMD_ADD_JOB;
	}

	@Override
	public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
		// TODO Auto-generated method stub
		HttpCmdResponse response = new HttpCmdResponse();
		response.setSuccess(false);
		String jobJSON = request.getParam("job");
		if (StringUtil.isEmpty(jobJSON)) {
			response.setMsg("job can not be null");
			return response;
		}
		try {
			Job job = JsonConvert.deserialize(jobJSON, Job.class);
			if (job == null) {
				response.setMsg("job can not be null");
				return response;
			}

			if (job.isNeedFeedback() && StringUtil.isEmpty(job.getSubmitNodeGroup())) {
				response.setMsg("if needFeedback, job.SubmitNodeGroup can not be null");
				return response;
			}

			job.checkField();

			JobSubmitRequest jobSubmitRequest = new JobSubmitRequest();
			jobSubmitRequest.setJobs(Collections.singletonList(job));
			context.getJobReceiver().receive(jobSubmitRequest);

			logger.info("add job succeed, {}", job);

			response.setSuccess(true);

		} catch (Exception e) {
			logger.error("add job error, message:", e);
			response.setMsg("add job error, message:" + e.getMessage());
		}
		return response;

	}

}
