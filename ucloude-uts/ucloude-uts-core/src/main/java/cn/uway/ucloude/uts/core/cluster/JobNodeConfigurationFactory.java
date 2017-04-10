package cn.uway.ucloude.uts.core.cluster;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.uts.core.UtsConfiguration;

public class JobNodeConfigurationFactory {
	
	private static ILogger logger = LoggerManager.getLogger(JobNodeConfigurationFactory.class);
	private static final AtomicInteger SEQ = new AtomicInteger(0);

    public static UtsConfiguration getDefaultConfig() {
    	UtsConfiguration config = new UtsConfiguration();
        config.setWorkThreads(64);
        config.setNodeGroup("uts");
        config.setRegistryAddress("zookeeper://127.0.0.1:2181");
        config.setInvokeTimeoutMillis(1000 * 60);
        config.setDataPath(UCloudeConstants.USER_HOME);
        config.setClusterName(UCloudeConstants.DEFAULT_CLUSTER_NAME);
        return config;
    }

    public static void buildIdentity(UtsConfiguration config) {
    	String date = "";
    	try {
			date = DateUtil.format(new Date(), DateUtil.TimePattern.yyyyMMddHHmmssSSS);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error("时间转化问题",e);
		}
        String sb = getNodeTypeShort(config.getNodeType()) +
                "_" +
                config.getIp() +
                "_" +
                getPid() +
                "_" +
                date
                + "_" + SEQ.incrementAndGet();
        config.setIdentity(sb);
    }

    private static String getNodeTypeShort(NodeType nodeType) {
        switch (nodeType) {
            case JOB_CLIENT:
                return "JC";
            case JOB_TRACKER:
                return "JT";
            case TASK_TRACKER:
                return "TT";
            case MONITOR:
                return "MO";
            case BACKEND:
                return "BA";
        }
        throw new IllegalArgumentException();
    }

    private static Integer getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        int index = name.indexOf("@");
        if (index != -1) {
            return Integer.parseInt(name.substring(0, index));
        }
        return 0;
    }
}
