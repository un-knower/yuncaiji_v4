package cn.uway.framework.parser.database;

/**
 * 数据库解析模版 DbParserTemplet
 * 
 * @author chenrongqiang 2012-12-4
 */
public class DatabaseParserTemplate {

	// 查询SQL语句
	private String sql;

	//sql前缀，用于公共语名的优化；
	private String prefixSql;

	// 模版ID
	private long id;

	private Integer dataType;

	private int typeId;// label_type_id

	private String busType;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
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

	public String getPrefixSql() {
		return prefixSql;
	}

	public void setPrefixSql(String prefixSql) {
		this.prefixSql = prefixSql;
	}

}
