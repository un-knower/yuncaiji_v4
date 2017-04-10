package cn.uway.ucloude.uts.tasktracker.support;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.EventSubscriber;
import cn.uway.ucloude.ec.IObserver;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.exception.RpcCommandFieldCheckException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.exception.JobTrackerNotFoundException;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.JobPullRequest;
import cn.uway.ucloude.uts.jvmmonitor.JVMConstants;
import cn.uway.ucloude.uts.jvmmonitor.JVMMonitor;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

/**
 * 用来向JobTracker去取任务
 * 1. 会订阅JobTracker的可用,不可用消息主题的订阅
 * 2. 只有当JobTracker可用的时候才会去Pull任务
 * 3. Pull只是会给JobTracker发送一个通知
 *
 * @author uway
 */
public class JobPullMachine {
	 private static final ILogger LOGGER = LoggerManager.getLogger(JobPullMachine.class.getSimpleName());

	    // 定时检查TaskTracker是否有空闲的线程，如果有，那么向JobTracker发起任务pull请求
	    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("UTS-JobPullMachine-Executor", true));
	    private ScheduledFuture<?> scheduledFuture;
	    private AtomicBoolean start = new AtomicBoolean(false);
	    private TaskTrackerContext context;
	    private Runnable worker;
	    private int jobPullFrequency;
	    // 是否启用机器资源检查
	    private boolean machineResCheckEnable = false;

	    public JobPullMachine(final TaskTrackerContext appContext) {
	        this.context = appContext;
	        this.jobPullFrequency = appContext.getConfiguration().getParameter(ExtConfigKeys.JOB_PULL_FREQUENCY, ExtConfigKeys.DEFAULT_JOB_PULL_FREQUENCY);

	        this.machineResCheckEnable = appContext.getConfiguration().getParameter(ExtConfigKeys.LB_MACHINE_RES_CHECK_ENABLE, false);
	        LOGGER.info("jvm event center");
	        appContext.getEventCenter().subscribe(
	                new EventSubscriber(JobPullMachine.class.getSimpleName().concat(appContext.getConfiguration().getIdentity()),
	                        new IObserver() {
	                            @Override
	                            public void onObserved(EventInfo eventInfo) {
	                            	 LOGGER.info("EcTopic.JOB_TRACKER_AVAILABLE");
	                                if (EcTopic.JOB_TRACKER_AVAILABLE.equals(eventInfo.getTopic())) {
	                                    // JobTracker 可用了
	                                    start();
	                                } else if (EcTopic.NO_JOB_TRACKER_AVAILABLE.equals(eventInfo.getTopic())) {
	                                    stop();
	                                }
	                            }
	                        }), EcTopic.JOB_TRACKER_AVAILABLE, EcTopic.NO_JOB_TRACKER_AVAILABLE);
	        this.worker = new Runnable() {
	            @Override
	            public void run() {
	            	//LOGGER.info("job run... ");
	                try {
	                    if (!start.get()) {
	                        return;
	                    }
	                    if (!isMachineResEnough()) {
	                        // 如果机器资源不足,那么不去取任务
	                        return;
	                    }
	                    sendRequest();
	                } catch (Exception e) {
	                    LOGGER.error("Job pull machine run error!", e);
	                }
	            }
	        };
	    }

	    private void start() {
	        try {
	            if (start.compareAndSet(false, true)) {
	                if (scheduledFuture == null) {
	                    scheduledFuture = executorService.scheduleWithFixedDelay(worker, jobPullFrequency * 1000, jobPullFrequency * 1000, TimeUnit.MILLISECONDS);
	                }
	                LOGGER.info("Start Job pull machine success!");
	            }
	        } catch (Throwable t) {
	            LOGGER.error("Start Job pull machine failed!", t);
	        }
	    }

	    private void stop() {
	        try {
	            if (start.compareAndSet(true, false)) {
//	                scheduledFuture.cancel(true);
//	                executorService.shutdown();
	                LOGGER.info("Stop Job pull machine success!");
	            }
	        } catch (Throwable t) {
	            LOGGER.error("Stop Job pull machine failed!", t);
	        }
	    }

	    /**
	     * 发送Job pull 请求
	     */
	    private void sendRequest() throws RpcCommandFieldCheckException {
	        int availableThreads = context.getRunnerPool().getAvailablePoolSize();
	        if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("current availableThreads:{}", availableThreads);
	        }
	        if (availableThreads == 0) {
	            return;
	        }
	        JobPullRequest requestBody = context.getCommandBodyWrapper().wrapper(new JobPullRequest());
	        requestBody.setAvailableThreads(availableThreads);
	        RpcCommand request = RpcCommand.createRequestCommand(JobProtos.RequestCode.JOB_PULL.code(), requestBody);

	        try {
	            RpcCommand responseCommand = context.getRpcClient().invokeSync(request);
	            if (responseCommand == null) {
	                LOGGER.warn("Job pull request failed! response command is null!");
	                return;
	            }
	            if (JobProtos.ResponseCode.JOB_PULL_SUCCESS.code() == responseCommand.getCode()) {
	                if (LOGGER.isDebugEnabled()) {
	                    LOGGER.debug("Job pull request success!");
	                }
	                return;
	            }
	            LOGGER.warn("Job pull request failed! response command is null!");
	        } catch (JobTrackerNotFoundException e) {
	            LOGGER.warn("no job tracker available!");
	        }
	    }

	    /**
	     * 查看当前机器资源是否足够
	     */
	    private boolean isMachineResEnough() {

	        if (!machineResCheckEnable) {
	            // 如果没有启用,直接返回
	            return true;
	        }

	        boolean enough = true;

	        try {
	            // 1. Cpu usage
	            Double maxCpuTimeRate = context.getConfiguration().getParameter(ExtConfigKeys.LB_CPU_USED_RATE_MAX, 90d);
	            Object processCpuTimeRate = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_THREAD_NAME, "ProcessCpuTimeRate");
	            if (processCpuTimeRate != null) {
	                Double cpuRate = Double.valueOf(processCpuTimeRate.toString()) / (UCloudeConstants.AVAILABLE_PROCESSOR * 1.0);
	                if (cpuRate >= maxCpuTimeRate) {
	                    LOGGER.info("Pause Pull, CPU USAGE is " + String.format("%.2f", cpuRate) + "% >= " + String.format("%.2f", maxCpuTimeRate) + "%");
	                    enough = false;
	                    return false;
	                }
	            }

	            // 2. Memory usage
	            Double maxMemoryUsedRate = context.getConfiguration().getParameter(ExtConfigKeys.LB_MEMORY_USED_RATE_MAX, 90d);
	            Runtime runtime = Runtime.getRuntime();
	            long maxMemory = runtime.maxMemory();
	            long usedMemory = runtime.totalMemory() - runtime.freeMemory();

	            Double memoryUsedRate = new BigDecimal(usedMemory / (maxMemory*1.0), new MathContext(4)).doubleValue();

	            if (memoryUsedRate >= maxMemoryUsedRate) {
	                LOGGER.info("Pause Pull, MEMORY USAGE is " + memoryUsedRate + " >= " + maxMemoryUsedRate);
	                enough = false;
	                return false;
	            }
	            enough = true;
	            return true;
	        } catch (Exception e) {
	            LOGGER.warn("Check Machine Resource error", e);
	            return true;
	        } finally {
	            Boolean machineResEnough = context.getConfiguration().getInternalData(ExtConfigKeys.MACHINE_RES_ENOUGH, true);
	            if (machineResEnough != enough) {
	            	context.getConfiguration().setInternalData(ExtConfigKeys.MACHINE_RES_ENOUGH, enough);
	            }
	        }
	    }

}
