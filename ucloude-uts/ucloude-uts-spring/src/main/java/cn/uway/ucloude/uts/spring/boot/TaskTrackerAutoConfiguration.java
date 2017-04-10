package cn.uway.ucloude.uts.spring.boot;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ConfigurableApplicationContext;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.cluster.AbstractJobNode;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.spring.boot.annotation.EnableTaskTracker;
import cn.uway.ucloude.uts.spring.boot.annotation.JobRunner4TaskTracker;
import cn.uway.ucloude.uts.spring.boot.properties.TaskTrackerProperties;
import cn.uway.ucloude.uts.spring.tasktracker.JobDispatcher;
import cn.uway.ucloude.uts.spring.tasktracker.JobRunnerHolder;
import cn.uway.ucloude.uts.spring.tasktracker.UTS;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;
import cn.uway.ucloude.uts.tasktracker.TaskTrackerBuilder;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerFactory;

/**
 * @author magic.s.g.xie
 */
@Configuration
@ConditionalOnBean(annotation = EnableTaskTracker.class)
@EnableConfigurationProperties(TaskTrackerProperties.class)
public class TaskTrackerAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private TaskTrackerProperties properties;
    private TaskTracker taskTracker;

    @Override
    protected void initJobNode() {

        taskTracker = TaskTrackerBuilder.buildByProperties(properties);

        if (!isEnableDispatchRunner()) {

            Map<String, Object> jobRunners = applicationContext.getBeansWithAnnotation(JobRunner4TaskTracker.class);
            if (CollectionUtil.isNotEmpty(jobRunners)) {
                if (jobRunners.size() > 1) {
                    throw new IllegalArgumentException("annotation @" + JobRunner4TaskTracker.class.getSimpleName() + " only should have one");
                }
                for (final Map.Entry<String, Object> entry : jobRunners.entrySet()) {
                    Object handler = entry.getValue();
                    if (handler instanceof JobRunner) {
                        taskTracker.setRunnerFactory(new RunnerFactory() {
                            @Override
                            public JobRunner newRunner() {
                                return (JobRunner) entry.getValue();
                            }
                        });
                    } else {
                        throw new IllegalArgumentException(entry.getKey() + "  is not instance of " + JobRunner.class.getName());
                    }
                }
            }
        } else {

            Map<String, Object> ltsBeanMap = applicationContext.getBeansWithAnnotation(UTS.class);
            if (CollectionUtil.isNotEmpty(ltsBeanMap)) {
                for (Map.Entry<String, Object> entry : ltsBeanMap.entrySet()) {
                    Object bean = entry.getValue();
                    JobRunnerHolder.addUTSBean(bean);
                }
            }
            registerRunnerBeanDefinition();
            taskTracker.setRunnerFactory(new RunnerFactory() {
                @Override
                public JobRunner newRunner() {
                    return (JobRunner) applicationContext.getBean(JOB_RUNNER_BEAN_NAME);
                }
            });
        }

    }

    String JOB_RUNNER_BEAN_NAME = "UTS_".concat(JobDispatcher.class.getSimpleName());

    private void registerRunnerBeanDefinition() {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        if (!beanFactory.containsBean(JOB_RUNNER_BEAN_NAME)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(JobDispatcher.class);
            builder.setScope(BeanDefinition.SCOPE_SINGLETON);
            builder.setLazyInit(false);
            builder.getBeanDefinition().getPropertyValues().addPropertyValue("shardField", properties.getDispatchRunner().getShardValue());
            beanFactory.registerBeanDefinition(JOB_RUNNER_BEAN_NAME, builder.getBeanDefinition());
        }
    }

    private boolean isEnableDispatchRunner() {
        return properties.getDispatchRunner() != null && properties.getDispatchRunner().isEnable();
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.TASK_TRACKER;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return taskTracker;
    }
}
