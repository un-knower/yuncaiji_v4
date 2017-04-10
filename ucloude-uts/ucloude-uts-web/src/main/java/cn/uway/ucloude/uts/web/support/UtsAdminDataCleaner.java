package cn.uway.ucloude.uts.web.support;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.Callable;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.QuietUtils;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;
import cn.uway.ucloude.uts.web.request.NodeOnOfflineLogQueryRequest;

public class UtsAdminDataCleaner implements InitializingBean {

    private static final ILogger LOGGER = LoggerManager.getLogger(UtsAdminDataCleaner.class);

    @Autowired
    private BackendAppContext appContext;

    private ScheduledExecutorService cleanExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("UTS-Admin-Clean", true));

    private AtomicBoolean start = new AtomicBoolean(false);

    public void start() {
        if (start.compareAndSet(false, true)) {
            cleanExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        clean();
                    } catch (Throwable t) {
                        LOGGER.error("Clean monitor data error ", t);
                    }
                }
            }, 1, 24, TimeUnit.HOURS);
            LOGGER.info("UtsAdminDataCleaner start succeed ");
        }
    }

    private void clean() {
        //  1. 清除TaskTracker JobTracker, JobClient的统计数据(3天之前的)
        final MDataRequest request = new MDataRequest();
        request.setEndTime(DateUtil.addDays(new Date(), -3).getTime());

        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendTaskTrackerMAccess().delete(request);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJobTrackerMAccess().delete(request);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJobClientMAccess().delete(request);
            }
        });

        // 2. 清除30天以前的节点上下线日志
        final NodeOnOfflineLogQueryRequest nodeOnOfflineLogPaginationReq = new NodeOnOfflineLogQueryRequest();
        nodeOnOfflineLogPaginationReq.setEndLogTime(DateUtil.addDays(new Date(), -30));

        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendNodeOnOfflineLogAccess().delete(nodeOnOfflineLogPaginationReq);
            }
        });

        // 3. 清除3天前的JVM监控信息
        final JvmDataRequest jvmDataReq = new JvmDataRequest();
        jvmDataReq.setEndTime(DateUtil.addDays(new Date(), -3).getTime());
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJVMGCAccess().delete(jvmDataReq);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJVMThreadAccess().delete(jvmDataReq);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJVMMemoryAccess().delete(jvmDataReq);
            }
        });

        LOGGER.info("Clean monitor data succeed ");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}