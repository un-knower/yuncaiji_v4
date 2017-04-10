package cn.uway.ucloude.uts.spring.boot.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.uts.core.cluster.AbstractConfigProperties;
import cn.uway.ucloude.uts.core.exception.ConfigPropertiesIllegalException;

/**
 * @author magic.s.g.xie
 */
@ConfigurationProperties(prefix = "ucloude.uts.monitor")
public class MonitorProperties extends AbstractConfigProperties {

    @Override
    public void checkProperties() throws ConfigPropertiesIllegalException {
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
    }
}
