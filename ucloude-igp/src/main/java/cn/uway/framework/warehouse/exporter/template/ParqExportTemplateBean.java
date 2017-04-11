package cn.uway.framework.warehouse.exporter.template;

public class ParqExportTemplateBean extends ExportTemplateBean {

	/**
	 * table and column mapping
	 */
	private ParqTableTemplateBean table;

	/**
	 * 是否入到主库（config.ini中配置的log_clt_insert所在库）-- 1:true/yes/on 表示是;2:false/no/off 表示否（不区分大小写）
	 */
	private String isToMainDB;
	
	/**
	 * 入impala分区可配置 ，取值可以为：无分区:0, 年：1，月：2，日，3，小时：4, 默认是小时
	 */
	protected int partitionType = 4;
	
	/**
	 * 使用哪种文件Creater生成parq的文件名称
	 * 没有配置时，使用默认创建器
	 * 1用于生成配置表同步到IMPALA的文件名称
	 * 如果没有适合创建器请定义
	 * 通过在导出模板中配置ctType字段指定文件生成器
	 */
	protected int ctType;

	/**
	 * 分隔符
	 */
	private String split;

	public ParqExportTemplateBean() {
		super();
	}

	public ParqExportTemplateBean(ParqExportTemplateBean dbExportTemplateBean) {
		this.id = dbExportTemplateBean.getId();
		this.type = dbExportTemplateBean.getType();
		this.dataType = dbExportTemplateBean.getDataType();
		this.targetId = dbExportTemplateBean.getTargetId();
		this.table = dbExportTemplateBean.getTable();
		this.split = dbExportTemplateBean.getSplit();
		this.partitionType = dbExportTemplateBean.getPartitionType();
		this.ctType = dbExportTemplateBean.getCtType();
		this.exportTargetBean = null;
		this.isToMainDB = dbExportTemplateBean.getIsToMainDB();
	}

	public ParqTableTemplateBean getTable() {
		return table;
	}

	public void setTable(ParqTableTemplateBean table) {
		this.table = table;
	}

	/**
	 * @return isToMainDB
	 */
	public String getIsToMainDB() {
		return isToMainDB;
	}

	/**
	 * @param isToMainDB
	 */
	public void setIsToMainDB(String isToMainDB) {
		this.isToMainDB = isToMainDB;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public int getPartitionType() {
		return partitionType;
	}

	public void setPartitionType(int partitionType) {
		this.partitionType = partitionType;
	}

	public int getCtType() {
		return ctType;
	}

	public void setCtType(int ctType) {
		this.ctType = ctType;
	}
	
	
}
