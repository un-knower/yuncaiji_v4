package cn.uway.ucloude.uts.igp.tasktracker;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.dao.ConnectionInfoDAO;
import cn.uway.framework.connection.dao.impl.DatabaseConnectionInfoDAO;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.AbstractJob;
import cn.uway.framework.job.AdaptiveStreamJob;
import cn.uway.framework.job.GenericJob;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.task.ExtraInfo;
import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.MultiElementGatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.spring.tasktracker.JobRunnerItem;
import cn.uway.ucloude.uts.spring.tasktracker.UTS;
import cn.uway.ucloude.uts.tasktracker.Result;
import cn.uway.ucloude.uts.tasktracker.logger.BizLogger;
import cn.uway.ucloude.uts.tasktracker.runner.TaskLoggerFactory;

@SuppressWarnings("resource")
@UTS
public class IgpJobScheduler {
	private static final ILogger LOGGER = LoggerManager
			.getLogger(IgpJobScheduler.class);
	
	static {
		String rootPath = System.getProperty("CONF_HOME", System.getProperty("UTS_IGP_ROOT_PATH"));
		if (rootPath == null || rootPath.length() < 1) {
			rootPath = "../";
		}
		System.setProperty("UTS_IGP_ROOT_PATH", rootPath);
		LOGGER.debug("UTS_IGP_ROOT_PATH:{}", rootPath);
		
    	new ClassPathXmlApplicationContext("/igpApplicationContext.xml");
	}
	
	/**
	 * 连接信息查询DAO
	 */
	protected ConnectionInfoDAO connectionInfoDAO = AppContext.getBean("connectionInfoDAO", DatabaseConnectionInfoDAO.class);
	
	@SuppressWarnings("unused")
	@JobRunnerItem(shardValue = "igp_v4")
	public Result execute(Job job) {
		try {
			BizLogger bizLogger = TaskLoggerFactory.getBizLogger();

			long taskid = NumberUtils.toLong(job.getParam("igp_task_id"));
			boolean is_period = BooleanUtils.toBoolean(job.getParam("is_period"));
			int period = NumberUtils.toInt(job.getParam("period"));
			long groupid = NumberUtils.toInt(job.getParam("groupID"));
			boolean repairJob = BooleanUtils.toBoolean(job
					.getParam("repairJob"));
			long dataTimeMillSec = NumberUtils
					.toLong(job.getParam("data_time"));
			long solutionid = NumberUtils.toLong(job.getParam("solution"));
			int connid = NumberUtils.toInt(job.getParam("conntionID"));
			String gather_path = job.getParam("gather_path");
			String parseTemplate = job.getParam("parseTemplate");
			String exportTemplate = job.getParam("exportTemplate");
			String pc_name = job.getParam("pc_name");
			String vendor = job.getParam("vendor");
			int netType = NumberUtils.toInt(job.getParam("netType"));
			int omcID = NumberUtils.toInt(job.getParam("omcID"));
			int cityID = NumberUtils.toInt(job.getParam("cityID"));
			int bscID = NumberUtils.toInt(job.getParam("bscID"));
			String shellBeforGather = job.getParam("shell_before_gather");
			String shellAfterGather = job.getParam("shell_after_gather");
			int shellTimeOut = NumberUtils.toInt(job.getParam("shell_timeout"));
			
			int entryNum = NumberUtils.toInt(job.getParam("entryNum"));
			String parseFiles = job.getParam("parseFiles");
			long parseFirstFileSize = NumberUtils.toInt(job.getParam("parseFirstFileSize"));

			Task task = null;
			if (is_period)
				task = new PeriodTask();
			else
				task = new Task();
			
			ExtraInfo extraInfo = new ExtraInfo(cityID, omcID, bscID, netType);
			task.setExtraInfo(extraInfo);
			task.getExtraInfo().setVendor(vendor);
			
			task.setId(taskid);
			task.setDataTime(new Date(dataTimeMillSec));
			task.setSolutionId(solutionid);
			task.setConnectionId(connid);
			task.setGatherPathDescriptor(new GatherPathDescriptor(gather_path));
			task.setShellBefore(shellBeforGather);
			task.setShellAfter(shellAfterGather);
			task.setShellTimeout(shellTimeOut);
			task.setTimeoutMinutes(0);
			task.setEndDataTime(null);
			task.setPcName(pc_name);
			task.setGroupId(groupid);
			//task.setDescription("");
			task.setParserTemplates(parseTemplate);
			task.setExportTemplates(exportTemplate);
			task.setPeriod(period);
			//task.setDelayDataScanPeriod(0);
			//task.setDelayDataTimeDelay(0);
			
			if (is_period) {
				PeriodTask periodTask = (PeriodTask)task;
				/*periodTask.setGatherTimeDelay(0);
				periodTask.setPeriodMinutes(0);
				periodTask.setMaxGatherTime(0);
				periodTask.setRegatherTimeOffsetMinutes(0);*/
			}
			
			String[] pathEntrys = StringUtil.split(parseFiles, ',');
			GatherSolution solution = SolutionLoader.getSolution(task);
			ConnectionInfo connInfo = connectionInfoDAO.getConnectionInfo(connid);
			
			GatherPathEntry pathEntry = null;
			if (pathEntrys.length > 1)
				pathEntry = new MultiElementGatherPathEntry(Arrays.asList(pathEntrys));
			else
				pathEntry = new GatherPathEntry(pathEntrys[0]);
			pathEntry.setSize(parseFirstFileSize);
			
			JobParam param = new JobParam(task, connInfo, solution, pathEntry);
			
			AbstractJob igpJob = null;
			if (solution.isAdaptiveStreamJobAvaliable() || pathEntrys.length > 1)
				igpJob = new AdaptiveStreamJob(param);
			else
				igpJob = new GenericJob(param);
			
			// 会发送到 LTS (JobTracker上)
			//bizLogger.info("开始运行...");
			
			igpJob.call();
		} catch (Exception e) {
			LOGGER.warn("Run job failed!", e);
			return new Result(Action.EXECUTE_LATER, e.getMessage());
		}
		
		LOGGER.debug("任务执行完成.");
		return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
	}
}
