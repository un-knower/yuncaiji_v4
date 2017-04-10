package cn.uway.usummary.util;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.nfunk.jep.JEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符串工具类。
 * 
 */
public final class StringUtil {

	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

	public static Boolean isBlank(String str) {
		return StringUtils.isBlank(str);
	}

	/**
	 * 从路径中获取文件名
	 * 
	 * @param ftpPath
	 * @return filename (without path)
	 */
	public static String getFilename(String ftpPath) {
		return FilenameUtils.getName(ftpPath);
	}

	/** 计算字符串表达式的值 ，比如 字符串"1+2" 计算后为3 */
	public static int parseExpression(String str) {
		JEP myParser = new JEP();

		myParser.parseExpression(str);

		return (int) myParser.getValue();
	}

	/** 当时间为0-9的时候返回 00-09，为10-23的时候返回10-23 */
	public static String trimHour(int hour) {
		String str = String.valueOf(hour);
		if (str.length() == 1) {
			str = "0" + str;
		}

		return str;
	}

	/** 取（）之间的内容 */
	public static String getExpression(String str) {

		String result = null;

		int b = str.indexOf("(");
		int e = str.indexOf(")");

		if (b > 0 && e > b) {
			result = str.substring(b + 1, e);
		}

		if (result != null && !result.contains("%%"))
			return null;

		return result;
	}

