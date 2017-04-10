package cn.uway.ucloude.configuration.auto;

public class PropertiesConfigurationResolveException extends RuntimeException {
	public PropertiesConfigurationResolveException() {
        super();
    }

    public PropertiesConfigurationResolveException(String message) {
        super(message);
    }

    public PropertiesConfigurationResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertiesConfigurationResolveException(Throwable cause) {
        super(cause);
    }
}
