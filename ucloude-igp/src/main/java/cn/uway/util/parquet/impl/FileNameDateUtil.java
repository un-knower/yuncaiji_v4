package cn.uway.util.parquet.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class FileNameDateUtil {
	protected static final ILogger LOG = LoggerManager
			.getLogger(FileNameDateUtil.class);

	// "52746041120006_CDT_1X_TXT_2016-05-09_15-55_0836010400000000.zip"
	// 以“20”开头能过滤干扰数字串
	private static Pattern parrten = Pattern
			.compile("20\\d{2}[-]?[01]\\d[-]?[0123]\\d[\\.T -\\_]?[012]\\d[:-]?\\d{2}");
	// 朗讯2 3G话单 16051615.PCMD
	private static Pattern lucParrten = Pattern
			.compile("16\\d{6}");

	/**
	 * 从文件名中匹配出时间字符串，如果无法匹配则返回null
	 * 
	 * @param fn
	 * @return
	 */
	public static String getFileDate(String fn) {
		Matcher m = parrten.matcher(fn);
		if (m.find()) {
			String fdStr = m.group(0);
			fdStr = fdStr.replaceAll("\\.", "");
			fdStr = fdStr.replaceAll("T", "");
			fdStr = fdStr.replaceAll(" ", "");
			fdStr = fdStr.replaceAll("-", "");
			fdStr = fdStr.replaceAll("_", "");
			fdStr = fdStr.replaceAll(":", "");
			return fdStr;
		}
		m = lucParrten.matcher(fn);
		if (m.find()) {
			String fdStr = m.group(0);
			fdStr = "20"+fdStr;
			return fdStr;
		}
		return null;
	}
	
	public static void main(String[] args){
		String fn = "52746041120006_CDT_1X_TXT_2016-05-09_15-55_0836010400000000.zip";
		System.out.println(FileNameDateUtil.getFileDate(fn));
	}
}
