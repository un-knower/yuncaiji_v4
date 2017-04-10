package cn.uway.ucloude.uts.core.exception;

/**
 *  当没有 找到 JobTracker 节点的时候抛出这个异常
 * @author uway
 *
 */
public class JobTrackerNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -274936989304810888L;
	public JobTrackerNotFoundException() {
    }

    public JobTrackerNotFoundException(String message) {
        super(message);
    }

    public JobTrackerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobTrackerNotFoundException(Throwable cause) {
        super(cause);
    }
}
