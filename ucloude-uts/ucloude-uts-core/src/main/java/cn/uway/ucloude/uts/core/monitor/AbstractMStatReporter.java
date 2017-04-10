package cn.uway.ucloude.uts.core.monitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.domain.monitor.MData;
import cn.uway.ucloude.uts.jvmmonitor.JVMMonitor;

public abstract class AbstractMStatReporter implements MStatReporter {

	protected final ILogger LOGGER = LoggerManager.getLogger(AbstractMStatReporter.class);

    protected UtsContext context;
    protected UtsConfiguration configuration;

    private ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor(new NamedThreadFactory("UTS-Monitor-data-collector", true));
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);

    public AbstractMStatReporter(UtsContext context) {
        this.context = context;
        this.configuration = context.getConfiguration();
    }

    public final void start() {

        // 启动JVM监控
        JVMMonitor.start();

        try {
            if (!configuration.getParameter(ExtConfigKeys.M_STAT_REPORTER_CLOSED, false)) {
                if (start.compareAndSet(false, true)) {
                    scheduledFuture = executor.scheduleWithFixedDelay(
                            new MStatReportWorker(context, this), 1, 1, TimeUnit.SECONDS);
                    LOGGER.info("MStatReporter start succeed.");
                }
            }
        } catch (Exception e) {
            LOGGER.error("MStatReporter start failed.", e);
        }
    }

    /**
     * 用来收集数据
     */
    protected abstract MData collectMData();

    protected abstract NodeType getNodeType();

    public final void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                executor.shutdown();
                JVMMonitor.stop();
                LOGGER.info("MStatReporter stop succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("MStatReporter stop failed.", e);
        }
    }

}
