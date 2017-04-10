package cn.uway.ucloude.ec;

import cn.uway.ucloude.container.SPI;

/**
 * 事件中心接口
 * @author magic.s.g.xie
 *
 */
@SPI(key = "event.center", dftValue = "injvm")
public interface IEventCenter {
	 /**
     * 订阅主题
     */
    public void subscribe(EventSubscriber subscriber, String... topics);

    /**
     * 取消订阅主题
     */
    public void unSubscribe(String topic, EventSubscriber subscriber);

    /**
     * 同步发布主题消息
     */
    public void publishSync(EventInfo eventInfo);

    /**
     * 异步发送主题消息
     */
    public void publishAsync(EventInfo eventInfo);
}
