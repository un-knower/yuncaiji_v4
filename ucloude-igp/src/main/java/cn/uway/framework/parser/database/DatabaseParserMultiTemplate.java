package cn.uway.framework.parser.database;

import java.util.List;

/**
 * 多语句模板
 * 
 * @author tylerlee @ 2016年3月18日
 */
public class DatabaseParserMultiTemplate {

	// 查询SQL语句
	private List<String> sqlList;

	// 模版ID
	private long id;

	private Integer dataType;

	private String busType;

	private int typeId;// label_type_id

	private boolean others;
	
	private boolean isConf;

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the dataType
	 */
	public final Integer getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public final void setDataType(Integer dataType) {
		this.dataType = dataType;
	}

	public String getBusType() {
		return busType;
	}

	public void setBusType(String busType) {
		this.busType = busType;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public boolean isOthers() {
		return others;
	}

	public void setOthers(boolean others) {
		this.others = others;
	}

	public boolean isConf() {
		return isConf;
	}

	public void setConf(boolean isConf) {
		this.isConf = isConf;
	}
	
}
