package cn.uway.ucloude.uts.tasktracker.logger;



import java.util.Collections;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.ResponseFuture;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.utils.Callable;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.BizLog;
import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.exception.JobTrackerNotFoundException;
import cn.uway.ucloude.uts.core.failstore.FailStorePathBuilder;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.BizLogSendRequest;
import cn.uway.ucloude.uts.core.protocol.command.CommandBodyWrapper;
import cn.uway.ucloude.uts.core.rpc.RpcClientDelegate;
import cn.uway.ucloude.uts.core.support.NodeShutdownHook;
import cn.uway.ucloude.uts.core.support.RetryScheduler;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

/**
 * 业务日志记录器实现
 * 1. 业务日志会发送给JobTracker
 * 2. 也会采取Fail And Store 的方式
 *
 * @author uway
 */
public class BizLoggerImpl extends BizLoggerAdapter implements BizLogger {

    private Level level;
    private RpcClientDelegate rpcClient;
    private TaskTrackerContext appContext;
    private RetryScheduler<BizLog> retryScheduler;

    public BizLoggerImpl(Level level, final RpcClientDelegate rpcClient, TaskTrackerContext appContext) {
        this.level = level;
        if (this.level == null) {
            this.level = Level.INFO;
        }
        this.appContext = appContext;
        this.rpcClient = rpcClient;
        this.retryScheduler = new RetryScheduler<BizLog>(BizLogger.class.getSimpleName(), appContext, FailStorePathBuilder.getBizLoggerPath(appContext)) {
            @Override
            protected boolean isRpcEnable() {
                return rpcClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<BizLog> list) {
                return sendBizLog(list);
            }
        };
        this.retryScheduler.start();

        NodeShutdownHook.registerHook(appContext.getEventCenter(),appContext.getConfiguration().getIdentity(), this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                retryScheduler.stop();
            }
        });
    }

    @Override
    public void debug(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            sendMsg(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            sendMsg(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            sendMsg(msg);
        }
    }

    private void sendMsg(String msg) {

        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(appContext, new BizLogSendRequest());

        final BizLog bizLog = new BizLog();
        bizLog.setTaskTrackerIdentity(requestBody.getIdentity());
        bizLog.setTaskTrackerNodeGroup(requestBody.getNodeGroup());
        bizLog.setLogTime(SystemClock.now());
        JobMeta jobMeta = getJobMeta();
        bizLog.setJobId(jobMeta.getJobId());
        bizLog.setTaskId(jobMeta.getJob().getTaskId());
        bizLog.setRealTaskId(jobMeta.getRealTaskId());
        bizLog.setJobType(jobMeta.getJobType());
        bizLog.setMsg(msg);
        bizLog.setLevel(level);

        requestBody.setBizLogs(Collections.singletonList(bizLog));

        if (!rpcClient.isServerEnable()) {
            retryScheduler.inSchedule(StringUtil.generateUUID(), bizLog);
            return;
        }

        RpcCommand request = RpcCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            // 有可能down机，日志丢失
            rpcClient.invokeAsync(request, new AsyncCallback() {
                @Override
                public void onComplete(ResponseFuture responseFuture) {
                    RpcCommand response = responseFuture.getResponseCommand();

                    if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                        // success
                    } else {
                        retryScheduler.inSchedule(StringUtil.generateUUID(), bizLog);
                    }
                }
            });
        } catch (JobTrackerNotFoundException e) {
            retryScheduler.inSchedule(StringUtil.generateUUID(), bizLog);
        }
    }

    private boolean sendBizLog(List<BizLog> bizLogs) {
        if (CollectionUtil.isEmpty(bizLogs)) {
            return true;
        }
        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(appContext, new BizLogSendRequest());
        requestBody.setBizLogs(bizLogs);

        RpcCommand request = RpcCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            RpcCommand response = rpcClient.invokeSync(request);
            if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                // success
                return true;
            }
        } catch (JobTrackerNotFoundException ignored) {
        }
        return false;
    }

}
