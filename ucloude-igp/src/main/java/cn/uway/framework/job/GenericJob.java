package cn.uway.framework.job;


/**
 * 一般性Job
 * <p>
 * 一个Job只是一个线程.
 * </p>
 * 
 * @author MikeYang
 * @Date 2012-10-30
 * @version 1.0
 * @since 1.0
 */
public class GenericJob extends AbstractJob {

	/**
	 * 构造方法
	 * 
	 * @param jobParam
	 *            作业运行参数{@link JobParam}
	 */
	public GenericJob(JobParam jobParam) {
		super(jobParam);
	}

	@Override
	public void beforeAccess(String beforeAccessShell) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeParse(String beforeParseShell) {
		// TODO Auto-generated method stub

	}

}
