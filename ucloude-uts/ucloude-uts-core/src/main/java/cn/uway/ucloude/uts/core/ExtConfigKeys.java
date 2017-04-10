package cn.uway.ucloude.uts.core;

import cn.uway.ucloude.common.UCloudeConstants;

public interface ExtConfigKeys {
	/**
	 * 任务队列, 选择自己实现的值
	 */
	String JOB_QUEUE = "job.queue";

    /**
     * 所有端: 链接zk的客户端, 可选值 zkclient, curator, uts 默认 zkclient
     */
    String ZK_CLIENT_KEY = "zk.client";
    
    /**
     * JobTracker端:设置任务最多重试次数, 默认10次
     */
    String JOB_MAX_RETRY_TIMES = "job.max.retry.times";
    
    /**
     * 注册中心失败事件重试事件
     */
    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
    String REDIS_SESSION_TIMEOUT = "redis.session.timeout";
    
	/**
	 * JobTracker端: 任务biz logger 可选值 c，默认DataBase
	 */
	String JOB_LOGGER = "job.logger";

	// 是否延迟批量刷盘日志, 如果启用，采用队列的方式批量将日志刷盘(在应用关闭的时候，可能会造成日志丢失) , 默认关闭
	String LAZY_JOB_LOGGER = "lazy.job.logger";

	/**
	 * JobClient,JobTracker,TaskTracker端: Java 编译器, 可选值 jdk, javassist, 默认
	 * javassist
	 */
	String COMPILER = "java.compiler";

	/**
	 * UTS 内部使用的json, 默认fastjson
	 */
	String UTS_JSON = "uts.json";

	String UTS_LOGGER = "uts.logger";

	/**
	 * JobClient,JobTracker,TaskTracker, Monitor端: Http Cmd 端口
	 */
	String HTTP_CMD_PORT = "uts.http.cmd.port";

	/**
	 * JobClient,JobTracker,TaskTracker端: 远程通讯请求处理线程数量, 默认 32 +
	 * AVAILABLE_PROCESSOR * 5
	 */
	String PROCESSOR_THREAD = "uts.job.processor.thread";

	/**
	 * JobClient,TaskTracker端: 选择JobTracker的负载均衡算法
	 */
	String JOB_TRACKER_SELECT_LOADBALANCE = "jobtracker.select.loadbalance";

	String LOADBALANCE = "loadbalance";

	int JOB_TRACKER_DEFAULT_LISTEN_PORT = 35001;

	String M_STAT_REPORTER_CLOSED = "mStatReporterClosed";

	/**
	 * JobClient,JobTracker,TaskTracker端: 向monitor汇报数据间隔
	 */
	String UTS_MONITOR_REPORT_INTERVAL = "uts.monitor.report.interval";

	/**
	 * JobClient,JobTracker,TaskTracker端: 各个节点选择连接Monitor的负载均衡算法
	 */
	String MONITOR_SELECT_LOADBALANCE = "monitor.select.loadbalance";

	String JOB_TRACKER_PUSHER_THREAD_NUM = "uts.job.tracker.pusher.thread.num";

	/**
	 * JobTracker端: 不依赖上周期任务的生成调度时间, 默认10分钟 (不建议自己设置)
	 */
	String JOB_TRACKER_NON_RELYON_PREV_CYCLE_JOB_SCHEDULER_INTERVAL_MINUTE = "jobtracker.nonRelyOnPrevCycleJob.schedule.interval.minute";

	String JOB_TRACKER_PUSH_BATCH_SIZE = "uts.job.tracker.push.batch.size";

	String FIRST_FIRE_TIME = "__UTS_Repeat_Job_First_Fire_Time";

	// 执行的序号
	String EXE_SEQ_ID = "__UTS_Seq_Id";
	/**
	 * JobTracker端: 是否开启远程请求最大QPS限流
	 */
	String JOB_TRACKER_RPC_REQ_LIMIT_ENABLE = "rpc.req.limit.enable";

	/**
	 * JobTracker端: 远程请求最大QPS限流, 默认 5000
	 */
	String JOB_TRACKER_RPC_REQ_LIMIT_MAX_QPS = "rpc.req.limit.maxQPS";

	/**
	 * JobTracker端: 远程请求的lock获取 timeout, 默认 50毫秒 (不建议自己设置)
	 */
	String JOB_TRACKER_RPC_REQ_LIMIT_ACQUIRE_TIMEOUT = "rpc.req.limit.acquire.timeout";

	String JOB_RETRY_TIME_GENERATOR = "jobtracker.retry.time.generator";

	/**
	 * JobTracker端: 任务重试时间间隔, 默认 30s
	 */
	String JOB_TRACKER_JOB_RETRY_INTERVAL_MILLIS = "jobtracker.job.retry.interval.millis";

