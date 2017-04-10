package cn.uway.ucloude.uts.core.cluster;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.ucloude.common.ConcurrentHashSet;
import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.ListUtils;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.listener.NodeChangeListener;

public class SubscribedNodeManager implements NodeChangeListener {

	private static final ILogger logger = LoggerManager.getLogger(SubscribedNodeManager.class);
	private final ConcurrentHashMap<NodeType, Set<Node>> NODES = new ConcurrentHashMap<NodeType, Set<Node>>();

	private UtsContext context;

	public SubscribedNodeManager(UtsContext context) {
		this.context = context;
	}

	@Override
	public void addNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(nodes)) {
			return;
		}
		for (Node node : nodes) {
			addNode(node);
		}
	}

	private void addNode(Node node) {
		_addNode(node);
	}

	private void _addNode(Node node) {
		Set<Node> nodeSet = NODES.get(node.getNodeType());
		if (CollectionUtil.isEmpty(nodeSet)) {
			nodeSet = new ConcurrentHashSet<Node>();
			Set<Node> oldNodeList = NODES.putIfAbsent(node.getNodeType(), nodeSet);
			if (oldNodeList != null)
				nodeSet = oldNodeList;
		}
		nodeSet.add(node);
		EventInfo eventInfo = new EventInfo(EcTopic.NODE_ADD);
		eventInfo.setParam("node", node);
		context.getEventCenter().publishAsync(eventInfo);
		logger.info("Add {}", node);
	}

	public List<Node> getNodeList(final NodeType nodeType, final String nodeGroup) {
		Set<Node> nodes = NODES.get(nodeType);
		return ListUtils.filter(CollectionUtil.setToList(nodes), new ListUtils.Filter<Node>() {

			@Override
			public boolean filter(Node node) {
				// TODO Auto-generated method stub
				return node.getGroup().equals(nodeGroup);
			}

		});
	}

	public List<Node> getNodeList(NodeType nodeType) {
		return CollectionUtil.setToList(NODES.get(nodeType));
	}

	@Override
	public void removeNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(nodes)) {
			return;
		}
		for (Node node : nodes) {
			removeNode(node);
		}
	}
	
	private void removeNode(Node delNode){
		Set<Node> nodeSet = NODES.get(delNode.getNodeType());
		if(CollectionUtil.isNotEmpty(nodeSet)){
			for(Node node:nodeSet){
				if(node.getIdentity().equals(delNode.getIdentity())){
					nodeSet.remove(node);
					EventInfo eventInfo = new EventInfo(EcTopic.NODE_REMOVE);
                    eventInfo.setParam("node", node);
                    context.getEventCenter().publishSync(eventInfo);
                    logger.info("Remove {}", node);
				}
			}
		}
	}

}
