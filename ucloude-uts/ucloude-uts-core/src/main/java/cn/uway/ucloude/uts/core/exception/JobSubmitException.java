package cn.uway.ucloude.uts.core.exception;

public class JobSubmitException extends RuntimeException {

	String xx;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5938770043730940263L;
	
	public JobSubmitException() {
        super();
    }

    public JobSubmitException(String message) {
        super(message);
    }

    public JobSubmitException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobSubmitException(Throwable cause) {
        super(cause);
    }

}
