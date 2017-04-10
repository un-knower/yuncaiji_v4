package cn.uway.ucloude.uts.web.admin.view;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.DateUtil.TimePattern;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupPo;
import cn.uway.ucloude.uts.web.admin.vo.NodeInfo;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;

@Controller
public class MonitorView {
	@Autowired
	private BackendAppContext context;
	ILogger logger = LoggerManager.getLogger(MonitorView.class);

	@RequestMapping("monitor")
	public String jobTrackerMonitor(Model model) {

		initTimeRange(model);
		return "monitor/monitor";
	}

	private void initTimeRange(Model model) {
		Date endDate = new Date();
		model.addAttribute("startTime",
				DateUtil.formatNonException(DateUtil.addHours(endDate, -3), TimePattern.yyyy__MM_1dd));
		model.addAttribute("endTime", DateUtil.formatNonException(endDate, TimePattern.yyyy__MM__1dd____HH$mm$ss));
	}

}
