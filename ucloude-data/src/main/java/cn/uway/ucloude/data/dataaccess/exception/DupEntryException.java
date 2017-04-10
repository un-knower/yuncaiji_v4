package cn.uway.ucloude.data.dataaccess.exception;

public class DupEntryException extends JdbcException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -616593575291833893L;

	public DupEntryException() {
        super();
    }

    public DupEntryException(String message) {
        super(message);
    }

    public DupEntryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DupEntryException(Throwable cause) {
        super(cause);
    }
}
