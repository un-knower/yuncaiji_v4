package cn.uway.ucloude.uts.core.failstore;

import cn.uway.ucloude.uts.core.UtsContext;

public class FailStorePathBuilder {
	public static String getBizLoggerPath(UtsContext appContext) {
        return getStorePath(appContext) + "/bizlog_failstore/";
    }

    public static String getJobFeedbackPath(UtsContext appContext) {
        return getStorePath(appContext) + "/job_feedback_failstore/";
    }

    public static String getJobSubmitFailStorePath(UtsContext appContext) {
        return getStorePath(appContext) + "/job_submit_failstore/";
    }

    public static String getDepJobSubmitFailStorePath(UtsContext appContext) {
        return getStorePath(appContext) + "/dep_job_submit_failstore/";
    }

    private static String getStorePath(UtsContext appContext) {
        return appContext.getConfiguration().getDataPath()
                + "/.ucloude.uts" + "/" +
                appContext.getConfiguration().getNodeType() + "/" +
                appContext.getConfiguration().getNodeGroup();
    }
}
