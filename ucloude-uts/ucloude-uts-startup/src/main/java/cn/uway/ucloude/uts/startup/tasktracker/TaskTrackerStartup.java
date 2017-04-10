package cn.uway.ucloude.uts.startup.tasktracker;

import cn.uway.ucloude.data.dataaccess.DataSourceProvider;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;

/**
 * @author magic.s.g.xie
 */
public class TaskTrackerStartup {

    public static void main(String[] args) {
        String cfgPath = args[0];
        start(cfgPath);
    }

    public static void start(String cfgPath) {
        try {
        	DataSourceProvider.initialDataSource(cfgPath);
            TaskTrackerCfg cfg = TaskTrackerCfgLoader.load(cfgPath);
            System.setProperty(ExtConfigKeys.CONF_TRACKER_PATH, cfgPath);
            final TaskTracker taskTracker;

            if (cfg.isUseSpring()) {
                taskTracker = SpringStartup.start(cfg);
            } else {
                taskTracker = DefaultStartup.start(cfg);
            }

            taskTracker.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    taskTracker.stop();
                }
            }));

        } catch (CfgException e) {
            System.err.println("TaskTracker Startup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
