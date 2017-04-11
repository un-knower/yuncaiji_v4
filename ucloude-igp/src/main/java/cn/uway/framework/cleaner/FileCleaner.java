package cn.uway.framework.cleaner;

import java.util.TimerTask;

/**
 * 文件清理接口<br>
 * 继承java.util.TimerTask。支持被定时调用<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public abstract class FileCleaner extends TimerTask {

	/**
	 * 清理方法
	 */
	abstract void clean();

	/**
	 * 线程执行方法
	 */
	public void run() {
		Thread.currentThread().setName("【文件清理线程】");
		clean();
	}
}
