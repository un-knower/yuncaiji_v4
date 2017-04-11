package cn.uway.framework.warehouse.exporter;

import java.util.List;

import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.warehouse.exporter.template.ExportTemplate;
import cn.uway.framework.warehouse.exporter.template.ExportTemplatePojo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * RepositoryExportConfig
 * 
 * @author chenrongqiang 2012-11-1
 * @version 1.0
 * @since 3.0
 */
public class ExportDefinition {

	// 定义数据输出配置 配置文件可支持多个
	private List<String> exportDefinitionXmlPath;

	// 输出模版bean
	private ExportTemplatePojo exportTemplatePojo;

	private static final ILogger LOGGER = LoggerManager.getLogger(ExportDefinition.class); // 日志

	// 增加默认的构造函数 支持Spring set方法注入 2012-11-11
	public ExportDefinition() {
		super();
	}

	/**
	 * 在启动时进行模版初始化 避免程序运行后出错
	 */
	public void parseExportTemplet() {
		if (exportDefinitionXmlPath == null || exportDefinitionXmlPath.isEmpty()) {
			LOGGER.error("模版未配置");
			return;
		}
		for (String xmlPath : exportDefinitionXmlPath) {
			xmlPath = SolutionLoader.addTemplateDir(xmlPath);
			
			try {
				if (exportTemplatePojo == null) {
					exportTemplatePojo = ExportTemplate.parseExportTemplate(xmlPath);
					continue;
				}
				ExportTemplatePojo exportTemplatePojo_ = ExportTemplate.parseExportTemplate(xmlPath);
				exportTemplatePojo.getExportTemplates().addAll(exportTemplatePojo_.getExportTemplates());
			} catch (Exception e) {
				LOGGER.error("输出模版文件:" + xmlPath + "解析异常.", e);
			}
		}
	}

	public ExportDefinition(List<String> exportDefinitionXmlPath) {
		super();
		this.setExportDefinitionXmlPath(exportDefinitionXmlPath);
	}

	public List<String> getExportDefinitionXmlPath() {
		return exportDefinitionXmlPath;
	}

	public void setExportDefinitionXmlPath(List<String> exportDefinitionXmlPath) {
		this.exportDefinitionXmlPath = exportDefinitionXmlPath;
	}

	/**
	 * @return ExportTemplatePojo
	 */
	public ExportTemplatePojo getExportTemplatePojo() {
		return exportTemplatePojo;
	}
}
