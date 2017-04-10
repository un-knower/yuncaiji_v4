package cn.uway.ucloude.uts.igp.jobclient;

import java.util.ArrayList;
import java.util.List;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.MultiElementGatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.TaskQueue;
import cn.uway.framework.task.TaskTrigger;
import cn.uway.framework.task.loader.TaskLoader;
import cn.uway.framework.task.worker.ITaskDeliver;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.jobclient.JobClient;
import cn.uway.ucloude.uts.jobclient.domain.Response;
import cn.uway.util.StringUtil;


public class IGPJobClientRunner implements ITaskDeliver {
	private static final ILogger LOGGER = LoggerManager.getLogger(IGPJobClientRunner.class);
	
	// 由spring注入
	private TaskLoader taskLoader;
	private TaskQueue taskQueue;
	private TaskTrigger taskTrigger;
	
	@SuppressWarnings("rawtypes")
	private JobClient jobClient;
	
	public IGPJobClientRunner() {
		
	}
	
	public static IGPJobClientRunner getInstance() {
		if (Main.context.containsBean("igpJobClientRunner")) {
			IGPJobClientRunner igpRunner = (IGPJobClientRunner)Main.context
					.getBean("igpJobClientRunner");
			return igpRunner;
		}
		
		return null;
	}
	
	public void runnerValidate() {
		if (taskLoader == null) {
			LOGGER.error("IGPJobClientRunner:taskLoader invalid.");
			System.exit(-1);
		}
		
		if (taskTrigger == null) {
			LOGGER.error("IGPJobClientRunner:taskTrigger invalid.");
			System.exit(-2);
		}
	}
		
	public void start() {
		LOGGER.info("begin to run IGPJobClientRunner...");
		taskTrigger.setTaskDeliver(this);
		//先启动taskTrigger避免队列爆满
		taskTrigger.start();
		taskLoader.loadTask();
		LOGGER.info("IGPJobClientRunner run started.");
	}
	
	public void shutdown() {
		taskTrigger.stopTrigger();
	}
	
	@Override
	public boolean submit(Task task, ConnectionInfo connInfo, GatherPathEntry pathEntry, int fileIndex) {
        List<String> pathEntrys = null;
        Boolean repairJob = false;
		if (pathEntry instanceof MultiElementGatherPathEntry) {
			MultiElementGatherPathEntry multiElementGatherPathEntry = (MultiElementGatherPathEntry) pathEntry;
			repairJob = multiElementGatherPathEntry.isRepairTask();
			pathEntrys = multiElementGatherPathEntry.getGatherPaths();

			LOGGER.debug("本次分组解码共有{}个文件需要处理", pathEntrys.size());
		} else {
			pathEntrys = new ArrayList<String>(1);
			pathEntrys.add(pathEntry.getPath());
		}
		
		Job job = new Job();
		job.setParam("shopId", "igp_v4");
		String taskJobID =  genTaskID(task, pathEntry, fileIndex);
        job.setTaskId(taskJobID);
        
        //set task parameter:
        job.setParam("repairJob", repairJob.toString());
        job.setParam("igp_task_id", task.getId() + "");
        job.setParam("is_period", (task instanceof PeriodTask) ? "true" : "false");
        job.setParam("period", task.getPeriod() + "");
        job.setParam("groupID", task.getGroupId() + "");
        job.setParam("data_time", task.getDataTime() + "");
        job.setParam("solution", task.getSolutionId() + "");
        job.setParam("conntionID", task.getConnectionId() + "");
        job.setParam("gather_path", task.getGatherPathDescriptor().getRawData());
        job.setParam("parseTemplate", task.getParserTemplates());
        job.setParam("exportTemplate", task.getExportTemplates());
        job.setParam("pc_name", task.getPcName());
        
        job.setParam("vendor", task.getExtraInfo().getVendor());
        job.setParam("netType", String.valueOf(task.getExtraInfo().getNetType()));
        job.setParam("omcID", String.valueOf(task.getExtraInfo().getOmcId()));
        job.setParam("cityID", String.valueOf(task.getExtraInfo().getCityId()));
        job.setParam("bscID", String.valueOf(task.getExtraInfo().getBscId()));
                
        job.setParam("shell_before_gather", task.getShellBefore());
        job.setParam("shell_after_gather", task.getShellAfter());
        job.setParam("shell_timeout", String.valueOf(task.getShellTimeout()));
        
        //set file parse parameter: 
        int entryNum = pathEntrys.size();
        job.setParam("entryNum", entryNum + "");
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entryNum; i++) {
			String path = pathEntrys.get(i);
			String decodedFTPPath = path;

			if (connInfo instanceof FTPConnectionInfo) {
				FTPConnectionInfo ftpConnInfo = (FTPConnectionInfo) connInfo;
				decodedFTPPath = StringUtil.decodeFTPPath(path, ftpConnInfo.getCharset());
			}
			
			if (sb.length() > 0)
				sb.append(",");
			
			sb.append(decodedFTPPath);
        }
        job.setParam("parseFiles", sb.toString());
        job.setParam("parseFirstFileSize", String.valueOf(pathEntry.getSize()));
                
        job.setTaskTrackerNodeGroup("igp_v4_tasktracker");
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
                
        Response response = jobClient.submitJob(job);
        if (response != null)
        	return true;
        
        return false;
	}
	
	public String genTaskID(Task task, GatherPathEntry pathEntry, int fileIndex) {
		int currTime = (int)((System.currentTimeMillis() / 1000l) & 0x7FFFFFFFl);
		StringBuilder sb = new StringBuilder(64);
		sb.append(task.getId()).append('-').append(currTime).append('-').append(fileIndex);
		
		return sb.toString();
	}
	
	public TaskLoader getTaskLoader() {
		return taskLoader;
	}

	public void setTaskLoader(TaskLoader taskLoader) {
		this.taskLoader = taskLoader;
	}
	
	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}
	
	@SuppressWarnings("rawtypes")
	public JobClient getJobClient() {
		return jobClient;
	}
	
	@SuppressWarnings("rawtypes")
	public void setJobClient(JobClient jobClient) {
		this.jobClient = jobClient;
	}
	
	public TaskTrigger getTaskTrigger() {
		return taskTrigger;
	}
	
	public void setTaskTrigger(TaskTrigger taskTrigger) {
		this.taskTrigger = taskTrigger;
	}
}
