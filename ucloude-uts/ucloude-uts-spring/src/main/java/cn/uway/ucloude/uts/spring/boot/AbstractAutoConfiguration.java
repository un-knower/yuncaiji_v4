package cn.uway.ucloude.uts.spring.boot;



import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.cluster.AbstractJobNode;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;
import cn.uway.ucloude.uts.spring.boot.annotation.MasterNodeListener;

/**
 * @author magic.s.g.xie
 */
public abstract class AbstractAutoConfiguration implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final ILogger LOGGER = LoggerManager.getLogger(AbstractAutoConfiguration.class);
    protected ApplicationContext applicationContext;

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        initJobNode();
        injectMasterChangeListeners();
        getJobNode().start();
    }

    @Override
    public final void destroy() throws Exception {
        if (getJobNode() != null) {
            getJobNode().stop();
        }
    }

    private void injectMasterChangeListeners() {
        Map<String, Object> listeners = applicationContext.getBeansWithAnnotation(MasterNodeListener.class);
        if (CollectionUtil.isNotEmpty(listeners)) {
            for (Map.Entry<String, Object> entry : listeners.entrySet()) {
                Object listener = entry.getValue();
                MasterNodeListener annotation = listener.getClass().getAnnotation(MasterNodeListener.class);
                NodeType[] nodeTypes = annotation.nodeTypes();
                boolean ok = false;
                if (nodeTypes != null && nodeTypes.length > 0) {
                    for (NodeType type : nodeTypes) {
                        if (type == nodeType()) {
                            ok = true;
                            break;
                        }
                    }
                } else {
                    ok = true;
                }
                if (!ok) {
                    continue;
                }
                if (listener instanceof MasterChangeListener) {
                    getJobNode().addMasterChangeListener((MasterChangeListener) listener);
                } else {
                    LOGGER.warn(entry.getKey() + "  is not instance of " + MasterChangeListener.class.getName());
                }
            }
        }
    }

    protected abstract void initJobNode();

    protected abstract NodeType nodeType();

    protected abstract AbstractJobNode getJobNode();
}
