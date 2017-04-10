package cn.uway.ucloude.uts.tasktracker.runner;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

public class DefaultRunnerFactory implements RunnerFactory {
	private static final ILogger LOGGER = LoggerManager.getLogger(DefaultRunnerFactory.class);
	
	private TaskTrackerContext  context;
	
	public DefaultRunnerFactory(TaskTrackerContext context) {
		super();
		this.context = context;
	}

	@Override
	public JobRunner newRunner() {
		// TODO Auto-generated method stub
		try {
            return (JobRunner) context.getJobRunnerClass().newInstance();
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
	}
	
	

}
