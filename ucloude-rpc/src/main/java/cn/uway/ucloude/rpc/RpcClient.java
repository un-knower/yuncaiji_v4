package cn.uway.ucloude.rpc;

import java.util.concurrent.ExecutorService;

import cn.uway.ucloude.rpc.exception.RpcConnectionException;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.exception.RpcSendRequestException;
import cn.uway.ucloude.rpc.exception.RpcTimeoutException;
import cn.uway.ucloude.rpc.exception.RpcTooMuchRequestException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;

/**
 * 远程通信，Client接口
 * @author uway
 *
 */
public interface RpcClient {
	void start() throws RpcException;

    /**
     * 同步调用
     */
    RpcCommand invokeSync(final String addr, final RpcCommand request,
                               final long timeoutMillis) throws InterruptedException, RpcConnectionException,
            RpcSendRequestException, RpcTimeoutException;

    /**
     * 异步调用
     */
    void invokeAsync(final String addr, final RpcCommand request, final long timeoutMillis,
                     final AsyncCallback asyncCallback) throws InterruptedException, RpcConnectionException,
            RpcTooMuchRequestException, RpcTimeoutException, RpcSendRequestException;

    /**
     * 单向调用
     */
    void invokeOneway(final String addr, final RpcCommand request, final long timeoutMillis)
            throws InterruptedException, RpcConnectionException, RpcTooMuchRequestException,
            RpcTimeoutException, RpcSendRequestException;

    /**
     * 注册处理器
     */
    void registerProcessor(final int requestCode, final RpcProcessor processor,
                           final ExecutorService executor);

    /**
     * 注册默认处理器
     */
    void registerDefaultProcessor(final RpcProcessor processor, final ExecutorService executor);

    void shutdown();
}
