package cn.uway.framework.warehouse.exporter.template;

/**
 * ExporterBean <br>
 * 每个ExporterBean对应一个export配置<br>
 * 输出配置主要有两个来源： <br>
 * 1、export_config.xml中的配置，主要控制文件的输出 <br>
 * 2、IGP_CFG_DB_EXPORT表中的配置，控制数据库输出 <br>
 * 
 * @author chenrongqiang 2012-11-12
 */
public class ExporterBean{

	// taget_id 输出M目的地ID
	private int id;

	// 是否打开标记
	private boolean openFlag;

	public int getId(){
		return id;
	}

	public boolean isOpenFlag(){
		return openFlag;
	}

	public void setId(int id){
		this.id = id;
	}

	public void setOpenFlag(boolean openFlag){
		this.openFlag = openFlag;
	}

	public boolean getOpenFlag(){
		return this.openFlag;
	}

}
