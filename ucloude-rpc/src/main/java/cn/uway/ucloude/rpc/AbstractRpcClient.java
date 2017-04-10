package cn.uway.ucloude.rpc;



import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.uway.ucloude.common.Pair;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.common.RpcHelper;
import cn.uway.ucloude.rpc.configuration.ClientConfiguration;
import cn.uway.ucloude.rpc.exception.RpcConnectionException;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.exception.RpcSendRequestException;
import cn.uway.ucloude.rpc.exception.RpcTimeoutException;
import cn.uway.ucloude.rpc.exception.RpcTooMuchRequestException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.thread.NamedThreadFactory;


/**
 * Rpc客户端实现
 */
public abstract class AbstractRpcClient extends AbstractRpc implements RpcClient {
    protected static final ILogger LOGGER = LoggerManager.getLogger(AbstractRpcClient.class);

    private static final long LockTimeoutMillis = 3000;

    protected final ClientConfiguration clientConfig;

    private final Lock lockChannelTables = new ReentrantLock();
    private final ConcurrentHashMap<String /* addr */, ChannelWrapper> channelTables = new ConcurrentHashMap<String, ChannelWrapper>();
    // 定时器
    private final Timer timer = new Timer("ClientHouseKeepingService", true);
    // 处理Callback应答器
    private final ExecutorService publicExecutor;

