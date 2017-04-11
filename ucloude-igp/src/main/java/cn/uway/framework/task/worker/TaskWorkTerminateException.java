package cn.uway.framework.task.worker;


public class TaskWorkTerminateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * 错误识别码
	 */
	public static int exceptionCode = -9091;
	
	public TaskWorkTerminateException(String errMsg) {
		super(errMsg);
	}

}
