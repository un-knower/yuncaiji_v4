package cn.uway.ucloude.uts.spring.boot;




import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.cluster.AbstractJobNode;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.properties.JobClientProperties;
import cn.uway.ucloude.uts.jobclient.JobClient;
import cn.uway.ucloude.uts.jobclient.JobClientBuilder;
import cn.uway.ucloude.uts.jobclient.support.JobCompletedHandler;
import cn.uway.ucloude.uts.spring.boot.annotation.EnableJobClient;
import cn.uway.ucloude.uts.spring.boot.annotation.JobCompletedHandler4JobClient;

/**
 * @author magic.s.g.xie
 */
@Configuration
@ConditionalOnBean(annotation = EnableJobClient.class)
@EnableConfigurationProperties(JobClientProperties.class)
public class JobClientAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private JobClientProperties properties;
    private JobClient jobClient;

    @Bean
    public JobClient jobClient() {
        return jobClient;
    }

    @Override
    protected void initJobNode() {
        jobClient = JobClientBuilder.buildByProperties(properties);

        Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(JobCompletedHandler4JobClient.class);
        if (CollectionUtil.isNotEmpty(handlers)) {
            if (handlers.size() > 1) {
                throw new IllegalArgumentException("annotation @" + JobCompletedHandler4JobClient.class.getSimpleName() + " only should have one");
            }
            for (Map.Entry<String, Object> entry : handlers.entrySet()) {
                Object handler = entry.getValue();
                if (handler instanceof JobCompletedHandler) {
                    jobClient.setJobCompletedHandler((JobCompletedHandler) entry.getValue());
                } else {
                    throw new IllegalArgumentException(entry.getKey() + "  is not instance of " + JobCompletedHandler.class.getName());
                }
            }
        }
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.JOB_CLIENT;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return jobClient;
    }
}
