package cn.uway.framework.warehouse.exporter.template;

import java.util.List;

/**
 * @author yuy 输出模板pojo
 */
public class ExportTemplatePojo {

	/**
	 * 输出策略，两种类型：one-one;one-multi
	 * one-one:一对一，即一个文件流对应一个输出器(一个文件流只入一张表)；one-multi：一对多，即一个文件流对应多个输出器
	 * 默认取值为one-multi
	 */
	public String exportStrategy;

	/**
	 * 是否汇总标志
	 */
	public boolean isSummary;

	/**
	 * 输出器列表
	 */
	public List<ExportTemplateBean> exportTemplates;

	/**
	 * @return exportStrategy
	 */
	public String getExportStrategy() {
		return exportStrategy;
	}

	/**
	 * @param exportStrategy
	 */
	public void setExportStrategy(String exportStrategy) {
		this.exportStrategy = exportStrategy;
	}

	/**
	 * @return List<ExportTemplateBean>
	 */
	public List<ExportTemplateBean> getExportTemplates() {
		return exportTemplates;
	}

	/**
	 * @param exportTemplates
	 */
	public void setExportTemplates(List<ExportTemplateBean> exportTemplates) {
		this.exportTemplates = exportTemplates;
	}

	/**
	 * @return 是否汇总标志
	 */
	public boolean isSummary() {
		return isSummary;
	}

	/**
	 * @param isSummary
	 *            是否汇总标志
	 */
	public void setSummary(boolean isSummary) {
		this.isSummary = isSummary;
	}

}
