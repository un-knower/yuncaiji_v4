package cn.uway.ucloude.utils;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class PeriodUtils {
	private static final ILogger LOGGER = LoggerManager.getLogger(PeriodUtils.class);
    private static final ThreadLocal<Period> TL = new ThreadLocal<Period>();

    public static void start() {
        if (!SystemPropertyUtils.isEnablePeriod()) {
            return;
        }
        Period period = new Period();
        period.start = SystemClock.now();
        TL.set(period);
    }

    public static void end(String msg, Object... args) {
        if (!SystemPropertyUtils.isEnablePeriod()) {
            return;
        }
        Period period = TL.get();
        if (period == null) {
            throw new IllegalStateException("please start first");
        }
        long mills = SystemClock.now() - period.start;
        TL.remove();
        LOGGER.warn("[Period]" + msg + ", mills:{}", args, mills);
    }

    private static class Period {
        private long start;
    }
}
