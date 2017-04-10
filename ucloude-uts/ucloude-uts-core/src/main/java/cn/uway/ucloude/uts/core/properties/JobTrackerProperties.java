package cn.uway.ucloude.uts.core.properties;

import cn.uway.ucloude.configuration.auto.annotation.ConfigurationProperties;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.uts.core.cluster.AbstractConfigProperties;
import cn.uway.ucloude.uts.core.exception.ConfigPropertiesIllegalException;

@ConfigurationProperties(prefix = "ucloude.uts.jobtracker")
public class JobTrackerProperties extends AbstractConfigProperties {

	/**
     * 监听端口
     */
    private Integer listenPort;

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

	@Override
	public void checkProperties() throws ConfigPropertiesIllegalException {
		// TODO Auto-generated method stub
		Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
	}

}
