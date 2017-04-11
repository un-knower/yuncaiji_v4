package cn.uway.framework.task.worker;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

public class TimeWildcardHandler {

	public String timeWildcard;

	public String regexExpression;

	public static Set<String> commonSigns = new HashSet<String>();

	protected static final ILogger LOGGER = LoggerManager.getLogger(TimeWildcardHandler.class);

	static {
		commonSigns.add("-");
		commonSigns.add("_");
		commonSigns.add("=");
		commonSigns.add("+");
		commonSigns.add("|");
		commonSigns.add("?");
		commonSigns.add("!");
		commonSigns.add("@");
		commonSigns.add("#");
		commonSigns.add("%");
		commonSigns.add("&");
		commonSigns.add("~");
		commonSigns.add(".");
	}

	public TimeWildcardHandler(String gatherPath) {
		timeWildcard = gatherPath.substring(gatherPath.indexOf("{") + 1, gatherPath.indexOf("}"));
		getRegexExpression(gatherPath);
	}

	public void getRegexExpression(String gatherPath) {
		if (gatherPath == null)
			return;
		int wcIndex = gatherPath.indexOf(timeWildcard);
		String front = gatherPath.substring(wcIndex - 2, wcIndex - 1);
		String back = gatherPath.substring(wcIndex + timeWildcard.length() + 1, wcIndex + timeWildcard.length() + 2);
		if (!commonSigns.contains(front))
			front = "";
		if (!commonSigns.contains(back))
			back = "";
		String s = timeWildcard;
		// year
		s = s.replaceAll("%%Y", "\\\\d{4}");
		s = s.replaceAll("%%y", "\\\\d{4}");
		s = s.replaceAll("%%FY", "\\\\d{2}");// 简写，如14表示2014
		s = s.replaceAll("%%fy", "\\\\d{2}");// 简写，如14表示2014
		// moth
		s = s.replaceAll("%%M", "\\\\d{2}");
		s = s.replaceAll("%%FM", "\\\\d{1,2}");// 比如一月份，1和01都可表示
		// day
		s = s.replaceAll("%%D", "\\\\d{2}");
		s = s.replaceAll("%%d", "\\\\d{2}");
		s = s.replaceAll("%%FD", "\\\\d{1,2}");
		s = s.replaceAll("%%fd", "\\\\d{1,2}");
		// hour
		s = s.replaceAll("%%H", "\\\\d{2}");
		s = s.replaceAll("%%h", "\\\\d{2}");
		s = s.replaceAll("%%FH", "\\\\d{1,2}");
		s = s.replaceAll("%%fh", "\\\\d{1,2}");
		// minute
		s = s.replaceAll("%%m", "\\\\d{2}");
		s = s.replaceAll("%%fm", "\\\\d{1,2}");
		// second
		s = s.replaceAll("%%S", "\\\\d{2}");
		s = s.replaceAll("%%s", "\\\\d{2}");
		s = s.replaceAll("%%FS", "\\\\d{1,2}");
		s = s.replaceAll("%%fs", "\\\\d{1,2}");
		// millisecond
		s = s.replaceAll("%%ems", "\\\\d{3}");
		// english month
		// s = s.replaceAll("%%EM", "\\\\[a-zA-Z]{3}");
		s = front + s + back;
		String tmp = s;
		for (String sign : commonSigns) {
			// 不能用replaceAll
			// s = s.replaceAll(sign, "[" + sign + "]");
			int lastIndex = 0;
			while (true) {
				int index = tmp.indexOf(sign, lastIndex);
				if (index > -1) {
					String fs = tmp.substring(0, index);
					String bs = tmp.substring(index + 1);
					tmp = (fs + "[" + sign + "]" + bs);
					lastIndex = index + 3;
				} else
					break;
			}
		}
		s = tmp.equals("") ? s : tmp;
		regexExpression = s;
	}

	public Date getDateTime(String filePath) {
		Date date = null;
		String timeStr = StringUtil.getPattern(filePath, regexExpression);
		if(timeStr == null)
		{
			return null;
		}
		String firstChar = timeStr.substring(0, 1);
		String lastChar = timeStr.substring(timeStr.length() - 2);
		if (commonSigns.contains(firstChar))
			timeStr = timeStr.substring(1);
		if (commonSigns.contains(lastChar))
			timeStr = timeStr.substring(0, timeStr.length() - 1);
		String s = timeWildcard;
		s = s.replaceAll("%%Y", "yyyy").replaceAll("%%y", "yyyy").replaceAll("%%FY", "yy").replaceAll("%%fy", "yy");
		s = s.replaceAll("%%M", "MM").replaceAll("%%FM", "M");
		s = s.replaceAll("%%D", "dd").replaceAll("%%d", "dd").replaceAll("%%FD", "d").replaceAll("%%fd", "d");
		s = s.replaceAll("%%H", "hh").replaceAll("%%h", "hh").replaceAll("%%FH", "h").replaceAll("%%fh", "h");
		s = s.replaceAll("%%m", "mm").replaceAll("%%fm", "m");
		s = s.replaceAll("%%S", "ss").replaceAll("%%s", "ss").replaceAll("%%FS", "s").replaceAll("%%fs", "s");
		s = s.replaceAll("%%ems", "SSS");
		try {
			date = TimeUtil.getDate(timeStr, s);
		} catch (ParseException e) {
			LOGGER.error("转换日期格式时出错", e);
		}
		return date;
	}

	public static void main(String[] args) {
		TimeWildcardHandler handler = new TimeWildcardHandler("*_{%%FY-%%FM-%%FD-%%fh-%%fm-%%fs}*.xml");
		System.out.println(handler.regexExpression);
		System.out.println(handler.getDateTime("1_14-11-2-1-1-2-123_export.xml"));

		handler = new TimeWildcardHandler("*_{%%Y%%M%%D}*.xml");
		System.out.println(handler.regexExpression);
		System.out.println(handler.getDateTime("1_20141102_export.xml"));
	}
}
