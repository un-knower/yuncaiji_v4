package cn.uway.framework.warehouse.exporter.template;

import java.util.ArrayList;
import java.util.List;

/**
 * RemoteFileExportTemplateBean (用于描述输出模板来源于xxx.xml中的配置信息)
 * 
 */
public class RemoteFileExportTemplateBean extends ExportTemplateBean {
	

	/**
	 * TODO:是否入到主远程路径（config.ini中配置）-- 1:true/yes/on 表示是;2:false/no/off 表示否（不区分大小写）
	 */
	private String isToMainRemote;
	
	// File field list
	private List<FieldTemplateBean> exportFileds = new ArrayList<FieldTemplateBean>();

	public RemoteFileExportTemplateBean() {
		super();
	}

	public RemoteFileExportTemplateBean(RemoteFileExportTemplateBean dbExportTemplateBean) {
		this.id = dbExportTemplateBean.getId();
		this.type = dbExportTemplateBean.getType();
		this.dataType = dbExportTemplateBean.getDataType();
		this.targetId = dbExportTemplateBean.getTargetId();
		this.exportTargetBean = null;
		this.exportFileds = dbExportTemplateBean.exportFileds;
		this.isToMainRemote = dbExportTemplateBean.getIsToMainRemote();
	}


	public List<FieldTemplateBean> getExportFileds() {
		return exportFileds;
	}

	public void setExportFileds(List<FieldTemplateBean> exportFileds) {
		this.exportFileds = exportFileds;
	}

	public void addFieldTemplate(FieldTemplateBean fieldTemplate) {
		exportFileds.add(fieldTemplate);
	}
	
	public String getIsToMainRemote() {
		return isToMainRemote;
	}
	
	public void setIsToMainRemote(String isToMainRemote) {
		this.isToMainRemote = isToMainRemote;
	}
}
