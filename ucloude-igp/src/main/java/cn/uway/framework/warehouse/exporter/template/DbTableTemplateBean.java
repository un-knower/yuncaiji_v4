package cn.uway.framework.warehouse.exporter.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database table column mapping configuration
 * 
 * @author Spike
 * @date 2012/11/01
 * @version 1.0
 * @since 1.0
 */
public class DbTableTemplateBean{

	/**
	 * insert 对数据执行插入更新表，默认。
	 */
	public static final String STORAGE_INSERT_TYPE = "insert";

	/**
	 * update 对数据执行更新和删除动作
	 */
	public static final String STORAGE_UPDATE_TYPE = "update";

	/**
	 * 输出数据库表名
	 */
	private String tableName;

	/**
	 * 存储类型
	 */
	public String storageType;

	/**
	 * 存储SQL
	 */
	public String sql = "";

	/**
	 * 当前输出数据库表包含的所有列的信息<br>
	 * K:字段名称 V:字段详细信息
	 */
	private Map<String,ColumnTemplateBean> columns = new HashMap<String,ColumnTemplateBean>();
	
	/**
	 * 输出字段列表，有序的.
	 */
	private List<ColumnTemplateBean> columnsList = new ArrayList<ColumnTemplateBean>();

	/**
	 * @return the sql
	 */
	public final String getSql(){
		return sql;
	}

	/**
	 * @param sql the sql to set
	 */
	public final void setSql(String sql){
		this.sql = sql;
	}

	public Map<String,ColumnTemplateBean> getColumns(){
		return columns;
	}
	
	public List<ColumnTemplateBean> getColumnsList() {
		return columnsList;
	}
	
	public void removeColumn(String columnName) {
		if(columnName == null || columnName.trim().length() < 1){
			return;
		}
		
		columns.remove(columnName);
		for (ColumnTemplateBean cTemplate : columnsList) {
			if (cTemplate.getColumnName().equalsIgnoreCase(columnName)) {
				columnsList.remove(cTemplate);
				break;
			}
		}
	}
	
	public void setColumn(String columnName, String propertyName){
		if(columnName == null || columnName.trim().length() < 1){
			return;
		}
		if(propertyName == null || propertyName.trim().length() < 1){
			return;
		}
		// No duplicate for column name
		if(!columns.containsKey(columnName)){
			ColumnTemplateBean cTemplate = new ColumnTemplateBean(columnName, propertyName);
			columns.put(columnName, cTemplate);
			columnsList.add(cTemplate);
		}
	}

	public void setColumn(String columnName, String propertyName, String format){
		this.setColumn(columnName, propertyName);
		if(format == null || format.trim().length() < 1){
			return;
		}
		if(columns.containsKey(columnName)){
			ColumnTemplateBean cTemplate = columns.get(columnName);
			cTemplate.setFormat(format);
		}
	}

	public ColumnTemplateBean getColumn(String columnName){
		return columns.get(columnName);
	}

	/**
	 * @return the storageType
	 */
	public final String getStorageType(){
		return storageType;
	}

	/**
	 * @param storageType the storageType to set
	 */
	public final void setStorageType(String storageType){
		if(storageType == null || "".equals(storageType))
			this.storageType = STORAGE_INSERT_TYPE;
		else
			this.storageType = storageType;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public final void setTableName(String tableName){
		this.tableName = tableName;
	}

	/**
	 * @return the tableName
	 */
	public final String getTableName(){
		return tableName;
	}

}
