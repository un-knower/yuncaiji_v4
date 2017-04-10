package cn.uway.ucloude.uts.jobtracker.support.listener;

import java.util.List;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.listener.NodeChangeListener;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobNodeChangeListener implements NodeChangeListener {


    private JobTrackerContext context;
	public JobNodeChangeListener(JobTrackerContext context){
		this.context = context;
	}
	
	@Override
	public void addNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                context.getTaskTrackerManager().addNode(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                context.getJobClientManager().addNode(node);
            }
        }
	}

	@Override
	public void removeNodes(List<Node> nodes) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                context.getTaskTrackerManager().removeNode(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                context.getJobClientManager().removeNode(node);
            }
        }
	}

}
