package cn.uway.ucloude.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class StringUtil extends org.apache.commons.lang3.StringUtils {
	private static final ILogger LOGGER = LoggerManager.getLogger(StringUtil.class);
			
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");
    
	public static String format(String format, Object... args) {
		FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
		return ft.getMessage();
	}

    public static boolean isInteger(String str) {
        return !(str == null || str.length() == 0) && INT_PATTERN.matcher(str).matches();
    }

	/**
	 * <pre>
	 * 区别org.apache.commons.lang.StringUtils.split，
	 * 避免在函数内部生成新的数组对象并返回，实现对效率要求非常高，数据量巨大情况实现高效字符分割。
	 * 使用该函数前，必须预先定义一个存放分割后内容的数组，以便函数将分割后的内容写入数组中；
	 * </pre>
	 * 
	 * @param srcStr 待分割的字符串
	 * @param splitChar  分割的字符
	 * @param detStrs
	 *            目标字符串数组
	 * @param capicity
	 *            目标字符串数据的容量
	 * @return 返回被分割的子字符串个数； 如果分割失败返回0，
	 *         如果返回值小于0，则代表实际能分割的子字符串个数大于能存放的数组容量capicity。
	 */
	public static int split(String srcStr, char splitChar, String[] detStrs, int capicity) {
		if (srcStr == null || srcStr.length() < 1)
			return 0;

		int i = 0;
		int start = 0;
		int index = srcStr.indexOf(splitChar);
		if (index < 0 && i < capicity) {
			detStrs[i++] = srcStr;
			return i;
		}

		while (index != -1 && i < capicity) {
			if (index > start)
				detStrs[i++] = srcStr.substring(start, index);
			else
				detStrs[i] = null;

			start = index + 1;
			index = srcStr.indexOf(splitChar, start);
		}

		if (start < srcStr.length() && i < capicity)
			detStrs[i++] = srcStr.substring(start);
		else if (start < srcStr.length() && i >= capicity)
			return -1;

		return i;
	}

	public static byte[] convertHexString(String ss) {
		byte digest[] = new byte[ss.length() / 2];
		for (int i = 0; i < digest.length; i++) {
			String byteString = ss.substring(2 * i, 2 * i + 2);
			int byteValue = Integer.parseInt(byteString, 16);
			digest[i] = (byte) byteValue;
		}
		return digest;
	}

	public static String toHexString(byte b[]) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String plainText = Integer.toHexString(0xff & b[i]);
			if (plainText.length() < 2)
				plainText = "0" + plainText;
			hexString.append(plainText);
		}
		return hexString.toString();
	}

	public static boolean hasText(String str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static String generateUUID() {
		return replace(java.util.UUID.randomUUID().toString(), "-", "").toUpperCase();
	}

	public static boolean isNotEmpty(String... strs) {
		if (strs != null) {
			for (String s : strs) {
				if (isEmpty(s)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 区别org.apache.commons.lang.StringUtils.split，
	 * 避免在函数内部生成新的数组对象并返回，实现对效率要求非常高，数据量巨大情况实现高效字符分割。
	 * 使用该函数前，必须预先定义一个存放分割后内容的数组，以便函数将分割后的内容写入数组中；
	 * </pre>
	 * 
	 * @param srcStr
	 *            待分割的字符串
	 * @param splitChar
	 *            分割的字符串(如果只有一个字符，建议将该参数定义成char能有效提高效率)
	 * @param detStrs
	 *            目标字符串数组
	 * @param capicity
	 *            目标字符串数据的容量
	 * @return 返回被分割的子字符串个数； 如果分割失败返回0，
	 *         如果返回值小于0，则代表实际能分割的子字符串个数大于能存放的数组容量capicity。
	 */
	public static int split(String srcStr, String splitChars, String[] detStrs, int capicity) {
		if (srcStr == null || srcStr.length() < 1)
			return 0;

		int i = 0;
		int start = 0;
		int index = srcStr.indexOf(splitChars);
		if (index < 0 && i < capicity) {
			detStrs[i++] = srcStr;
			return i;
		}

		while (index != -1 && i < capicity) {
			if (index > start)
				detStrs[i++] = srcStr.substring(start, index);
			else
				detStrs[i] = null;

			start = index + 1;
			index = srcStr.indexOf(splitChars, start);
		}

		if (start < srcStr.length() && i < capicity)
			detStrs[i++] = srcStr.substring(start);
		else if (start < srcStr.length() && i >= capicity)
			return -1;

		return i;
	}

	/**
	 * 获取指定字符中间的一个或所有子符串；如：双引号，单引号，括号，中括号……等中间的字符
	 * 
	 * @param srcStr
	 *            待查找的字符串
	 * @param splitChar
	 *            分割的字符，适应于左右分割字符一致的情形
	 * @return 返回第一个子字符串
	 */
	public static String getSubString(String srcStr, char splitChar) {
		return getSubString(srcStr, splitChar, splitChar);
	}

	/**
	 * 获取指定字符中间的一个或所有子符串；如：双引号，单引号，括号，中括号……等中间的字符
	 * 
	 * @param srcStr
	 *            待查找的字符串
	 * @param leftSplitChar
	 *            左侧分割的字符标识
	 * @param rightSplitChar
	 *            右侧分割的字符标识
	 * @return 返回第一个子字符串
	 */
	public static String getSubString(String srcStr, char leftSplitChar, char rightSplitChar) {
		if (srcStr == null)
			return null;

		int start = 0;
		int leftCharPos = -1, rightCharPos = -1;
		do {
			leftCharPos = srcStr.indexOf(leftSplitChar, start);
			if (leftCharPos > 0) {
				char sign = srcStr.charAt(leftCharPos - 1);
				if (sign == '\\') {
					++start;
					continue;
				}
			}
			break;
		} while (true);

		if (leftCharPos < 0)
			return null;

		++leftCharPos;
		start = leftCharPos;
		do {
			rightCharPos = srcStr.indexOf(rightSplitChar, start);
			if (rightCharPos > 0 && rightCharPos > leftCharPos) {
				char sign = srcStr.charAt(rightCharPos - 1);
				if (sign == '\\') {
					++start;
					continue;
				}
			}
			break;
		} while (true);

		if (rightCharPos < 0 || rightCharPos < leftCharPos)
			return null;

		return srcStr.substring(leftCharPos, rightCharPos);
	}

	/**
	 * 获取指定字符中间的一个或所有子符串；如：双引号，单引号，括号，中括号……等中间的字符
	 * 
	 * @param srcStr
	 *            待查找的字符串
	 * @param leftSplitChar
	 *            左侧分割的字符标识
	 * @param rightSplitChar
	 *            右侧分割的字符标识
	 * @return 返回所有子字符串
	 */
	public static String[] getSubStrings(String srcStr, char leftSplitChar, char rightSplitChar) {
		if (srcStr == null)
			return null;

		List<String> subStrList = new LinkedList<String>();
		int start = 0;
		while (start < srcStr.length()) {
			int leftCharPos = -1;
			int rightCharPos = -1;
			do {
				leftCharPos = srcStr.indexOf(leftSplitChar, start);
				if (leftCharPos > 0) {
					char sign = srcStr.charAt(leftCharPos - 1);
					if (sign == '\\') {
						++start;
						continue;
					}
				}
				break;
			} while (true);

			if (leftCharPos < 0)
				break;

			++leftCharPos;
			start = leftCharPos;
			do {
				rightCharPos = srcStr.indexOf(rightSplitChar, start);
				if (rightCharPos > 0 && rightCharPos > leftCharPos) {
					char sign = srcStr.charAt(rightCharPos - 1);
					if (sign == '\\') {
						++start;
						continue;
					}
				}
				break;
			} while (true);

			if (rightCharPos < 0 || rightCharPos < leftCharPos)
				break;

			subStrList.add(srcStr.substring(leftCharPos, rightCharPos));
			start = rightCharPos + 1;
		}

		String[] strs = new String[subStrList.size()];
		return subStrList.toArray(strs);
	}

	/**
	 * 合并1个或多个子字符串到一个大字符串中 函数的实现，需要用StringBuilder，不能简单直接用“+”号。如，函数调用方式为：
	 * concatWithSplitChars(“,”, “1”,”2”,”3”); 期望返回值为：”1,2,3”
	 * 
	 * @param strs
	 *            待合并的子字符串(可变参数)
	 * @return 返回合并后的新字符串
	 */
	public static String concat(String... strs) {
		return concatWithSplitChars(null, strs);
	}

	/**
	 * 合并1个或多个子字符串到一个大字符串中 函数的实现，需要用StringBuilder，不能简单直接用“+”号。如，函数调用方式为：
	 * concatWithSplitChars(“,”, “1”,”2”,”3”); 期望返回值为：”1,2,3”
	 * 
	 * @param splitChars
	 *            每个待合并的子串中间添加的分割符
	 * @param strs
	 *            待合并的子字符串(可变参数)
	 * @return 返回合并后的新字符串
	 */
	public static String concatWithSplitChars(String splitChars, String... strs) {
		StringBuilder sb = new StringBuilder();
		for (String str : strs) {
			if (splitChars != null && sb.length() > 0)
				sb.append(splitChars);
			sb.append(str);
		}

		return sb.toString();
	}

	/**
	 * 按指定的正规则表达式，查找第1个子字符串
	 * 
	 * @param str
	 *            待查找的字符串
	 * @param regx
	 *            子字符串正则表达式
	 * @return 根据正则表达式，返回第1个找到子字符串
	 */
	public static String getPattern(String str, String regx) {
		return getPattern(str, regx, 0);
	}

	/**
	 * 按指定的正规则表达式， 查找第N个子字符串
	 * 
	 * @param str
	 *            待查找的字符串
	 * @param regx
	 *            子字符串正则表达式
	 * @param groupIndex
	 *            返回第N个Group内容
	 * 
	 *            <pre>
	 * 			group（0）就是指的整个串，
	 * 			group（1） 指的是第1个括号里的东西，
	 * 			group（2）指的第2个括号里的东西
	 * 			....
	 * 			example:  getPatterns("example_201611281300.txt", "(\\d{4})(\\d{2})(\\d{2})(\\d*)", n)
	 * 					当n=0时，返回＂201611281300＂
	 * 					当n=1时，返回＂2016＂
	 * 					当n=2时，返回＂11＂
	 * 					当n=3时，返回＂28＂
	 * 					....
	 *            </pre>
	 * 
	 * @return 根据正则表达式，返回第N个找到子字符串
	 */
	public static String getPattern(String str, String regx, int groupIndex) {
		try {
			Pattern pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(str);
			while (matcher.find()) {
				return matcher.group(groupIndex);
			}
		} catch (Throwable e) {
			// 没找到匹配的字符串
		}
		return null;
	}

	/**
	 * 按指定的正规则表达式， 查找第N个子字符串
	 * 
	 * @param str
	 *            待查找的字符串
	 * @param regx
	 *            子字符串正则表达式
	 * @param groupIndex
	 *            返回第N个Group内容，默认0
	 * 
	 *            <pre>
	 * 			group（0）就是指的整个串，
	 * 			group（1） 指的是第1个括号里的东西，
	 * 			group（2）指的第2个括号里的东西
	 * 			....
	 * 			example:  getPatterns("example_201611281300.txt", "(\\d{4})(\\d{2})(\\d{2})(\\d*)", n)
	 * 					当n=0时，返回＂201611281300＂
	 * 					当n=1时，返回＂2016＂
	 * 					当n=2时，返回＂11＂
	 * 					当n=3时，返回＂28＂
	 * 					....
	 *            </pre>
	 * 
	 * @return 根据正则表达式，返回第N个找到子字符串
	 */
	public static String[] getPatterns(String str, String regx, int groupIndex) {
		List<String> matchedStrList = new LinkedList<String>();
		try {
			Pattern pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(str);
			while (matcher.find()) {
				matchedStrList.add(matcher.group(groupIndex));
			}
		} catch (Throwable e) {
			// 没找到匹配的字符串
		}

		String[] matchedStrs = new String[matchedStrList.size()];
		return matchedStrList.toArray(matchedStrs);
	}

	public static boolean hasLength(String str) {
		return (str != null && str.length() > 0);
	}

	public static String toString(Throwable e) {
		StringWriter w = new StringWriter();
		PrintWriter p = new PrintWriter(w);
		p.print(e.getClass().getName());
		if (e.getMessage() != null) {
			p.print(": " + e.getMessage());
		}
		p.println();
		try {
			e.printStackTrace(p);
			return w.toString();
		} finally {
			p.close();
		}
	}

	public static String toString(String msg, Throwable e) {
		StringWriter w = new StringWriter();
		w.write(msg + "\n");
		PrintWriter p = new PrintWriter(w);
		try {
			e.printStackTrace(p);
			return w.toString();
		} finally {
			p.close();
		}
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

	public static void main(String[] args) {
		System.out.println(StringUtil.getSubString("sdfsdfsdf\"sdfsadf\"", '\"'));

		String[] strs = StringUtil.getSubStrings("(sdf) (sdf\\)() (ssdf)", '(', ')');
		for (String s : strs)
			System.out.println(s);

		System.out.println(StringUtil.concat("shi", "gang"));
		System.out.println(StringUtil.concatWithSplitChars(".", "shi", "gang"));

		System.out.println(StringUtil.getPattern("example_201611281300.txt", "(\\d{4})(\\d{2})(\\d{2})(\\d*)", 0));

		System.out.println("finished.");
	}
}
