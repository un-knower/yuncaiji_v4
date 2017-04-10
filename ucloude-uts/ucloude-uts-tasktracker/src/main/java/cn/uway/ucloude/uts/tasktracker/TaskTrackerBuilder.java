package cn.uway.ucloude.uts.tasktracker;

import java.util.Map;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.AbstractNodeBuilder;
import cn.uway.ucloude.uts.core.properties.TaskTrackerProperties;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;

public class TaskTrackerBuilder extends AbstractNodeBuilder<TaskTracker, TaskTrackerBuilder> {

	@Override
	protected TaskTracker build0() {
		// TODO Auto-generated method stub
		TaskTrackerProperties properties = PropertiesConfigurationFactory.createPropertiesConfiguration(TaskTrackerProperties.class, locations);
        return buildByProperties(properties);
	}
	
	@SuppressWarnings("unchecked")
    public static TaskTracker buildByProperties(TaskTrackerProperties properties) {
        TaskTracker taskTracker = new TaskTracker();
        taskTracker.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtil.isNotEmpty(properties.getClusterName())) {
            taskTracker.setClusterName(properties.getClusterName());
        }
        if (StringUtil.isNotEmpty(properties.getIdentity())) {
            taskTracker.setIdentity(properties.getIdentity());
        }
        if (StringUtil.isNotEmpty(properties.getNodeGroup())) {
            taskTracker.setNodeGroup(properties.getNodeGroup());
        }
        if (StringUtil.isNotEmpty(properties.getDataPath())) {
            taskTracker.setDataPath(properties.getDataPath());
        }
        if (StringUtil.isNotEmpty(properties.getBindIp())) {
            taskTracker.setBindIp(properties.getBindIp());
        }
        if (CollectionUtil.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                taskTracker.addConfiguration(entry.getKey(), entry.getValue());
            }
        }
        if (properties.getBizLoggerLevel() != null) {
            taskTracker.setBizLoggerLevel(properties.getBizLoggerLevel());
        }
        if (properties.getWorkThreads() != 0) {
            taskTracker.setWorkThreads(properties.getWorkThreads());
        }
        if (properties.getJobRunnerClass() != null) {
            taskTracker.setJobRunnerClass((Class<? extends JobRunner>) properties.getJobRunnerClass());
        }

        return taskTracker;
    }

}