	/**
	 * JobTracker端: 选择jobClient的负载均衡算法
	 */
	String JOB_CLIENT_SELECT_LOADBALANCE = "jobclient.select.loadbalance";

	/**
	 * JobTracker端: 正在执行任务队列中死任务的检查频率
	 */
	String JOB_TRACKER_EXECUTING_JOB_FIX_CHECK_INTERVAL_SECONDS = "jobtracker.executing.job.fix.check.interval.seconds";

	/**
	 * JobTracker端: 正在执行任务修复死任务检查的时间限制(不建议自己设置)
	 */
	String JOB_TRACKER_EXECUTING_JOB_FIX_DEADLINE_SECONDS = "jobtracker.executing.job.fix.deadline.seconds";

	/**
	 * JobTracker端: 修改ExecutingJobQueue死任务的时候, 等待等待完成任务的时间
	 */
	String JOB_TRACKER_FIX_EXECUTING_JOB_WAITING_MILLS = "jobtracker.fix.executing.job.waiting.mills";

	/**
	 * TaskTracker端: Pull 任务频率(秒) , 默认 1s(不建议自己设置)
	 */
	String JOB_PULL_FREQUENCY = "job.pull.frequency";

	/**
	 * TaskTracker端: 是否启用TaskTracker端的负载均衡, 默认关闭
	 */
	String LB_MACHINE_RES_CHECK_ENABLE = "lb.machine.res.check.enable";

	/**
	 * TaskTracker端: 负载均衡, 最大内存使用率,超过该使用率,停止pull任务, 默认 0.9(90%)
	 */
	String LB_MEMORY_USED_RATE_MAX = "lb.memoryUsedRate.max";

	/**
	 * TaskTracker端: 负载均衡, 最大cpu使用率,超过该使用率,停止pull任务, 默认 0.9 (90%)
	 */
	String LB_CPU_USED_RATE_MAX = "lb.cpuUsedRate.max";
	/**
	 * TaskTracker端: 是否开启网络隔离, 自杀程序, TaskTracker超过一定时间断线JobTracker，自动停止当前的所有任务
	 */
	String TASK_TRACKER_STOP_WORKING_ENABLE = "tasktracker.stop.working.enable";
	/**
	 * JobClient端: 提交并发请求size
	 */
	String JOB_SUBMIT_MAX_QPS = "job.submit.maxQPS";

	/**
	 * JobClient端: 提交任务获取 lock的 timeout (毫秒)
	 */
	String JOB_SUBMIT_LOCK_ACQUIRE_TIMEOUT = "job.submit.lock.acquire.timeout";

	/**
	 * Admin和Monitor端: 使用的数据存储,目前只有mysql实现
	 */
	String ACCESS_DB = "uts.admin.access.db";
	
	/**
     * JobTracker端: 最大 Job preload 的 size , 默认 300
     */
    String JOB_TRACKER_PRELOADER_SIZE = "job.preloader.size";
    /**
     * JobTracker端: Job preload 的 阀值  默认 0.2 (20%)
     */
    String JOB_TRACKER_PRELOADER_FACTOR = "job.preloader.factor";
    /**
     * JobTracker端: Job preload 信号检测频率
     */
    String JOB_TRACKER_PRELOADER_SIGNAL_CHECK_INTERVAL = "job.preloader.signal.check.interval";

	/**
	 * 配置key对应的默认值
	 */
	int DEFAULT_JOB_TRACKER_PUSHER_THREAD_NUM = 32 + UCloudeConstants.AVAILABLE_PROCESSOR * 5;

	int DEFAULT_JOB_TRACKER_PUSH_BATCH_SIZE = 10;

	// TaskTracker 离线(网络隔离)时间 10s，超过10s，自动停止当前执行任务
	long DEFAULT_TASK_TRACKER_OFFLINE_LIMIT_MILLIS = 10 * 1000;

	String MACHINE_RES_ENOUGH = "__UTS.INNER.MACHINE.RES.ENOUGH";

	int LATCH_TIMEOUT_MILLIS = 60 * 1000; // 60s

	String ONCE = "__UTS_ONCE";

	String IS_RETRY_JOB = "__UTS_Is_Retry_Job";

	int DEFAULT_JOB_PULL_FREQUENCY = 1;

	int DEFAULT_JOB_SUBMIT_MAX_QPS = 500;

	String CONNECTION_KEY = "uts_core";
	
	String ADMIN_ID_PREFIX = "UTS_admin_";
	


    String OLD_PRIORITY = "__UTS_Tmp_Old_Priority";
    
    /**
     * 重试周期
     */
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    int DEFAULT_JOB_MAX_RETRY_TIMES = 10;
    
    String CONF_TRACKER_PATH="uts.tasktracker.cfg.path";
    String CONF_JOBCLEINT_PATH="uts.jobclient.cfg.path";
}
