package cn.uway.framework.parser.file.templet;

import java.util.List;

/**
 * csv模板描述
 * 
 * @author yuy
 */
public class Templet {

	public int id;

	public String dataName;

	public int dataType;

	public String encoding;

	/** 内容分隔符 */
	public String splitSign;

	/** 跳过的行数 */
	public Integer skipLines;

	public List<Field> fieldList;

	public int getId() {
		return id;
	}

	public List<Field> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<Field> fieldList) {
		this.fieldList = fieldList;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	/**
	 * @return splitSign
	 */
	public String getSplitSign() {
		return splitSign;
	}

	/**
	 * @param splitSign
	 */
	public void setSplitSign(String splitSign) {
		this.splitSign = splitSign;
	}

	/**
	 * @return skipLines
	 */
	public Integer getSkipLines() {
		return skipLines;
	}

	/**
	 * @param skipLines
	 */
	public void setSkipLines(Integer skipLines) {
		this.skipLines = skipLines;
	}

	@Override
	public String toString() {
		return "Templet [id=" + id + ", dataName=" + dataName + ", dataType=" + dataType + "]";
	}
}
