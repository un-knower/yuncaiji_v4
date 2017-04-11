package cn.uway.framework.warehouse.exporter.template;

/**
 * DbExportTemplateBean
 * 
 * @author chenrongqiang 2012-11-12
 */
public class DbExportTemplateBean extends ExportTemplateBean {

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
	private DbTableTemplateBean table;

	/**
	 * 汇总数据类型
	 */
	private String summaryDataType;

	/**
	 * 是否入到主库（config.ini中配置的log_clt_insert所在库）-- 1:true/yes/on 表示是;2:false/no/off 表示否（不区分大小写）
	 */
	private String isToMainDB;

	public DbExportTemplateBean() {
		super();
	}

	public DbExportTemplateBean(DbExportTemplateBean dbExportTemplateBean) {
		this.id = dbExportTemplateBean.getId();
		this.type = dbExportTemplateBean.getType();
		this.dataType = dbExportTemplateBean.getDataType();
		this.targetId = dbExportTemplateBean.getTargetId();
		this.table = dbExportTemplateBean.getTable();
		this.exportTargetBean = null;
		this.summaryDataType = dbExportTemplateBean.getSummaryDataType();
		this.isToMainDB = dbExportTemplateBean.getIsToMainDB();
	}

	public DbTableTemplateBean getTable() {
		return table;
	}

	public void setTable(DbTableTemplateBean table) {
		this.table = table;
	}

	/**
	 * @return the summaryDataType
	 */
	public final String getSummaryDataType() {
		return summaryDataType;
	}

	/**
	 * @param summaryDataType
	 *            the summaryDataType to set
	 */
	public final void setSummaryDataType(String summaryDataType) {
		this.summaryDataType = summaryDataType;
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
}
