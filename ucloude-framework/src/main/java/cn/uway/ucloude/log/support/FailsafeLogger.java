package cn.uway.ucloude.log.support;

import cn.uway.ucloude.common.Version;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.utils.NetUtils;

public class FailsafeLogger extends AbstractLogger implements ILogger {

    private ILogger logger;

    public FailsafeLogger(ILogger logger) {
        this.logger = logger;
    }

    public ILogger getLogger() {
        return logger;
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    private String appendContextMessage(String msg) {
        return " [UCloude] " + msg + ", UCloude version: " + Version.getVersion() + ", current host: " + NetUtils.getLocalHost();
    }

    public void trace(String msg, Throwable e) {
        try {
            logger.trace(appendContextMessage(msg), e);
        } catch (Throwable ignored) {
        }
    }

    public void trace(Throwable e) {
        try {
            logger.trace(e);
        } catch (Throwable ignored) {
        }
    }

    public void trace(String msg) {
        try {
            logger.trace(appendContextMessage(msg));
        } catch (Throwable ignored) {
        }
    }

    public void debug(String msg, Throwable e) {
        try {
            logger.debug(appendContextMessage(msg), e);
        } catch (Throwable ignored) {
        }
    }

    public void debug(Throwable e) {
        try {
            logger.debug(e);
        } catch (Throwable ignored) {
        }
    }

    public void debug(String msg) {
        try {
            logger.debug(appendContextMessage(msg));
        } catch (Throwable ignored) {
        }
    }

    public void info(String msg, Throwable e) {
        try {
            logger.info(appendContextMessage(msg), e);
        } catch (Throwable ignored) {
        }
    }

    public void info(String msg) {
        try {
            logger.info(appendContextMessage(msg));
        } catch (Throwable ignored) {
        }
    }

    public void warn(String msg, Throwable e) {
        try {
            logger.warn(appendContextMessage(msg), e);
        } catch (Throwable ignored) {
        }
    }

    public void warn(String msg) {
        try {
            logger.warn(appendContextMessage(msg));
        } catch (Throwable ignored) {
        }
    }

    public void error(String msg, Throwable e) {
        try {
            logger.error(appendContextMessage(msg), e);
        } catch (Throwable ignored) {
        }
    }

    public void error(String msg) {
        try {
            logger.error(appendContextMessage(msg));
        } catch (Throwable ignored) {
        }
    }

    public void error(Throwable e) {
        try {
            logger.error(e);
        } catch (Throwable ignored) {
        }
    }

    public void info(Throwable e) {
        try {
            logger.info(e);
        } catch (Throwable ignored) {
        }
    }

    public void warn(Throwable e) {
        try {
            logger.warn(e);
        } catch (Throwable ignored) {
        }
    }

    public boolean isTraceEnabled() {
        try {
            return logger.isTraceEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isDebugEnabled() {
        try {
            return logger.isDebugEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isInfoEnabled() {
        try {
            return logger.isInfoEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isWarnEnabled() {
        try {
            return logger.isWarnEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isErrorEnabled() {
        try {
            return logger.isErrorEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

}