package cn.uway.ucloude.uts.jobtracker;

import java.util.Map;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.AbstractNodeBuilder;
import cn.uway.ucloude.uts.core.properties.JobTrackerProperties;



public class JobTrackerBuilder extends AbstractNodeBuilder<JobTracker, JobTrackerBuilder>{

	@Override
	protected JobTracker build0() {
		// TODO Auto-generated method stub
		JobTrackerProperties properties = PropertiesConfigurationFactory.createPropertiesConfiguration(JobTrackerProperties.class, locations);
        return buildByProperties(properties);
	}
	
	 public static JobTracker buildByProperties(JobTrackerProperties properties) {

	        properties.checkProperties();

	        JobTracker jobTracker = new JobTracker();
	        jobTracker.setRegistryAddress(properties.getRegistryAddress());
	        if (StringUtil.isNotEmpty(properties.getClusterName())) {
	            jobTracker.setClusterName(properties.getClusterName());
	        }
	        if (properties.getListenPort() != null) {
	            jobTracker.setListenPort(properties.getListenPort());
	        }
	        if (StringUtil.isNotEmpty(properties.getIdentity())) {
	            jobTracker.setIdentity(properties.getIdentity());
	        }
	        if (StringUtil.isNotEmpty(properties.getBindIp())) {
	            jobTracker.setBindIp(properties.getBindIp());
	        }
	        if (CollectionUtil.isNotEmpty(properties.getConfigs())) {
	            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
	                jobTracker.addConfiguration(entry.getKey(), entry.getValue());
	            }
	        }
	        return jobTracker;
	    }

}
