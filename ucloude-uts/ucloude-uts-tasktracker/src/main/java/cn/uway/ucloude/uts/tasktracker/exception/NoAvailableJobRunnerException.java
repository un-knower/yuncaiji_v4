package cn.uway.ucloude.uts.tasktracker.exception;

/**
 * 没有可用的线程
 * @author uway
 *
 */
public class NoAvailableJobRunnerException extends Exception{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6868552008018589689L;

	public NoAvailableJobRunnerException() {
        super();
    }

    public NoAvailableJobRunnerException(String message) {
        super(message);
    }

    public NoAvailableJobRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAvailableJobRunnerException(Throwable cause) {
        super(cause);
    }

}