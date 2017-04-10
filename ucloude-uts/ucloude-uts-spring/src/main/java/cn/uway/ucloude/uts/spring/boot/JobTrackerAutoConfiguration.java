package cn.uway.ucloude.uts.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import cn.uway.ucloude.uts.core.cluster.AbstractJobNode;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.jobtracker.JobTracker;
import cn.uway.ucloude.uts.jobtracker.JobTrackerBuilder;
import cn.uway.ucloude.uts.spring.boot.annotation.EnableJobTracker;
import cn.uway.ucloude.uts.spring.boot.properties.JobTrackerProperties;

/**
 * @author magic.s.g.xie
 */
@Configuration
@ConditionalOnBean(annotation = EnableJobTracker.class)
@EnableConfigurationProperties(JobTrackerProperties.class)
public class JobTrackerAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private JobTrackerProperties properties;
    private JobTracker jobTracker;

    @Override
    protected void initJobNode() {
        jobTracker = JobTrackerBuilder.buildByProperties(properties);
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.JOB_TRACKER;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return jobTracker;
    }
}
