package cn.uway.framework.warehouse.exporter.template;

import cn.uway.framework.connection.DatabaseConnectionInfo;

/**
 * DatabaseExporterBean 数据库输出目标配置<br>
 * 1、配置来源于IGP_CFG_DB_EXPORT
 * 
 * @author chenrongqiang 2012-11-12
 */
public class DatabaseExporterBean extends ExporterBean{

	/**
	 * 输出连接信息
	 */
	private DatabaseConnectionInfo connectionInfo;

	/**
	 * 批量提交条数
	 */
	private int batchNum;

	public void setBatchNum(int batchNum){
		this.batchNum = batchNum;
	}

	public int getBatchNum(){
		return batchNum;
	}

	public DatabaseConnectionInfo getConnectionInfo(){
		return connectionInfo;
	}

	public void setConnectionInfo(DatabaseConnectionInfo connectionInfo){
		this.connectionInfo = connectionInfo;
	}

	@Override
	public String toString(){
		return "DBExportTargetBean [connectionInfo=" + connectionInfo + ", batchNum=" + batchNum + "]";
	}
}
