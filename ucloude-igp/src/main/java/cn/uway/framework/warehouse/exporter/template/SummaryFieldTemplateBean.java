package cn.uway.framework.warehouse.exporter.template;

/**
 * Field configuration of export to file template
 * 
 * @author Spike
 * @date 2012/11/01
 * @version 1.0
 * @since 1.0
 */
public class SummaryFieldTemplateBean extends FieldTemplateBean {

	/**
	 * 字段长度
	 */
	private int length;

	/**
	 * 起始位置
	 */
	private int fromLen;

	/**
	 * 运算公式
	 */
	private String formula;

	/**
	 * 是否隐藏，虚拟字段，针对多种运算
	 */
	private boolean hidden;

	/**
	 * 汇总关键字，类似于数据库的group by操作
	 */
	private String groupBy;

	/**
	 * 该字段所属的表名，起到归类作用，便于维护
	 */
	private String tableName;
	
	/**
	 * 原indexKeys，用于获取正确的indexKeys
	 */
	private String srcIndexKeys;
	
	/**
	 * 原dataType，同解析模板中的dataType
	 */
	private Integer srcDataType;

	/**
	 * @return 字段长度
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length
	 *            字段长度
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return 起始位置
	 */
	public int getFromLen() {
		return fromLen;
	}

	/**
	 * @param fromLen
	 *            起始位置
	 */
	public void setFromLen(int fromLen) {
		this.fromLen = fromLen;
	}

	/**
	 * @return 运算公式
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @param formula
	 *            运算公式
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * @return 是否隐藏，虚拟字段，针对多种运算
	 */
	public boolean getHidden() {
		return hidden;
	}

	/**
	 * @param hidden
	 *            是否隐藏，虚拟字段，针对多种运算
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return 汇总关键字，类似于数据库的group by操作
	 */
	public String getGroupBy() {
		return groupBy;
	}

	/**
	 * @param groupBy
	 *            汇总关键字，类似于数据库的group by操作
	 */
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	/**
	 * @return 该字段所属的表名，起到归类作用，便于维护
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            该字段所属的表名，起到归类作用，便于维护
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	
	/**
	 * @return srcIndexKeys
	 */
	public String getSrcIndexKeys() {
		return srcIndexKeys;
	}

	
	/**
	 * @param srcIndexKeys
	 */
	public void setSrcIndexKeys(String srcIndexKeys) {
		this.srcIndexKeys = srcIndexKeys;
	}

	
	/**
	 * @return srcDataType
	 */
	public Integer getSrcDataType() {
		return srcDataType;
	}

	
	/**
	 * @param srcDataType
	 */
	public void setSrcDataType(Integer srcDataType) {
		this.srcDataType = srcDataType;
	}

}
