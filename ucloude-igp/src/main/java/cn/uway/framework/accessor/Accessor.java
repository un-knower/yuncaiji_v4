package cn.uway.framework.accessor;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.Task;

/**
 * 数据接入器接口。<br>
 * 操作顺序:<br>
 * 1、调用setConnectionInfo设置连接信息<br>
 * 2、执行beforeAccess<br>
 * 3、执行access<br>
 * 4、使用完毕后执行close<br>
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public interface Accessor {

	/**
	 * 设置数据源连接对象
	 * 
	 * @param connInfo
	 */
	void setConnectionInfo(ConnectionInfo connInfo);

	/**
	 * 数据接入之前的操作
	 * 
	 * @return 操作是否成功
	 */
	boolean beforeAccess();

	/**
	 * 接入方法
	 * 
	 * @param connectionInfo 数据源连接信息{@link ConnectionInfo}
	 * @param paths 采集路径实体{@link GatherPathEntry}数组
	 * @return 接入输出对象{@link AccessOutObject}
	 */
	AccessOutObject access(GatherPathEntry path) throws Exception;

	/**
	 * 接入关闭方法，主要用来释放资源，比如关闭连接
	 */
	void close();

	/**
	 * 获取接入数据处理报告
	 * 
	 * @param taskId
	 */
	AccessorReport report();
	
	Task getTask();
	
	
	void setTask(Task task);
}
