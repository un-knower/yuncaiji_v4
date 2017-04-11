package cn.uway.framework.warehouse.exporter.template;

/**
 * Column template for export
 * 
 * @author Spike
 * @date 2012/11/01
 * @version 1.0
 * @since 1.0
 */
public class ColumnTemplateBean{

	private String columnName; // column name

	private String propertyName; // property name

	private String format; // format (such as yyyy-mm-dd hh24:mi:ss)

	private String isSpan; // set defaultValue as part of sql. 

	private String defaultValue;
	
	private String sequence; // 序列名，一般用于主键字段

	public ColumnTemplateBean(String columnName, String propertyName){
		super();
		this.columnName = columnName;
		this.propertyName = propertyName;
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

	public String getDefaultValue(){
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue){
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the isSpan
	 */
	public String getIsSpan(){
		return isSpan;
	}

	/**
	 * @param isSpan the isSpan to set
	 */
	public void setIsSpan(String isSpan){
		this.isSpan = isSpan;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

}