    public AbstractRpcClient(final ClientConfiguration clientConfig,
                                  final ChannelEventListener channelEventListener) {
        super(clientConfig.getClientOnewaySemaphoreValue(), clientConfig
                .getClientAsyncSemaphoreValue(), channelEventListener);
        this.clientConfig = clientConfig;

        int publicThreadNums = clientConfig.getClientCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new NamedThreadFactory("RpcClientPublicExecutor", true));

    }

    @Override
    public void start() throws RpcException {

        clientStart();

        // 每隔1秒扫描下异步调用超时情况
        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    AbstractRpcClient.this.scanResponseTable();
                } catch (Exception e) {
                    LOGGER.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);

        if (this.channelEventListener != null) {
            this.RpcEventExecutor.start();
        }
    }

    protected abstract void clientStart() throws RpcException;

    @Override
    public void shutdown() {
        try {
            this.timer.cancel();

            for (ChannelWrapper cw : this.channelTables.values()) {
                this.closeChannel(null, cw.getChannel());
            }

            this.channelTables.clear();

            if (this.RpcEventExecutor != null) {
                this.RpcEventExecutor.shutdown();
            }

            clientShutdown();

        } catch (Exception e) {
            LOGGER.error("NettyRpcClient shutdown exception, ", e);
        }

        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                LOGGER.error("NettyRpcServer shutdown exception, ", e);
            }
        }
    }

    protected abstract void clientShutdown();

    private Channel getAndCreateChannel(final String addr) throws InterruptedException {

        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isConnected()) {
            return cw.getChannel();
        }

        return this.createChannel(addr);
    }

    private Channel createChannel(final String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isConnected()) {
            return cw.getChannel();
        }

        // 进入临界区后，不能有阻塞操作，网络连接采用异步方式
        if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection = false;
                cw = this.channelTables.get(addr);
                if (cw != null) {
                    // channel正常
                    if (cw.isConnected()) {
                        return cw.getChannel();
                    }
                    // 正在连接，退出锁等待
                    else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    }
                    // 说明连接不成功
                    else {
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                }
                // ChannelWrapper不存在
                else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture =
                            connect(RpcHelper.string2SocketAddress(addr));
                    LOGGER.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
                    cw = new ChannelWrapper(channelFuture);
                    this.channelTables.put(addr, cw);
                }
            } catch (Exception e) {
                LOGGER.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            LOGGER.warn("createChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();

            if (channelFuture.awaitUninterruptibly(this.clientConfig.getConnectTimeoutMillis())) {
                if (cw.isConnected()) {
                    LOGGER.info("createChannel: connect remote host[{}] success, {}", addr,
                            channelFuture.toString());
                    return cw.getChannel();
                } else {
                    LOGGER.warn(
                            "createChannel: connect remote host[" + addr + "] failed, "
                                    + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                LOGGER.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr,
                        this.clientConfig.getConnectTimeoutMillis(), channelFuture.toString());
            }
        }

        return null;
    }

    protected abstract ChannelFuture connect(SocketAddress socketAddress);

    public void closeChannel(final String addr, final Channel channel) {
        if (null == channel)
            return;

        final String addrRemote = null == addr ? RpcHelper.parseChannelRemoteAddr(channel) : addr;

        try {
            if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final ChannelWrapper prevCW = this.channelTables.get(addrRemote);

                    LOGGER.info("closeChannel: begin close the channel[{}] Found: {}", addrRemote,
                            (prevCW != null));

                    if (null == prevCW) {
                        LOGGER.info(
                                "closeChannel: the channel[{}] has been removed from the channel table before",
                                addrRemote);
                        removeItemFromTable = false;
                    } else if (prevCW.getChannel() != channel) {
                        LOGGER.info(
                                "closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.",
                                addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        LOGGER.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }

                    RpcHelper.closeChannel(channel);
                } catch (Exception e) {
                    LOGGER.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                LOGGER.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
            }
        } catch (InterruptedException e) {
            LOGGER.error("closeChannel exception", e);
        }
    }

    public void closeChannel(final Channel channel) {
        if (null == channel)
            return;

        try {
            if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    ChannelWrapper prevCW = null;
                    String addrRemote = null;

                    for (String key : channelTables.keySet()) {
                        ChannelWrapper prev = this.channelTables.get(key);
                        if (prev.getChannel() != null) {
                            if (prev.getChannel() == channel) {
                                prevCW = prev;
                                addrRemote = key;
                                break;
                            }
                        }
                    }

                    if (null == prevCW) {
                        LOGGER.info(
                                "eventCloseChannel: the channel has been removed from the channel table before");
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        LOGGER.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                        RpcHelper.closeChannel(channel);
                    }
                } catch (Exception e) {
                    LOGGER.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                LOGGER.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
            }
        } catch (InterruptedException e) {
            LOGGER.error("closeChannel exception", e);
        }
    }

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
    public RpcCommand invokeSync(String addr, final RpcCommand request, long timeoutMillis)
            throws InterruptedException, RpcConnectionException, RpcSendRequestException,
            RpcTimeoutException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isConnected()) {
            try {
                return this.invokeSyncImpl(channel, request, timeoutMillis);
            } catch (RpcSendRequestException e) {
                LOGGER.warn("invokeSync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            } catch (RpcTimeoutException e) {
                LOGGER.warn("invokeSync: wait response timeout exception, the channel[{}]", addr);
                // 超时异常如果关闭连接可能会产生连锁反应
                // this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RpcConnectionException(addr);
        }
    }

    @Override
    public void invokeAsync(String addr, RpcCommand request, long timeoutMillis,
                            AsyncCallback asyncCallback) throws InterruptedException, RpcConnectionException,
            RpcTooMuchRequestException, RpcTimeoutException, RpcSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isConnected()) {
            try {
                this.invokeAsyncImpl(channel, request, timeoutMillis, asyncCallback);
            } catch (RpcSendRequestException e) {
                LOGGER.warn("invokeAsync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RpcConnectionException(addr);
        }
    }

    @Override
    public void invokeOneway(String addr, RpcCommand request, long timeoutMillis)
            throws InterruptedException, RpcConnectionException, RpcTooMuchRequestException,
            RpcTimeoutException, RpcSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isConnected()) {
            try {
                this.invokeOnewayImpl(channel, request, timeoutMillis);
            } catch (RpcSendRequestException e) {
                LOGGER.warn("invokeOneway: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RpcConnectionException(addr);
        }
    }

    @Override
    protected ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }

    private class ChannelWrapper {
        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isConnected() {
            return channelFuture.isConnected();
        }

        private Channel getChannel() {
            return channelFuture.getChannel();
        }

        private ChannelFuture getChannelFuture() {
            return this.channelFuture;
        }
    }

}
