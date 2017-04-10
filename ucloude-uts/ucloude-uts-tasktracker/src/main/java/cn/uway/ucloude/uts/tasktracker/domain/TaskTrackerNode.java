package cn.uway.ucloude.uts.tasktracker.domain;

import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class TaskTrackerNode extends Node{
	public TaskTrackerNode(){
		//设置节点类型
		this.setNodeType(NodeType.TASK_TRACKER);
		//监听JobTracker节点
		this.addListenNodeType(NodeType.JOB_TRACKER);
		
		//监听TASKTtracker节点
		this.addListenNodeType(NodeType.TASK_TRACKER);
		//监听器节点
		this.addListenNodeType(NodeType.MONITOR);
	}
	
	
}
