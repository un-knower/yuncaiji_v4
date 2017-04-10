package cn.uway.ucloude.uts.core.exception;

public class UtsRuntimeException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8037345321477624802L;

	public UtsRuntimeException() {
        super();
    }

    public UtsRuntimeException(String message) {
        super(message);
    }

    public UtsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UtsRuntimeException(Throwable cause) {
        super(cause);
    }
}
