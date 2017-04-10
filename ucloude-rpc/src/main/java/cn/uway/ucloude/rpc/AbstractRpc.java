package cn.uway.ucloude.rpc;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;

import cn.uway.ucloude.common.Pair;
import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.codec.Codec;
import cn.uway.ucloude.rpc.codec.DefaultCodec;
import cn.uway.ucloude.rpc.common.RpcHelper;
import cn.uway.ucloude.rpc.common.SemaphoreReleaseOnlyOnce;
import cn.uway.ucloude.rpc.common.ServiceThread;
import cn.uway.ucloude.rpc.exception.RpcSendRequestException;
import cn.uway.ucloude.rpc.exception.RpcTimeoutException;
import cn.uway.ucloude.rpc.exception.RpcTooMuchRequestException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcCommandHelper;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.utils.StringUtil;


/**
 * Server与Client公用抽象类
 */
public abstract class AbstractRpc {
    private static final ILogger LOGGER = LoggerManager.getLogger(AbstractRpc.class);

    // 信号量，Oneway情况会使用，防止本地缓存请求过多
    protected final Semaphore semaphoreOneway;

    // 信号量，异步调用情况会使用，防止本地缓存请求过多
    protected final Semaphore semaphoreAsync;

    // 缓存所有对外请求
    protected final ConcurrentHashMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);
    // 注册的各个RPC处理器
    protected final HashMap<Integer/* request code */, Pair<RpcProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, Pair<RpcProcessor, ExecutorService>>(64);
    protected final RpcEventExecutor RpcEventExecutor = new RpcEventExecutor();
    // 默认请求代码处理器
    protected Pair<RpcProcessor, ExecutorService> defaultRequestProcessor;
    protected final ChannelEventListener channelEventListener;

    public AbstractRpc(final int permitsOneway, final int permitsAsync, ChannelEventListener channelEventListener) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
        this.channelEventListener = channelEventListener;
    }

    public ChannelEventListener getChannelEventListener() {
        return this.channelEventListener;
    }

    public void putRpcEvent(final RpcEvent event) {
        this.RpcEventExecutor.putRpcEvent(event);
    }

    public void processRequestCommand(final Channel channel, final RpcCommand cmd) {
        final Pair<RpcProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<RpcProcessor, ExecutorService> pair =
                null == matched ? this.defaultRequestProcessor : matched;

        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        final RpcCommand response = pair.getKey().processRequest(channel, cmd);
                        // Oneway形式忽略应答结果
                        if (!RpcCommandHelper.isOnewayRPC(cmd)) {
                            if (response != null) {
                                response.setOpaque(cmd.getOpaque());
                                RpcCommandHelper.markResponseType(cmd);
                                try {
                                    channel.writeAndFlush(response).addListener(new ChannelHandlerListener() {
                                        @Override
                                        public void operationComplete(Future future) throws Exception {
                                            if (!future.isSuccess()) {
                                                LOGGER.error("response to " + RpcHelper.parseChannelRemoteAddr(channel) + " failed", future.cause());
                                                LOGGER.error(cmd.toString());
                                                LOGGER.error(response.toString());
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    LOGGER.error("process request over, but response failed", e);
                                    LOGGER.error(cmd.toString());
                                    LOGGER.error(response.toString());
                                }
                            } else {
                                // 收到请求，但是没有返回应答，可能是processRequest中进行了应答，忽略这种情况
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("process request exception", e);
                        LOGGER.error(cmd.toString());

                        if (!RpcCommandHelper.isOnewayRPC(cmd)) {
                            final RpcCommand response =
                                    RpcCommand.createResponseCommand(RpcProtos.ResponseCode.SYSTEM_ERROR.code(),//
                                            StringUtil.toString(e));
                            response.setOpaque(cmd.getOpaque());
                            channel.writeAndFlush(response);
                        }
                    }
                }
            };

            try {
                // 这里需要做流控，要求线程池对应的队列必须是有大小限制的
                pair.getValue().submit(run);
            } catch (RejectedExecutionException e) {
                LOGGER.warn(RpcHelper.parseChannelRemoteAddr(channel) //
                        + ", too many requests and system thread pool busy, RejectedExecutionException " //
                        + pair.getKey().toString() //
                        + " request code: " + cmd.getCode());
                if (!RpcCommandHelper.isOnewayRPC(cmd)) {
                    final RpcCommand response =
                            RpcCommand.createResponseCommand(RpcProtos.ResponseCode.SYSTEM_BUSY.code(),
                                    "too many requests and system thread pool busy, please try another server");
                    response.setOpaque(cmd.getOpaque());
                    channel.writeAndFlush(response);
                }
            }
        } else {
            String error = " request type " + cmd.getCode() + " not supported";
            final RpcCommand response =
                    RpcCommand.createResponseCommand(RpcProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                            error);
            response.setOpaque(cmd.getOpaque());
            channel.writeAndFlush(response);
            LOGGER.error(RpcHelper.parseChannelRemoteAddr(channel) + error);
        }
    }

    public void processResponseCommand(Channel channel, RpcCommand cmd) {
        final ResponseFuture responseFuture = responseTable.get(cmd.getOpaque());
        if (responseFuture != null) {
            responseFuture.setResponseCommand(cmd);

            responseFuture.release();

            // 异步调用
            if (responseFuture.getAsyncCallback() != null) {
                boolean runInThisThread = false;
                ExecutorService executor = this.getCallbackExecutor();
                if (executor != null) {
                    try {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    responseFuture.executeInvokeCallback();
                                } catch (Exception e) {
                                    LOGGER.warn("execute callback in executor exception, and callback throw", e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        runInThisThread = true;
                        LOGGER.warn("execute callback in executor exception, maybe executor busy", e);
                    }
                } else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                }
            }
            // 同步调用
            else {
                responseFuture.putResponse(cmd);
            }
        } else {
            LOGGER.warn("receive response, but not matched any request, "
                    + RpcHelper.parseChannelRemoteAddr(channel));
            LOGGER.warn(cmd.toString());
        }

        responseTable.remove(cmd.getOpaque());
    }

    public void processMessageReceived(Channel channel, final RpcCommand cmd) throws Exception {
        if (cmd != null) {
            switch (RpcCommandHelper.getRpcCommandType(cmd)) {
                case REQUEST_COMMAND:
                    processRequestCommand(channel, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(channel, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    protected abstract ExecutorService getCallbackExecutor();

    public void scanResponseTable() {
        Iterator<Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= SystemClock.now()) {
                it.remove();
                rep.release();
                try {
                    rep.executeInvokeCallback();
                } catch (Exception e) {
                    LOGGER.error("scanResponseTable, operationComplete exception", e);
                }

                LOGGER.warn("remove timeout request, " + rep);
            }
        }
    }

    public RpcCommand invokeSyncImpl(final Channel channel, final RpcCommand request,
                                          final long timeoutMillis) throws InterruptedException, RpcSendRequestException,
            RpcTimeoutException {
        try {
            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, null, null);
            this.responseTable.put(request.getOpaque(), responseFuture);
            channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(request.getOpaque());
                    responseFuture.setCause(future.cause());
                    responseFuture.putResponse(null);
                    LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                    LOGGER.warn(request.toString());
                }
            });

            RpcCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                // 发送请求成功，读取应答超时
                if (responseFuture.isSendRequestOK()) {
                    throw new RpcTimeoutException(RpcHelper.parseChannelRemoteAddr(channel),
                            timeoutMillis, responseFuture.getCause());
                }
                // 发送请求失败
                else {
                    throw new RpcSendRequestException(RpcHelper.parseChannelRemoteAddr(channel),
                            responseFuture.getCause());
                }
            }

            return responseCommand;
        } finally {
            this.responseTable.remove(request.getOpaque());
        }
    }

    public void invokeAsyncImpl(final Channel channel, final RpcCommand request,
                                final long timeoutMillis, final AsyncCallback asyncCallback) throws InterruptedException,
            RpcTooMuchRequestException, RpcTimeoutException, RpcSendRequestException {
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, asyncCallback, once);
            this.responseTable.put(request.getOpaque(), responseFuture);
            try {
                channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        if (future.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        } else {
                            responseFuture.setSendRequestOK(false);
                        }

                        responseFuture.putResponse(null);
                        responseFuture.executeInvokeCallback();

                        responseTable.remove(request.getOpaque());
                        LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        LOGGER.warn(request.toString());
                    }
                });
            } catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RpcSendRequestException(RpcHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RpcTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                LOGGER.warn("invokeAsyncImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreAsync.getQueueLength());
                LOGGER.warn(request.toString());

                throw new RpcTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    public void invokeOnewayImpl(final Channel channel, final RpcCommand request,
                                 final long timeoutMillis) throws InterruptedException, RpcTooMuchRequestException,
            RpcTimeoutException, RpcSendRequestException {
        RpcCommandHelper.markOnewayRPC(request);
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        once.release();
                        if (!future.isSuccess()) {
                            LOGGER.warn("send a request command to channel <" + channel.remoteAddress()
                                    + "> failed.");
                            LOGGER.warn(request.toString());
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RpcSendRequestException(RpcHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RpcTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                LOGGER.warn("invokeOnewayImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreOneway.getQueueLength());
                LOGGER.warn(request.toString());

                throw new RpcTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    class RpcEventExecutor extends ServiceThread {
        private final LinkedBlockingQueue<RpcEvent> eventQueue = new LinkedBlockingQueue<RpcEvent>();
        private final int MaxSize = 10000;

        public void putRpcEvent(final RpcEvent event) {
            if (this.eventQueue.size() <= MaxSize) {
                this.eventQueue.add(event);
            } else {
                LOGGER.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(),
                        event.toString());
            }
        }

        @Override
        public void run() {

            LOGGER.info(this.getServiceName() + " service started");

            final ChannelEventListener listener = AbstractRpc.this.getChannelEventListener();

            while (!this.isStopped()) {
                try {
                    RpcEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        switch (event.getType()) {
                            case ALL_IDLE:
                                listener.onChannelIdle(IdleState.ALL_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case WRITER_IDLE:
                                listener.onChannelIdle(IdleState.WRITER_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case READER_IDLE:
                                listener.onChannelIdle(IdleState.READER_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn(this.getServiceName() + " service has exception. ", e);
                }
            }

            LOGGER.info(this.getServiceName() + " service end");
        }

        @Override
        public String getServiceName() {
            return RpcEventExecutor.class.getSimpleName();
        }
    }

    protected Codec getCodec() {
        // TODO 改为SPI
        return new DefaultCodec();
    }

}
