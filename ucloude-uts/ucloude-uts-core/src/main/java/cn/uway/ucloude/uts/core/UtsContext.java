package cn.uway.ucloude.uts.core;

import cn.uway.ucloude.cmd.HttpCmdServer;
import cn.uway.ucloude.data.dataaccess.DataSourceProvider;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.uts.core.cluster.MasterElector;
import cn.uway.ucloude.uts.core.cluster.SubscribedNodeManager;
import cn.uway.ucloude.uts.core.monitor.MStatReporter;
import cn.uway.ucloude.uts.core.protocol.command.CommandBodyWrapper;
import cn.uway.ucloude.uts.core.registry.RegistryStatMonitor;

public class UtsContext {
	
	
	private UtsConfiguration configuration;
	
	/**
	 * master节点选举者
	 */
	private MasterElector masterElector;
	
	/**
	 * 事件中心
	 */
    private IEventCenter eventCenter;
    
    /**
     * 节点管理
     */
    private SubscribedNodeManager subscribedNodeManager;
    
    /**
     * 注册中心状态监控
     */
    private RegistryStatMonitor registryStatMonitor;
    
    /**
     * 节点通信CommandBody包装器
     */
    private CommandBodyWrapper commandBodyWrapper;
    
    /**
     * 命令中心
     */
    private HttpCmdServer httpCmdServer;
    
    /**
     *  监控中心
     */
    private MStatReporter mStatReporter;

	public MStatReporter getMStatReporter() {
		return mStatReporter;
	}

	public void setMStatReporter(MStatReporter mStatReporter) {
		this.mStatReporter = mStatReporter;
	}

	public HttpCmdServer getHttpCmdServer() {
		return httpCmdServer;
	}

	public void setHttpCmdServer(HttpCmdServer httpCmdServer) {
		this.httpCmdServer = httpCmdServer;
	}

	public CommandBodyWrapper getCommandBodyWrapper() {
		return commandBodyWrapper;
	}

	public void setCommandBodyWrapper(CommandBodyWrapper commandBodyWrapper) {
		this.commandBodyWrapper = commandBodyWrapper;
	}

	public RegistryStatMonitor getRegistryStatMonitor() {
		return registryStatMonitor;
	}

	public void setRegistryStatMonitor(RegistryStatMonitor registryStatMonitor) {
		this.registryStatMonitor = registryStatMonitor;
	}

	public MasterElector getMasterElector() {
		return masterElector;
	}

	public void setMasterElector(MasterElector masterElector) {
		this.masterElector = masterElector;
	}

	public SubscribedNodeManager getSubscribedNodeManager() {
		return subscribedNodeManager;
	}

	public void setSubscribedNodeManager(SubscribedNodeManager subscribedNodeManager) {
		this.subscribedNodeManager = subscribedNodeManager;
	}

	public UtsConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(UtsConfiguration configuration) {
		this.configuration = configuration;
	}

	public IEventCenter getEventCenter() {
		return eventCenter;
	}

	public void setEventCenter(IEventCenter eventCenter) {
		this.eventCenter = eventCenter;
	}
}
