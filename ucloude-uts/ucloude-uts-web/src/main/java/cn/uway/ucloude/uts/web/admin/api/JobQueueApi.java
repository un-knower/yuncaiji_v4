package cn.uway.ucloude.uts.web.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.uway.ucloude.cmd.DefaultHttpCmd;
import cn.uway.ucloude.cmd.HttpCmd;
import cn.uway.ucloude.cmd.HttpCmdClient;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.common.Pair;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.JobLoggerRequest;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.domain.JobType;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;
import cn.uway.ucloude.uts.core.support.CronExpression;
import cn.uway.ucloude.uts.web.access.domain.ShopItem;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.support.Builder;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;
import cn.uway.ucloude.uts.web.support.AppConfigurer;
import cn.uway.ucloude.uts.web.support.I18nManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author magic.s.g.xie
 */
/**
 * @author Uway-M3
 *
 */
/**
 * @author Uway-M3
 *
 */
@RestController
public class JobQueueApi extends AbstractMVC {

	@Autowired
	private BackendAppContext appContext;

	@RequestMapping("/job-queue/executable-job-get")
	public @ResponseBody Pagination<JobPo> executableJobGet(JobQueueReq request) {
		Pagination<JobPo> paginationRsp = appContext.getExecutableJobQueue().pageSelect(request);

		boolean needClear = Boolean
				.valueOf(AppConfigurer.getProperty("uts.admin.remove.running.job.on.executable.search", "false"));
		if (needClear) {
			paginationRsp = clearRunningJob(paginationRsp);
		}

		return paginationRsp;
	}

	@RequestMapping("/job-queue/executable-job-getById")
	public RestfulResponse executableJobGetById(String jobId) {
		Assert.hasLength(jobId, "jobId不能为空!");
		JobPo jobInfo = appContext.getExecutableJobQueue().getJob(jobId);
		RestfulResponse result = new RestfulResponse();
		if (jobInfo != null) {
			result.setSuccess(true);
			result.setMsg("获取成功");
			List<JobPo> rows = new ArrayList<JobPo>();
			rows.add(jobInfo);
			result.setRows(rows);
		} else {
			result.setSuccess(false);
			result.setMsg("无效的JobId");
		}
		return result;
	}

