package cn.uway.ucloude.utils;

import java.text.DecimalFormat;

public class NumberUtil extends org.apache.commons.lang3.math.NumberUtils {

	/**
	 * 转换成整形，区别于普通函数会抛出异常
	 * 
	 * @param str
	 *            待转换的"数字"字符串
	 * @return　如果转换成功，返回转换后的值，否则返回null
	 */
	public static Short parseShort(String str) {
		try {
			return Short.parseShort(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 转换成整形，区别于普通函数会抛出异常
	 * 
	 * @param str
	 *            待转换的"数字"字符串
	 * @return　如果转换成功，返回转换后的值，否则返回null
	 */
	public static Integer parseInteger(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 转换成整形，区别于普通函数会抛出异常
	 * 
	 * @param str
	 *            待转换的"数字"字符串
	 * @return　如果转换成功，返回转换后的值，否则返回null
	 */
	public static Long parseLong(String str) {
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 转换成整形，区别于普通函数会抛出异常
	 * 
	 * @param str
	 *            待转换的"数字"字符串
	 * @return　如果转换成功，返回转换后的值，否则返回null
	 */
	public static Float parseFloat(String str) {
		try {
			return Float.parseFloat(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 转换成整形，区别于普通函数会抛出异常
	 * 
	 * @param str
	 *            待转换的"数字"字符串
	 * @return　如果转换成功，返回转换后的值，否则返回null
	 */
	public static Double parseDouble(String str) {
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * format将一个字符串，按照pattern样式格式化，比如科学记数法、千分位分割记数、小数为保持精度等
	 * 
	 * @param val
	 *            待格式化的数字
	 * @param pattern
	 *            转换样式，样式采用DecimalFormat的格式规范
	 * @return 返回格式后的结果，如果格式化错误，则返回空值。
	 */
	public static String format(Number val, String pattern) {
		try {
			DecimalFormat df = new DecimalFormat(pattern);
			return df.format(val);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 比较两个浮点数的值是否相等，因为浮点数的值不能用equal或=号直接比较的，
	 * 所以需要实现在一个在指定精度下两个浮点数的差值是否小于一个固定值，则判断为相等，否则为不等。
	 * @param vall		待比较的浮点数1
	 * @param val2		待比较的浮点数2
	 * @param percision	精度保留位数
	 * @return	返回两者小于精度值的取值范围，则返回true，否则返回false。
	 */
	public static boolean isEqual(Float vall, Float val2, int percision) {
		return Math.abs(vall - val2) < Math.pow(0.1, percision);
	}
	
	/**
	 * 比较两个浮点数的值是否相等，因为浮点数的值不能用equal或=号直接比较的，
	 * 所以需要实现在一个在指定精度下两个浮点数的差值是否小于一个固定值，则判断为相等，否则为不等。
	 * @param vall		待比较的浮点数1
	 * @param val2		待比较的浮点数2
	 * @param percision	精度保留位数
	 * @return	返回两者小于精度值的取值范围，则返回true，否则返回false。
	 */
	public static boolean isEqual(Double vall, Double val2, int percision) {
		return Math.abs(vall - val2) < Math.pow(0.1, percision);
	}
}
