package cn.uway.ucloude.uts.core.properties;

import cn.uway.ucloude.configuration.auto.annotation.ConfigurationProperties;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.uts.core.cluster.AbstractConfigProperties;
import cn.uway.ucloude.uts.core.exception.ConfigPropertiesIllegalException;

@ConfigurationProperties(prefix = "ucloude.uts.jobclient")
public class JobClientProperties extends AbstractConfigProperties {

    private String nodeGroup;
    private boolean useRetryClient = true;
    private String dataPath;

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public boolean isUseRetryClient() {
        return useRetryClient;
    }

    public void setUseRetryClient(boolean useRetryClient) {
        this.useRetryClient = useRetryClient;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    public void checkProperties() throws ConfigPropertiesIllegalException {
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getNodeGroup(), "nodeGroup must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
    }
}