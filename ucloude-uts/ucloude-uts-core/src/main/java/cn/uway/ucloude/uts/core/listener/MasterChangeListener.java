package cn.uway.ucloude.uts.core.listener;

import cn.uway.ucloude.uts.core.cluster.Node;

/**
 * Master 节点变化 监听器
 * @author uway
 *
 */
public interface MasterChangeListener {
	/**
     * 节点变化 监听
     * @param master master节点
     * @param isMaster 表示当前节点是不是master节点
     */
    public void change(Node master, boolean isMaster);
}
