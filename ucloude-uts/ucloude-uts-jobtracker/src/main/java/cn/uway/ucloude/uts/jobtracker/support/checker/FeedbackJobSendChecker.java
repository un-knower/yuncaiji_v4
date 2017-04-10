package cn.uway.ucloude.uts.jobtracker.support.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;
import cn.uway.ucloude.uts.jobtracker.domain.JobClientNode;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifier;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifyHandler;

/**
 * 用来检查 执行完成的任务, 发送给客户端失败的 由master节点来做,单例
 * @author uway
 *
 */
public class FeedbackJobSendChecker {
	private static final ILogger LOGGER = LoggerManager.getLogger(FeedbackJobSendChecker.class);

    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("UTS-FeedbackJobSend-Executor", true));
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    private ClientNotifier clientNotifier;
    private JobTrackerContext context;

    /**
     * 是否已经启动
     */
    @SuppressWarnings("unused")
	private boolean isStart() {
        return start.get();
    }

    public FeedbackJobSendChecker(final JobTrackerContext context) {
        this.context = context;

        clientNotifier = new ClientNotifier(context, new ClientNotifyHandler<JobRunResultWrapper>() {
            @Override
            public void handleSuccess(List<JobRunResultWrapper> jobResults) {
                for (JobRunResultWrapper jobResult : jobResults) {
                    String submitNodeGroup = jobResult.getJobMeta().getJob().getSubmitNodeGroup();
                    context.getJobFeedbackQueue().remove(submitNodeGroup, jobResult.getId());
                }
            }

            @Override
            public void handleFailed(List<JobRunResultWrapper> jobResults) {
                // do nothing
            }
        });
    }

    /**
     * 启动
     */
    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                scheduledFuture = RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runner()
                        , 30, 30, TimeUnit.SECONDS);
            }
            LOGGER.info("Feedback job checker started!");

        } catch (Throwable t) {
            LOGGER.error("Feedback job checker start failed!", t);
        }
    }

    /**
     * 停止
     */
    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                RETRY_EXECUTOR_SERVICE.shutdown();
                LOGGER.info("Feedback job checker stopped!");
            }
        } catch (Throwable t) {
            LOGGER.error("Feedback job checker stop failed!", t);
        }
    }

    private volatile boolean isRunning = false;

    private class Runner implements Runnable {
        @Override
        public void run() {
            try {
                // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                if (!context.getRegistryStatMonitor().isAvailable()) {
                    return;
                }
                if (isRunning) {
                    return;
                }
                isRunning = true;

                Set<String> taskTrackerNodeGroups = context.getJobClientManager().getNodeGroups();
                if (CollectionUtil.isEmpty(taskTrackerNodeGroups)) {
                    return;
                }

                for (String taskTrackerNodeGroup : taskTrackerNodeGroups) {
                    check(taskTrackerNodeGroup);
                }

            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            } finally {
                isRunning = false;
            }
        }

        private void check(String jobClientNodeGroup) {

            // check that node group job client
            JobClientNode jobClientNode = context.getJobClientManager().getAvailableJobClient(jobClientNodeGroup);
            if (jobClientNode == null) {
                return;
            }

            long count = context.getJobFeedbackQueue().getCount(jobClientNodeGroup);
            if (count == 0) {
                return;
            }

            LOGGER.info("{} jobs need to feedback.", count);
            // 检测是否有可用的客户端

            List<JobFeedbackPo> jobFeedbackPos;
            int limit = 5;
            do {
                jobFeedbackPos = context.getJobFeedbackQueue().fetchTop(jobClientNodeGroup, limit);
                if (CollectionUtil.isEmpty(jobFeedbackPos)) {
                    return;
                }
                List<JobRunResultWrapper> jobResults = new ArrayList<JobRunResultWrapper>(jobFeedbackPos.size());
                for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
                    // 判断是否是过时的数据，如果是，那么移除
                    if (context.getOldDataHandler() == null ||
                            (!context.getOldDataHandler().handle(context.getJobFeedbackQueue(), jobFeedbackPo, jobFeedbackPo))) {
                        jobResults.add(new JobRunResultWrapper(jobFeedbackPo.getId(), jobFeedbackPo.getJobRunResult()));
                    }
                }
                // 返回发送成功的个数
                int sentSize = clientNotifier.send(jobResults);

                LOGGER.info("Send to client: {} success, {} failed.", sentSize, jobResults.size() - sentSize);
            } while (jobFeedbackPos.size() > 0);
        }
    }

    private class JobRunResultWrapper extends JobRunResult {
		
    	
		/**
		 * 
		 */
		private static final long serialVersionUID = 9221845157862328600L;
		private String id;

        public String getId() {
            return id;
        }

        public JobRunResultWrapper(String id, JobRunResult result) {
            this.id = id;
            setJobMeta(result.getJobMeta());
            setMsg(result.getMsg());
            setAction(result.getAction());
            setTime(result.getTime());
        }
    }
}
