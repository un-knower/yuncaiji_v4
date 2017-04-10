package cn.uway.ucloude.alarm;

public class AlarmNotifyException extends RuntimeException {
	public AlarmNotifyException() {
        super();
    }

    public AlarmNotifyException(String message) {
        super(message);
    }

    public AlarmNotifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlarmNotifyException(Throwable cause) {
        super(cause);
    }
}
