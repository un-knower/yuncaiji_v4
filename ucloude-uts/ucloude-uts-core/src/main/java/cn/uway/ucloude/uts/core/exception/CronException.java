package cn.uway.ucloude.uts.core.exception;

public class CronException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2002948190614392428L;
	public CronException() {
        super();
    }

    public CronException(String message) {
        super(message);
    }

    public CronException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronException(Throwable cause) {
        super(cause);
    }
}
