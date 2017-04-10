package cn.uway.ucloude.uts.core.exception;

public class RpcSendException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3622071070653194L;

	public RpcSendException() {
        super();
    }

    public RpcSendException(String message) {
        super(message);
    }

    public RpcSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcSendException(Throwable cause) {
        super(cause);
    }
}
