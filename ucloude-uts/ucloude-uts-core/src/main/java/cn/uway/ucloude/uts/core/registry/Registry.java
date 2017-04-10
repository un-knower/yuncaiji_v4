package cn.uway.ucloude.uts.core.registry;

import cn.uway.ucloude.uts.core.cluster.Node;

/**
 * 节点注册接口
 * @author uway
 *
 */
public interface Registry {
	/**
     * 节点注册
     */
    void register(Node node);

    /**
     * 节点 取消注册
     */
    void unregister(Node node);

    /**
     * 监听节点
     */
    void subscribe(Node node, NotifyListener listener);

    /**
     * 取消监听节点
     */
    void unsubscribe(Node node, NotifyListener listener);

    /**
     * 销毁
     */
    void destroy();
}
