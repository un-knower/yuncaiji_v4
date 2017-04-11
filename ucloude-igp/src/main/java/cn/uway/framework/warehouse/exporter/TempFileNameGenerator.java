package cn.uway.framework.warehouse.exporter;

/**
 * TempFileNameGenerator
 * 
 * @author chenrongqiang 2012-11-21
 */
public class TempFileNameGenerator {

	private static int tempNameSuffix = 0;

	/**
	 * 生成随机的临时文件名
	 * 
	 * @return
	 */
	public synchronized static String getTempFileName() {
		tempNameSuffix++;
		return "temp" + tempNameSuffix + ".temp";
	}
}
