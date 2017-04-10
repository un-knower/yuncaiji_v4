package cn.uway.ucloude.uts.igp.jobclient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.jobclient.JobClient;

public class Main {
	private static final ILogger LOGGER = LoggerManager.getLogger(Main.class);
	public static ApplicationContext context;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		String rootPath = System.getProperty("CONF_HOME", System.getProperty("UTS_IGP_ROOT_PATH"));
		if (rootPath == null || rootPath.length() < 1) {
			rootPath = "../";
		}
		System.setProperty("UTS_IGP_ROOT_PATH", rootPath);
		LOGGER.debug("UTS_IGP_ROOT_PATH:{}", rootPath);
		
		JobClient jobClient = null;
		context = new ClassPathXmlApplicationContext(
				"/ucloude-uts-ipg-jobclient.xml");
		if (context.containsBean("jobClient")) {
			jobClient = (JobClient) context.getBean("jobClient");
			// System.out.println("submitJob");
			// Job job = new Job();
			// job.setTaskId("111");
			// job.setParam("shopId", "testest");
			// job.setTaskTrackerNodeGroup("ucloude_uts_cluster");
			// job.setNeedFeedback(true);
			// job.setReplaceOnExist(true); // 当任务队列中存在这个任务的时候，是否替换更新
			// Response response = jobClient.submitJob(job);
			// System.out.println(response);
		} else {
			LOGGER.error("no contains jobClient.");
			return;
		}

		if (context.containsBean("igpJobClientRunner")) {
			IGPJobClientRunner igpRunner = (IGPJobClientRunner) context
					.getBean("igpJobClientRunner");
			igpRunner.setJobClient(jobClient);
			igpRunner.start();
		} else {
			LOGGER.error("no contains igpJobClientRunner.");
		}
	}
}
