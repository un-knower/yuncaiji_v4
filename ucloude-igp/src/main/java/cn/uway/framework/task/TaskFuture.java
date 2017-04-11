package cn.uway.framework.task;


/**
 * 任务执行的结果
 * 
 * @author MikeYang
 * @Date 2012-10-29
 * @version 1.0
 * @since 3.0
 */
public class TaskFuture {

	private int code = 0; // 执行结果码

	private String cause; // 失败原因

	private Task task; // 参考对象

	/**
	 * 构造操作成功的结果对象
	 */
	public TaskFuture() {
		super();
	}

	/**
	 * 构建任务执行的结果对象
	 * 
	 * @param code
	 *            操作结果码
	 * @param refObj
	 *            如果失败，可以填写一下参考对象
	 */
	public TaskFuture(int code, Task refObj) {
		super();
		this.code = code;
		this.task = refObj;
	}

	/**
	 * 构建任务执行的结果对象
	 * 
	 * @param code
	 *            操作结果码
	 * @param cause
	 *            如果失败，可以填写一下原因
	 * @param refObj
	 *            如果失败，可以填写一下参考对象
	 */
	public TaskFuture(int code, String cause, Task refObj) {
		super();
		this.code = code;
		this.cause = cause;
		this.task = refObj;
	}

	/**
	 * 获取操作结果码
	 */
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * 获取失败原因
	 */
	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	/**
	 * 获取参考引用对象
	 */
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

}
