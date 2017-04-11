package cn.uway.framework.warehouse.exporter.template;

/**
 * RemoteFTPExportTempletBean 远程文件输出模版
 * 
 * @author chenrongqiang 2012-12-5
 */
public class ObsoleteRemoteFTPExportTempletBean extends FileExportTemplateBean {

	/**
	 * 远程输出文件目的地模版配置
	 */
	private ObsoleteRemoteFileExportTargetBean remoteFileExportTargetBean;

	public ObsoleteRemoteFileExportTargetBean getRemoteFileExportTargetBean() {
		return remoteFileExportTargetBean;
	}

	public void setRemoteFileExportTargetBean(ObsoleteRemoteFileExportTargetBean remoteFileExportTargetBean) {
		this.remoteFileExportTargetBean = remoteFileExportTargetBean;
	}
}
