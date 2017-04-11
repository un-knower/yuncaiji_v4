package cn.uway.framework.cleaner;

import java.util.List;

/**
 * 本地文件清理规则<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public interface CleanRule {

	/**
	 * 判断是否可以被清除
	 * 
	 * @param localFile
	 * @return boolean 是否可以被清除
	 */
	boolean cleanable(List<String> localFile);
}
