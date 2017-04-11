package cn.uway.framework.warehouse.exporter.template;

/**
 * Export template configuration
 * 
 * @author Spike
 * @date 2012/11/01
 * @version 1.0
 * @since 1.0
 */
public class ExportTemplateBean {

	/**
	 * 输出方式 JDBC方式
	 */
	public static final int JDBC_EXPORTER = 101;

	/**
	 * 输出方式mysql
	 */
	public static final int EXPORT_DB_MYSQL = 102;

	/**
	 * 输出方式 greenplum
	 */
	public static final int EXPORT_DB_GREENPLUM = 103;

	/**
	 * 使用数据库连接池并且具有容错能力的JDBC输出器
	 */
	public static final int POOLED_JDBC_EXPORTER = 104;

	/**
	 * 使用数据库连接池并且具有容错能力的JDBC输出器
	 */
	public static final int FAULT_TOLERANT_POOLED_JDBC_EXPORTER = 105;

	/**
	 * 输出方式 适用于汇总输出的JDBC方式
	 */
	public static final int EXPORT_DB_SUMMARY = 106;
	
	/**
	 * 输出方式 oracle的sqlldr工具
	 */
	public static final int EXPORT_DB_SQLLDR = 107;
	
	/**
	 * 输出方式 JDBC方式(继承自101。先清表，再插入，适合全量采集)
	 */
	public static final int JDBC_TRUNCATE_AND_EXPORTER = 108;
	
	/**
	 * 输出方式 JDBC方式(继承自101。先备份(源表名_HIS)，再清表，再插入，适合全量采集)
	 */
	public static final int JDBC_BAKUP_AND_EXPORTER = 110;
	
	/**
	 * 输出parquet文件
	 */
	public static final int PARQUET_EXPORTER = 109;
	
	/**
	 * 数据库配置同步到IMPALA,输出parquet文件（先将数据写到临时表，然后删除原有文件，将临时文件重命名为原有文件名称，最后刷新表）
	 */
	public static final int CONFIGURE_PARQUET_EXPORTER = 112;

	/**
	 * kafka 输出
	 */
	public static final int KAFKA_EXPORTER = 111;
	
	/**
	 * 输出方式 本地文件
	 */
	public static final int LOACL_FILE_EXPORTER = 2;

	/**
	 * 远程FTP/SFTP/HDFS输出
	 */
	public static final int REMOTE_FILE_EXPORTER = 3;
	
	/**
	 * 远程FTP输出扩展
	 */
	public static final int EXTEND_REMOTE_FILE_EXPORTER = 7;
	
	/**
	 * 输出方式 生成本地infoBright文件
	 */
	public static final int EXPORT_INFOBRIGHT = 5;

	/**
	 * 输出方式 汇总文件
	 */
	public static final int EXPORT_SUMFILE = 6;
	
	/**
	 * 空输出，主要为了调试，和有的程序不需要通过warehouse输出
	 */
	public static final int EXPORT_EMPTY = -1;

	protected int id;

	/**
	 * 输出器类型，不用的输出器类型会实例化不同的Exporter
	 */
	protected int type;

	/**
	 * 输出开关 默认打开
	 */
	protected boolean on = true;

	/**
	 * 数据类型
	 */
	protected int dataType;

	protected int targetId;

	protected ExporterBean exportTargetBean;

	protected String encode;

	public boolean isOn() {
		return on;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public ExporterBean getExportTargetBean() {
		return exportTargetBean;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public void setExportTargetBean(ExporterBean exportTargetBean) {
		this.exportTargetBean = exportTargetBean;
	}
}
