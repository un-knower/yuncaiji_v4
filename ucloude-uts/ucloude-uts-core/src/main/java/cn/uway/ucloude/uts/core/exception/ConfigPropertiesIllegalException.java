package cn.uway.ucloude.uts.core.exception;

public class ConfigPropertiesIllegalException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1193119987837541382L;

	public ConfigPropertiesIllegalException() {
        super();
    }

    public ConfigPropertiesIllegalException(String message) {
        super(message);
    }

    public ConfigPropertiesIllegalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigPropertiesIllegalException(Throwable cause) {
        super(cause);
    }
}
