package cn.uway.ucloude.uts.web.support;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.utils.NetUtils;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.biz.logger.SmartJobLogger;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.queue.JobQueueFactory;
import cn.uway.ucloude.uts.core.registry.RegistryStatMonitor;
import cn.uway.ucloude.uts.web.access.BackendAccessFactory;
import cn.uway.ucloude.uts.web.access.NodeMemCacheAccess;
import cn.uway.ucloude.uts.web.admin.support.NoRelyJobGenerator;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;
import cn.uway.ucloude.uts.web.cluster.BackendNode;
import cn.uway.ucloude.uts.web.cluster.BackendRegistrySrv;

public class BackendAppContextFactoryBean implements FactoryBean<BackendAppContext>, InitializingBean {

    private BackendAppContext appContext;

    @Override
    public BackendAppContext getObject() throws Exception {
        return appContext;
    }

    @Override
    public Class<?> getObjectType() {
        return BackendAppContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Node node = new BackendNode();
        node.setCreateTime(SystemClock.now());
        node.setIp(NetUtils.getLocalHost());
        node.setHostName(NetUtils.getLocalHostName());
        node.setIdentity(ExtConfigKeys.ADMIN_ID_PREFIX + StringUtil.generateUUID());

        UtsConfiguration config = new UtsConfiguration();
        config.setIdentity(node.getIdentity());
        config.setNodeType(node.getNodeType());
        config.setRegistryAddress(AppConfigurer.getProperty("registryAddress"));
        String clusterName = AppConfigurer.getProperty("clusterName");
        if (StringUtil.isEmpty(clusterName)) {
            throw new IllegalArgumentException("clusterName in uts-admin.cfg can not be null.");
        }
        config.setClusterName(clusterName);

        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 config. 开头的配置都加入到config中
            if (entry.getKey().startsWith("configs.")) {
                config.setParameter(entry.getKey().replaceFirst("configs.", ""), entry.getValue());
            }
        }

        appContext = new BackendAppContext();
        appContext.setConfiguration(config);
        appContext.setNode(node);
        appContext.setEventCenter(ServiceFactory.load(IEventCenter.class, config));
        appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));
        appContext.setBackendRegistrySrv(new BackendRegistrySrv(appContext));

        initAccess(config);

        // ----------------------下面是JobQueue的配置---------------------------
        UtsConfiguration jobTConfig = (UtsConfiguration) BeanUtils.cloneBean(config);
        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 jobT. 开头的配置都加入到jobTConfig中
            if (entry.getKey().startsWith("jobT.")) {
                String key = entry.getKey().replace("jobT.", "");
                String value = entry.getValue();
                jobTConfig.setParameter(key, value);
            }
        }
        initJobQueue(jobTConfig);

        appContext.getBackendRegistrySrv().start();
    }

    private void initJobQueue(UtsConfiguration config) {
        JobQueueFactory factory = ServiceFactory.load(JobQueueFactory.class, config);
        appContext.setExecutableJobQueue(factory.getExecutableJobQueue());
        appContext.setExecutingJobQueue(factory.getExecutingJobQueue());
        appContext.setCronJobQueue(factory.getCronJobQueue());
        appContext.setRepeatJobQueue(factory.getRepeatJobQueue());
        appContext.setSuspendJobQueue(factory.getSuspendJobQueue());
        appContext.setJobFeedbackQueue(factory.getJobFeedbackQueue());
        appContext.setNodeGroupStore(factory.getNodeGroupStore());
        appContext.setJobLogger(new SmartJobLogger(appContext));
        appContext.setNoRelyJobGenerator(new NoRelyJobGenerator(appContext));
    }

    private void initAccess(UtsConfiguration config) {
        BackendAccessFactory factory = ServiceFactory.load(BackendAccessFactory.class, config);
        appContext.setBackendJobClientMAccess(factory.getBackendJobClientMAccess());
        appContext.setBackendJobTrackerMAccess(factory.getJobTrackerMAccess());
        appContext.setBackendTaskTrackerMAccess(factory.getBackendTaskTrackerMAccess());
        appContext.setBackendJVMGCAccess(factory.getBackendJVMGCAccess());
        appContext.setBackendJVMMemoryAccess(factory.getBackendJVMMemoryAccess());
        appContext.setBackendJVMThreadAccess(factory.getBackendJVMThreadAccess());
        appContext.setBackendNodeOnOfflineLogAccess(factory.getBackendNodeOnOfflineLogAccess());
        appContext.setNodeMemCacheAccess(new NodeMemCacheAccess());
    }

}
