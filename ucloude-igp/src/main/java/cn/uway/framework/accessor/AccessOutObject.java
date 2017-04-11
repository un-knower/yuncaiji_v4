package cn.uway.framework.accessor;

import cn.uway.framework.task.Task;

/**
 * 接入输出的对象
 * <p>
 * 数据接入之后从接入方法返回的对象.
 * </p>
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public class AccessOutObject{

	/**
	 * 原始接入对象名
	 */
	private String rawAccessName;
	
	/**
	 * 原始接入子对象包名(一般为一个压缩包的名字)
	 */
	private String rawAccessPackName;

	/**
	 * 采集任务
	 */
	private Task task;

	public String getRawAccessName(){
		return rawAccessName;
	}

	public Task getTask(){
		return task;
	}

	public void setTask(Task task){
		this.task = task;
	}

	public void setRawAccessName(String rawAccessName){
		this.rawAccessName = rawAccessName;
	}

	public String getRawAccessPackName() {
		if (rawAccessPackName == null)
			return this.rawAccessName;
		
		return rawAccessPackName;
	}

	public void setRawAccessPackName(String rawAccessPackName) {
		this.rawAccessPackName = rawAccessPackName;
	}
	
}
