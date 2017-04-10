package cn.uway.ucloude.log;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.log.log4j.Log4jLoggerAdapter;
import cn.uway.ucloude.log.support.FailsafeLogger;

public class LoggerManager {

	private static volatile LoggerAdapter LOGGER_ADAPTER;
	
	private static final ConcurrentMap<String, FailsafeLogger> LOGGERS = new ConcurrentHashMap<String, FailsafeLogger>();

	private LoggerManager() {

	}

	static {
		setLoggerAdapter(new Log4jLoggerAdapter());
	}

	public static void setLoggerAdapter(String loggerAdapter) {
		if (loggerAdapter != null && loggerAdapter.length() > 0) {
            setLoggerAdapter(ServiceFactory.load(LoggerAdapter.class, loggerAdapter));
        }
	}

	/**
	 * 设置日志输出器供给器
	 *
	 * @param loggerAdapter
	 *            日志输出器供给器
	 */
	public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
		if (loggerAdapter != null) {
            ILogger logger = loggerAdapter.getLogger(LoggerManager.class.getName());
            logger.info("using logger: " + loggerAdapter.getClass().getName());
            LoggerManager.LOGGER_ADAPTER = loggerAdapter;
            for (Map.Entry<String, FailsafeLogger> entry : LOGGERS.entrySet()) {
                entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
            }
        }
	}

	/**
     * 获取日志输出器
     *
     * @param key 分类键
     * @return 日志输出器, 后验条件: 不返回null.
     */
	public static ILogger getLogger(Class<?> key) {
		 FailsafeLogger logger = LOGGERS.get(key.getName());
	        if (logger == null) {
	            LOGGERS.putIfAbsent(key.getName(), new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
	            logger = LOGGERS.get(key.getName());
	        }
	        return logger;
	}

	/**
     * 获取日志输出器
     *
     * @param key 分类键
     * @return 日志输出器, 后验条件: 不返回null.
     */
	public static ILogger getLogger(String key) {
		FailsafeLogger logger = LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key);
        }
        return logger;
	}
	
	 /**
     * 动态设置输出日志级别
     *
     * @param level 日志级别
     */
    public static void setLevel(Level level) {
        LOGGER_ADAPTER.setLevel(level);
    }

    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    public static Level getLevel() {
        return LOGGER_ADAPTER.getLevel();
    }

    /**
     * 获取日志文件
     *
     * @return 日志文件
     */
    public static File getFile() {
        return LOGGER_ADAPTER.getFile();
    }

}
