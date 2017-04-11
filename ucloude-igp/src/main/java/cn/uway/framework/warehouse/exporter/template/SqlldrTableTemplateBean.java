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
public class SqlldrTableTemplateBean {

	/**
	 * 输出数据库表名
	 */
	private String tableName;

	/**
	 * 当前输出数据库表包含的所有列的信息<br>
	 * K:字段名称 V:字段详细信息
	 */
	private Map<String, SqlldrColumnTemplateBean> columns = new HashMap<String, SqlldrColumnTemplateBean>();

	/**
	 * 输出字段列表，有序的.
	 */
	private List<SqlldrColumnTemplateBean> columnsList = new ArrayList<SqlldrColumnTemplateBean>();

	public Map<String, SqlldrColumnTemplateBean> getColumns() {
		return columns;
	}

	public List<SqlldrColumnTemplateBean> getColumnsList() {
		return columnsList;
	}

	public void removeColumn(String columnName) {
		if (columnName == null || columnName.trim().length() < 1) {
			return;
		}

		columns.remove(columnName);
		for (SqlldrColumnTemplateBean cTemplate : columnsList) {
			if (cTemplate.getColumnName().equalsIgnoreCase(columnName)) {
				columnsList.remove(cTemplate);
				break;
			}
		}
	}

	public void setColumn(String columnName, String propertyName) {
		this.setColumn(columnName, propertyName, null, null);
	}

	public void setColumn(String columnName, String propertyName, String type,
			String format) {
		if (columns.containsKey(columnName))
			return;
		if (columnName == null || columnName.trim().length() < 1
				|| propertyName == null || propertyName.trim().length() < 1) {
			return;
		}
		if ((!("2".equals(type.trim()) || "3".equals(type.trim())))
				|| format == null || format.trim().length() < 1) {
			SqlldrColumnTemplateBean cTemplate = new SqlldrColumnTemplateBean(
					columnName, propertyName);
			columns.put(columnName, cTemplate);
			columnsList.add(cTemplate);
		} else {
			SqlldrColumnTemplateBean cTemplate = new SqlldrColumnTemplateBean(
					columnName.trim(), propertyName.trim(), type.trim(),
					format.trim());
			columns.put(columnName, cTemplate);
			columnsList.add(cTemplate);
		}
	}

	public SqlldrColumnTemplateBean getColumn(String columnName) {
		return columns.get(columnName);
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public final void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the tableName
	 */
	public final String getTableName() {
		return tableName;
	}

}
