package cn.uway.ucloude.uts.spring;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;
import cn.uway.ucloude.uts.core.properties.TaskTrackerProperties;
import cn.uway.ucloude.uts.spring.tasktracker.JobDispatcher;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;
import cn.uway.ucloude.uts.tasktracker.TaskTrackerBuilder;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * TaskTracker Spring Bean 工厂类
 * 如果用这个工厂类，那么JobRunner中引用SpringBean的话,只有通过注解的方式注入
 *
 * @author magic.s.g.xie
 */
@SuppressWarnings("rawtypes")
public class TaskTrackerAnnotationFactoryBean implements FactoryBean<TaskTracker>, ApplicationContextAware,
        InitializingBean, DisposableBean {

    private ApplicationContext applicationContext;
    private TaskTracker taskTracker;
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 节点组名称
     */
    private String nodeGroup;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String dataPath;
    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 任务执行类
     */
    private Class jobRunnerClass;
    /**
     * 业务日志级别
     */
    private Level bizLoggerLevel;
    /**
     * spring中jobRunner的bean name
     */
    private String jobRunnerBeanName;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;

    /**
     * 只有当使用 JobDispatcher 的时候才有效果
     */
    private String shardField;

    private String identity;

    private String bindIp;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();

    private String[] locations;

    @Override
    public TaskTracker getObject() throws Exception {
        return taskTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {

        TaskTrackerProperties properties = null;
        if (locations == null || locations.length == 0) {
            properties = new TaskTrackerProperties();
            properties.setClusterName(clusterName);
            properties.setDataPath(dataPath);
            properties.setNodeGroup(nodeGroup);
            properties.setRegistryAddress(registryAddress);
            properties.setBindIp(bindIp);
            properties.setIdentity(identity);
            properties.setWorkThreads(workThreads);
            properties.setConfigs(CollectionUtil.toMap(configs));
            properties.setBizLoggerLevel(bizLoggerLevel);
        } else {
            properties = PropertiesConfigurationFactory
                    .createPropertiesConfiguration(TaskTrackerProperties.class, locations);
        }

        taskTracker = TaskTrackerBuilder.buildByProperties(properties);

        registerRunnerBeanDefinition();

        // 设置config
        for (Map.Entry<Object, Object> entry : configs.entrySet()) {
            taskTracker.addConfiguration(entry.getKey().toString(), entry.getValue().toString());
        }

        taskTracker.setRunnerFactory(new RunnerFactory() {
            @Override
            public JobRunner newRunner() {
                return (JobRunner) applicationContext.getBean(jobRunnerBeanName);
            }
        });

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                taskTracker.addMasterChangeListener(masterChangeListener);
            }
        }
    }

    /**
     * 将 JobRunner 生成Bean放入spring容器中管理
     * 采用原型 scope， 所以可以在JobRunner中使用@Autowired
     */
    private void registerRunnerBeanDefinition() {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        jobRunnerBeanName = "UTS_".concat(jobRunnerClass.getSimpleName());
        if (!beanFactory.containsBean(jobRunnerBeanName)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(jobRunnerClass);
            if (jobRunnerClass == JobDispatcher.class) {
                builder.setScope(BeanDefinition.SCOPE_SINGLETON);
                builder.setLazyInit(false);
                builder.getBeanDefinition().getPropertyValues().addPropertyValue("shardField", shardField);
            } else {
                builder.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            }
            beanFactory.registerBeanDefinition(jobRunnerBeanName, builder.getBeanDefinition());
        }
    }

    /**
     * 可以自己得到TaskTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            taskTracker.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        taskTracker.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public void setJobRunnerClass(Class jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public void setMasterChangeListeners(MasterChangeListener... masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setBizLoggerLevel(String bizLoggerLevel) {
        if (StringUtil.isNotEmpty(bizLoggerLevel)) {
            this.bizLoggerLevel = Level.valueOf(bizLoggerLevel);
        }
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    public void setShardField(String shardField) {
        this.shardField = shardField;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public void setLocations(String... locations) {
        this.locations = locations;
    }
}
