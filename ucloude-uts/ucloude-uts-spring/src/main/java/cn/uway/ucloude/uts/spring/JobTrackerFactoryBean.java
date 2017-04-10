package cn.uway.ucloude.uts.spring;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import cn.uway.ucloude.configuration.auto.PropertiesConfigurationFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;
import cn.uway.ucloude.uts.core.properties.JobTrackerProperties;
import cn.uway.ucloude.uts.jobtracker.JobTracker;
import cn.uway.ucloude.uts.jobtracker.JobTrackerBuilder;
import cn.uway.ucloude.uts.jobtracker.support.OldDataHandler;

import java.util.Properties;

/**
 * JobTracker Spring Bean 工厂类
 *
 * @author magic.s.g.xie
 */
public class JobTrackerFactoryBean implements FactoryBean<JobTracker>,
        InitializingBean, DisposableBean {

    private JobTracker jobTracker;
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();
    /**
     * 监听端口
     */
    private Integer listenPort;

    private String identity;

    private String bindIp;
    /**
     * 老数据处理接口
     */
    private OldDataHandler oldDataHandler;

    private String[] locations;

    @Override
    public JobTracker getObject() throws Exception {
        return jobTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return JobTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        JobTrackerProperties properties = null;
        if (locations == null || locations.length == 0) {
            properties = new JobTrackerProperties();
            properties.setListenPort(listenPort);
            properties.setClusterName(clusterName);
            properties.setRegistryAddress(registryAddress);
            properties.setBindIp(bindIp);
            properties.setIdentity(identity);
            properties.setConfigs(CollectionUtil.toMap(configs));
        } else {
            properties = PropertiesConfigurationFactory.createPropertiesConfiguration(JobTrackerProperties.class, locations);
        }

        jobTracker = JobTrackerBuilder.buildByProperties(properties);

        if (oldDataHandler != null) {
            jobTracker.setOldDataHandler(oldDataHandler);
        }

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                jobTracker.addMasterChangeListener(masterChangeListener);
            }
        }
    }

    /**
     * 可以自己得到JobTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            jobTracker.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        jobTracker.stop();
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setMasterChangeListeners(MasterChangeListener... masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        this.oldDataHandler = oldDataHandler;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
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