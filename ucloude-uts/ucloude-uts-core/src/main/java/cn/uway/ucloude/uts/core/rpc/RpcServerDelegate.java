package cn.uway.ucloude.uts.core.rpc;

import java.util.concurrent.ExecutorService;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.RpcServer;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.exception.RpcSendException;

public class RpcServerDelegate {
	private static final ILogger LOGGER = LoggerManager.getLogger(RpcServerDelegate.class);
	 private RpcServer rpcServer;
	    private UtsContext context;

	    public RpcServerDelegate(RpcServer rpcServer, UtsContext context) {
	        this.rpcServer = rpcServer;
	        this.context = context;
	    }

	    public void start() {
	        try {
	            rpcServer.start();
	            LOGGER.info("rpc server start");
	        } catch (RpcException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    public void registerProcessor(int requestCode, RpcProcessor processor,
	                                  ExecutorService executor) {
	        rpcServer.registerProcessor(requestCode, processor, executor);
	    }

	    public void registerDefaultProcessor(RpcProcessor processor, ExecutorService executor) {
	        rpcServer.registerDefaultProcessor(processor, executor);
	    }

	    public RpcCommand invokeSync(Channel channel, RpcCommand request)
	            throws RpcSendException {
	        try {
	        	LOGGER.info("rpc server invokeSync");
	            return rpcServer.invokeSync(channel, request,
	                    context.getConfiguration().getInvokeTimeoutMillis());
	        } catch (Throwable t) {
	        	LOGGER.info("rpc server invokeSync RpcSendException");
	            throw new RpcSendException(t);
	        }
	    }

	    public void invokeAsync(Channel channel, RpcCommand request, AsyncCallback asyncCallback)
	            throws RpcSendException {
	        try {
	        	LOGGER.info("rpc server invokeAsync");
	            rpcServer.invokeAsync(channel, request,
	                    context.getConfiguration().getInvokeTimeoutMillis(), asyncCallback);
	        } catch (Throwable t) {
	        	LOGGER.info("rpc server invokeAsync RpcSendException");
	            throw new RpcSendException(t);
	        }
	    }

	    public void invokeOneway(Channel channel, RpcCommand request)
	            throws RpcSendException {
	        try {
	        	LOGGER.info("rpc server invokeOneway");
	            rpcServer.invokeOneway(channel, request,
	                    context.getConfiguration().getInvokeTimeoutMillis());
	        } catch (Throwable t) {
	         	LOGGER.info("rpc server invokeOneway RpcSendException");
	            throw new RpcSendException(t);
	        }
	    }

	    public void shutdown() {
	        rpcServer.shutdown();
	    }
}
