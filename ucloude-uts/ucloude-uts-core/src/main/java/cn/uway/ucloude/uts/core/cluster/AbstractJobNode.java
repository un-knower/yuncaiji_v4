package cn.uway.ucloude.uts.core.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.cmd.HttpCmdServer;
import cn.uway.ucloude.compiler.AbstractCompiler;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.RpcConfigKeys;
import cn.uway.ucloude.rpc.serialize.AdaptiveSerializable;
import cn.uway.ucloude.serialize.JsonFactory;
import cn.uway.ucloude.thread.AliveKeeping;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.GenericsUtils;
import cn.uway.ucloude.utils.NetUtils;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cmd.JVMInfoGetHttpCmd;
import cn.uway.ucloude.uts.core.cmd.StatusCheckHttpCmd;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;
import cn.uway.ucloude.uts.core.listener.MasterElectionListener;
import cn.uway.ucloude.uts.core.listener.NodeChangeListener;
import cn.uway.ucloude.uts.core.listener.SelfChangeListener;
import cn.uway.ucloude.uts.core.protocol.command.CommandBodyWrapper;
import cn.uway.ucloude.uts.core.registry.AbstractRegistry;
import cn.uway.ucloude.uts.core.registry.NotifyEvent;
import cn.uway.ucloude.uts.core.registry.NotifyListener;
import cn.uway.ucloude.uts.core.registry.Registry;
import cn.uway.ucloude.uts.core.registry.RegistryFactory;
import cn.uway.ucloude.uts.core.registry.RegistryStatMonitor;
import cn.uway.ucloude.uts.core.support.ConfigValidator;

public abstract class AbstractJobNode<T extends Node, Context extends UtsContext> implements JobNode {

	protected static final ILogger LOGGER = LoggerManager.getLogger(JobNode.class);

	protected Registry registry;

	protected T node;

	protected UtsConfiguration configuration;

	protected Context context;

	private List<NodeChangeListener> nodeChangeListeners;

	private List<MasterChangeListener> masterChangeListeners;

	protected AtomicBoolean started = new AtomicBoolean(false);

	public AbstractJobNode() {
		context = getContext();
		node = NodeFactory.create(getNodeClass());
		configuration = JobNodeConfigurationFactory.getDefaultConfig();
		configuration.setNodeType(node.getNodeType());
		context.setConfiguration(configuration);
		nodeChangeListeners = new ArrayList<NodeChangeListener>();
		masterChangeListeners = new ArrayList<MasterChangeListener>();
	}

	@Override
	final public void start() {
		// TODO Auto-generated method stub
		if (started.compareAndSet(false, true)) {
			configValidate();
			// 初始化配置
			initConfiguration();

			// 初始化HttpCmdServer
			initHttpCmdServer();
			beforeRpcStart();

			rpcStart();

			afterRpcStart();

			initRegistry();

			registry.register(node);

			AliveKeeping.start();

			LOGGER.info("========== Start success, nodeType={}, identity={}", configuration.getNodeType(),
					configuration.getIdentity());
		}
	}

	private void initHttpCmdServer() {
		// 命令中心
		int port = context.getConfiguration().getParameter(ExtConfigKeys.HTTP_CMD_PORT, 8719);
		context.setHttpCmdServer(HttpCmdServer.Factory.getHttpCmdServer(configuration.getIp(), port));

		// 先启动，中间看端口是否被占用
		context.getHttpCmdServer().start();
		// 设置command端口，会暴露到注册中心上
		node.setHttpCmdPort(context.getHttpCmdServer().getPort());

		context.getHttpCmdServer().registerCommands(new StatusCheckHttpCmd(context.getConfiguration()),
				new JVMInfoGetHttpCmd(context.getConfiguration()));
	}

	protected abstract void rpcStart();

	protected abstract void rpcStop();

	protected abstract void beforeRpcStart();

	protected abstract void afterRpcStart();

	protected abstract void beforeRpcStop();

	protected abstract void afterRpcStop();

	/**
	 * 
	 */
	protected void configValidate() {
		ConfigValidator.validateNodeGroup(configuration.getNodeGroup());
		ConfigValidator.validateClusterName(configuration.getClusterName());
		ConfigValidator.validateIdentity(configuration.getIdentity());
	}

	protected void initConfiguration() {
		String compiler = configuration.getParameter(ExtConfigKeys.COMPILER);
		if (StringUtil.isNotEmpty(compiler)) {
			AbstractCompiler.setCompiler(compiler);
		}

		if (StringUtil.isEmpty(configuration.getIp())) {
			configuration.setIp(NetUtils.getLocalHost());
		}
		if (StringUtil.isEmpty(configuration.getIdentity())) {
			JobNodeConfigurationFactory.buildIdentity(configuration);
		}
		NodeFactory.build(node, configuration);

		LOGGER.info("Current Node config :{}", configuration);

		context.setEventCenter(ServiceFactory.load(IEventCenter.class, configuration));

		context.setCommandBodyWrapper(new CommandBodyWrapper(configuration));
		context.setMasterElector(new MasterElector(context));
		context.getMasterElector().addMasterChangeListener(masterChangeListeners);
		context.setRegistryStatMonitor(new RegistryStatMonitor(context));

		// 订阅的node管理
		SubscribedNodeManager subscribedNodeManager = new SubscribedNodeManager(context);
		context.setSubscribedNodeManager(subscribedNodeManager);
		nodeChangeListeners.add(subscribedNodeManager);
		// 用于master选举的监听器
		nodeChangeListeners.add(new MasterElectionListener(context));
		// 监听自己节点变化（如，当前节点被禁用了）
		nodeChangeListeners.add(new SelfChangeListener(context));

		setSpiConfig();
	}

