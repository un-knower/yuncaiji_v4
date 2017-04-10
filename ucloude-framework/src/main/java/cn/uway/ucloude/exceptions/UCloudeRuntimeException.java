package cn.uway.ucloude.exceptions;

public class UCloudeRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3693711746717895439L;

	public UCloudeRuntimeException() {
		super();
	}

	public UCloudeRuntimeException(String message) {
		super(message);
	}

	public UCloudeRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UCloudeRuntimeException(Throwable cause) {
		super(cause);
	}
}
