package cn.uway.ucloude.utils;

public class SystemPropertyUtils {
	private static boolean enableDotLog;
    private static boolean enablePeriod = false;

    static {
        enableDotLog = "true".equals(System.getProperty("enableDotLog"));
        enablePeriod = "true".equals(System.getProperty("enablePeriod"));
    }

    public static boolean isEnableDotLog() {
        return enableDotLog;
    }

    public static boolean isEnablePeriod() {
        return enablePeriod;
    }
}
