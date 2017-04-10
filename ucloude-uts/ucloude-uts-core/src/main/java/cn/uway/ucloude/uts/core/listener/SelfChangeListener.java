package cn.uway.ucloude.uts.core.listener;

import java.util.List;

import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class SelfChangeListener implements NodeChangeListener {

	private UtsConfiguration config;
    private UtsContext context;
    
    public SelfChangeListener(UtsContext context) {
        this.config = context.getConfiguration();
        this.context = context;
    }

    private void change(Node node) {
        if (node.getIdentity().equals(config.getIdentity())) {
            // 是当前节点, 看看节点配置是否发生变化
            // 1. 看 threads 有没有改变 , 目前只有 TASK_TRACKER 对 threads起作用
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)
                    && (node.getThreads() != config.getWorkThreads())) {
                config.setWorkThreads(node.getThreads());
                context.getEventCenter().publishAsync(new EventInfo(EcTopic.WORK_THREAD_CHANGE));
            }

            // 2. 看 available 有没有改变
            if (node.isAvailable() != config.isAvailable()) {
                String topic = node.isAvailable() ? EcTopic.NODE_ENABLE : EcTopic.NODE_DISABLE;
                config.setAvailable(node.isAvailable());
                context.getEventCenter().publishAsync(new EventInfo(topic));
            }
        }
    }
    
	@Override
	public void addNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            change(node);
        }
	}

	@Override
	public void removeNodes(List<Node> nodes) {
		// TODO Auto-generated method stub

	}

}
