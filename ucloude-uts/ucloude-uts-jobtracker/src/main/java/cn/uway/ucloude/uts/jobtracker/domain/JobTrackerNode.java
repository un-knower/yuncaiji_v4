package cn.uway.ucloude.uts.jobtracker.domain;

import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class JobTrackerNode extends Node{
	public JobTrackerNode(){
		this.setNodeType(NodeType.JOB_TRACKER);
		this.addListenNodeType(NodeType.JOB_CLIENT);
		this.addListenNodeType(NodeType.TASK_TRACKER);
		this.addListenNodeType(NodeType.JOB_TRACKER);
		this.addListenNodeType(NodeType.MONITOR);
		
	}
}
