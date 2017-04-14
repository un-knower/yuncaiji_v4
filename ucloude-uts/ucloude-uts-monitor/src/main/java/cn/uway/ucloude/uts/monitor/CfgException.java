package cn.uway.ucloude.uts.monitor;

/**
 * @author magic.s.g.xie
 */
public class CfgException extends RuntimeException {

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
