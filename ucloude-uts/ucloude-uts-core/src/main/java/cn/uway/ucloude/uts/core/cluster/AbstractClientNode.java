package cn.uway.ucloude.uts.core.cluster;

import java.util.concurrent.Executors;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.rpc.RpcClient;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.RpcTransporter;
import cn.uway.ucloude.rpc.configuration.ClientConfiguration;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.rpc.HeartBeatMonitor;
import cn.uway.ucloude.uts.core.rpc.RpcClientDelegate;

/**
 *  抽象客户端
 * @author uway
 *
 * @param <T>
 * @param <Context>
 */
public abstract class AbstractClientNode<T extends Node, Context extends UtsContext> extends AbstractJobNode<T, Context> {
	protected RpcClientDelegate rpcClient;
    private HeartBeatMonitor heartBeatMonitor;

    protected void rpcStart() {
        rpcClient.start();
        heartBeatMonitor.start();

        RpcProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = configuration.getParameter(ExtConfigKeys.PROCESSOR_THREAD, UCloudeConstants.DEFAULT_PROCESSOR_THREAD);
            rpcClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName(), true)));
        }
    }

    /**
     * 得到默认的处理器
     */
    protected abstract RpcProcessor getDefaultProcessor();

    protected void rpcStop() {
        heartBeatMonitor.stop();
        rpcClient.shutdown();
    }

    /**
     * 设置节点组名
     */
    public void setNodeGroup(String nodeGroup) {
        configuration.setNodeGroup(nodeGroup);
    }

    public boolean isServerEnable() {
        return rpcClient.isServerEnable();
    }

    @Override
    protected void beforeRpcStart() {
        //
        this.rpcClient = new RpcClientDelegate(getRpcClient(new ClientConfiguration()), context);
        this.heartBeatMonitor = new HeartBeatMonitor(rpcClient, context);

        beforeStart();
    }

    private RpcClient getRpcClient(ClientConfiguration rpcClientConfig) {
        return ServiceFactory.load(RpcTransporter.class, configuration).getRpcClient(context.getConfiguration(), rpcClientConfig);
    }

    @Override
    protected void afterRpcStart() {
        // 父类要做的
        afterStart();
    }

    @Override
    protected void beforeRpcStop() {
        beforeStop();
    }

    @Override
    protected void afterRpcStop() {
        afterStop();
    }

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();
}
