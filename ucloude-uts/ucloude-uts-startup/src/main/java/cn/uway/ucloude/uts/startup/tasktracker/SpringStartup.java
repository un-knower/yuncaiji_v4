package cn.uway.ucloude.uts.startup.tasktracker;


import org.springframework.context.ApplicationContext;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;

/**
 * @author magic.s.g.xie
 */
public class SpringStartup {
	
	
    @SuppressWarnings("resource")
    public static TaskTracker start(TaskTrackerCfg cfg) {

        

        String[] springXmlPaths = cfg.getSpringXmlPaths();

        String[] paths;

        if (springXmlPaths != null) {
            paths = new String[springXmlPaths.length + 1];
            paths[0] = "classpath:spring/ucloude-uts-tasktracker-startup.xml";
            System.arraycopy(springXmlPaths, 0, paths, 1, springXmlPaths.length);
        } else {
            paths = new String[]{"classpath*:spring/ucloude-uts-tasktracker-startup.xml"};
        }

        ApplicationContext context = new UTSXmlApplicationContext(paths);
        return (TaskTracker) context.getBean("utsTaskTracker");
    }

}
