package cn.uway.ucloude.uts.spring;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;
import cn.uway.ucloude.uts.spring.boot.properties.TaskTrackerProperties;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;
import cn.uway.ucloude.uts.tasktracker.TaskTrackerBuilder;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerFactory;

import java.util.Properties;

/**
 * TaskTracker Spring Bean 工厂类
 * 如果用这个工厂类，那么JobRunner中引用SpringBean的话,只有通过xml的方式注入
 *
 * @author magic.s.g.xie
 */
public abstract class TaskTrackerXmlFactoryBean implements FactoryBean<TaskTracker>,
        InitializingBean, DisposableBean {

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

    private String identity;

    private String bindIp;

    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 业务日志级别
     */
    private Level bizLoggerLevel;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
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
            properties.setBizLoggerLevel(bizLoggerLevel);
            properties.setWorkThreads(workThreads);

            properties.setConfigs(CollectionUtil.toMap(configs));
        } else {
            properties = PropertiesConfigurationFactory.createPropertiesConfiguration(TaskTrackerProperties.class, locations);
        }

        taskTracker = TaskTrackerBuilder.buildByProperties(properties);

        taskTracker.setRunnerFactory(new RunnerFactory() {
            @Override
            public JobRunner newRunner() {
                return createJobRunner();
            }
        });

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                taskTracker.addMasterChangeListener(masterChangeListener);
            }
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

    protected abstract JobRunner createJobRunner();

    @Override
    public void destroy() throws Exception {
        taskTracker.stop();
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

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setLocations(String... locations) {
        this.locations = locations;
    }
}
