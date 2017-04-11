package cn.uway.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nfunk.jep.JEP;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 公共方法类
 * 
 * @author YangJian
 * @version 1.0
 */
public class Util{

	private static ILogger log = LoggerManager.getLogger(Util.class);

	public static final int COLLECT_FIELD_DATATYPE_LOB = 4;// 无限长度

	public static final int COLLECT_FIELD_TO_NUMBER = 5;// 进行to_number()转换

	// liangww add 2012-06-06 用于sqlldr时的字段特殊处理类型
	public static final int COLLECT_FIELD_SPECIAL_FORMAT = 6;// 特殊format类型，

	/** 获取本地计算机名 */
	public static String getHostName(){
		String strHostName = null;
		try{
			strHostName = InetAddress.getLocalHost().getHostName();
		}catch(UnknownHostException e){
			// log.error("获取hostname时异常", e);
			strHostName = Util.nvl(e.getMessage(), "").trim();
			try{
				strHostName = strHostName.split(":")[0].trim();
			}catch(Exception exx){}
		}

		return strHostName;
	}

	/** 显示当前系统物理状况 */
	public static void showOSState(){
		long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
		long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
		long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
		long usedMemory = totalMemory - freeMemory;
		freeMemory = maxMemory - usedMemory;

		log.debug("OS State: mem used:" + usedMemory + "KB,mem free:" + freeMemory + "KB,mem total:" + totalMemory
				+ "KB.");
	}

	/** 显示 程序运行的 系统环境 信息 */
	public static void printEnvironmentInfo(){
		try{
			Properties props = System.getProperties();

			StringBuffer sb = new StringBuffer();

			sb.append("\n----------------------Environment Info--------------------------\n");
			sb.append("os.name : " + props.getProperty("os.name") + "\n"); // 操作系统名称
			sb.append("os.arch : " + props.getProperty("os.arch") + "\n"); // 操作系统构架
			sb.append("os.version : " + props.getProperty("os.version") + "\n"); // 操作系统版本

			sb.append("java.version : " + props.getProperty("java.version") + "\n"); // Java 运行时环境版本
			sb.append("java.vendor : " + props.getProperty("java.vendor") + "\n"); // Java 运行时环境供应商
			sb.append("java.vm.name : " + props.getProperty("java.vm.name") + "\n"); // Java 虚拟机实现名称
			sb.append("java.home : " + props.getProperty("java.home") + "\n"); // Java
			// 安装目录
			sb.append("java.class.path : " + props.getProperty("java.class.path") + "\n"); // Java 类路径
			sb.append("java.library.path : " + props.getProperty("java.library.path") + "\n"); // 加载库时搜索的路径列表

			sb.append("user.name : " + props.getProperty("user.name") + "\n"); // 用户的账户名称
			sb.append("user.home : " + props.getProperty("user.home") + "\n"); // 用户的主目录
			sb.append("user.dir : " + props.getProperty("user.dir") + "\n"); // 用户的当前工作目录

			sb.append("file.encoding : " + props.getProperty("file.encoding") + "\n");

			sb.append("--------Disk information---------\n");
			printDiskInfo(sb);

			sb.append("----------------------------------------------------------------");

			log.info(sb.toString());
		}catch(Exception e){}
	}

	/** 记录磁盘信息 */
	public static void printDiskInfo(StringBuffer sb){
		try{
			File [] roots = File.listRoots();
			for(File _file : roots){
				sb.append(_file.getPath() + "\n");
				sb.append("Free space = " + _file.getFreeSpace() + "\n");
				sb.append("Usable space = " + _file.getUsableSpace() + "\n");
				sb.append("Total space = " + _file.getTotalSpace() + "\n");
				sb.append("\n");
			}
		}catch(Exception e){}
	}

	/**
	 * 在标准输出端口上打印当前内存占用情况。
	 */
	public static void printMemoryStatus(){
		float maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		float totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		float freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		float usedMemory = totalMemory - freeMemory;
		freeMemory = maxMemory - usedMemory;
		System.out.println("已使用: " + usedMemory + "M  剩余: " + freeMemory + "M  最大内存: " + maxMemory + "M");
	}

	/**
	 * @param b byte[]
	 * @return String
	 */
	public static String string2Hex(String b){
		if(b == null || b.equals(""))
			return null;

		return bytes2Hex(b.getBytes());
	}

