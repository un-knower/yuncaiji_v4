package cn.uway.ucloude.uts.core.cluster;

import java.util.concurrent.Executors;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.RpcServer;
import cn.uway.ucloude.rpc.RpcTransporter;
import cn.uway.ucloude.rpc.configuration.ServerConfiguration;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.rpc.RpcServerDelegate;

public abstract class AbstractServerNode<T extends Node, Context extends UtsContext> extends AbstractJobNode<T, Context> {
	protected RpcServerDelegate rpcServer;

    protected void rpcStart() {

        rpcServer.start();

        RpcProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = configuration.getParameter(ExtConfigKeys.PROCESSOR_THREAD, UCloudeConstants.DEFAULT_PROCESSOR_THREAD);
            rpcServer.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize, new NamedThreadFactory(AbstractServerNode.class.getSimpleName(), true)));
        }
    }

    public void setListenPort(int listenPort) {
        configuration.setListenPort(listenPort);
    }

    protected void rpcStop() {
        rpcServer.shutdown();
    }

    @Override
    protected void beforeRpcStart() {
        ServerConfiguration rpcServerConfig = new ServerConfiguration();
        // configuration 配置
        if (configuration.getListenPort() == 0) {
            configuration.setListenPort(ExtConfigKeys.JOB_TRACKER_DEFAULT_LISTEN_PORT);
            node.setPort(configuration.getListenPort());
        }
        rpcServerConfig.setListenPort(configuration.getListenPort());

        rpcServer = new RpcServerDelegate(getRpcServer(rpcServerConfig), context);

        beforeStart();
    }

    private RpcServer getRpcServer(ServerConfiguration rpcServerConfig) {
        return ServiceFactory.load(RpcTransporter.class, configuration).getRpcServer(context.getConfiguration(), rpcServerConfig);
    }

    @Override
    protected void afterRpcStart() {
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

    /**
     * 得到默认的处理器
     */
    protected abstract RpcProcessor getDefaultProcessor();

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();
}
