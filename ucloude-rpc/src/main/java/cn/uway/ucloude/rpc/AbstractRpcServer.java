package cn.uway.ucloude.rpc;



import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.uway.ucloude.common.Pair;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.configuration.ServerConfiguration;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.exception.RpcSendRequestException;
import cn.uway.ucloude.rpc.exception.RpcTimeoutException;
import cn.uway.ucloude.rpc.exception.RpcTooMuchRequestException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.thread.NamedThreadFactory;


/**
 * Rpc服务端实现
 */
public abstract class AbstractRpcServer extends AbstractRpc implements RpcServer {
    protected static final ILogger LOGGER = LoggerManager.getLogger(AbstractRpcServer.class);

    protected final ServerConfiguration serverConfig;
    // 处理Callback应答器
    private final ExecutorService publicExecutor;
    // 定时器
    private final Timer timer = new Timer("ServerHouseKeepingService", true);

    public AbstractRpcServer(final ServerConfiguration serverConfig,
                                  final ChannelEventListener channelEventListener) {
        super(serverConfig.getServerOnewaySemaphoreValue(),
        		serverConfig.getServerAsyncSemaphoreValue(), channelEventListener);
        this.serverConfig = serverConfig;

        int publicThreadNums = serverConfig.getServerCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new NamedThreadFactory("RpcServerPublicExecutor", true));
    }

    @Override
    public final void start() throws RpcException {

        serverStart();

        if (channelEventListener != null) {
            this.RpcEventExecutor.start();
        }

        // 每隔1秒扫描下异步调用超时情况
        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    AbstractRpcServer.this.scanResponseTable();
                } catch (Exception e) {
                    LOGGER.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);
    }

    protected abstract void serverStart() throws RpcException;

    @Override
    public void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if (null == executor) {
            executorThis = this.publicExecutor;
        }

        Pair<RpcProcessor, ExecutorService> pair =
                new Pair<RpcProcessor, ExecutorService>(processor, executorThis);
        this.processorTable.put(requestCode, pair);
    }

    @Override
    public void registerDefaultProcessor(RpcProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<RpcProcessor, ExecutorService>(processor, executor);
    }

    @Override
    public RpcCommand invokeSync(final Channel channel, final RpcCommand request,
                                      final long timeoutMillis) throws InterruptedException, RpcSendRequestException,
            RpcTimeoutException {
        return this.invokeSyncImpl(channel, request, timeoutMillis);
    }

    @Override
    public void invokeAsync(Channel channel, RpcCommand request, long timeoutMillis,
                            AsyncCallback asyncCallback) throws InterruptedException, RpcTooMuchRequestException,
            RpcTimeoutException, RpcSendRequestException {
        this.invokeAsyncImpl(channel, request, timeoutMillis, asyncCallback);
    }

    @Override
    public void invokeOneway(Channel channel, RpcCommand request, long timeoutMillis)
            throws InterruptedException, RpcTooMuchRequestException, RpcTimeoutException,
            RpcSendRequestException {
        this.invokeOnewayImpl(channel, request, timeoutMillis);
    }

    @Override
    public void shutdown() {
        try {
            if (this.timer != null) {
                this.timer.cancel();
            }

            if (this.RpcEventExecutor != null) {
                this.RpcEventExecutor.shutdown();
            }

            serverShutdown();

        } catch (Exception e) {
            LOGGER.error("RpcServer shutdown exception, ", e);
        }

        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                LOGGER.error("RpcServer shutdown exception, ", e);
            }
        }
    }

    protected abstract void serverShutdown() throws RpcException;

    @Override
    protected ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }
}