	/**
	 * 字节转化为十六进制
	 * 
	 * @param b byte[]
	 * @return String
	 */
	public static String bytes2Hex(byte [] b){
		if(b == null)
			return null;
		String ret = "";
		for(int i = 0; i < b.length; i++){
			String hex = Integer.toHexString(b[i] & 0xFF);
			if(hex.length() == 1){
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	/** 字符串 null 或 空 判断 */
	public static boolean isNull(String str){
		boolean bReturn = false;

		if(str == null || str.trim().equals("")){
			bReturn = true;
		}

		return bReturn;
	}

	/** 字符串 非null 和 非空 判断 */
	public static boolean isNotNull(String str){
		boolean bReturn = false;

		if(str != null && !str.trim().equals("")){
			bReturn = true;
		}

		return bReturn;
	}

	/** 转换 byte数组 为 char数组 */
	public static char [] bytesToChars(byte [] bytes){
		String s = new String(bytes);
		char [] c = s.toCharArray();

		return c;
	}

	/** 计算字符串表达式的值 ，比如 字符串"1+2" 计算后为3 */
	public static int parseExpression(String str){
		JEP myParser = new JEP();

		myParser.parseExpression(str);

		return (int)myParser.getValue();
	}

	/** 当时间为0-9的时候返回 00-09，为10-23的时候返回10-23 */
	public static String trimHour(int hour){
		String str = String.valueOf(hour);
		if(str.length() == 1){
			str = "0" + str;
		}

		return str;
	}

	/**
	 * 将list转换为字符串数组
	 * 
	 * @param list
	 * @return
	 */
	public static String [] list2Array(List<String> list){
		if(list == null)
			return null;
		int size = list.size();
		String [] values = new String[size];

		for(int i = 0; i < size; i++){
			values[i] = list.get(i);
		}
		return values.length > 0 ? values : null;
	}

	/**
	 * 将list转换为字符串数组
	 * 
	 * @param list
	 * @return
	 */
	public static String list2String(List<String> list, String split){
		if(list == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for(String s : list){
			sb.append(s).append(split);
		}
		return sb.toString();
	}

	/** 数组转化为字符串 */
	public static String array2String(String [] strs){
		if(strs == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for(String s : strs){
			sb.append(s).append(";");
		}
		return sb.toString();
	}

	/** 判断是否为windows操作系统 */
	public static boolean isWindows(){
		boolean bReturn = false;

		String os = System.getProperties().getProperty("os.name").toLowerCase();
		if(os == null && "".equals(os))
			return true;

		if(os.indexOf("window") >= 0){
			bReturn = true;
		}

		return bReturn;
	}

	/***************************************************************************
	 * 以下为 时间相关 方法
	 **************************************************************************/

	/** 转换时间为字符串格式 yyyy-MM-dd HH:mm:ss */
	public static String getDateString(Date date){
		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat f = new SimpleDateFormat(pattern);

		return f.format(date);
	}

	/** 转换时间为字符串格式 yyyy-MM-dd HH:mm:ss */
	public static String getDateString(Timestamp tsp){
		Date date = new Date(tsp.getTime());

		return getDateString(date);
	}

	/** 转换时间为字符串格式 yyyyMMddHHmmss */
	public static String getDateString_yyyyMMddHHmmss(Date date){
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		String strTime = f.format(date);

		return strTime;
	}

	/** 转换时间为字符串格式 yyyyMMddHH */
	public static String getDateString_yyyyMMddHH(Date date){
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHH");
		String strTime = f.format(date);

		return strTime;
	}

	/** 转换时间为字符串格式 yyyyMMdd */
	public static String getDateString_yyyyMMdd(Date date){
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		String strTime = f.format(date);

		return strTime;
	}

	/** 转换时间为字符串格式 yyyyMMddHHmmssSSS */
	public static String getDateString_yyyyMMddHHmmssSSS(Date date){
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String strTime = f.format(date);

		return strTime;
	}

	/** 转换时间为字符串格式 yyyyMMddHHmmss */
	public static String getDateString_yyyyMMddHHmmss(Timestamp tsp){
		Date date = new Date(tsp.getTime());

		return getDateString_yyyyMMddHHmmss(date);
	}

	/** 把 字符串 转换成 时间 */
	public static Date getDate(String str, String pattern) throws ParseException{
		SimpleDateFormat f = new SimpleDateFormat(pattern);
		Date d = f.parse(str);

		return d;
	}

	/** 把 yyyy-MM-dd HH:mm:ss形式的字符串 转换成 时间 */
	public static Date getDate1(String str) throws ParseException{
		String pattern = "yyyy-MM-dd HH:mm:ss";

		return getDate(str, pattern);
	}

	/** 把 yyyyMMddhhmmss形式的字符串 转换成 时间 */
	public static Date getDate2(String str) throws ParseException{
		String pattern = "yyyyMMddhhmmss";

		return getDate(str, pattern);
	}

	// 通过正则表达式查找
	public static String findByRegex(String str, String regEx, int group){
		String resultValue = null;
		if(regEx == null || (regEx != null && "".equals(regEx.trim()))){
			return resultValue;
		}
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);

		boolean result = m.find();// 查找是否有匹配的结果
		if(result){
			resultValue = m.group(group);// 找出匹配的结果
		}
		return resultValue;
	}

	public static long crc32(String str){
		java.util.zip.CRC32 x = new java.util.zip.CRC32();
		x.update(str.getBytes());
		return x.getValue();
	}

	/**
	 * 把一组pojo对象转换为xml格式
	 * 
	 * @param pojos
	 * @return
	 */
	public static <T>Document pojosToXML(List<T> pojos){
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("pojos");
		for(Object pojo : pojos){
			Element child = root.addElement("pojo");
			Class<?> cls = pojo.getClass();
			Field [] fields = cls.getDeclaredFields();

			final String DATE1 = "java.sql.Timestamp";
			final String DATE2 = "java.util.Date";

			for(Field f : fields){
				f.setAccessible(true);
				String name = f.getName();
				String value = null;
				String type = f.getType().getName();
				if(type.equals(DATE1) || type.equals(DATE2)){
					try{
						Object obj = f.get(pojo);
						if(obj == null){
							value = "";
						}else{
							Date d = (Date)obj;
							value = getDateString(d);
						}
					}catch(Exception e){
						e.printStackTrace();
					}

				}else{
					try{
						Object obj = f.get(pojo);
						if(obj == null){
							value = "";
						}else{
							value = obj.toString();
						}
					}catch(Exception e){
						e.printStackTrace();
					}

				}
				Element e = child.addElement(name);
				e.setText(value);
			}
		}
		return doc;
	}

	/**
	 * 把一个pojo对象转换为xml格式
	 * 
	 * @param pojo
	 * @return
	 */
	public static Document pojoToXML(Object pojo){
		List<Object> list = new ArrayList<Object>();
		list.add(pojo);
		return pojosToXML(list);
	}

	/**
	 * 将字符串转为md5码，如果传入的字符串为null，将返回null
	 * 
	 * @param s 字符串
	 * @return md5码
	 */
	public static String toMD5(String s){
		if(s == null){
			return null;
		}
		StringBuilder result = new StringBuilder();

		byte [] bs = null;
		try{
			bs = MessageDigest.getInstance("md5").digest(s.getBytes());
		}catch(NoSuchAlgorithmException e){
			log.error("转md5时异常", e);
			return null;
		}

		String tmp = null;
		for(int i = 0; i < bs.length; i++){
			tmp = (Integer.toHexString(bs[i] & 0xFF));
			if(tmp.length() == 1){
				result.append("0");
			}
			result.append(tmp);
		}

		return result.toString();
	}

	// 单元测试
	public static void main(String [] args) throws Exception{}

	public static boolean isFileNotNull(FTPFile [] fs, String ip){
		if(fs == null){
			// if ( ip != null )
			// {
			// log.debug(ip + " - test - fs == null!");
			// }
			return false;
		}
		if(fs.length == 0){
			// if ( ip != null )
			// {
			// log.debug(ip + " - test - fs.length == 0!");
			// }
			return false;
		}
		boolean b = false;
		for(FTPFile f : fs){
			// if ( f != null && ip != null )
			// {
			// log.debug(ip + " - test - [" + f.getName() + "]");
			// }
			// else if ( f == null )
			// {
			// log.debug(ip + " - test f == null!");
			// }

			if((f != null && Util.isNotNull(f.getName()) && !f.getName().contains("\t"))
					|| (f != null && Util.isNotNull(f.getName()) && f.getName().contains(".loc"))){
				return true;
			}

		}
		return b;
	}

	public static boolean isFileNotNull(FTPFile [] fs){

		return isFileNotNull(fs, null);
	}

	private static FTPClient newFTP(String ip, int port, String user, String pwd, String type) throws Exception{
		FTPClient ftp = new FTPClient();
		ftp.connect(ip, port);
		ftp.login(user, pwd);
		ftp.configure(new FTPClientConfig(type));
		return ftp;
	}

	public static FTPClient setFTPClientConfig(FTPClient ftp, String ip, int port, String user, String pwd)
			throws Exception{

		// 修改成与FTPTool一样的设置顺序--liangww modify 2012-11-21
		ftp.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_NT);
		}else{
			return ftp;
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_AS400);
		}else{
			return ftp;
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_L8);
		}else{
			return ftp;
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_MVS);
		}else{
			return ftp;
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_NETWARE);
		}else{
			return ftp;
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_OS2);
		}else{
			log.debug("use SYST_NETWARE");
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_OS400);
		}else{
			return ftp;
		}
		if(!isFileNotNull(ftp.listFiles("/*"))){
			ftp.disconnect();
			ftp = newFTP(ip, port, user, pwd, FTPClientConfig.SYST_VMS);
		}else{
			return ftp;
		}
		return ftp;
	}

	/**
	 * 判断一个字符，是否可作为ORACLE的NUMBER类型数据，并返回相应的{@link Double}对象。如果传入的字符串为
	 * <code>null</code>或空字符串，将返回 <code>null</code>.
	 * 
	 * @param str 需要判断的字符串。
	 * @return {@link Double}对象。
	 * @author ChenSijiang 2011-01-04 17:23
	 */
	public static Double isOracleNumberString(String str){
		if(Util.isNull(str))
			return null;
		String s = str.trim();
		try{
			Double dou = Double.parseDouble(s);
			return dou;
		}catch(Exception unused){
			return null;
		}
	}

	/**
	 * <p>
	 * 关闭{@link Closeable}.
	 * </p>
	 * 
	 * @param closeable {@link Closeable}对象
	 * @author ChenSijiang 2011-01-04 18:52
	 */
	public static void closeCloseable(Closeable closeable){
		if(closeable != null){
			try{
				closeable.close();
			}catch(IOException unused){}
		}
	}

	/**
	 * 空字符串处理方法。当<code>str</code>为<code>null</code>或空字符串时，返回<code>replace</code>
	 * ，否则返回 <code>str</code>.
	 * 
	 * @param str 需要进行处理的字符串。
	 * @param replace 当<code>str</code>为<code>null</code>或空字符串时的替换值。
	 * @return 当<code>str</code>为<code>null</code>或空字符串时，返回<code>replace</code>
	 *         ，否则返回 <code>str</code>.
	 */
	public static String nvl(String str, String replace){
		return Util.isNull(str) ? replace : str;
	}

	public static String listStringArray(String [] strArr, char splitChar){
		if(strArr == null || strArr.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();

		for(String s : strArr){
			sb.append(s).append(splitChar);
		}
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	}

	/**
	 * 从data中提取字符串
	 * 
	 * @param data
	 * @param beginStr 开始标记
	 * @param endStr 结束标记
	 * @return
	 */
	public static String getStr(String data, String beginStr, String endStr){
		return getStr(data, "", beginStr, endStr);
	}

	/**
	 * 从data中提取字符串
	 * 
	 * @param data
	 * @param begin1Str 开始1标记
	 * @param begin2Str 开始2标记
	 * @param endStr 结束标记
	 * @return
	 */
	public static String getStr(String data, String begin1Str, String begin2Str, String endStr){
		if(data == null || begin1Str == null || begin2Str == null || endStr == null){
			return null;
		}

		int begin1 = data.indexOf(begin1Str);
		if(begin1 == -1){
			return null;
		}

		int begin2 = data.indexOf(begin2Str, begin1 + begin1Str.length());
		if(begin2 == -1){
			return null;
		}

		int end = data.indexOf(endStr, begin2 + begin2Str.length());
		if(end == -1){
			return null;
		}

		return data.substring(begin2 + begin2Str.length(), end);
	}

	/**
	 * 从尾总开始的提取data数据
	 * 
	 * @param data
	 * @param begin1Str 从尾总查找的开始1标记
	 * @param begin2Str 从尾总查找的开始2标记
	 * @param endStr 从尾总查找的结束标记
	 * @return
	 */
	public static String getStrFromLast(String data, String begin1Str, String begin2Str, String endStr){
		int begin1 = data.lastIndexOf(begin1Str);
		if(begin1 == -1){
			return null;
		}

		int begin2 = data.substring(0, begin1).lastIndexOf(begin2Str);
		if(begin2 == -1){
			return null;
		}

		int end = data.substring(0, begin2).lastIndexOf(endStr);
		if(end == -1){
			return null;
		}

		return data.substring(end + endStr.length(), begin2);
	}

	/**
	 * 判断是否是32位操作系统
	 * 
	 * @return
	 */
	public static boolean is32Digit(){
		int os_digit = Integer.parseInt(System.getProperty("sun.arch.data.model"));
		log.debug("操作系统位数：" + os_digit);
		if(os_digit == 32)
			return true;
		return false;
	}

	/**
	 * 解析英文格式的时间 如：16-JUL-13 01.40.37.076000 AM
	 * 
	 * @param time
	 * @return
	 * @throws ParseException
	 */
	public static String parseEnglishTime(String time) throws ParseException{
		time = time.replace("000 ", " ");
		DateFormat df1 = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS aa", Locale.ENGLISH);
		Date date = df1.parse(time);
		df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df1.format(date);
	}

	/**
	 * 字符串拆分
	 * 
	 * @param string
	 * @param perfix
	 * @param maxArrayLength
	 * @return 拆分后的数组
	 */
	public static final String [] split(String string, String perfix){
		if(isNull(string))
			return null;
		if(perfix == null || perfix.length() == 0)
			return new String[]{string};
		int index = string.indexOf(perfix);
		if(index < 0)
			return new String[]{string};
		List<String> splitList = new ArrayList<String>();
		while((index = string.indexOf(perfix)) != -1){
			splitList.add(string.substring(0, index));
			string = string.substring(index + perfix.length());
		}
		splitList.add(string.substring(0));
		String [] array = new String[splitList.size()];
		return splitList.toArray(array);
	}

	public static String ParseFilePath(String strPath, Date date){
		return ParseFilePath(strPath, new Timestamp(date.getTime()));
	}

	public static String ParseFilePath(String strPath, Timestamp timestamp){
		// 如：zblecp-2006102311.hsmr 文件名包含时间，表示这个时间的数据。
		// 这个时间有是timestamp传递进来的时间
		// 但是，有的文件名是一个时间段，前后有2个时间。
		// 如：Domain125_PSbasicmeasurement_18Jul2008_0900-18Jul2008_1000.csv
		// 现在从strPath传递进来一个参数表示前后时间的间隔数，
		// 如：Domain125_PSbasicmeasurement_%%D%%EM%%Y_%%H%%m-%%ND%%ENM%%NY_%%NH%%Nm.csv|360000

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

		int iHwrIdx = strPath.indexOf("%%TA");
		int iDiff = 0;
		if(iHwrIdx >= 0){
			// 偏移量 0－9，只支持正偏移
			iDiff = Integer.parseInt(strPath.substring(iHwrIdx + 4, iHwrIdx + 5));
			strPath = strPath.replaceAll("%%TA.", "");
		}

		timestamp = new Timestamp(timestamp.getTime() + iDiff * 3600 * 1000);
		String strTime = formatter.format(timestamp);

		if(strPath.indexOf("%%Y") >= 0)
			strPath = strPath.replace("%%Y", strTime.substring(0, 4));
		Calendar calendar = Calendar.getInstance();

		Date date = new Date();
		date.setTime(timestamp.getTime());
		calendar.setTime(new Date());

		calendar.setTime(date);
		int nDayOrYear = calendar.get(Calendar.DAY_OF_YEAR);

		if(strPath.indexOf("%%WEEK") >= 0){
			int dow = calendar.get(Calendar.DAY_OF_WEEK);
			dow = dow - 1;
			if(dow == 0)
				dow = 7;
			strPath = strPath.replace("%%WEEK", String.valueOf(dow));
		}

		if(nDayOrYear < 10)
			strPath = strPath.replace("%%DayOfYear", "00" + nDayOrYear);
		else if(nDayOrYear < 100)
			strPath = strPath.replace("%%DayOfYear", "0" + nDayOrYear);
		else
			strPath = strPath.replace("%%DayOfYear", String.valueOf(nDayOrYear));

		if(strPath.indexOf("%%y") >= 0)
			strPath = strPath.replace("%%y", strTime.substring(2, 4));

		if(strPath.indexOf("%%EM") >= 0){
			switch(Integer.parseInt(strTime.substring(4, 6))){
				case 1:
					strPath = strPath.replace("%%EM", "Jan");
					break;
				case 2:
					strPath = strPath.replace("%%EM", "Feb");
					break;
				case 3:
					strPath = strPath.replace("%%EM", "Mar");
					break;
				case 4:
					strPath = strPath.replace("%%EM", "Apr");
					break;
				case 5:
					strPath = strPath.replace("%%EM", "May");
					break;
				case 6:
					strPath = strPath.replace("%%EM", "Jun");
					break;
				case 7:
					strPath = strPath.replace("%%EM", "Jul");
					break;
				case 8:
					strPath = strPath.replace("%%EM", "Aug");
					break;
				case 9:
					strPath = strPath.replace("%%EM", "Sep");
					break;
				case 10:
					strPath = strPath.replace("%%EM", "Oct");
					break;
				case 11:
					strPath = strPath.replace("%%EM", "Nov");
					break;
				case 12:
					strPath = strPath.replace("%%EM", "Dec");
					break;
			}
		}

		if(strPath.indexOf("%%M") >= 0)
			strPath = strPath.replace("%%M", strTime.substring(4, 6));

		if(strPath.indexOf("%%d") >= 0)
			strPath = strPath.replace("%%d", strTime.substring(6, 8));

		if(strPath.indexOf("%%D") >= 0)
			strPath = strPath.replace("%%D", strTime.substring(6, 8));

		// add by liuwx 针对天,小时是否补0 ,如果天为0-9 ，则返回0-9 天为10-31返回10-31 ，小时0-9则0-9
		// ，小时10-23则10-23
		if(strPath.indexOf("%%fd") >= 0){
			strPath = strPath.replace("%%fd", strTime.substring(6, 8));
		}
		String sd = null;
		if(strPath.indexOf("%%FD") >= 0){
			sd = strTime.substring(6, 8);
			try{
				if(Integer.valueOf(sd) < 10){
					strPath = strPath.replace("%%FD", strTime.substring(7, 8));
				}else
					strPath = strPath.replace("%%FD", strTime.substring(6, 8));
			}catch(NumberFormatException e){
				e.printStackTrace();
			}
		}
		String fh = null;
		if(strPath.indexOf("%%FH") >= 0){
			String strHour = getExpression(strPath);
			if(strHour != null && !strHour.equals("")){
				String strHourTmp = strHour.replaceAll("%%FH", strTime.substring(8, 10));
				int nHour = Util.parseExpression(strHourTmp);

				/*
				 * ChenSijiang 2011-01-18
				 * 如果现在是23点，那么(%%H+1)会变成24，应该是不存在24点的说法。所以此处改成，如果小时过了23之后，变为0.
				 */
				if(nHour > 23)
					nHour = 0;

				strPath = strPath.replace("(" + strHour + ")", String.valueOf(nHour));
				if(nHour < 10)
					strPath = strPath.replaceAll("%%FH", strTime.substring(7, 10));
				else
					strPath = strPath.replaceAll("%%FH", strTime.substring(8, 10));
			}else{
				fh = strTime.substring(8, 10);
				if(Integer.valueOf(fh) < 10){
					strPath = strPath.replace("%%FH", strTime.substring(9, 10));
				}else
					strPath = strPath.replace("%%FH", strTime.substring(8, 10));
			}
		}
		// end add

		// G网贝尔用，有24点的文件名。
		if(strPath.indexOf("%%BH") >= 0){
			String strHour = getExpression(strPath);
			if(strHour != null && !strHour.equals("")){
				String strHourTmp = strHour.replaceAll("%%BH", strTime.substring(8, 10));
				int nHour = Util.parseExpression(strHourTmp);

				strPath = strPath.replace("(" + strHour + ")", Util.trimHour(nHour));
				strPath = strPath.replaceAll("%%BH", strTime.substring(8, 10));
			}else{
				strPath = strPath.replaceAll("%%BH", strTime.substring(8, 10));
			}
		}

		if(strPath.indexOf("%%H") >= 0){
			String strHour = getExpression(strPath);
			if(strHour != null && !strHour.equals("")){
				String strHourTmp = strHour.replaceAll("%%H", strTime.substring(8, 10));
				int nHour = Util.parseExpression(strHourTmp);

				/*
				 * ChenSijiang 2011-01-18
				 * 如果现在是23点，那么(%%H+1)会变成24，应该是不存在24点的说法。所以此处改成，如果小时过了23之后，变为0.
				 */
				if(nHour > 23 && !strPath.toLowerCase().contains("/apme/obsynt/"))
					nHour = 0;

				// String temp = strPath.substring(strPath.indexOf("("),
				// strPath.indexOf(")") + 1);

				// if ( temp.contains("%%") )
				strPath = strPath.replace("(" + strHour + ")", Util.trimHour(nHour));
				strPath = strPath.replaceAll("%%H", strTime.substring(8, 10));
			}else{
				strPath = strPath.replaceAll("%%H", strTime.substring(8, 10));
			}
		}

		if(strPath.indexOf("%%h") >= 0)
			strPath = strPath.replaceAll("%%h", strTime.substring(8, 10));

		if(strPath.indexOf("%%m") >= 0)
			strPath = strPath.replace("%%m", strTime.substring(10, 12));

		if(strPath.indexOf("%%s") >= 0)
			strPath = strPath.replace("%%s", strTime.substring(12, 14));

		if(strPath.indexOf("%%S") >= 0)
			strPath = strPath.replace("%%S", strTime.substring(12, 14));

		String strInterval = "";
		int nInterval = 0;
		if(strPath.indexOf("|") > 0){
			strInterval = strPath.substring(strPath.indexOf("|") + 1);
			strPath = strPath.substring(0, strPath.indexOf("|"));

			nInterval = Integer.parseInt(strInterval);
			timestamp = new Timestamp(timestamp.getTime() + nInterval);
			strTime = formatter.format(timestamp);

			calendar.setTime(timestamp);

			if(strPath.indexOf("%%NWEEK") >= 0){
				int dow = calendar.get(Calendar.DAY_OF_WEEK);
				dow = dow - 1;
				if(dow == 0)
					dow = 7;
				strPath = strPath.replace("%%NWEEK", String.valueOf(dow));
			}

			if(strPath.indexOf("%%NY") >= 0)
				strPath = strPath.replace("%%NY", strTime.substring(0, 4));

			if(strPath.indexOf("%%Ny") >= 0)
				strPath = strPath.replace("%%Ny", strTime.substring(2, 4));

			if(strPath.indexOf("%%NEM") >= 0){
				switch(Integer.parseInt(strTime.substring(4, 6))){
					case 1:
						strPath = strPath.replace("%%NEM", "Jan");
						break;
					case 2:
						strPath = strPath.replace("%%NEM", "Feb");
						break;
					case 3:
						strPath = strPath.replace("%%NEM", "Mar");
						break;
					case 4:
						strPath = strPath.replace("%%NEM", "Apr");
						break;
					case 5:
						strPath = strPath.replace("%%NEM", "May");
						break;
					case 6:
						strPath = strPath.replace("%%NEM", "Jun");
						break;
					case 7:
						strPath = strPath.replace("%%NEM", "Jul");
						break;
					case 8:
						strPath = strPath.replace("%%NEM", "Aug");
						break;
					case 9:
						strPath = strPath.replace("%%NEM", "Sep");
						break;
					case 10:
						strPath = strPath.replace("%%NEM", "Oct");
						break;
					case 11:
						strPath = strPath.replace("%%NEM", "Nov");
						break;
					case 12:
						strPath = strPath.replace("%%NEM", "Dec");
						break;
				}
			}

			if(strPath.indexOf("%%NM") >= 0)
				strPath = strPath.replace("%%NM", strTime.substring(4, 6));

			if(strPath.indexOf("%%Nd") >= 0)
				strPath = strPath.replace("%%Nd", strTime.substring(6, 8));

			if(strPath.indexOf("%%ND") >= 0)
				strPath = strPath.replace("%%ND", strTime.substring(6, 8));

			if(strPath.indexOf("%%NH") >= 0)
				strPath = strPath.replace("%%NH", strTime.substring(8, 10));

			if(strPath.indexOf("%%NV4") >= 0){
				int nNum = Integer.parseInt(strTime.substring(8, 10));
				nNum = (nNum + 1) / 4;
				strPath = strPath.replace("%%NV4", "0" + nNum);
			}

			if(strPath.indexOf("%%Nh") >= 0)
				strPath = strPath.replace("%%Nh", strTime.substring(8, 10));

			if(strPath.indexOf("%%Nm") >= 0)
				strPath = strPath.replace("%%Nm", strTime.substring(10, 12));

			if(strPath.indexOf("%%Ns") >= 0)
				strPath = strPath.replace("%%Ns", strTime.substring(12, 14));

			if(strPath.indexOf("%%NS") >= 0)
				strPath = strPath.replace("%%NS", strTime.substring(12, 14));
		}

		return strPath;
	}

	/** 取（）之间的内容 */
	public static String getExpression(String str){

		String result = null;

		int b = str.indexOf("(");
		int e = str.indexOf(")");

		if(b > 0 && e > b){
			result = str.substring(b + 1, e);
		}

		if(result != null && !result.contains("%%"))
			return null;

		return result;
	}

	/* 在Oracle 数据库中将Clob转换成String类型 */
	public static String ClobParse(Clob clob){
		if(clob == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try{
			java.io.Reader is = clob.getCharacterStream();
			java.io.BufferedReader br = new java.io.BufferedReader(is);
			String s = null;
			// 当到达最后一行readLine的时候,s为空退出
			while((s = br.readLine()) != null){
				sb.append(s);
				// content += s;
			}
			br.close();
			is.close();
			// content = clob.getSubString((long)1,(int)clob.length());
		}catch(Exception e){
			log.error("读取clob字段时发生异常。", e);
		}
		return sb.toString();
	}

	public static String ParseFilePathForDB(String strPath, Timestamp timestamp){
		// 如：zblecp-2006102311.hsmr 文件名包含时间，表示这个时间的数据。
		// 这个时间有是timestamp传递进来的时间
		// 但是，有的文件名是一个时间段，前后有2个时间。
		// 如：Domain125_PSbasicmeasurement_18Jul2008_0900-18Jul2008_1000.csv
		// 现在从strPath传递进来一个参数表示前后时间的间隔数，
		// 如：Domain125_PSbasicmeasurement_%%D%%EM%%Y_%%H%%m-%%ND%%ENM%%NY_%%NH%%Nm.csv|360000

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

		int iHwrIdx = strPath.indexOf("%%TA");
		int iDiff = 0;
		if(iHwrIdx >= 0){
			// 偏移量 0－9，只支持正偏移
			iDiff = Integer.parseInt(strPath.substring(iHwrIdx + 4, iHwrIdx + 5));
			strPath = strPath.replaceAll("%%TA.", "");
		}

		timestamp = new Timestamp(timestamp.getTime() + iDiff * 3600 * 1000);
		String strTime = formatter.format(timestamp);

		if(strPath.indexOf("%%Y") >= 0)
			strPath = strPath.replace("%%Y", strTime.substring(0, 4));
		Calendar calendar = Calendar.getInstance();

		Date date = new Date();
		date.setTime(timestamp.getTime());
		calendar.setTime(new Date());

		calendar.setTime(date);
		int nDayOrYear = calendar.get(Calendar.DAY_OF_YEAR);

		if(nDayOrYear < 10)
			strPath = strPath.replace("%%DayOfYear", "00" + nDayOrYear);
		else if(nDayOrYear < 100)
			strPath = strPath.replace("%%DayOfYear", "0" + nDayOrYear);
		else
			strPath = strPath.replace("%%DayOfYear", String.valueOf(nDayOrYear));

		if(strPath.indexOf("%%y") >= 0)
			strPath = strPath.replace("%%y", strTime.substring(2, 4));

		if(strPath.indexOf("%%EM") >= 0){
			switch(Integer.parseInt(strTime.substring(4, 6))){
				case 1:
					strPath = strPath.replace("%%EM", "Jan");
					break;
				case 2:
					strPath = strPath.replace("%%EM", "Feb");
					break;
				case 3:
					strPath = strPath.replace("%%EM", "Mar");
					break;
				case 4:
					strPath = strPath.replace("%%EM", "Apr");
					break;
				case 5:
					strPath = strPath.replace("%%EM", "May");
					break;
				case 6:
					strPath = strPath.replace("%%EM", "Jun");
					break;
				case 7:
					strPath = strPath.replace("%%EM", "Jul");
					break;
				case 8:
					strPath = strPath.replace("%%EM", "Aug");
					break;
				case 9:
					strPath = strPath.replace("%%EM", "Sep");
					break;
				case 10:
					strPath = strPath.replace("%%EM", "Oct");
					break;
				case 11:
					strPath = strPath.replace("%%EM", "Nov");
					break;
				case 12:
					strPath = strPath.replace("%%EM", "Dec");
					break;
			}
		}

		if(strPath.indexOf("%%M") >= 0)
			strPath = strPath.replace("%%M", strTime.substring(4, 6));

		if(strPath.indexOf("%%NZM") >= 0){
			// 前面未补"0"的月份(NZM=no zero month) chensj 2011-7-18增加
			strPath = strPath.replace("%%NZM", String.valueOf(Integer.parseInt(strTime.substring(4, 6))));
		}

		if(strPath.indexOf("%%d") >= 0)
			strPath = strPath.replace("%%d", strTime.substring(6, 8));

		if(strPath.indexOf("%%D") >= 0)
			strPath = strPath.replace("%%D", strTime.substring(6, 8));

		if(strPath.indexOf("%%H") >= 0){
			strPath = strPath.replace("%%H", strTime.substring(8, 10));
		}

		if(strPath.indexOf("%%h") >= 0)
			strPath = strPath.replace("%%h", strTime.substring(8, 10));

		if(strPath.indexOf("%%m") >= 0)
			strPath = strPath.replace("%%m", strTime.substring(10, 12));

		if(strPath.indexOf("%%s") >= 0)
			strPath = strPath.replace("%%s", strTime.substring(12, 14));

		if(strPath.indexOf("%%S") >= 0)
			strPath = strPath.replace("%%S", strTime.substring(12, 14));

		String strInterval = "";
		int nInterval = 0;
		if(strPath.indexOf("|") > 0){
			strInterval = strPath.substring(strPath.indexOf("|") + 1);
			strPath = strPath.substring(0, strPath.indexOf("|"));

			nInterval = Integer.parseInt(strInterval);
			timestamp = new Timestamp(timestamp.getTime() + nInterval);
			strTime = formatter.format(timestamp);

			if(strPath.indexOf("%%NY") >= 0)
				strPath = strPath.replace("%%NY", strTime.substring(0, 4));

			if(strPath.indexOf("%%Ny") >= 0)
				strPath = strPath.replace("%%Ny", strTime.substring(2, 4));

			if(strPath.indexOf("%%NEM") >= 0){
				switch(Integer.parseInt(strTime.substring(4, 6))){
					case 1:
						strPath = strPath.replace("%%NEM", "Jan");
						break;
					case 2:
						strPath = strPath.replace("%%NEM", "Feb");
						break;
					case 3:
						strPath = strPath.replace("%%NEM", "Mar");
						break;
					case 4:
						strPath = strPath.replace("%%NEM", "Apr");
						break;
					case 5:
						strPath = strPath.replace("%%NEM", "May");
						break;
					case 6:
						strPath = strPath.replace("%%NEM", "Jun");
						break;
					case 7:
						strPath = strPath.replace("%%NEM", "Jul");
						break;
					case 8:
						strPath = strPath.replace("%%NEM", "Aug");
						break;
					case 9:
						strPath = strPath.replace("%%NEM", "Sep");
						break;
					case 10:
						strPath = strPath.replace("%%NEM", "Oct");
						break;
					case 11:
						strPath = strPath.replace("%%NEM", "Nov");
						break;
					case 12:
						strPath = strPath.replace("%%NEM", "Dec");
						break;
				}
			}

			if(strPath.indexOf("%%NM") >= 0)
				strPath = strPath.replace("%%NM", strTime.substring(4, 6));

			if(strPath.indexOf("%%Nd") >= 0)
				strPath = strPath.replace("%%Nd", strTime.substring(6, 8));

			if(strPath.indexOf("%%ND") >= 0)
				strPath = strPath.replace("%%ND", strTime.substring(6, 8));

			if(strPath.indexOf("%%NH") >= 0)
				strPath = strPath.replace("%%NH", strTime.substring(8, 10));

			if(strPath.indexOf("%%NV4") >= 0){
				int nNum = Integer.parseInt(strTime.substring(8, 10));
				nNum = (nNum + 1) / 4;
				strPath = strPath.replace("%%NV4", "0" + nNum);
			}

			if(strPath.indexOf("%%Nh") >= 0)
				strPath = strPath.replace("%%Nh", strTime.substring(8, 10));

			if(strPath.indexOf("%%Nm") >= 0)
				strPath = strPath.replace("%%Nm", strTime.substring(10, 12));

			if(strPath.indexOf("%%Ns") >= 0)
				strPath = strPath.replace("%%Ns", strTime.substring(12, 14));

			if(strPath.indexOf("%%NS") >= 0)
				strPath = strPath.replace("%%NS", strTime.substring(12, 14));
		}

		return strPath;
	}
}
