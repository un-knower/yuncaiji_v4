package cn.uway.ucloude.uts.startup.jobtracker;



import java.util.Map;

import cn.uway.ucloude.data.dataaccess.DataSourceProvider;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.jobtracker.JobTracker;
import cn.uway.ucloude.uts.jobtracker.support.policy.OldDataDeletePolicy;

/**
 * @author magic.s.g.xie
 */
public class JobTrackerStartup {

    public static void main(String[] args) {
        try {


            String confPath = args[0];
        	DataSourceProvider.initialDataSource(confPath);
            JobTrackerCfg cfg = JobTrackerCfgLoader.load(confPath);

            final JobTracker jobTracker = new JobTracker();
            jobTracker.setRegistryAddress(cfg.getRegistryAddress());
            jobTracker.setListenPort(cfg.getListenPort());
            jobTracker.setClusterName(cfg.getClusterName());
            if (StringUtil.isNotEmpty(cfg.getBindIp())) {
                jobTracker.setBindIp(cfg.getBindIp());
            }

            jobTracker.setOldDataHandler(new OldDataDeletePolicy());

            for (Map.Entry<String, String> config : cfg.getConfigs().entrySet()) {
                jobTracker.addConfiguration(config.getKey(), config.getValue());
            }
            // 启动节点
            jobTracker.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    jobTracker.stop();
                }
            }));

        } catch (CfgException e) {
            System.err.println("JobTracker Startup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
