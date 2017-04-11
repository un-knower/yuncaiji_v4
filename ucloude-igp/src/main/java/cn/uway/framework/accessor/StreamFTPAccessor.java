package cn.uway.framework.accessor;

import java.io.InputStream;

import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.Task;

/**
 * 基于数据流的FTP接入器。
 * 
 * @author ChenSijiang 2012-11-2
 */
public class StreamFTPAccessor extends FTPAccessor{

	/**
	 * 构造方法。
	 * 
	 * @param task 任务对象。
	 */
	public StreamFTPAccessor(){}

	@Override
	public AccessOutObject toAccessOutObject(InputStream in, String rawName, long len, GatherPathEntry gatherPathInfo, Task task){
		StreamAccessOutObject out = new StreamAccessOutObject();
		out.setOutObject(in);
		out.setLen(len);
		out.setRawAccessName(rawName);
		return out;
	}
}
