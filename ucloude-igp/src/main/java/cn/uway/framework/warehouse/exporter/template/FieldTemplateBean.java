package cn.uway.framework.warehouse.exporter.template;

/**
 * Field configuration of export to file template
 * 
 * @author Spike
 * @date 2012/11/01
 * @version 1.0
 * @since 1.0
 */
public class FieldTemplateBean {

	/**
	 * 字段属性名称
	 */
	private String propertyName; 
	
	private String columnName;

	/**
	 * 字段格式化格式.主要是时间字段
	 */
	private String format; 

	public FieldTemplateBean() {
		super();
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String fieldName) {
		this.propertyName = fieldName;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
}
