package cn.uway.ucloude.data.dataaccess.builder;

import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.utils.StringUtil;

public final class SqlUtil {
	public static <T> void getWhere(String filedName, LogicOptType logicType, T value, List<String> whereList,
			List<Object> params) {
		if (value != null) {
			String where = filedName;
			if (value.getClass() == String.class) {
				String strValue = (String) value;
				if (!StringUtil.isNotEmpty(strValue)) {
					return;
				} else if (strValue == "null") {
					where += " is null";
				} else {
					where += logicType.getDesc() + "?";
					params.add(value);
				}
			} else if (value instanceof Enum) {
				where += logicType.getDesc() + "?";
				Enum enumValue = (Enum) value;
				params.add(enumValue.ordinal());
			} else if (value.getClass() == Date.class) {
				Date date = (Date) value;
				where += logicType.getDesc() + "?";
				params.add(new java.sql.Date(date.getTime()));
			} else {
				where += logicType.getDesc() + "?";
				params.add(value);
			}
			whereList.add(where);
		}
	}

	public static <T> void getSetFields(String fieldName, T value, List<String> FieldList, List<Object> params) {
		if (value != null) {
			FieldList.add(fieldName);
			if (value.getClass() == String.class) {
				String strValue = (String) value;
				if (strValue == "null") {
					params.add(null);
				} else {
					params.add(value);
				}
			} else if (value instanceof Enum) {
				Enum enumValue = (Enum) value;
				params.add(enumValue.ordinal());
			} else if (value.getClass() == Date.class) {
				Date date = (Date) value;
				params.add(new java.sql.Date(date.getTime()));
			} else {
				params.add(value);
			}
		}
	}
}
