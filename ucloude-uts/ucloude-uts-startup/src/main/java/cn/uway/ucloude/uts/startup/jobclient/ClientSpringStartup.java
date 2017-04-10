package cn.uway.ucloude.uts.startup.jobclient;

import org.springframework.context.ApplicationContext;

import cn.uway.ucloude.uts.jobclient.JobClient;
import cn.uway.ucloude.uts.startup.tasktracker.UTSXmlApplicationContext;

public class ClientSpringStartup {


	public static ApplicationContext initialize(JobClientCfg cfg){
		String[] springXmlPaths = cfg.getSpringXmlPaths();

        String[] paths;

        if (springXmlPaths != null) {
            paths = new String[springXmlPaths.length + 1];
            paths[0] = "classpath:spring/ucloude-uts-jobclient.xml";
            System.arraycopy(springXmlPaths, 0, paths, 1, springXmlPaths.length);
        } else {
            paths = new String[]{"classpath*:spring/ucloude-uts-jobclient.xml"};
        }

       return new UTSXmlApplicationContext(paths);

	}
}
