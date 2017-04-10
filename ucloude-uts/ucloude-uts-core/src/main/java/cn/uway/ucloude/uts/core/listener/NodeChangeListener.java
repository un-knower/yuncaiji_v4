package cn.uway.ucloude.uts.core.listener;

import java.util.List;

import cn.uway.ucloude.uts.core.cluster.Node;

/**
 * 节点变化监听器
 * @author uway
 *
 */
public interface NodeChangeListener {
	/**
     * 添加节点
     *
     * @param nodes 节点列表
     */
    public void addNodes(List<Node> nodes);

    /**
     * 移除节点
     * @param nodes 节点列表
     */
    public void removeNodes(List<Node> nodes);

}
