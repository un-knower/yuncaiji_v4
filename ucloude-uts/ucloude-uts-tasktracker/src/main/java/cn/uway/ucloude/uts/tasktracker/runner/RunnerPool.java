package cn.uway.ucloude.uts.tasktracker.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.EventSubscriber;
import cn.uway.ucloude.ec.IObserver;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;
import cn.uway.ucloude.uts.tasktracker.exception.NoAvailableJobRunnerException;

/**
 * 线程池管理
 * 
 * @author uway
 *
 */
public class RunnerPool {
	private final ILogger LOGGER = LoggerManager.getLogger(RunnerPool.class);
	private ThreadPoolExecutor threadPoolExecutor;

	private RunnerFactory runnerFactory;

	private TaskTrackerContext context;

	private RunningJobManager runningJobManager;
	
	

	public RunnerPool(final TaskTrackerContext context) {
		super();
		this.context = context;
		this.runningJobManager = new RunningJobManager();
		this.threadPoolExecutor = initThreadPoolExecutor();
		this.runnerFactory = context.getRunnerFactory();
		if(runnerFactory == null){
			runnerFactory = new DefaultRunnerFactory(context);
		}
		
		context.getEventCenter().subscribe(new EventSubscriber(context.getConfiguration().getIdentity(), new IObserver(){

			@Override
			public void onObserved(EventInfo eventInfo) {
				// TODO Auto-generated method stub
				setWorkThread(context.getConfiguration().getWorkThreads());
			}
			
		}),EcTopic.WORK_THREAD_CHANGE);
	}
	

    public RunningJobManager getRunningJobManager() {
        return runningJobManager;
    }
	
    public void setWorkThread(int workThread) {
        if (workThread == 0) {
            throw new IllegalArgumentException("workThread can not be zero!");
        }

        threadPoolExecutor.setMaximumPoolSize(workThread);
        threadPoolExecutor.setCorePoolSize(workThread);

        LOGGER.info("workThread update to {}", workThread);
    }
    
    public void execute(JobMeta jobMeta, RunnerCallback callback) throws NoAvailableJobRunnerException {
        try {
            threadPoolExecutor.execute(
                    new JobRunnerDelegate(context, jobMeta, callback));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Receive job success ! " + jobMeta);
            }
        } catch (RejectedExecutionException e) {
            LOGGER.warn("No more thread to run job .");
            throw new NoAvailableJobRunnerException(e);
        }
    }
	
    /**
     * 得到当前可用的线程数
     */
    public int getAvailablePoolSize() {
        return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
    }
    
    public void stopWorking() {
        try {
            threadPoolExecutor.shutdownNow();
            Thread.sleep(1000);
            threadPoolExecutor = initThreadPoolExecutor();
            LOGGER.info("stop working succeed ");
        } catch (Throwable t) {
            LOGGER.error("stop working failed ", t);
        }
    }

    public void shutDown() {
        try {
            threadPoolExecutor.shutdownNow();
            LOGGER.info("stop working succeed ");
        } catch (Throwable t) {
            LOGGER.error("stop working failed ", t);
        }
    }


	private ThreadPoolExecutor initThreadPoolExecutor(){
		int workThreads = context.getConfiguration().getWorkThreads();
		return new ThreadPoolExecutor(workThreads, workThreads, 30,TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				new NamedThreadFactory("JobRunnerPool"),
				new ThreadPoolExecutor.AbortPolicy());
	}
	
	 /**
     * 得到最大线程数
     */
    public int getWorkThread() {
        return threadPoolExecutor.getCorePoolSize();
    }

    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }



	/*
	 * 用来管理线程任务
	 */
	public class RunningJobManager {

		private final ConcurrentMap<String/* JobId */, JobRunnerDelegate> JOBS = new ConcurrentHashMap<String, JobRunnerDelegate>();

		public void in(String jobId, JobRunnerDelegate jobRunnerDelegate) {
			JOBS.putIfAbsent(jobId, jobRunnerDelegate);
		}

		public void out(String jobId) {
			JOBS.remove(jobId);
		}

		public boolean running(String jobId) {
			return JOBS.containsKey(jobId);
		}

		public List<String> getNotExists(List<String> jobIds) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Ask jobs: " + jobIds + " Running jobs ：" + JOBS.keySet());
			}
			
			List<String> notExistList = new ArrayList<String>();
			for (String jobId : jobIds) {
                if (!running(jobId)) {
                    notExistList.add(jobId);
                }
            }
            return notExistList;
		}
		
		 public void terminateJob(String jobId) {
	            JobRunnerDelegate jobRunnerDelegate = JOBS.get(jobId);
	            if (jobRunnerDelegate != null) {
	                try {
	                    jobRunnerDelegate.currentThread().interrupt();
	                } catch (Throwable e) {
	                    LOGGER.error("terminateJob [" + jobId + "]  error", e);
	                }
	            }
	        }

	}
}
