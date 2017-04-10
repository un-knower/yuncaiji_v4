package cn.uway.ucloude.uts.spring.quartz;

/**
 * @author magic.s.g.xie
 */
public class QuartzProxyException extends RuntimeException {

    public QuartzProxyException() {
        super();
    }

    public QuartzProxyException(String message) {
        super(message);
    }

    public QuartzProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuartzProxyException(Throwable cause) {
        super(cause);
    }
}
