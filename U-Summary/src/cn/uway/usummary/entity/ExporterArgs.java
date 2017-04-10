package cn.uway.usummary.entity;

import java.util.Date;
import java.util.List;

import cn.uway.usummary.warehouse.repository.Repository;

public class ExporterArgs {
	
	private long sqlNum;
	
	private int storageType;
	
	private ConnectionInfo connInfo;
	
	private DBExportInfo dbExpInfo;
	
	private FtpExportInfo ftpExpInfo;
	
	private Repository repository;
	
	private List<String> headers;
	
	private Date dataTime;

	public long getSqlNum() {
		return sqlNum;
	}

	public void setSqlNum(long sqlNum) {
		this.sqlNum = sqlNum;
	}

	public int getStorageType() {
		return storageType;
	}

	public void setStorageType(int storageType) {
		this.storageType = storageType;
	}

	public ConnectionInfo getConnInfo() {
		return connInfo;
	}

	public void setConnInfo(ConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}

	public DBExportInfo getDbExpInfo() {
		return dbExpInfo;
	}

	public void setDbExpInfo(DBExportInfo dbExpInfo) {
		this.dbExpInfo = dbExpInfo;
	}

	public FtpExportInfo getFtpExpInfo() {
		return ftpExpInfo;
	}

	public void setFtpExpInfo(FtpExportInfo ftpExpInfo) {
		this.ftpExpInfo = ftpExpInfo;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}
	
	
}
