package cn.uway.framework.warehouse.exporter.template;

/**
 * Sqlldr column template for export
 * 
 * @author linp
 * @date 2015/06/08
 * @version 1.0
 * @since 1.0
 */
public class SqlldrColumnTemplateBean{

	private String columnName; // column name

	private String propertyName; // property name
	
	private String type; // type (2为大字段(超过255)，3为时间)

	private String format; // format (such as yyyy-mm-dd hh24:mi:ss)
	
	private String isSpan;
	
	private String defaultValue;

	public SqlldrColumnTemplateBean(String columnName, String propertyName){
		super();
		this.columnName = columnName;
		this.propertyName = propertyName;
	}
	
	public SqlldrColumnTemplateBean(String columnName, String propertyName, String type, String format){
		super();
		this.columnName = columnName;
		this.propertyName = propertyName;
		this.type = type;
		this.format = format;
	}

	public String getColumnName(){
		return columnName;
	}

	public String getPropertyName(){
		return propertyName;
	}

	public String getFormat(){
		return format;
	}

	public void setFormat(String format){
		this.format = format;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIsSpan() {
		return isSpan;
	}

	public void setIsSpan(String isSpan) {
		this.isSpan = isSpan;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
