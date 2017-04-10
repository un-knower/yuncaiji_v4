package cn.uway.ucloude.uts.tasktracker.monitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.EventSubscriber;
import cn.uway.ucloude.ec.IObserver;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

/**
 * 当TaskTracker和JobTracker断开超过了一段时间，TaskTracker立即停止当前的所有任务
 * @author uway
 *
 */
public class StopWorkingMonitor {
	private static final ILogger LOGGER = LoggerManager.getLogger(StopWorkingMonitor.class);
	private TaskTrackerContext context;
	
	private AtomicBoolean start = new AtomicBoolean(false);
	private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1, new NamedThreadFactory("uts-stopworking-monitor", true));
	private ScheduledFuture<?> scheduledFuture;
	private String ecSubscriberName = StopWorkingMonitor.class.getSimpleName();
	private EventSubscriber eventSubscriber;
	
	private Long offlineTimestamp = null;

	public StopWorkingMonitor(TaskTrackerContext context) {
		super();
		this.context = context;
	}
	
	public void start(){
		if(start.compareAndSet(false, true)){
			eventSubscriber = new EventSubscriber(ecSubscriberName, new IObserver(){

				@Override
				public void onObserved(EventInfo eventInfo) {
					// TODO Auto-generated method stub
					offlineTimestamp = null;
				}
				
			});
			context.getEventCenter().subscribe(eventSubscriber, EcTopic.JOB_TRACKER_AVAILABLE);
			scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (offlineTimestamp == null && context.getRpcClient().isServerEnable()) {
                        offlineTimestamp = SystemClock.now();
                    }

                    if (offlineTimestamp != null &&
                            SystemClock.now() - offlineTimestamp > ExtConfigKeys.DEFAULT_TASK_TRACKER_OFFLINE_LIMIT_MILLIS) {
                        // 停止所有任务
                        context.getRunnerPool().stopWorking();
                        offlineTimestamp = null;
                    }
				}
				
			}, 3, 3, TimeUnit.SECONDS);
		}
	}
	
	 public void stop() {
	        try {
	            if (start.compareAndSet(true, false)) {
	                scheduledFuture.cancel(true);
	                SCHEDULED_CHECKER.shutdown();

	                context.getEventCenter().unSubscribe(EcTopic.JOB_TRACKER_AVAILABLE, eventSubscriber);

	                LOGGER.info("stop succeed ");
	            }
	        } catch (Throwable t) {
	            LOGGER.error("stop failed ", t);
	        }
	    }

}
