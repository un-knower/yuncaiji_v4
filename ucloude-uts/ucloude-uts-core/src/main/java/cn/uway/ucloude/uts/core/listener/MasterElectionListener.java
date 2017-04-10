package cn.uway.ucloude.uts.core.listener;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.Node;

/**
 * 用来监听 自己类型 节点的变化,用来选举master
 * @author uway
 *
 */
public class MasterElectionListener implements NodeChangeListener {

	private UtsContext context;
	
	public MasterElectionListener(UtsContext context){
		this.context = context;
	}
	
	private boolean isSameGroup(Node node){
		return node.getNodeType().equals(context.getConfiguration().getNodeType())
				&& node.getGroup().equals(context.getConfiguration().getNodeGroup());
	}
	
	
	@Override
	public void addNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if(CollectionUtil.isEmpty(nodes)){
			return;
		}
		// 只需要和当前节点相同的节点类型和组
		List<Node> groupNodes = new ArrayList<Node>();
		for(Node node:nodes){
			if(isSameGroup(node)){
				groupNodes.add(node);
			}
		}
		
		if(groupNodes.size() > 0){
			context.getMasterElector().addNodes(groupNodes);
		}
	}

	@Override
	public void removeNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(nodes)) {
            return;
        }
        // 只需要和当前节点相同的节点类型和组
        List<Node> groupNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isSameGroup(node)) {
                groupNodes.add(node);
            }
        }
        if (groupNodes.size() > 0) {
            context.getMasterElector().removeNode(groupNodes);
        }
	}

}
