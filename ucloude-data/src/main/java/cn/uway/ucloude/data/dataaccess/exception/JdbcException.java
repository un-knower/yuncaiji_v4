package cn.uway.ucloude.data.dataaccess.exception;

public class JdbcException extends RuntimeException {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -8935774127112495322L;

	public JdbcException() {
	        super();
    }

    public JdbcException(String message) {
        super(message);
    }

    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcException(Throwable cause) {
        super(cause);
    }
}
