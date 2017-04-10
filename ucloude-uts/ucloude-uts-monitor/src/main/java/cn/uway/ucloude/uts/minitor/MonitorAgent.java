package cn.uway.ucloude.uts.minitor;

import cn.uway.ucloude.cmd.HttpCmdServer;
import cn.uway.ucloude.compiler.AbstractCompiler;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonFactory;
import cn.uway.ucloude.thread.AliveKeeping;
import cn.uway.ucloude.utils.NetUtils;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.cluster.JobNodeConfigurationFactory;
import cn.uway.ucloude.uts.core.cluster.NodeFactory;
import cn.uway.ucloude.uts.core.cmd.JVMInfoGetHttpCmd;
import cn.uway.ucloude.uts.core.cmd.StatusCheckHttpCmd;
import cn.uway.ucloude.uts.core.registry.AbstractRegistry;
import cn.uway.ucloude.uts.core.registry.Registry;
import cn.uway.ucloude.uts.core.registry.RegistryFactory;
import cn.uway.ucloude.uts.core.registry.RegistryStatMonitor;
import cn.uway.ucloude.uts.jvmmonitor.JVMMonitor;
import cn.uway.ucloude.uts.minitor.access.MonitorAccessFactory;
import cn.uway.ucloude.uts.minitor.cmd.MDataAddHttpCmd;
import cn.uway.ucloude.uts.minitor.cmd.MDataSrv;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author magic.s.g.xie
 */
public class MonitorAgent {

    private static final ILogger LOGGER = LoggerManager.getLogger(MonitorAgent.class);
    private HttpCmdServer httpCmdServer;
    private MonitorAppContext appContext;
    private UtsConfiguration config;
    private Registry registry;
    private MonitorNode node;
    private AtomicBoolean start = new AtomicBoolean(false);

    public MonitorAgent() {
        this.appContext = new MonitorAppContext();
        this.node = NodeFactory.create(MonitorNode.class);
        this.config = JobNodeConfigurationFactory.getDefaultConfig();
        this.config.setNodeType(node.getNodeType());
        this.appContext.setConfiguration(config);
    }

    public void start() {

        if (!start.compareAndSet(false, true)) {
            return;
        }

        try {
            // 初始化
            intConfig();

            // 默认端口
            int port = config.getParameter(ExtConfigKeys.HTTP_CMD_PORT, 8730);
            this.httpCmdServer = HttpCmdServer.Factory.getHttpCmdServer(config.getIp(), port);

            this.httpCmdServer.registerCommands(
                    new MDataAddHttpCmd(this.appContext),
                    new StatusCheckHttpCmd(config),
                    new JVMInfoGetHttpCmd(config));
            // 启动
            this.httpCmdServer.start();

            // 设置真正启动的端口
            this.appContext.setHttpCmdPort(httpCmdServer.getPort());

            initNode();

            // 暴露在 zk 上
            initRegistry();
            registry.register(node);

            JVMMonitor.start();
            AliveKeeping.start();

            LOGGER.info("========== Start Monitor Success");

        } catch (Throwable t) {
            LOGGER.error("========== Start Monitor Error:", t);
        }
    }

    public void initRegistry() {
        registry = RegistryFactory.getRegistry(appContext);
        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(node);
        }
    }

    private void initNode() {
        config.setListenPort(this.appContext.getHttpCmdPort());
        NodeFactory.build(node, config);
        this.node.setHttpCmdPort(this.appContext.getHttpCmdPort());
    }

    private void intConfig() {

        String compiler = config.getParameter(ExtConfigKeys.COMPILER);
        if (StringUtil.isNotEmpty(compiler)) {
            AbstractCompiler.setCompiler(compiler);
        }
        // 设置json
        String ltsJson = config.getParameter(ExtConfigKeys.UTS_JSON);
        if (StringUtil.isNotEmpty(ltsJson)) {
            JsonFactory.setJSONAdapter(ltsJson);
        }

        if (StringUtil.isEmpty(config.getIp())) {
            config.setIp(NetUtils.getLocalHost());
        }
        JobNodeConfigurationFactory.buildIdentity(config);

        // 初始化一些 db access
        MonitorAccessFactory factory = ServiceFactory.load(MonitorAccessFactory.class, config);
        this.appContext.setJobTrackerMAccess(factory.getJobTrackerMAccess());
        this.appContext.setJvmGCAccess(factory.getJVMGCAccess());
        this.appContext.setJvmMemoryAccess(factory.getJVMMemoryAccess());
        this.appContext.setJvmThreadAccess(factory.getJVMThreadAccess());
        this.appContext.setTaskTrackerMAccess(factory.getTaskTrackerMAccess());
        this.appContext.setJobClientMAccess(factory.getJobClientMAccess());

        this.appContext.setMDataSrv(new MDataSrv(this.appContext));

        this.appContext.setEventCenter(ServiceFactory.load(IEventCenter.class, config));
        this.appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));
    }

    public void stop() {
        if (!start.compareAndSet(true, false)) {
            return;
        }

        try {
            if (registry != null) {
                // 先取消暴露
                this.registry.unregister(node);
            }
            if (httpCmdServer != null) {
                // 停止服务
                this.httpCmdServer.stop();
            }

            JVMMonitor.stop();
            AliveKeeping.stop();

            LOGGER.error("========== Stop Monitor Success");

        } catch (Throwable t) {
            LOGGER.error("========== Stop Monitor Error:", t);
        }
    }

    /**
     * 设置集群名字
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 设置zookeeper注册中心地址
     */
    public void setRegistryAddress(String registryAddress) {
        config.setRegistryAddress(registryAddress);
    }

    /**
     * 设置额外的配置参数
     */
    public void addConfig(String key, String value) {
        config.setParameter(key, value);
    }

    /**
     * 节点标识(必须要保证这个标识是唯一的才能设置，请谨慎设置)
     * 这个是非必须设置的，建议使用系统默认生成
     */
    public void setIdentity(String identity) {
        config.setIdentity(identity);
    }

    /**
     * 显示设置绑定ip
     */
    public void setBindIp(String bindIp) {
        if (StringUtil.isEmpty(bindIp)
                || !NetUtils.isValidHost(bindIp)
                ) {
            throw new IllegalArgumentException("Invalided bind ip:" + bindIp);
        }
        config.setIp(bindIp);
    }
}
