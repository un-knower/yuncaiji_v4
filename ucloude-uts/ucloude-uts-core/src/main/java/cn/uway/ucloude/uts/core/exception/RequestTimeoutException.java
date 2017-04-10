package cn.uway.ucloude.uts.core.exception;

public class RequestTimeoutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6476434800897264052L;
	public RequestTimeoutException() {
        super();
    }

    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestTimeoutException(Throwable cause) {
        super(cause);
    }

}
