package cn.uway.ucloude.uts.startup.tasktracker;

/**
 * @author magic.s.g.xie
 */
public class CfgException extends Exception {

	private static final long serialVersionUID = -661377294271386745L;

	public CfgException() {
        super();
    }

    public CfgException(String message) {
        super(message);
    }

    public CfgException(String message, Throwable cause) {
        super(message, cause);
    }

    public CfgException(Throwable cause) {
        super(cause);
    }
}
