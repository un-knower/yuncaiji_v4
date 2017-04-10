package cn.uway.ucloude.uts.spring.boot;

import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.minitor.MonitorAgent;
import cn.uway.ucloude.uts.spring.boot.annotation.EnableMonitor;
import cn.uway.ucloude.uts.spring.boot.properties.MonitorProperties;

/**
 * @author magic.s.g.xie
 */
@Configuration
@ConditionalOnBean(annotation = EnableMonitor.class)
@EnableConfigurationProperties(MonitorProperties.class)
public class MonitorAutoConfiguration implements InitializingBean, DisposableBean {

    @Autowired(required = false)
    private MonitorProperties properties;
    private MonitorAgent agent;

    @Override
    public void afterPropertiesSet() throws Exception {
        properties.checkProperties();

        agent = new MonitorAgent();

        agent.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtil.isNotEmpty(properties.getClusterName())) {
            agent.setClusterName(properties.getClusterName());
        }
        if (StringUtil.isNotEmpty(properties.getIdentity())) {
            agent.setIdentity(properties.getIdentity());
        }
        if (StringUtil.isNotEmpty(properties.getBindIp())) {
            agent.setBindIp(properties.getBindIp());
        }
        if (CollectionUtil.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                agent.addConfig(entry.getKey(), entry.getValue());
            }
        }

        agent.start();
    }

    @Override
    public void destroy() throws Exception {
        if (agent != null) {
            agent.stop();
        }
    }

}
