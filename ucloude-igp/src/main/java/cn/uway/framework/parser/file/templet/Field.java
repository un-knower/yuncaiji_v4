package cn.uway.framework.parser.file.templet;

import java.util.List;

/**
 * csv模板 列描述
 * 
 * @author yuy
 */
public class Field{

	/* 列名 */
	private String name;

	/* 索引 */
	private String index;

	/* 是否过滤掉毫秒 */
	private String isPassMS;

	/* 是否分拆（特殊，适用场景：爱立信性能） */
	private String isSpecialSplit;

	/*
	 * 1.单独配置isSplit时正常拆分，这个时候属性个数=值个数。
	 * 2.单独配置isDirectSplit时为特殊场景拆分，如爱立信性能。这个时候属性个数不一定等于值个数
	 * 3.现在新增逻辑 isSplit与 isDirectSplit同时配置为true，实现的功能是值个数少于属性个数时不丢弃这种数据;
	 * 	新逻辑：
	 * 			a.取数据以逗号分割，得到数组（文件中用逗号连接的数字或是字符）;
	 * 			b.取字段的子字段列表;
	 * 			c.按数组元素逐个为子字段列表赋值。
	 * */
	
	/* 是否直接分拆（特殊，适用场景：爱立信性能） */
	private String isDirectSplit;

	/* 是否分拆 */
	private String isSplit;

	/* 是否按照顺序读取 */
	private String isOrder;

	/* 是否有多个表达式 */
	private String hasOtherRegexs;

	/* 表达式数量 */
	private int regexsNum;

	/* 表达式分隔符 */
	private String regexsSplitSign;

	/* 表达式内容 */
	private String regex;// eg:?/eNodeB功能:eNodeB名称=?

	/* 分拆的子字段列表 */
	private List<Field> subFieldList;
	
	/*是否sha1加密*/
	private boolean isSha1 ;

	private String type;

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	private boolean isHex;

	public boolean isHex(){
		return isHex;
	}

	public void setHex(boolean isHex){
		this.isHex = isHex;
	}

	/**
	 * @return name
	 */
	public String getName(){
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * @return index
	 */
	public String getIndex(){
		return index;
	}

	/**
	 * @param index
	 */
	public void setIndex(String index){
		this.index = index;
	}

	/**
	 * @return isPassMS
	 */
	public String getIsPassMS(){
		return isPassMS;
	}

	/**
	 * @param isPassMS
	 */
	public void setIsPassMS(String isPassMS){
		this.isPassMS = isPassMS;
	}

	/**
	 * @return isSpecialSplit
	 */
	public String getIsSpecialSplit(){
		return isSpecialSplit;
	}

	/**
	 * @param isSpecialSplit
	 */
	public void setIsSpecialSplit(String isSpecialSplit){
		this.isSpecialSplit = isSpecialSplit;
	}

	/**
	 * @return isDirectSplit
	 */
	public String getIsDirectSplit(){
		return isDirectSplit;
	}

	/**
	 * @param isDirectSplit
	 */
	public void setIsDirectSplit(String isDirectSplit){
		this.isDirectSplit = isDirectSplit;
	}

	/**
	 * @return isSplit
	 */
	public String getIsSplit(){
		return isSplit;
	}

	/**
	 * @param isSplit
	 */
	public void setIsSplit(String isSplit){
		this.isSplit = isSplit;
	}

	/**
	 * @return isOrder
	 */
	public String isOrder(){
		return isOrder;
	}

	/**
	 * @param isOrder
	 */
	public void setOrder(String isOrder){
		this.isOrder = isOrder;
	}

	/**
	 * @return hasOtherRegexs
	 */
	public String getHasOtherRegexs(){
		return hasOtherRegexs;
	}

	/**
	 * @param hasOtherRegexs
	 */
	public void setHasOtherRegexs(String hasOtherRegexs){
		this.hasOtherRegexs = hasOtherRegexs;
	}

	/**
	 * @return regexsNum
	 */
	public int getRegexsNum(){
		return regexsNum;
	}

	/**
	 * @param regexsNum
	 */
	public void setRegexsNum(int regexsNum){
		this.regexsNum = regexsNum;
	}

	/**
	 * @return regexsSplitSign
	 */
	public String getRegexsSplitSign(){
		return regexsSplitSign;
	}

	/**
	 * @param regexsSplitSign
	 */
	public void setRegexsSplitSign(String regexsSplitSign){
		this.regexsSplitSign = regexsSplitSign;
	}

	/**
	 * @return regex
	 */
	public String getRegex(){
		return regex;
	}

	/**
	 * @param regex
	 */
	public void setRegex(String regex){
		this.regex = regex;
	}

	/**
	 * @return subFieldList
	 */
	public List<Field> getSubFieldList(){
		return subFieldList;
	}

	/**
	 * @param subFieldList
	 */
	public void setSubFieldList(List<Field> subFieldList){
		this.subFieldList = subFieldList;
	}

	@Override
	public String toString(){
		return "Field [index=" + index + ", name=" + name + ", isSplit=" + isSplit + "]";
	}

	public boolean isSha1() {
		return isSha1;
	}

	public void setSha1(boolean isSha1) {
		this.isSha1 = isSha1;
	}

}
