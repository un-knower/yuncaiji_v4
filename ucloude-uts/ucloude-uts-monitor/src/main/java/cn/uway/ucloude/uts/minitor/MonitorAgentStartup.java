package cn.uway.ucloude.uts.minitor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.utils.StringUtil;

/**
 * @author uway
 */
public class MonitorAgentStartup {

    private final static MonitorAgent agent = new MonitorAgent();
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void main(String[] args) {
        String cfgPath = args[0];
        start(cfgPath);
    }

    public static void start(String cfgPath) {

        if (!started.compareAndSet(false, true)) {
            return;
        }

        try {
            MonitorCfg cfg = MonitorCfgLoader.load(cfgPath);

            agent.setRegistryAddress(cfg.getRegistryAddress());
            agent.setClusterName(cfg.getClusterName());
            if (StringUtil.isNotEmpty(cfg.getBindIp())) {
                agent.setBindIp(cfg.getBindIp());
            }
            if (StringUtil.isNotEmpty(cfg.getIdentity())) {
                agent.setIdentity(cfg.getIdentity());
            }
            for (Map.Entry<String, String> config : cfg.getConfigs().entrySet()) {
                agent.addConfig(config.getKey(), config.getValue());
            }

            agent.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    agent.stop();
                }
            }));

        } catch (CfgException e) {
            System.err.println("Monitor Startup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (started.compareAndSet(true, false)) {
            agent.stop();
        }
    }

}
