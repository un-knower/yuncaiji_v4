package cn.uway.ucloude.uts.core.support;

import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.EventSubscriber;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.ec.IObserver;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.Callable;
import cn.uway.ucloude.uts.core.EcTopic;

public class NodeShutdownHook {
	private static final ILogger LOGGER = LoggerManager.getLogger(NodeShutdownHook.class);

    public static void registerHook(IEventCenter center, String identity,final String name, final Callable callback) {
    	center.subscribe(new EventSubscriber(name + "_" + identity, new IObserver() {
            @Override
            public void onObserved(EventInfo eventInfo) {
                if (callback != null) {
                    try {
                        callback.call();
                    } catch (Exception e) {
                        LOGGER.warn("Call shutdown hook {} error", name, e);
                    }
                }
            }
        }), EcTopic.NODE_SHUT_DOWN);
    }
}
