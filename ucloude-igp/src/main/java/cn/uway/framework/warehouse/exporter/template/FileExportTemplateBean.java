package cn.uway.framework.warehouse.exporter.template;

import java.util.ArrayList;
import java.util.List;

/**
 * FileExportTemplateBean
 * 
 * @author chenrongqiang 2012-11-12
 */
public class FileExportTemplateBean extends ExportTemplateBean {

	// File field list
	private List<FieldTemplateBean> exportFileds = new ArrayList<FieldTemplateBean>();

	public List<FieldTemplateBean> getExportFileds() {
		return exportFileds;
	}

	public void setExportFileds(List<FieldTemplateBean> exportFileds) {
		this.exportFileds = exportFileds;
	}

	public void addFieldTemplate(FieldTemplateBean fieldTemplate) {
		exportFileds.add(fieldTemplate);
	}
}
