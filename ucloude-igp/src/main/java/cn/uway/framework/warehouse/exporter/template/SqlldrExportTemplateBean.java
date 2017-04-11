package cn.uway.framework.warehouse.exporter.template;

public class SqlldrExportTemplateBean extends ExportTemplateBean {

	/**
	 * 参数性能汇总
	 */
	public static final String CM_SUMMARY = "CM";

	/**
	 * 性能数据汇总
	 */
	public static final String PM_SUMMARY = "PM";
	
	/**
	 * table and column mapping
	 */
	private SqlldrTableTemplateBean table;

	/**
	 * 是否入到主库（config.ini中配置的log_clt_insert所在库）-- 1:true/yes/on 表示是;2:false/no/off 表示否（不区分大小写）
	 */
	private String isToMainDB;
	
	/**
	 * 分隔符
	 */
	private String split;

	public SqlldrExportTemplateBean() {
		super();
	}

	public SqlldrExportTemplateBean(SqlldrExportTemplateBean sqlldrExportTemplateBean) {
		this.id = sqlldrExportTemplateBean.getId();
		this.type = sqlldrExportTemplateBean.getType();
		this.dataType = sqlldrExportTemplateBean.getDataType();
		this.targetId = sqlldrExportTemplateBean.getTargetId();
		this.table = sqlldrExportTemplateBean.getTable();
		this.exportTargetBean = null;
		this.isToMainDB = sqlldrExportTemplateBean.getIsToMainDB();
		this.split = sqlldrExportTemplateBean.getSplit();
		this.encode = sqlldrExportTemplateBean.getEncode();
	}

	public SqlldrTableTemplateBean getTable() {
		return table;
	}

	public void setTable(SqlldrTableTemplateBean table) {
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
	
}
