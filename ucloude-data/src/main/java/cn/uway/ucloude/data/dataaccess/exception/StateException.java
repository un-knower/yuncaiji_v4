package cn.uway.ucloude.data.dataaccess.exception;

public class StateException extends RuntimeException {
	private static final long serialVersionUID = -1431221683278943387L;

	public StateException() {
    }

    public StateException(String message) {
        super(message);
    }

    public StateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateException(Throwable cause) {
        super(cause);
    }
}
