package cn.uway.ucloude.log;

import cn.uway.ucloude.utils.SystemPropertyUtils;

public class DotLogUtils {
	private static final ILogger LOGGER = LoggerManager.getLogger(DotLogUtils.class);

    public static void dot(String msg, Object... args) {
        if (SystemPropertyUtils.isEnableDotLog()) {
            LOGGER.warn("[{}] " + msg, Thread.currentThread().getName(), args);
        }
    }
}
