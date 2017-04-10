package cn.uway.ucloude.uts.tasktracker.logger;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.domain.Level;

/**
 * @author uway
 */
public class MockBizLogger extends BizLoggerAdapter implements BizLogger {

    private static final ILogger LOGGER = LoggerManager.getLogger(MockBizLogger.class);
    private Level level;

    public MockBizLogger(Level level) {
        this.level = level;
        if (level == null) {
            this.level = Level.INFO;
        }
    }

    @Override
    public void debug(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            LOGGER.debug(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            LOGGER.info(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            LOGGER.error(msg);
        }
    }
}
