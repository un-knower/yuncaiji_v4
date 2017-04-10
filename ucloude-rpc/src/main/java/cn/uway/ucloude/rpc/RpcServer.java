package cn.uway.ucloude.rpc;

import java.util.concurrent.ExecutorService;

import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.exception.RpcSendRequestException;
import cn.uway.ucloude.rpc.exception.RpcTimeoutException;
import cn.uway.ucloude.rpc.exception.RpcTooMuchRequestException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;

/**
 * 远程通信，Server接口
 * @author uway
 *
 */
public interface RpcServer {
	void start() throws RpcException;


    /**
     * 注册请求处理器，ExecutorService必须要对应一个队列大小有限制的阻塞队列，防止OOM
     */
    void registerProcessor(final int requestCode, final RpcProcessor processor,
                           final ExecutorService executor);

    /**
     * 注册默认请求处理器
     */
    void registerDefaultProcessor(final RpcProcessor processor, final ExecutorService executor);


    /**
     * 同步调用
     */
    RpcCommand invokeSync(final Channel channel, final RpcCommand request,
                               final long timeoutMillis) throws InterruptedException, RpcSendRequestException,
            RpcTimeoutException;

    /**
     * 异步调用
     */
    void invokeAsync(final Channel channel, final RpcCommand request, final long timeoutMillis,
                     final AsyncCallback asyncCallback) throws InterruptedException,
            RpcTooMuchRequestException, RpcTimeoutException, RpcSendRequestException;

    /**
     * 单向调用
     */
    void invokeOneway(final Channel channel, final RpcCommand request, final long timeoutMillis)
            throws InterruptedException, RpcTooMuchRequestException, RpcTimeoutException,
            RpcSendRequestException;


    void shutdown();
}
