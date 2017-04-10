package cn.uway.ucloude.uts.startup.tasktracker;


import java.util.Map;

import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;

/**
 * @author magic.s.g.xie
 */
public class DefaultStartup {

    @SuppressWarnings("unchecked")
	public static TaskTracker start(TaskTrackerCfg cfg) {

        final TaskTracker taskTracker = new TaskTracker();
        taskTracker.setJobRunnerClass(cfg.getJobRunnerClass());
        taskTracker.setRegistryAddress(cfg.getRegistryAddress());
        taskTracker.setNodeGroup(cfg.getNodeGroup());
        taskTracker.setClusterName(cfg.getClusterName());
        taskTracker.setWorkThreads(cfg.getWorkThreads());
        taskTracker.setDataPath(cfg.getDataPath());
        // 业务日志级别
        if (cfg.getBizLoggerLevel() == null) {
            taskTracker.setBizLoggerLevel(Level.INFO);
        } else {
            taskTracker.setBizLoggerLevel(cfg.getBizLoggerLevel());
        }

        for (Map.Entry<String, String> config : cfg.getConfigs().entrySet()) {
            taskTracker.addConfiguration(config.getKey(), config.getValue());
        }

        return taskTracker;
    }

}
