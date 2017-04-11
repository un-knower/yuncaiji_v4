package cn.uway.framework.task.worker.luc;

import java.io.File;

/**
 * 朗讯EVDO工具类<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public class LucEvdoUtil {

	/**
	 * 获取指定的任务对应的文件的断点<br>
	 * 
	 * @return 指定文件的断点<br>
	 */
	public static int getBreakpoint(File idxFile) {
		return 0;
	}

	/**
	 * 获取文件的头信息<br>
	 * 
	 * @param taskId
	 * @param evdoName
	 * @return EVDO文件头
	 */
	public static String getHeader(long taskId, String evdoName) {
		return null;
	}

	/**
	 * 文件是否需要进行拆分<br>
	 * 
	 * @return boolean 是否需要进行下载拆分,返回true，则需要下载,false则表示文件已经做过
	 */
	public static boolean valid() {

		return true;
	}
}
