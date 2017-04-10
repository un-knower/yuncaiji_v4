package cn.uway.ucloude.uts.spring.tasktracker;

/**
 * @author magic.s.g.xie
 */
public class JobDispatchException extends Exception{

	private static final long serialVersionUID = -99670791735250890L;

	public JobDispatchException() {
        super();
    }

    public JobDispatchException(String message) {
        super(message);
    }

    public JobDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobDispatchException(Throwable cause) {
        super(cause);
    }
}
