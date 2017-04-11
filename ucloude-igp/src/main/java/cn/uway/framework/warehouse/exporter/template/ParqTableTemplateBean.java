package cn.uway.framework.warehouse.exporter.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParqTableTemplateBean {

	/**
	 * 输出数据库表名
	 */
	private String tableName;

	/**
	 * 当前输出数据库表包含的所有列的信息<br>
	 * K:字段名称 V:字段详细信息
	 */
	private Map<String, ColumnTemplateBean> columns = new HashMap<String, ColumnTemplateBean>();

	/**
	 * 输出字段列表，有序的.
	 */
	private List<ColumnTemplateBean> columnsList = new ArrayList<ColumnTemplateBean>();

	public Map<String, ColumnTemplateBean> getColumns() {
		return columns;
	}

	public List<ColumnTemplateBean> getColumnsList() {
		return columnsList;
	}

	public void removeColumn(String columnName) {
		if (columnName == null || columnName.trim().length() < 1) {
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

	public void setColumn(String columnName, String propertyName) {
		if (columnName == null || columnName.trim().length() < 1) {
			return;
		}
		if (propertyName == null || propertyName.trim().length() < 1) {
			return;
		}
		// No duplicate for column name
		if (!columns.containsKey(columnName)) {
			ColumnTemplateBean cTemplate = new ColumnTemplateBean(columnName,
					propertyName);
			columns.put(columnName, cTemplate);
			columnsList.add(cTemplate);
		}
	}

	public void setColumn(String columnName, String propertyName, String format) {
		this.setColumn(columnName, propertyName);
		if (format == null || format.trim().length() < 1) {
			return;
		}
		if (columns.containsKey(columnName)) {
			ColumnTemplateBean cTemplate = columns.get(columnName);
			cTemplate.setFormat(format);
		}
	}

	public ColumnTemplateBean getColumn(String columnName) {
		return columns.get(columnName);
	}

	public final void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public final String getTableName() {
		return tableName;
	}

}
