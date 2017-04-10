package cn.uway.ucloude.ec.injvm;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.uway.ucloude.common.ConcurrentHashSet;
import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.ec.EventSubscriber;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.thread.NamedThreadFactory;

public class InjvmEventCenter implements IEventCenter {
	private static final ILogger LOGGER = LoggerManager.getLogger(IEventCenter.class.getName());
	
	private final ConcurrentHashMap<String, Set<EventSubscriber>> ecMap =
            new ConcurrentHashMap<String, Set<EventSubscriber>>();

    private final ExecutorService executor = Executors.newFixedThreadPool(UCloudeConstants.AVAILABLE_PROCESSOR * 2, new NamedThreadFactory("UCloude-InjvmEventCenter-Executor", true));

	@Override
	public void subscribe(EventSubscriber subscriber, String... topics) {
		// TODO Auto-generated method stub
		for (String topic : topics) {
            Set<EventSubscriber> subscribers = ecMap.get(topic);
            if (subscribers == null) {
                subscribers = new ConcurrentHashSet<EventSubscriber>();
                Set<EventSubscriber> oldSubscribers = ecMap.putIfAbsent(topic, subscribers);
                if (oldSubscribers != null) {
                    subscribers = oldSubscribers;
                }
            }
            subscribers.add(subscriber);
        }
	}

	@Override
	public void unSubscribe(String topic, EventSubscriber subscriber) {
		// TODO Auto-generated method stub
		Set<EventSubscriber> subscribers = ecMap.get(topic);
        if (subscribers != null) {
            for (EventSubscriber eventSubscriber : subscribers) {
                if (eventSubscriber.getId().equals(subscriber.getId())) {
                    subscribers.remove(eventSubscriber);
                }
            }
        }
	}

	@Override
	public void publishSync(EventInfo eventInfo) {
		// TODO Auto-generated method stub
		Set<EventSubscriber> subscribers = ecMap.get(eventInfo.getTopic());
        if (subscribers != null) {
            for (EventSubscriber subscriber : subscribers) {
                eventInfo.setTopic(eventInfo.getTopic());
                try {
                    subscriber.getObserver().onObserved(eventInfo);
                } catch (Throwable t) {      // 防御性容错
                    LOGGER.error(" eventInfo:{}, subscriber:{}",
                            JsonConvert.serialize(eventInfo), JsonConvert.serialize(subscriber), t);
                }
            }
        }
	}

	@Override
	public void publishAsync(final EventInfo eventInfo) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                String topic = eventInfo.getTopic();

                Set<EventSubscriber> subscribers = ecMap.get(topic);
                if (subscribers != null) {
                    for (EventSubscriber subscriber : subscribers) {
                        try {
                            eventInfo.setTopic(topic);
                            subscriber.getObserver().onObserved(eventInfo);
                        } catch (Throwable t) {     // 防御性容错
                            LOGGER.error(" eventInfo:{}, subscriber:{}",
                            		JsonConvert.serialize(eventInfo), JsonConvert.serialize(subscriber), t);
                        }
                    }
                }
            }
        });
    }

}
