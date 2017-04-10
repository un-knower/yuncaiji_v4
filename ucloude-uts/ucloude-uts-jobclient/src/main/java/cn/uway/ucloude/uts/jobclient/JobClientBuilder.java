package cn.uway.ucloude.uts.jobclient;

import java.util.Map;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.AbstractNodeBuilder;
import cn.uway.ucloude.uts.core.properties.JobClientProperties;
import cn.uway.ucloude.uts.jobclient.support.JobCompletedHandler;

public class JobClientBuilder extends AbstractNodeBuilder<JobClient, JobClientBuilder> {

	private JobCompletedHandler jobCompletedHandler;
	


	public void setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
		this.jobCompletedHandler = jobCompletedHandler;
	}

	@Override
	protected JobClient build0() {
		// TODO Auto-generated method stub
		JobClientProperties properties = PropertiesConfigurationFactory
                .createPropertiesConfiguration(JobClientProperties.class, locations);

        JobClient jobClient = buildByProperties(properties);

        if (jobCompletedHandler != null) {
            jobClient.setJobCompletedHandler(jobCompletedHandler);
        }
        return jobClient;
	}
	
	public static JobClient buildByProperties(JobClientProperties properties){
		properties.checkProperties();
		JobClient jobClient;
        if (properties.isUseRetryClient()) {
            jobClient = new RetryJobClient();
        } else {
            jobClient = new JobClient();
        }
        jobClient.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtil.isNotEmpty(properties.getClusterName())) {
            jobClient.setClusterName(properties.getClusterName());
        }
        if (StringUtil.isNotEmpty(properties.getIdentity())) {
            jobClient.setIdentity(properties.getIdentity());
        }
        if (StringUtil.isNotEmpty(properties.getNodeGroup())) {
            jobClient.setNodeGroup(properties.getNodeGroup());
        }
        if (StringUtil.isNotEmpty(properties.getDataPath())) {
            jobClient.setDataPath(properties.getDataPath());
        }
        if (StringUtil.isNotEmpty(properties.getBindIp())) {
            jobClient.setBindIp(properties.getBindIp());
        }
        if (CollectionUtil.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                jobClient.addConfiguration(entry.getKey(), entry.getValue());
            }
        }
        return jobClient;
	}

}
