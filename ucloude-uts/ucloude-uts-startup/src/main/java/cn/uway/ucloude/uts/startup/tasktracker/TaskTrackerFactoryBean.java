package cn.uway.ucloude.uts.startup.tasktracker;



import java.util.Map;
import java.util.Properties;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.spring.TaskTrackerAnnotationFactoryBean;

/**
 * @author magic.s.g.xie
 */
public class TaskTrackerFactoryBean extends TaskTrackerAnnotationFactoryBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        String cfgPath = System.getProperty(ExtConfigKeys.CONF_TRACKER_PATH);

        TaskTrackerCfg cfg = TaskTrackerCfgLoader.load(cfgPath);

        setJobRunnerClass(cfg.getJobRunnerClass());
        setBizLoggerLevel(cfg.getBizLoggerLevel() == null ? null : cfg.getBizLoggerLevel().name());
        setClusterName(cfg.getClusterName());
        setRegistryAddress(cfg.getRegistryAddress());
        setNodeGroup(cfg.getNodeGroup());
        setWorkThreads(cfg.getWorkThreads());
        this.setShardField(cfg.getShardField());
        Map<String, String> configMap = cfg.getConfigs();
        Properties configs = new Properties();
        if(CollectionUtil.isNotEmpty(configMap)){
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                configs.put(entry.getKey(), entry.getValue());
            }
        }
        setConfigs(configs);

        super.afterPropertiesSet();
    }
}
