package cn.uway.ucloude;

import cn.uway.ucloude.common.Environment;

public class Globle {
	private static Environment environment = Environment.ONLINE;

    public static Environment getEnvironment() {
        if (environment == null) {
            return Environment.ONLINE;
        }
        return environment;
    }

    public static void setEnvironment(Environment environment) {
    	Globle.environment = environment;
    }
}
