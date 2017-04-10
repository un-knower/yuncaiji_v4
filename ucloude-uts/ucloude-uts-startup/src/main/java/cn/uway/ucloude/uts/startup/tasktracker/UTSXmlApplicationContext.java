package cn.uway.ucloude.uts.startup.tasktracker;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author magic.s.g.xie
 */
public class UTSXmlApplicationContext extends AbstractXmlApplicationContext {

    private static final ILogger LOGGER = LoggerManager.getLogger(UTSXmlApplicationContext.class);
    private Resource[] configResources;
    
    


    public UTSXmlApplicationContext(String[] paths) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resourceList = new ArrayList<Resource>();
        if (paths != null && paths.length > 0) {
            for (String path : paths) {
                try {
                    Resource[] resources = resolver.getResources(path);
                    if (resources != null && resources.length > 0) {
                        Collections.addAll(resourceList, resources);
                    }
                } catch (IOException e) {
                    LOGGER.error("resolve resource error: [path={}]", path, e);
                }
            }
        }

        configResources = new Resource[resourceList.size()];
        resourceList.toArray(configResources);

        refresh();
    }

    @Override
    protected Resource[] getConfigResources() {
        return configResources;
    }
}
