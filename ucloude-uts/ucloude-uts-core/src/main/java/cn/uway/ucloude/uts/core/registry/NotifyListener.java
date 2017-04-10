package cn.uway.ucloude.uts.core.registry;

import java.util.List;

import cn.uway.ucloude.uts.core.cluster.Node;

public interface NotifyListener {
	
	void notify(NotifyEvent event, List<Node> nodes);
}
