package cn.uway.ucloude.data.dataaccess.exception;

public class TableNotExistException extends JdbcException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5552310220041678239L;

	public TableNotExistException() {
        super();
    }

    public TableNotExistException(String message) {
        super(message);
    }

    public TableNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableNotExistException(Throwable cause) {
        super(cause);
    }
}
