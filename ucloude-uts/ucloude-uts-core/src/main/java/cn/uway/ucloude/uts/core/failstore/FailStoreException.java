package cn.uway.ucloude.uts.core.failstore;

public class FailStoreException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -885387655224791538L;

	public FailStoreException(String message) {
        super(message);
    }

    public FailStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailStoreException(Throwable cause) {
        super(cause);
    }
}