	private void initRegistry() {
		registry = RegistryFactory.getRegistry(context);
		if (registry instanceof AbstractRegistry) {
			((AbstractRegistry) registry).setNode(node);
		}
		registry.subscribe(node, new NotifyListener() {
			private final ILogger NOTIFY_LOGGER = LoggerManager.getLogger(NotifyListener.class);

			@Override
			public void notify(NotifyEvent event, List<Node> nodes) {
				if (CollectionUtil.isEmpty(nodes)) {
					return;
				}
				switch (event) {
				case ADD:
					for (NodeChangeListener listener : nodeChangeListeners) {
						try {
							listener.addNodes(nodes);
						} catch (Throwable t) {
							NOTIFY_LOGGER.error("{} add nodes failed , cause: {}", listener.getClass().getName(),
									t.getMessage(), t);
						}
					}
					break;
				case REMOVE:
					for (NodeChangeListener listener : nodeChangeListeners) {
						try {
							listener.removeNodes(nodes);
						} catch (Throwable t) {
							NOTIFY_LOGGER.error("{} remove nodes failed , cause: {}", listener.getClass().getName(),
									t.getMessage(), t);
						}
					}
					break;
				}
			}
		});
	}

	private void setSpiConfig() {
		// 设置默认序列化方式
		String defaultSerializable = configuration.getParameter(RpcConfigKeys.RPC_SERIALIZABLE_DFT);
		if (StringUtil.isNotEmpty(defaultSerializable)) {
			AdaptiveSerializable.setDefaultSerializable(defaultSerializable);
		}

		// 设置json
		String ucloudJson = configuration.getParameter("ucloude.json");
		if (StringUtil.isNotEmpty(ucloudJson)) {
			JsonFactory.setJSONAdapter(ucloudJson);
		}

		// 设置logger
		String logger = configuration.getParameter(ExtConfigKeys.UTS_LOGGER);
		if (StringUtil.isNotEmpty(logger)) {
			LoggerManager.setLoggerAdapter(logger);
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		try {
            if (started.compareAndSet(true, false)) {

                if (registry != null) {
                    registry.unregister(node);
                }

                beforeRpcStop();

                rpcStop();

                afterRpcStop();

                context.getEventCenter().publishSync(new EventInfo(EcTopic.NODE_SHUT_DOWN));

                AliveKeeping.stop();

                LOGGER.info("========== Stop success, nodeType={}, identity={}", configuration.getNodeType(), configuration.getIdentity());
            }
        } catch (Throwable e) {
            LOGGER.error("========== Stop failed, nodeType={}, identity={}", configuration.getNodeType(), configuration.getIdentity(), e);
        }
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		try {
			registry.destroy();
			LOGGER.info("Destroy success, nodeType={}, identity={}", configuration.getNodeType(),
					configuration.getIdentity());
		} catch (Throwable e) {
			LOGGER.error("Destroy failed, nodeType={}, identity={}", configuration.getNodeType(),
					configuration.getIdentity(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private Context getContext() {
		try {
			return ((Class<Context>) GenericsUtils.getSuperClassGenericType(this.getClass(), 1)).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<T> getNodeClass() {
		return (Class<T>) GenericsUtils.getSuperClassGenericType(this.getClass(), 0);
	}

	/**
	 * 设置zookeeper注册中心地址
	 */
	public void setRegistryAddress(String registryAddress) {
		configuration.setRegistryAddress(registryAddress);
	}

	/**
	 * 设置远程调用超时时间
	 */
	public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
		configuration.setInvokeTimeoutMillis(invokeTimeoutMillis);
	}

	/**
	 * 设置集群名字
	 */
	public void setClusterName(String clusterName) {
		configuration.setClusterName(clusterName);
	}

	/**
	 * 节点标识(必须要保证这个标识是唯一的才能设置，请谨慎设置) 这个是非必须设置的，建议使用系统默认生成
	 */
	public void setIdentity(String identity) {
		configuration.setIdentity(identity);
	}

	/**
	 * 添加节点监听器
	 */
	public void addNodeChangeListener(NodeChangeListener notifyListener) {
		if (notifyListener != null) {
			nodeChangeListeners.add(notifyListener);
		}
	}

	/**
	 * 显示设置绑定ip
	 */
	public void setBindIp(String bindIp) {
		if (StringUtil.isEmpty(bindIp) || !NetUtils.isValidHost(bindIp)) {
			throw new IllegalArgumentException("Invalided bind ip:" + bindIp);
		}
		configuration.setIp(bindIp);
	}

	/**
	 * 添加 master 节点变化监听器
	 */
	public void addMasterChangeListener(MasterChangeListener masterChangeListener) {
		if (masterChangeListener != null) {
			masterChangeListeners.add(masterChangeListener);
		}
	}

	public void setDataPath(String path) {
		if (StringUtil.isNotEmpty(path)) {
			configuration.setDataPath(path);
		}
	}

	/**
	 * 设置额外的配置参数
	 */
	public void addConfiguration(String key, String value) {
		configuration.setParameter(key, value);
	}
}
