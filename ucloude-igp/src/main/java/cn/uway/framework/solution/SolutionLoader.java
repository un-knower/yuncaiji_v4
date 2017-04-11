package cn.uway.framework.solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.ExportDefinition;
import cn.uway.ucloude.utils.UcloudePathUtil;

/**
 * 采集解决方案加载器
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public class SolutionLoader {

	/**
	 * 通过任务信息查找解决方案配置<br>
	 * 
	 * @param solutionId
	 *            解决方案编号
	 * @return GatherSolution 解决方案实例
	 */
	public static GatherSolution getSolution(Task task) {
		// TODO 待实现动态的解析、输出模版配置 主要用于数据库采集
		String beanName = "solution" + task.getSolutionId();
		GatherSolution solution = AppContext.getBean(beanName, GatherSolution.class);
		solution.setId(task.getSolutionId());
		solution.setBeforeAccessShell(task.getShellBefore());
		// 添加任务表中配置的模板,如果任务表中没有配置.则默认使用solution中的配置
		addTemplates(solution, task);
		task.setGatherSolutionInfo(solution);
		return solution;
	}

	/**
	 * 添加表中配置的模板
	 * 
	 * @param solution
	 * @param task
	 */
	private static void addTemplates(GatherSolution solution, Task task) {
		String parseDefaultTemplate = solution.getParser().getTemplates();
		String templates = task.getParserTemplates();
		if (templates != null && !templates.trim().isEmpty())
			solution.getParser().setTemplates(addTemplateDir(templates.trim()));
		else if (parseDefaultTemplate != null) {
			solution.getParser().setTemplates(addTemplateDir(parseDefaultTemplate));
		}
		
		// 输出模板的路径在ExportDefinition类中加
		templates = task.getExportTemplates();
		if (templates != null && !templates.trim().isEmpty()) {
			List<String> list = Arrays.asList(templates.split(";"));
			ExportDefinition exportDefinition = new ExportDefinition(addTemplateDirByBatch(list));
			exportDefinition.parseExportTemplet();
			solution.setExportDefinition(exportDefinition);
		}
	}

	public static List<String> addTemplateDirByBatch(List<String> list) {
		List<String> newList = new ArrayList<String>();
		for (String filePath : list) {
			newList.add(addTemplateDir(filePath));
		}
		return newList;
	}

	public static String addTemplateDir(String filePath) {
		if (filePath.startsWith("parser") || filePath.startsWith("export")) {
			return UcloudePathUtil.makeIgpTemplatePath(filePath);
		}
		
		return filePath;
	}

	/**
	 * 通过solution查找解决方案配置<br>
	 * 
	 * @param solutionId
	 *            解决方案编号
	 * @return GatherSolution 解决方案实例
	 */
	public static GatherSolution getSolution(long solution) {
		String beanName = "solution" + solution;
		return AppContext.getBean(beanName, GatherSolution.class);
	}
}
