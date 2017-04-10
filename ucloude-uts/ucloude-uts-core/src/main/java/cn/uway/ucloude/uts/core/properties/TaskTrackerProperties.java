package cn.uway.ucloude.uts.core.properties;

import cn.uway.ucloude.configuration.auto.annotation.ConfigurationProperties;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.uts.core.cluster.AbstractConfigProperties;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.exception.ConfigPropertiesIllegalException;

@ConfigurationProperties(prefix = "ucloude.uts.tasktracker")
public class TaskTrackerProperties extends AbstractConfigProperties {
	 /**
     * 节点Group
     */
    private String nodeGroup;
    /**
     * FailStore数据存储路径
     */
    private String dataPath;
    /**
     * 工作线程,默认64
     */
    private int workThreads;

    private Level bizLoggerLevel;

    private DispatchRunner dispatchRunner;

    private Class<?> jobRunnerClass;
    
    public String getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public int getWorkThreads() {
		return workThreads;
	}

	public void setWorkThreads(int workThreads) {
		this.workThreads = workThreads;
	}

	public Level getBizLoggerLevel() {
		return bizLoggerLevel;
	}

	public void setBizLoggerLevel(Level bizLoggerLevel) {
		this.bizLoggerLevel = bizLoggerLevel;
	}

	public DispatchRunner getDispatchRunner() {
		return dispatchRunner;
	}

	public void setDispatchRunner(DispatchRunner dispatchRunner) {
		this.dispatchRunner = dispatchRunner;
	}

	public Class<?> getJobRunnerClass() {
		return jobRunnerClass;
	}

	public void setJobRunnerClass(Class<?> jobRunnerClass) {
		this.jobRunnerClass = jobRunnerClass;
	}

	public static class DispatchRunner {
        /**
         * 是否使用shardRunner
         */
        private boolean enable = false;
        /**
         * shard的字段,默认taskId
         */
        private String shardValue;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getShardValue() {
            return shardValue;
        }

        public void setShardValue(String shardValue) {
            this.shardValue = shardValue;
        }
    }

	@Override
	public void checkProperties() throws ConfigPropertiesIllegalException {
		// TODO Auto-generated method stub
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getNodeGroup(), "nodeGroup must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
        Assert.isTrue(getWorkThreads() >= 0, "workThreads must >= 0.");
	}
}
