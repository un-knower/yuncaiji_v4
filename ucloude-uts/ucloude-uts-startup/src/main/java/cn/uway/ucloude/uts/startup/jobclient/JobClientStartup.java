package cn.uway.ucloude.uts.startup.jobclient;

import org.springframework.context.ApplicationContext;

import cn.uway.ucloude.data.dataaccess.DataSourceProvider;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.jobclient.JobClient;
import cn.uway.ucloude.uts.jobclient.processor.JobSubmitHandler;
import cn.uway.ucloude.uts.startup.jobtracker.CfgException;

public class JobClientStartup {
    public static void main(String[] args) {
        String cfgPath = args[0];
        start(cfgPath);
    }
    
    public static void start(String cfgPath) {
    	  try {
    		DataSourceProvider.initialDataSource(cfgPath);
			JobClientCfg cfg = JobClientCfgLoader.load(cfgPath);
			System.setProperty(ExtConfigKeys.CONF_JOBCLEINT_PATH, cfgPath);
			
			final ApplicationContext appContext = ClientSpringStartup.initialize(cfg);
			final JobClient jobClient = (JobClient)appContext.getBean("jobClient");
			String[] handlerNames = appContext.getBeanNamesForType(JobSubmitHandler.class);
			if(handlerNames != null && handlerNames.length > 0)
			{
				for(String handlerName :handlerNames){
					JobSubmitHandler handler = appContext.getBean(handlerName, JobSubmitHandler.class);
					handler.setJobClient(jobClient);
					new Thread(handler).start();
				}
			}

		} catch (CfgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          
    }
}
