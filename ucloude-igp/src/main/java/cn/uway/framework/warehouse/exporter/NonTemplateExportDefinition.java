package cn.uway.framework.warehouse.exporter;

import java.util.Collections;
import java.util.List;

import cn.uway.framework.warehouse.exporter.template.ExportTemplatePojo;

public class NonTemplateExportDefinition extends ExportDefinition {

	@Override
	public void parseExportTemplet() {
	}

	@Override
	public List<String> getExportDefinitionXmlPath() {
		return Collections.emptyList();
	}

	@Override
	public void setExportDefinitionXmlPath(List<String> exportDefinitionXmlPath) {
	}

	@Override
	public ExportTemplatePojo getExportTemplatePojo() {
		return null;
	}

}
