package cn.uway.ucloude.uts.startup.jobclient;

import java.util.Map;
import java.util.Properties;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.spring.JobClientFactoryBean;


public class JobClientXmlFactoryBean extends JobClientFactoryBean{

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		String cfgPath = System.getProperty(ExtConfigKeys.CONF_JOBCLEINT_PATH);

        JobClientCfg cfg = JobClientCfgLoader.load(cfgPath);
        
        this.setNodeGroup(cfg.getNodeGroup());
        this.setClusterName(cfg.getClusterName());
        this.setRegistryAddress(cfg.getRegistryAddress());
        this.setUseRetryClient(cfg.isUseRetryClient());
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
