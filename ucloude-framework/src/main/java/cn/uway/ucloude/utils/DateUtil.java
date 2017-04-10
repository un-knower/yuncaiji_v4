package cn.uway.ucloude.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class DateUtil extends org.apache.commons.lang3.time.DateUtils {

	/**
	 * <pre>
	 * 常量特殊符号命名定义规则(因为有些特定符号不能定义成变量名，则用下列符号替换）：
	 * 横  线：“－”	使用2个连续的“_”表示，例：“__”
	 * 空  格：“ ”	使用4个连续的“_”表示，例：“____”
	 * 冒  号：“：”	使用“$”号代替
	 * </pre>
	 */
	private static int enumVal = 9;

	public static enum TimePattern {
		yyyyMMdd(0, "yyyyMMdd", "\\d{8}"), 
		yyyyMMddHH(1, "yyyyMMddHH", "\\d{10}"), 
		yyyyMMddHHmm(2,	"yyyyMMddHHmm", "\\d{12}"), 
		yyyyMMddHHmmss(3, "yyyyMMddHHmmss", "\\d{14}"), 
		yyyyMMddHHmmssSSS(4, "yyyyMMddHHmmssSSS", "\\d{17}"), 
		yyyyMMdd_HHmmss(5, "yyyyMMdd_HHmmss", "\\d{8}_\\d{6}"), 
		yyyyMMdd__HHmmss(6, "yyyyMMdd-HHmmss", "\\d{8}-\\d{6}"), 
		yyyy__MM__1dd____HH$mm$ss(7, "yyyy-MM-dd HH:mm:ss", "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
		yyyy__MM_1dd(8, "yyyy-MM-dd","^\\d{4}-\\d{1,2}-\\d{1,2}$");
		
		protected int val = 0;

		protected String pattern;

		protected String regx;

		private TimePattern(int val, String pattern, String regx) {
			this.val = val;
			this.pattern = pattern;
			this.regx = regx;
		}
	}

	private static Object datetimeFormats[] = null;

	private static Pattern datetimeRegxPatterns[] = null;
	static {
		datetimeFormats = new Object[enumVal];
		datetimeRegxPatterns = new Pattern[enumVal];
		int i = 0;
		for (final TimePattern timePattern : TimePattern.values()) {
			datetimeRegxPatterns[i] = Pattern.compile(timePattern.regx);
			datetimeFormats[i] = new ThreadLocal<SimpleDateFormat>() {

				protected synchronized SimpleDateFormat initialValue() {
					return new SimpleDateFormat(timePattern.pattern);
				}
			};

			++i;
		}
	}

	@SuppressWarnings("unchecked")
	private static SimpleDateFormat getDateTimeFormat(TimePattern timePattern) {
		int valIndex = timePattern.val;
		return ((ThreadLocal<SimpleDateFormat>) datetimeFormats[valIndex])
				.get();
	}

	/**
	 * 将一个日期字符串，按指定格式，转换成日期格式
	 * 
	 * @param dateStr
	 *            要转换成日期的字符串，该字符串的格式必须是："yyyy-MM-dd HHmmss"
	 * @return 如果转换成功，返回转换后的Date对象，如果转换失败，则抛出ParseException异常。
	 * @throws ParseException
	 */
	public static Date parse(String dateStr) throws ParseException {
		return parse(dateStr, TimePattern.yyyy__MM__1dd____HH$mm$ss);
	}

	/**
	 * 将一个日期字符串，按指定格式，转换成日期格式
	 * 
	 * @param dateStr
	 *            要转换成日期的字符串
	 * @param pattern
	 *            字符串的时间格式
	 * @return 如果转换成功，返回转换后的Date对象，如果转换失败，则抛出ParseException异常。
	 * @throws ParseException
	 */
	public static Date parse(String dateStr, TimePattern pattern)
			throws ParseException {
		SimpleDateFormat dateFormat = getDateTimeFormat(pattern);
		if (dateFormat == null)
			throw new ParseException(
					"unknow TimePattern:" + pattern.toString(), 0);

		return dateFormat.parse(dateStr);
	}

	/**
	 * 将一个日期字符串，按指定格式，转换成日期格式
	 * 
	 * @param dateStr
	 *            要转换成日期的字符串
	 * @param pattern
	 *            字符串的时间格式通匹符，必须符合SimpleDateFormat的格式要求
	 * @return 如果转换成功，返回转换后的Date对象，如果转换失败，则抛出ParseException异常。
	 * @throws ParseException
	 */
	public static Date parseByUserDefinedPattern(String dateStr, String pattern)
			throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

		return dateFormat.parse(dateStr);
	}

	/**
	 * 将一个日期字符串，按指定格式，转换成日期格式
	 * 
	 * @param date
	 *            待格式化的日期，默认格式为："yyyy-MM-dd HHmmss"
	 * @return 返回将date按pattern转换后的字符串
	 * @throws ParseException
	 */
	public static String format(Date date) throws ParseException {
		return format(date, TimePattern.yyyy__MM__1dd____HH$mm$ss);
	}

	/**
	 * 将一个日期字符串，按指定格式，转换成日期格式
	 * 
	 * @param date
	 *            待格式化的日期
	 * @param pattern
	 *            时间格式(预定义)
	 * @return 返回将date按pattern转换后的字符串
	 * @throws ParseException
	 */
	public static String format(Date date, TimePattern pattern)
			throws ParseException {
		SimpleDateFormat dateFormat = getDateTimeFormat(pattern);
		if (dateFormat == null)
			throw new ParseException(
					"unknow TimePattern:" + pattern.toString(), 0);
		return dateFormat.format(date);
	}
	
	public static String formatNonException(Date date, TimePattern pattern)
    {
		SimpleDateFormat dateFormat = getDateTimeFormat(pattern);
		return dateFormat.format(date);
	}

	/**
	 * 将一个日期字符串，按指定格式，转换成日期格式.尽量用预定义的时间格式，
	 * 否则此函数每一次都会生成一个SimpleDateFormat对象，对批量处理时效率低下
	 * 
	 * @param date
	 *            待格式化的日期
	 * @param pattern
	 *            时间格式(自定义)
	 * @return 返回将date按pattern转换后的字符串
	 * @throws ParseException
	 */
	public static String formatByUserDefinedPattern(Date date, String pattern)
			throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(date);
	}

	/**
	 * 
	 * @param fileName
	 *            文件名字符串
	 * @param pattern
	 *            间格式(预定义)
	 * @return 返回从文件名中解析出的datetime，如果解析出现错误，则抛出ParseException异常。
	 * @throws ParseException
	 */
	public static Date parseTimeByeFileName(String fileName, TimePattern pattern)
			throws ParseException {

		String[] dateStrs = StringUtil.getPatterns(fileName, pattern.regx, 0);
		for (String dateStr : dateStrs) {
			try {
				Date d = parse(dateStr, pattern);
				if (isValidDate(d))
					return d;

			} catch (ParseException e) {

			}
		}

		return null;
	}

	/**
	 * 
	 * @param fileName
	 *            文件名字符串
	 * @param pattern
	 *            时间格式(自定义)
	 * @param regx
	 *            时间在字符串fileName的正则表达式(自定义)
	 * @return 返回从文件名中解析出的datetime，如果解析出现错误，则抛出ParseException异常。
	 * @throws ParseException
	 */
	public static Date parseTimeByeFileName(String fileName, String pattern,
			String regx) throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat(pattern);
		String[] dateStrs = StringUtil.getPatterns(fileName, regx, 0);
		for (String dateStr : dateStrs) {
			try {
				Date d = df.parse(dateStr);
				if (isValidDate(d))
					return d;
			} catch (ParseException e) {

			}
		}

		return null;
	}
	
	/**
	 * 检验日期是否正确
	 * @param date 待检验的日期
	 * @return 如果日期检验通过返回true, 否则返回false
	 */
	public static boolean isValidDate(Date date) {
		long millSecondDiff = Math.abs(date.getTime() - date.getTime());
		// 一般文件名上的时间都在当前系统时期10年内，超过大部份是有问题的
		if (millSecondDiff > (10 * 365 * 24 * 60 * 60 * 1000l))
			return false;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minu = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);

		if ((month >= 0 && month <= 11) && (day >= 1 && day <= 31)
				&& (hour >= 0 && hour <= 23) && (minu >= 0 && minu <= 59)
				&& (sec >= 0 && sec <= 59)) {
			return true;
		}

		return false;
	}
	
	public static void main(String[] args) throws ParseException {
		System.out.println(DateUtil.format(new Date(), TimePattern.yyyyMMddHHmmssSSS));
		
		System.out.println(
				DateUtil.format(
						DateUtil.parseTimeByeFileName(
								"FDD-LTE_MRE_NSN_OMC_241943_20150915000000.xml", 
								TimePattern.yyyyMMddHHmmss),
						TimePattern.yyyy__MM__1dd____HH$mm$ss
						));

	}
}
