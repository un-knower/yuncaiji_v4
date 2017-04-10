package cn.uway.ucloude.cmd;

public class HttpCmdException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2247163453986649939L;

	public HttpCmdException() {
        super();
    }

    public HttpCmdException(String message) {
        super(message);
    }

    public HttpCmdException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpCmdException(Throwable cause) {
        super(cause);
    }
}