	/**
	 * 比较恶心的逻辑,当等待执行队列的任务同时也在执行中队列, 则不展示
	 */
	private Pagination<JobPo> clearRunningJob(Pagination<JobPo> paginationRsp) {
		if (paginationRsp == null || paginationRsp.getTotal() == 0) {
			return paginationRsp;
		}
		Pagination<JobPo> rsp = new Pagination<JobPo>();
		List<JobPo> rows = new ArrayList<JobPo>();
		for (JobPo jobPo : paginationRsp.getData()) {
			if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
				// 没有正在执行, 则显示在等待执行列表中
				rows.add(jobPo);
			}
		}
		rsp.setData(rows);
		rsp.setTotal(paginationRsp.getTotal() - paginationRsp.getData().size() - rows.size());
		return rsp;
	}

	@RequestMapping("/job-queue/executing-job-trigger")
	public RestfulResponse triggerJobManually(JobQueueReq request) {

		try {
			Assert.hasLength(request.getJobId(), "jobId不能为空!");
			Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
		} catch (IllegalArgumentException e) {
			return Builder.build(false, e.getMessage());
		}

		HttpCmd httpCmd = new DefaultHttpCmd();
		httpCmd.setCommand(HttpCmdNames.HTTP_CMD_TRIGGER_JOB_MANUALLY);
		httpCmd.addParam("jobId", request.getJobId());
		httpCmd.addParam("nodeGroup", request.getTaskTrackerNodeGroup());

		List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.JOB_TRACKER);
		if (CollectionUtil.isEmpty(jobTrackerNodeList)) {
			return Builder.build(false, I18nManager.getMessage("job.tracker.not.found"));
		}

		HttpCmdResponse response = null;
		for (Node node : jobTrackerNodeList) {
			httpCmd.setNodeIdentity(node.getIdentity());
			response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
			if (response.isSuccess()) {
				return Builder.build(true);
			}
		}
		if (response != null) {
			return Builder.build(false, response.getMsg());
		} else {
			return Builder.build(false, "TriggerFailed failed");
		}
	}

	@RequestMapping("/job-queue/executing-job-get")
	public @ResponseBody Pagination<JobPo> executingJobGet(JobQueueReq request) {
		Pagination<JobPo> paginationRsp = appContext.getExecutingJobQueue().pageSelect(request);

		return paginationRsp;
	}
	
	@RequestMapping("/job-queue/executing-job-getById")
	public RestfulResponse executingJobGetById(String jobId) {
		Assert.hasLength(jobId, "jobId不能为空!");
		JobPo jobInfo = appContext.getExecutingJobQueue().getJob(jobId);
		RestfulResponse result = new RestfulResponse();
		if (jobInfo != null) {
			result.setSuccess(true);
			result.setMsg("获取成功");
			List<JobPo> rows = new ArrayList<JobPo>();
			rows.add(jobInfo);
			result.setRows(rows);
		} else {
			result.setSuccess(false);
			result.setMsg("无效的JobId");
		}
		return result;
	}

	@RequestMapping("/job-queue/executable-job-update")
	public RestfulResponse executableJobUpdate(JobQueueReq request) {
		// 检查参数
		// 1. 检测 cronExpression是否是正确的
		if (StringUtil.isNotEmpty(request.getCronExpression())) {
			try {
				CronExpression expression = new CronExpression(request.getCronExpression());
				if (expression.getTimeAfter(new Date()) == null) {
					return Builder.build(false,
							StringUtil.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
				}
			} catch (ParseException e) {
				return Builder.build(false, "请输入正确的 CronExpression!");
			}
		}
		try {
			Assert.hasLength(request.getJobId(), "jobId不能为空!");
			Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
		} catch (IllegalArgumentException e) {
			return Builder.build(false, e.getMessage());
		}
		boolean success = appContext.getExecutableJobQueue().selectiveUpdateByJobId(request);
		RestfulResponse response = new RestfulResponse();
		if (success) {
			response.setSuccess(true);
		} else {
			response.setSuccess(false);
			response.setCode("DELETE_OR_RUNNING");
		}
		return response;
	}

	@RequestMapping("/job-queue/executable-job-delete")
	public RestfulResponse executableJobDelete(JobQueueReq request) {
		try {
			Assert.hasLength(request.getJobId(), "jobId不能为空!");
			Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
		} catch (IllegalArgumentException e) {
			return Builder.build(false, e.getMessage());
		}

		boolean success = appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(),
				request.getJobId());
		if (success) {
			if (StringUtil.isNotEmpty(request.getCronExpression())) {
				// 是Cron任务, Cron任务队列的也要被删除
				try {
					appContext.getCronJobQueue().remove(request.getJobId());
				} catch (Exception e) {
					return Builder.build(false, "在Cron任务队列中删除该任务失败，请手动更新! error:" + e.getMessage());
				}
			}
			return Builder.build(true);
		} else {
			return Builder.build(false, "更新失败，该条任务可能已经删除.");
		}
	}

	@RequestMapping("/job-logger/job-logger-get")
	public @ResponseBody Pagination<JobLogPo> jobLoggerGet(JobLoggerRequest request) {

		Pagination<JobLogPo> paginationRsp = appContext.getJobLogger().search(request);

		return paginationRsp;
	}

	/**
	 * 给JobTracker发消息 加载任务到内存
	 */
	@RequestMapping("/job-queue/load-add")
	public RestfulResponse loadJob(JobQueueReq request) {
		RestfulResponse response = new RestfulResponse();

		String nodeGroup = request.getTaskTrackerNodeGroup();

		HttpCmd httpCmd = new DefaultHttpCmd();
		httpCmd.setCommand(HttpCmdNames.HTTP_CMD_LOAD_JOB);
		httpCmd.addParam("nodeGroup", nodeGroup);

		List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.JOB_TRACKER);
		if (CollectionUtil.isEmpty(jobTrackerNodeList)) {
			response.setMsg(I18nManager.getMessage("job.tracker.not.found"));
			response.setSuccess(false);
			return response;
		}

		boolean success = false;
		HttpCmdResponse cmdResponse = null;
		for (Node node : jobTrackerNodeList) {
			// 所有的JobTracker都load一遍
			httpCmd.setNodeIdentity(node.getIdentity());
			cmdResponse = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
			if (cmdResponse.isSuccess()) {
				success = true;
			}
		}
		if (success) {
			response.setMsg("Load success");
		} else {
			response.setMsg("Load failed");
		}
		response.setSuccess(success);
		return response;
	}

	@RequestMapping("/job-queue/job-add")
	public RestfulResponse jobAdd(JobQueueReq request) {
		// 表单check
		try {
			Assert.notNull(request.getJobType(), "任务类型不能为空!");
			Assert.hasLength(request.getTaskId(), I18nManager.getMessage("taskId.not.null"));
			Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
			if (request.getNeedFeedback()) {
				Assert.hasLength(request.getSubmitNodeGroup(), "submitNodeGroup不能为空!");
			}

			if (StringUtil.isNotEmpty(request.getCronExpression())) {
				try {
					CronExpression expression = new CronExpression(request.getCronExpression());
					Date nextTime = expression.getTimeAfter(new Date());
					if (nextTime == null) {
						return Builder.build(false,
								StringUtil.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
					} else {
						request.setTriggerTime(nextTime);
					}
				} catch (ParseException e) {
					return Builder.build(false, "请输入正确的 CronExpression!");
				}
			}

		} catch (IllegalArgumentException e) {
			return Builder.build(false, e.getMessage());
		}

		Pair<Boolean, String> pair = addJob(request);
		return Builder.build(pair.getKey(), pair.getValue());
	}

	
	/**获取shopId可选列表
	 * @return
	 */
	@RequestMapping("/job-queue/shop-id-get")
	public RestfulResponse getShopIdList(){
		List<ShopItem> shops = appContext.getShopIdAccess().getShopIdList();
		RestfulResponse response = Builder.build(true, "获取成功。");
		response.setRows(shops);
		return response;
	}
	
	private Pair<Boolean, String> addJob(JobQueueReq request) {

		Job job = new Job();
		job.setTaskId(request.getTaskId());
		if (CollectionUtil.isNotEmpty(request.getExtParams())) {
			for (Map.Entry<String, String> entry : request.getExtParams().entrySet()) {
				job.setParam(entry.getKey(), entry.getValue());
			}
		}
		// 执行节点的group名称
		job.setTaskTrackerNodeGroup(request.getTaskTrackerNodeGroup());
		job.setSubmitNodeGroup(request.getSubmitNodeGroup());

		job.setNeedFeedback(request.getNeedFeedback());
		job.setReplaceOnExist(true);

		// 这个是 cron expression 和 quartz 一样，可选
		job.setCronExpression(request.getCronExpression());
		if (request.getTriggerTime() != null) {
			job.setTriggerTime(request.getTriggerTime().getTime());
		}
		job.setRepeatCount(request.getRepeatCount() == null ? 0 : request.getRepeatCount());
		job.setRepeatInterval(request.getRepeatInterval());

		job.setPriority(request.getPriority());
		job.setMaxRetryTimes(request.getMaxRetryTimes() == null ? 0 : request.getMaxRetryTimes());
		job.setRelyOnPrevCycle(request.getRelyOnPrevCycle() == null ? true : request.getRelyOnPrevCycle());

		if (request.getJobType() == JobType.REAL_TIME) {
			job.setCronExpression(null);
			job.setTriggerTime(null);
			job.setRepeatInterval(null);
			job.setRepeatCount(0);
			job.setRelyOnPrevCycle(true);
		} else if (request.getJobType() == JobType.TRIGGER_TIME) {
			job.setCronExpression(null);
			job.setRepeatInterval(null);
			job.setRepeatCount(0);
			job.setRelyOnPrevCycle(true);
		} else if (request.getJobType() == JobType.CRON) {
			job.setRepeatInterval(null);
			job.setRepeatCount(0);
		} else if (request.getJobType() == JobType.REPEAT) {
			job.setCronExpression(null);
		}
		return addJob(job);
	}

	private Pair<Boolean, String> addJob(Job job) {
		HttpCmd httpCmd = new DefaultHttpCmd();
		httpCmd.setCommand(HttpCmdNames.HTTP_CMD_ADD_JOB);
		httpCmd.addParam("job", JsonConvert.serialize(job));

		List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.JOB_TRACKER);
		if (CollectionUtil.isEmpty(jobTrackerNodeList)) {
			return new Pair<Boolean, String>(false, I18nManager.getMessage("job.tracker.not.found"));
		}

		HttpCmdResponse response = null;
		for (Node node : jobTrackerNodeList) {
			httpCmd.setNodeIdentity(node.getIdentity());
			response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
			if (response.isSuccess()) {
				return new Pair<Boolean, String>(true, "Add success");
			}
		}
		if (response != null) {
			return new Pair<Boolean, String>(false, response.getMsg());
		} else {
			return new Pair<Boolean, String>(false, "Add failed");
		}
	}

	@RequestMapping("/job-queue/executing-job-terminate")
	public RestfulResponse jobTerminate(String jobId) {

		JobPo jobPo = appContext.getExecutingJobQueue().getJob(jobId);
		if (jobPo == null) {
			return Builder.build(false, "该任务已经执行完成或者被删除");
		}

		String taskTrackerIdentity = jobPo.getTaskTrackerIdentity();

		Node node = appContext.getNodeMemCacheAccess().getNodeByIdentity(taskTrackerIdentity);
		if (node == null) {
			return Builder.build(false, "执行该任务的TaskTracker已经离线");
		}

		HttpCmd cmd = new DefaultHttpCmd();
		cmd.setCommand(HttpCmdNames.HTTP_CMD_JOB_TERMINATE);
		cmd.setNodeIdentity(taskTrackerIdentity);
		cmd.addParam("jobId", jobId);
		HttpCmdResponse response = HttpCmdClient.doPost(node.getIp(), node.getHttpCmdPort(), cmd);
		if (response.isSuccess()) {
			return Builder.build(true);
		} else {
			return Builder.build(false, response.getMsg());
		}
	}
}
