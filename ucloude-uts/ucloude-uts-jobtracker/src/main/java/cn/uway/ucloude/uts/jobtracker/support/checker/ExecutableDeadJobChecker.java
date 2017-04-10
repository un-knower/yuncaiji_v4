package cn.uway.ucloude.uts.jobtracker.support.checker;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.cluster.JobClientManager;

/**
 * to fix the executable dead job
 * @author uway
 *
 */
public class ExecutableDeadJobChecker {
	private static final ILogger LOGGER = LoggerManager.getLogger(JobClientManager.class);
	
	// 1 分钟还锁着的，说明是有问题的
	private static final long MAX_TIME_OUT= 60*1000;
	
	private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("UTS-ExecutableJobQueue-Fix-Executor", true));
	
	private JobTrackerContext context;
	
	public ExecutableDeadJobChecker(JobTrackerContext context){
		this.context = context;
	}
	
	private AtomicBoolean start = new AtomicBoolean(false);
	
	private ScheduledFuture<?> scheduledFuture;
	
	
	public void start(){
		try{
			if(start.compareAndSet(false, true)){
				scheduledFuture  = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
                            // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                            if (!context.getRegistryStatMonitor().isAvailable()) {
                                return;
                            }
                            fix();
                        } catch (Throwable t) {
                            LOGGER.error(t.getMessage(), t);
                        }
					}
					
				}, 30, 60, TimeUnit.SECONDS); //3分钟执行一次
			}
			LOGGER.info("Executable dead job checker started!");
        } catch (Throwable t) {
            LOGGER.info("Executable dead job checker start failed!");
        }
	}
	
	 /**
     * fix the job that running is true and gmtModified too old
     */
    private void fix() {
        Set<String> nodeGroups = context.getTaskTrackerManager().getNodeGroups();
        if (CollectionUtil.isEmpty(nodeGroups)) {
            return;
        }
        for (String nodeGroup : nodeGroups) {
            List<JobPo> deadJobPo = context.getExecutableJobQueue().getDeadJob(nodeGroup, SystemClock.now() - MAX_TIME_OUT);
            if (CollectionUtil.isNotEmpty(deadJobPo)) {
                for (JobPo jobPo : deadJobPo) {
                    context.getExecutableJobQueue().resume(jobPo.getJobId(),jobPo.getTaskTrackerNodeGroup());
                    LOGGER.info("Fix executable job : {} ", JsonConvert.serialize(jobPo));
                }
            }
        }
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executable dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executable dead job checker stop failed!", t);
        }
    }
}
