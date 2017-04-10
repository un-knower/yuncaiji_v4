package cn.uway.ucloude.uts.core.rpc;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.EventSubscriber;
import cn.uway.ucloude.ec.IObserver;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.HeartBeatRequest;

/**
 * 如果用来发送心跳包，当没有连接上JobTracker的时候，启动快速检测连接；连接后，采用慢周期检测来保持长连接
 * @author uway
 *
 */
public class HeartBeatMonitor {
	private static final ILogger LOGGER = LoggerManager.getLogger(HeartBeatMonitor.class.getSimpleName());

    // 用来定时发送心跳
    private final ScheduledExecutorService PING_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("UTS-HeartBeat-Ping", true));
    private ScheduledFuture<?> pingScheduledFuture;
    // 当没有可用的JobTracker的时候，启动这个来快速的检查（小间隔）
    private final ScheduledExecutorService FAST_PING_EXECUTOR = Executors.newScheduledThreadPool(1, new NamedThreadFactory("UTS-HeartBeat-Fast-Ping", true));
    private ScheduledFuture<?> fastPingScheduledFuture;

    private RpcClientDelegate rpcClient;
    private UtsContext context;
    private EventSubscriber jobTrackerUnavailableEventSubscriber;

    public HeartBeatMonitor(RpcClientDelegate rpcClient, UtsContext context) {
        this.rpcClient = rpcClient;
        this.context = context;
        this.jobTrackerUnavailableEventSubscriber = new EventSubscriber(HeartBeatMonitor.class.getName()
                + "_PING_" + context.getConfiguration().getIdentity(),
                new IObserver() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        startFastPing();
                        stopPing();
                    }
                });
        context.getEventCenter().subscribe(new EventSubscriber(HeartBeatMonitor.class.getName()
                + "_NODE_ADD_" + context.getConfiguration().getIdentity(), new IObserver() {
            @Override
            public void onObserved(EventInfo eventInfo) {
                Node node = (Node) eventInfo.getParam("node");
                if (node == null || NodeType.JOB_TRACKER != node.getNodeType()) {
                    return;
                }
                try {
                    check(node);
                } catch (Throwable ignore) {
                }
            }
        }), EcTopic.NODE_ADD);
    }

    private AtomicBoolean pingStart = new AtomicBoolean(false);
    private AtomicBoolean fastPingStart = new AtomicBoolean(false);

    public void start() {
        startFastPing();
    }

    public void stop() {
        stopPing();
        stopFastPing();
    }

    private void startPing() {
        try {
            if (pingStart.compareAndSet(false, true)) {
                // 用来监听 JobTracker不可用的消息，然后马上启动 快速检查定时器
                context.getEventCenter().subscribe(jobTrackerUnavailableEventSubscriber, EcTopic.NO_JOB_TRACKER_AVAILABLE);
                if (pingScheduledFuture == null) {
                    pingScheduledFuture = PING_EXECUTOR_SERVICE.scheduleWithFixedDelay(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (pingStart.get()) {
                                        ping();
                                    }
                                }
                            }, 30, 30, TimeUnit.SECONDS);      // 30s 一次心跳
                }
                LOGGER.debug("Start slow ping success.");
            }
        } catch (Throwable t) {
            LOGGER.error("Start slow ping failed.", t);
        }
    }

    private void stopPing() {
        try {
            if (pingStart.compareAndSet(true, false)) {
//                pingScheduledFuture.cancel(true);
//                PING_EXECUTOR_SERVICE.shutdown();
                context.getEventCenter().unSubscribe(EcTopic.NO_JOB_TRACKER_AVAILABLE, jobTrackerUnavailableEventSubscriber);
                LOGGER.debug("Stop slow ping success.");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop slow ping failed.", t);
        }
    }

    private void startFastPing() {
        if (fastPingStart.compareAndSet(false, true)) {
            try {
                // 2s 一次进行检查
                if (fastPingScheduledFuture == null) {
                    fastPingScheduledFuture = FAST_PING_EXECUTOR.scheduleWithFixedDelay(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (fastPingStart.get()) {
                                        ping();
                                    }
                                }
                            }, 500, 500, TimeUnit.MILLISECONDS);
                }
                LOGGER.debug("Start fast ping success.");
            } catch (Throwable t) {
                LOGGER.error("Start fast ping failed.", t);
            }
        }
    }

    private void stopFastPing() {
        try {
            if (fastPingStart.compareAndSet(true, false)) {
//                fastPingScheduledFuture.cancel(true);
//                FAST_PING_EXECUTOR.shutdown();
                LOGGER.debug("Stop fast ping success.");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop fast ping failed.", t);
        }
    }

    private AtomicBoolean running = new AtomicBoolean(false);

    private void ping() {
        try {
            if (running.compareAndSet(false, true)) {
                // to ensure only one thread go there
                try {
                    check();
                } finally {
                    running.compareAndSet(true, false);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Ping JobTracker error", t);
        }
    }

    private void check() {
        List<Node> jobTrackers = context.getSubscribedNodeManager().getNodeList(NodeType.JOB_TRACKER);
        if (CollectionUtil.isEmpty(jobTrackers)) {
            return;
        }
        for (Node jobTracker : jobTrackers) {
            check(jobTracker);
        }
    }

    private void check(Node jobTracker) {
        // 每个JobTracker 都要发送心跳
        if (beat(rpcClient, jobTracker.getAddress())) {
            rpcClient.addJobTracker(jobTracker);
            if (!rpcClient.isServerEnable()) {
                rpcClient.setServerEnable(true);
                context.getEventCenter().publishAsync(new EventInfo(EcTopic.JOB_TRACKER_AVAILABLE));
            } else {
                rpcClient.setServerEnable(true);
            }
            stopFastPing();
            startPing();
        } else {
            rpcClient.removeJobTracker(jobTracker);
        }
    }

    /**
     * 发送心跳
     */
    private boolean beat(RpcClientDelegate rpcClient, String addr) {

        HeartBeatRequest commandBody = context.getCommandBodyWrapper().wrapper(new HeartBeatRequest());

        RpcCommand request = RpcCommand.createRequestCommand(
                JobProtos.RequestCode.HEART_BEAT.code(), commandBody);
        try {
            RpcCommand response = rpcClient.getRpcClient().invokeSync(addr, request, 5000);
            if (response != null && JobProtos.ResponseCode.HEART_BEAT_SUCCESS ==
                    JobProtos.ResponseCode.valueOf(response.getCode())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("heart beat success. ");
                }
                return true;
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return false;
    }
}
