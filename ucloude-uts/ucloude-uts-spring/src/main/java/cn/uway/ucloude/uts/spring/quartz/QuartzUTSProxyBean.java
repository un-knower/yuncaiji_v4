package cn.uway.ucloude.uts.spring.quartz;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.properties.JobClientProperties;
import cn.uway.ucloude.uts.core.properties.TaskTrackerProperties;



/**
 * @author magic.s.g.xie
 */
public class QuartzUTSProxyBean implements BeanFactoryPostProcessor {

    private static final ILogger LOGGER = LoggerManager.getLogger(QuartzUTSProxyBean.class);
    // 是否使用UTS
    private boolean ltsEnable = true;

    private String[] locations;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (ltsEnable) {
            // 如果启用才进行代理
            LOGGER.info("========UTS====== Proxy Quartz Scheduler");

            JobClientProperties jobClientProperties = PropertiesConfigurationFactory.createPropertiesConfiguration(JobClientProperties.class, locations);
            jobClientProperties.checkProperties();

            TaskTrackerProperties taskTrackerProperties = PropertiesConfigurationFactory.createPropertiesConfiguration(TaskTrackerProperties.class, locations);
            taskTrackerProperties.checkProperties();

            QuartzUTSConfig quartzUTSConfig = new QuartzUTSConfig();
            quartzUTSConfig.setJobClientProperties(jobClientProperties);
            quartzUTSConfig.setTaskTrackerProperties(taskTrackerProperties);

            QuartzUTSConfig.JobProperties jobProperties = PropertiesConfigurationFactory.createPropertiesConfiguration(QuartzUTSConfig.JobProperties.class, locations);
            quartzUTSConfig.setJobProperties(jobProperties);

            QuartzUTSProxyAgent agent = new QuartzUTSProxyAgent(quartzUTSConfig);
            QuartzProxyContext context = new QuartzProxyContext(quartzUTSConfig, agent);

            QuartzSchedulerBeanRegistrar registrar = new QuartzSchedulerBeanRegistrar(context);
            beanFactory.addPropertyEditorRegistrar(registrar);
        }
    }

    public void setLtsEnable(boolean ltsEnable) {
        this.ltsEnable = ltsEnable;
    }

    public void setLocations(String... locations) {
        this.locations = locations;
    }
}
