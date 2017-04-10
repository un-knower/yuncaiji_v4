package cn.uway.ucloude.uts.web.admin.view;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.DateUtil.TimePattern;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupPo;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;

@Controller
public class CommonView {
	@Autowired
	private BackendAppContext appContext;

	@RequestMapping("index")
	public String index(Model model) {
		setAttr(model);
		return "index";
	}

	@RequestMapping("node-manager")
	public String nodeManagerUI(Model model) {
		return "nodeManager";
	}

	@RequestMapping("node-group-manager")
	public String nodeGroupManagerUI(Model model) {
		return "nodeGroupManager";
	}

	@RequestMapping("node-onoffline-log")
	public String nodeOnOfflineLogUI(Model model) {
		model.addAttribute("startLogTime",
				DateUtil.formatNonException(DateUtil.addDays(new Date(), -10), TimePattern.yyyy__MM__1dd____HH$mm$ss));
		model.addAttribute("endLogTime",
				DateUtil.formatNonException(new Date(), TimePattern.yyyy__MM__1dd____HH$mm$ss));
		return "nodeOnOfflineLog";
	}

	@RequestMapping("node-jvm-info")
	public String nodeJVMInfo(Model model, String identity) {
		model.addAttribute("identity", identity);
		return "nodeJvmInfo";
	}

	@RequestMapping("job-add")
	public String addJobUI(Model model, String jobId, String jobqueue) {
		setAttr(model);
		model.addAttribute("jobId", jobId);
		model.addAttribute("jobqueue", jobqueue);
		return "jobAdd";
	}

	@RequestMapping("nav-logger")
	public String navLoggerUI(Model model) {
		return "navLogger";
	}

	@RequestMapping("job-logger")
	public String jobLoggerUI(Model model, String realTaskId, String taskTrackerNodeGroup, Date startLogTime,
			Date endLogTime) {
		model.addAttribute("realTaskId", realTaskId);
		model.addAttribute("taskTrackerNodeGroup", taskTrackerNodeGroup);
		if (startLogTime == null) {
			startLogTime = DateUtil.addMinutes(new Date(), -10);
		}
		model.addAttribute("startLogTime",
				DateUtil.formatNonException(startLogTime, TimePattern.yyyy__MM__1dd____HH$mm$ss));
		if (endLogTime == null) {
			endLogTime = new Date();
		}
		model.addAttribute("endLogTime",
				DateUtil.formatNonException(endLogTime, TimePattern.yyyy__MM__1dd____HH$mm$ss));
		setAttr(model);
		return "jobLogger";
	}

	@RequestMapping("cron-job-queue")
	public String cronJobQueueUI(Model model) {
		setAttr(model);
		return "cronJobQueue";
	}

	@RequestMapping("repeat-job-queue")
	public String repeatJobQueueUI(Model model) {
		setAttr(model);
		return "repeatJobQueue";
	}

	@RequestMapping("executable-job-queue")
	public String executableJobQueueUI(Model model) {
		setAttr(model);
		return "executableJobQueue";
	}

	@RequestMapping("executing-job-queue")
	public String executingJobQueueUI(Model model) {
		setAttr(model);
		return "executingJobQueue";
	}

	@RequestMapping("load-job")
	public String loadJobUI(Model model) {
		setAttr(model);
		return "loadJob";
	}

	@RequestMapping("cron_generator_iframe")
	public String cronGeneratorIframe(Model model) {
		return "cron/cronGenerator";
	}

	@RequestMapping("suspend-job-queue")
	public String suspendJobQueueUI(Model model) {
		setAttr(model);
		return "suspendJobQueue";
	}

	private void setAttr(Model model) {
		List<NodeGroupPo> jobClientNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.JOB_CLIENT);
		model.addAttribute("jobClientNodeGroupsJson", JsonConvert.serialize(jobClientNodeGroups));
		List<NodeGroupPo> taskTrackerNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
		model.addAttribute("taskTrackerNodeGroupsJson", JsonConvert.serialize(taskTrackerNodeGroups));
	}
}
