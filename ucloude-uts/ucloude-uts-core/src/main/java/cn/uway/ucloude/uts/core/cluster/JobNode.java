package cn.uway.ucloude.uts.core.cluster;

/**
 * 节点接口
 * @author uway
 *
 */
public interface JobNode {
	
	 /**
     * 启动节点
     */
    void start();

    /**
     * 停止节点
     */
    void stop();

    /**
     * destroy
     */
    void destroy();
}