	public static String ParseFilePath(String strPath, Timestamp timestamp) {
		// 如：zblecp-2006102311.hsmr 文件名包含时间，表示这个时间的数据。
		// 这个时间有是timestamp传递进来的时间
		// 但是，有的文件名是一个时间段，前后有2个时间。
		// 如：Domain125_PSbasicmeasurement_18Jul2008_0900-18Jul2008_1000.csv
		// 现在从strPath传递进来一个参数表示前后时间的间隔数，
		// 如：Domain125_PSbasicmeasurement_%%D%%EM%%Y_%%H%%m-%%ND%%ENM%%NY_%%NH%%Nm.csv|360000

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

		int iHwrIdx = strPath.indexOf("%%TA");
		int iDiff = 0;
		if (iHwrIdx >= 0) {
			// 偏移量 0－9，只支持正偏移
			iDiff = Integer.parseInt(strPath.substring(iHwrIdx + 4, iHwrIdx + 5));
			strPath = strPath.replaceAll("%%TA.", "");
		}

		timestamp = new Timestamp(timestamp.getTime() + iDiff * 3600 * 1000);
		String strTime = formatter.format(timestamp);

		if (strPath.indexOf("%%Y") >= 0)
			strPath = strPath.replace("%%Y", strTime.substring(0, 4));
		Calendar calendar = Calendar.getInstance();

		Date date = new Date();
		date.setTime(timestamp.getTime());
		calendar.setTime(new Date());

		calendar.setTime(date);
		int nDayOrYear = calendar.get(Calendar.DAY_OF_YEAR);

		if (strPath.indexOf("%%WEEK") >= 0) {
			int dow = calendar.get(Calendar.DAY_OF_WEEK);
			dow = dow - 1;
			if (dow == 0)
				dow = 7;
			strPath = strPath.replace("%%WEEK", String.valueOf(dow));
		}

		if (nDayOrYear < 10)
			strPath = strPath.replace("%%DayOfYear", "00" + nDayOrYear);
		else if (nDayOrYear < 100)
			strPath = strPath.replace("%%DayOfYear", "0" + nDayOrYear);
		else
			strPath = strPath.replace("%%DayOfYear", String.valueOf(nDayOrYear));

		if (strPath.indexOf("%%y") >= 0)
			strPath = strPath.replace("%%y", strTime.substring(2, 4));

		if (strPath.indexOf("%%EM") >= 0) {
			switch (Integer.parseInt(strTime.substring(4, 6))) {
				case 1 :
					strPath = strPath.replace("%%EM", "Jan");
					break;
				case 2 :
					strPath = strPath.replace("%%EM", "Feb");
					break;
				case 3 :
					strPath = strPath.replace("%%EM", "Mar");
					break;
				case 4 :
					strPath = strPath.replace("%%EM", "Apr");
					break;
				case 5 :
					strPath = strPath.replace("%%EM", "May");
					break;
				case 6 :
					strPath = strPath.replace("%%EM", "Jun");
					break;
				case 7 :
					strPath = strPath.replace("%%EM", "Jul");
					break;
				case 8 :
					strPath = strPath.replace("%%EM", "Aug");
					break;
				case 9 :
					strPath = strPath.replace("%%EM", "Sep");
					break;
				case 10 :
					strPath = strPath.replace("%%EM", "Oct");
					break;
				case 11 :
					strPath = strPath.replace("%%EM", "Nov");
					break;
				case 12 :
					strPath = strPath.replace("%%EM", "Dec");
					break;
			}
		}

		if (strPath.indexOf("%%M") >= 0)
			strPath = strPath.replace("%%M", strTime.substring(4, 6));

		if (strPath.indexOf("%%d") >= 0)
			strPath = strPath.replace("%%d", strTime.substring(6, 8));

		if (strPath.indexOf("%%D") >= 0)
			strPath = strPath.replace("%%D", strTime.substring(6, 8));

		// add by liuwx 针对天,小时是否补0 ,如果天为0-9 ，则返回0-9 天为10-31返回10-31 ，小时0-9则0-9
		// ，小时10-23则10-23
		if (strPath.indexOf("%%fd") >= 0) {
			strPath = strPath.replace("%%fd", strTime.substring(6, 8));
		}
		String sd = null;
		if (strPath.indexOf("%%FD") >= 0) {
			sd = strTime.substring(6, 8);
			try {
				if (Integer.valueOf(sd) < 10) {
					strPath = strPath.replace("%%FD", strTime.substring(7, 8));
				} else
					strPath = strPath.replace("%%FD", strTime.substring(6, 8));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		String fh = null;
		if (strPath.indexOf("%%FH") >= 0) {
			String strHour = getExpression(strPath);
			if (strHour != null && !strHour.equals("")) {
				String strHourTmp = strHour.replaceAll("%%FH", strTime.substring(8, 10));
				int nHour = parseExpression(strHourTmp);

				/*
				 * ChenSijiang 2011-01-18 如果现在是23点，那么(%%H+1)会变成24，应该是不存在24点的说法。所以此处改成，如果小时过了23之后，变为0.
				 */
				if (nHour > 23)
					nHour = 0;

				strPath = strPath.replace("(" + strHour + ")", String.valueOf(nHour));
				if (nHour < 10)
					strPath = strPath.replaceAll("%%FH", strTime.substring(7, 10));
				else
					strPath = strPath.replaceAll("%%FH", strTime.substring(8, 10));
			} else {
				fh = strTime.substring(8, 10);
				if (Integer.valueOf(fh) < 10) {
					strPath = strPath.replace("%%FH", strTime.substring(9, 10));
				} else
					strPath = strPath.replace("%%FH", strTime.substring(8, 10));
			}
		}
		// end add

		// G网贝尔用，有24点的文件名。
		if (strPath.indexOf("%%BH") >= 0) {
			String strHour = getExpression(strPath);
			if (strHour != null && !strHour.equals("")) {
				String strHourTmp = strHour.replaceAll("%%BH", strTime.substring(8, 10));
				int nHour = parseExpression(strHourTmp);

				strPath = strPath.replace("(" + strHour + ")", trimHour(nHour));
				strPath = strPath.replaceAll("%%BH", strTime.substring(8, 10));
			} else {
				strPath = strPath.replaceAll("%%BH", strTime.substring(8, 10));
			}
		}

		if (strPath.indexOf("%%H") >= 0) {
			String strHour = getExpression(strPath);
			if (strHour != null && !strHour.equals("")) {
				String strHourTmp = strHour.replaceAll("%%H", strTime.substring(8, 10));
				int nHour = parseExpression(strHourTmp);

				/*
				 * ChenSijiang 2011-01-18 如果现在是23点，那么(%%H+1)会变成24，应该是不存在24点的说法。所以此处改成，如果小时过了23之后，变为0.
				 */
				if (nHour > 23 && !strPath.toLowerCase().contains("/apme/obsynt/"))
					nHour = 0;

				// String temp = strPath.substring(strPath.indexOf("("),
				// strPath.indexOf(")") + 1);

				// if ( temp.contains("%%") )
				strPath = strPath.replace("(" + strHour + ")", trimHour(nHour));
				strPath = strPath.replaceAll("%%H", strTime.substring(8, 10));
			} else {
				strPath = strPath.replaceAll("%%H", strTime.substring(8, 10));
			}
		}

		if (strPath.indexOf("%%h") >= 0)
			strPath = strPath.replaceAll("%%h", strTime.substring(8, 10));

		if (strPath.indexOf("%%m") >= 0)
			strPath = strPath.replace("%%m", strTime.substring(10, 12));

		if (strPath.indexOf("%%s") >= 0)
			strPath = strPath.replace("%%s", strTime.substring(12, 14));

		if (strPath.indexOf("%%S") >= 0)
			strPath = strPath.replace("%%S", strTime.substring(12, 14));

		String strInterval = "";
		int nInterval = 0;
		if (strPath.indexOf("|") > 0) {
			strInterval = strPath.substring(strPath.indexOf("|") + 1);
			strPath = strPath.substring(0, strPath.indexOf("|"));

			nInterval = Integer.parseInt(strInterval);
			timestamp = new Timestamp(timestamp.getTime() + nInterval);
			strTime = formatter.format(timestamp);

			calendar.setTime(timestamp);

			if (strPath.indexOf("%%NWEEK") >= 0) {
				int dow = calendar.get(Calendar.DAY_OF_WEEK);
				dow = dow - 1;
				if (dow == 0)
					dow = 7;
				strPath = strPath.replace("%%NWEEK", String.valueOf(dow));
			}

			if (strPath.indexOf("%%NY") >= 0)
				strPath = strPath.replace("%%NY", strTime.substring(0, 4));

			if (strPath.indexOf("%%Ny") >= 0)
				strPath = strPath.replace("%%Ny", strTime.substring(2, 4));

			if (strPath.indexOf("%%NEM") >= 0) {
				switch (Integer.parseInt(strTime.substring(4, 6))) {
					case 1 :
						strPath = strPath.replace("%%NEM", "Jan");
						break;
					case 2 :
						strPath = strPath.replace("%%NEM", "Feb");
						break;
					case 3 :
						strPath = strPath.replace("%%NEM", "Mar");
						break;
					case 4 :
						strPath = strPath.replace("%%NEM", "Apr");
						break;
					case 5 :
						strPath = strPath.replace("%%NEM", "May");
						break;
					case 6 :
						strPath = strPath.replace("%%NEM", "Jun");
						break;
					case 7 :
						strPath = strPath.replace("%%NEM", "Jul");
						break;
					case 8 :
						strPath = strPath.replace("%%NEM", "Aug");
						break;
					case 9 :
						strPath = strPath.replace("%%NEM", "Sep");
						break;
					case 10 :
						strPath = strPath.replace("%%NEM", "Oct");
						break;
					case 11 :
						strPath = strPath.replace("%%NEM", "Nov");
						break;
					case 12 :
						strPath = strPath.replace("%%NEM", "Dec");
						break;
				}
			}

			if (strPath.indexOf("%%NM") >= 0)
				strPath = strPath.replace("%%NM", strTime.substring(4, 6));

			if (strPath.indexOf("%%Nd") >= 0)
				strPath = strPath.replace("%%Nd", strTime.substring(6, 8));

			if (strPath.indexOf("%%ND") >= 0)
				strPath = strPath.replace("%%ND", strTime.substring(6, 8));

			if (strPath.indexOf("%%NH") >= 0)
				strPath = strPath.replace("%%NH", strTime.substring(8, 10));

			if (strPath.indexOf("%%NV4") >= 0) {
				int nNum = Integer.parseInt(strTime.substring(8, 10));
				nNum = (nNum + 1) / 4;
				strPath = strPath.replace("%%NV4", "0" + nNum);
			}

			if (strPath.indexOf("%%Nh") >= 0)
				strPath = strPath.replace("%%Nh", strTime.substring(8, 10));

			if (strPath.indexOf("%%Nm") >= 0)
				strPath = strPath.replace("%%Nm", strTime.substring(10, 12));

			if (strPath.indexOf("%%Ns") >= 0)
				strPath = strPath.replace("%%Ns", strTime.substring(12, 14));

			if (strPath.indexOf("%%NS") >= 0)
				strPath = strPath.replace("%%NS", strTime.substring(12, 14));
		}

		return strPath;
	}

	/**
	 * 转换采集路径，将“%%”占位符转换为实际值。
	 * 
	 * @param raw
	 *            原始路径。
	 * @param date
	 *            时间点。
	 * @return 转换后的值。
	 */
	public static String convertCollectPath(String raw, Date date) {
		if (raw == null || date == null)
			return raw;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String s = raw;
		s = s.replaceAll("%%Y", String.format("%04d", cal.get(Calendar.YEAR)));
		s = s.replaceAll("%%y", String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
		s = s.replaceAll("%%M", String.format("%02d", cal.get(Calendar.MONTH) + 1));
		s = s.replaceAll("%%D", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
		s = s.replaceAll("%%d", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
		s = s.replaceAll("%%H", String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		s = s.replaceAll("%%h", String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		s = s.replaceAll("%%m", String.format("%02d", cal.get(Calendar.MINUTE)));
		//以10分钟为单位的路径格式化(临时)，应对文件非常多又无序的采集方案
		s = s.replaceAll("%%10m", String.format("%02d", cal.get(Calendar.MINUTE)).substring(0,1));
		s = s.replaceAll("%%S", String.format("%02d", cal.get(Calendar.SECOND)));
		s = s.replaceAll("%%s", String.format("%02d", cal.get(Calendar.SECOND)));
		String em = new SimpleDateFormat("MMM", Locale.ENGLISH).format(date);
		s = s.replaceAll("%%EM", em);
		return s;
	}

	/**
	 * 只替换第一个通配时间
	 * 
	 * @param raw
	 * @param date
	 * @return
	 */
	public static String convertCollectPathFirstDate(String raw, Date date) {
		if (raw == null || date == null)
			return raw;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String s = raw;
		s = s.replaceFirst("%%Y", String.format("%04d", cal.get(Calendar.YEAR)));
		s = s.replaceFirst("%%y", String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
		s = s.replaceFirst("%%M", String.format("%02d", cal.get(Calendar.MONTH) + 1));
		s = s.replaceFirst("%%D", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
		s = s.replaceFirst("%%d", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
		s = s.replaceFirst("%%H", String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		s = s.replaceFirst("%%h", String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		s = s.replaceFirst("%%m", String.format("%02d", cal.get(Calendar.MINUTE)));
		s = s.replaceFirst("%%S", String.format("%02d", cal.get(Calendar.SECOND)));
		s = s.replaceFirst("%%s", String.format("%02d", cal.get(Calendar.SECOND)));
		String em = new SimpleDateFormat("MMM", Locale.ENGLISH).format(date);
		s = s.replaceFirst("%%EM", em);
		return s;
	}

	public static String getPattern(String target, String regex) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(target);
			while (matcher.find()) {
				// 只提取第一次满足匹配的字符串
				return matcher.group();
			}
		} catch (Throwable e) {
			// 没找到匹配的字符串
		}
		return null;
	}

	/**
	 * 增强版匹配
	 * 
	 * @param target
	 * @param regex
	 * @return 返回分组匹配的结果
	 */
	public static String getPatternEX(String target, String regex) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(target);
			while (matcher.find()) {
				return matcher.group(1);
			}
		} catch (Throwable e) {
			// 没找到匹配的字符串
		}
		return null;
	}

	public static Date parseZteFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{4}[-]\\d{2}[-]\\d{2}[_]\\d{2}[-]\\d{2}");
			Date date = new SimpleDateFormat("yyyy-MM-dd_HH-mm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseZteCdrFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{14}");
			Date date = new SimpleDateFormat("yyyyMMddHHmmSSS").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseZteMRFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{12}");
			Date date = new SimpleDateFormat("yyyyMMddHHmm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseZte3GMRFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{4}[-]\\d{2}[-]\\d{2}[-]\\d{2}[-]\\d{2}[-]\\d{2}");
			Date date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseCvicMRFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{8}[_]\\d{4}");
			Date date = new SimpleDateFormat("yyyyMMdd_HHmm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseAluFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{8}");
			Date date = new SimpleDateFormat("yyMMddHH").parse(str);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			return calendar.getTime();
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseAluDoFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{10}");
			if (str == null)
				str = getPattern(name, "\\d{8}") + "00";
			Date date = new SimpleDateFormat("yyMMddHHmm").parse(str);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			return calendar.getTime();
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseAluMRFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{8}[.]\\d{4}");
			Date date = new SimpleDateFormat("yyyyMMdd.HHmm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseHwFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{12}");
			Date date = new SimpleDateFormat("yyyyMMddHHmm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseEricssonFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{8}[.]\\d{4}");
			Date date = new SimpleDateFormat("yyyyMMdd.HHmm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	public static Date parseNokiaFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{4}[_]\\d{2}[_]\\d{2}[_]\\d{2}[_]\\d{2}");
			Date date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	/*
	 * 解析诺西 WCAS文件 文件样式：2014_01_02_09_44.str.gz
	 */
	public static Date parseNokiaCdtFileDate(String filename) {
		try {
			String name = FilenameUtils.getBaseName(filename);
			String str = getPattern(name, "\\d{4}[_]\\d{2}[_]\\d{2}[_]\\d{2}[_]\\d{2}[.]");
			Date date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").parse(str);
			return date;
		} catch (Exception e) {
			return new Date(0);
		}
	}

	/**
	 * 字符串拆分
	 * 
	 * @param string
	 * @param perfix
	 * @param maxArrayLength
	 * @return 拆分后的数组
	 */
	public static final String[] split(String string, String perfix) {
		if (isEmpty(string))
			return null;
		if (perfix == null || perfix.length() == 0)
			return new String[]{string};
		int index = string.indexOf(perfix);
		if (index < 0)
			return new String[]{string};
		List<String> splitList = new ArrayList<String>();
		while ((index = string.indexOf(perfix)) != -1) {
			splitList.add(string.substring(0, index));
			string = string.substring(index + perfix.length());
		}
		splitList.add(string.substring(0));
		String[] array = new String[splitList.size()];
		return splitList.toArray(array);
	}

	/**
	 * 字符串格式数字小数点精度处理
	 * 
	 * @param num
	 * @param radixNum
	 * @return 处理精度后的数字字符串
	 */
	public static String formatNumberRadix(double num, int radixNum) {
		String str = String.valueOf(num);
		if (radixNum < 0)
			return str;
		int radixIndex = str.indexOf('.');
		if (radixIndex < 0)
			return str;
		if (radixNum == 0)
			return str.substring(0, radixIndex);
		int endIndex = radixIndex + radixNum + 1;
		if (endIndex > str.length())
			endIndex = str.length();
		str = str.substring(0, endIndex);
		return str;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param string
	 * @return boolean 如果字符串为Null、""或者空白字符串 返回true.否则返回false
	 */
	public static boolean isEmpty(String string) {
		return string == null || string.trim().length() == 0;
	}

	public static boolean isNotEmpty(String string) {
		return string != null && string.trim().length() > 0;
	}

	public static String nvl(String str, String replace) {
		return isEmpty(str) ? replace : str;
	}

	/**
	 * 判断字符串是否是数字
	 * 
	 * @param str
	 * @return true or false
	 */
	public static boolean isNum(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	/**
	 * 编码一条FTP路径
	 * 
	 * @param ftpPath
	 *            FTP路径
	 * @return 编码后的路径
	 */
	public static String encodeFTPPath(String ftpPath, String encode) {
		try {
			String str = StringUtil.isNotEmpty(encode) ? new String(ftpPath.getBytes(encode), "ISO_8859_1") : ftpPath;
			return str;
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("设置的编码不正确:" + encode, e);
		}
		return ftpPath;
	}

	/**
	 * 解码一条FTP路径
	 * 
	 * @param ftpPath
	 *            FTP路径
	 * @return 解码后的路径
	 */
	public static String decodeFTPPath(String ftpPath) {
		try {
			String str = StringUtil.isNotEmpty(ftpPath) ? new String(ftpPath.getBytes("ISO_8859_1"), "utf-8") : ftpPath;
			return str;
		} catch (UnsupportedEncodingException e) {
			return ftpPath;
		}
	}

	/**
	 * 解码一条FTP路径
	 * 
	 * @param ftpPath
	 *            FTP路径
	 * @param charset
	 *            FTP服务端编码集，若设置为null或""默认为JVM本地默认编码
	 * @return 解码后的路径
	 * @author Niow 2014-6-11
	 */
	public static String decodeFTPPath(String ftpPath, String charset) {
		try {
			String str = ftpPath;
			if (isNotEmpty(charset)) {
				str = StringUtil.isNotEmpty(ftpPath) ? new String(ftpPath.getBytes("ISO_8859_1"), charset) : ftpPath;
			} else {
				str = StringUtil.isNotEmpty(ftpPath) ? new String(ftpPath.getBytes("ISO_8859_1")) : ftpPath;
			}

			return str;
		} catch (UnsupportedEncodingException e) {
			return ftpPath;
		}
	}

	/**
	 * 编码一条FTP路径，把JVM默认编码格式转换为FTP协议的ISO_8859_1格式
	 * 
	 * @param ftpPath
	 *            FTP路径
	 * @return 采集路径
	 * @author Niow 2014-6-11
	 */
	public static String codeFTPPath(String ftpPath) {
		try {
			String str = StringUtil.isNotEmpty(ftpPath) ? new String(ftpPath.getBytes(), "ISO_8859_1") : ftpPath;
			return str;
		} catch (UnsupportedEncodingException e) {
			return ftpPath;
		}
	}

	/**
	 * 转换一个字符串的编码格式
	 * 
	 * @param str
	 *            原字符串
	 * @param targetCharset
	 *            目标编码格式
	 * @param srcCharset
	 *            原字符串的编码格式，如果为null或""则默认为本地编码格式
	 * @author Niow 2014-6-11
	 * @return 转换后的字符串
	 */
	public static String codeConvert(String str, String targetCharset, String srcCharset) {
		try {
			if (isNotEmpty(srcCharset)) {
				return StringUtil.isNotEmpty(str) ? new String(str.getBytes(), targetCharset) : str;
			} else {
				return StringUtil.isNotEmpty(str) ? new String(str.getBytes(srcCharset), targetCharset) : str;
			}
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	/**
	 * 数值转化为16进制
	 * 
	 * @param input
	 * @return
	 */
	public static String intToHexString(int input, int len) {
		String abc = Integer.toHexString(input);
		StringBuilder sb = new StringBuilder();
		if (abc.length() < len) {
			int zeroCount = len - abc.length();
			for (int i = 0; i < zeroCount; i++) {
				sb.append("0");
			}
			sb.append(abc);
		} else {
			sb.append(abc);;
		}
		return sb.toString();
	}

	public static void main(String[] arg1) {
		// System.out.println("abxxabxxab".replaceAll("ab", "0"));
		// System.out.println(convertCollectPath("%%Y%%M_%%y", new Date()));
		// System.out.println("2014".substring(2, 4));
		//
		// String str = "?/小区:eNodeB名称=?, 本地小区标识=?, 小区名称=?, eNodeB标识=?, 小区双工模式=?";
		// String[] valList = StringUtil.split(str, "?");
		// int n = 0;
		// for (String val : valList) {
		// System.out.println(++n + ":" + val);
		// }
		//
		System.out.println(intToHexString(1234, 4));
	}

}
