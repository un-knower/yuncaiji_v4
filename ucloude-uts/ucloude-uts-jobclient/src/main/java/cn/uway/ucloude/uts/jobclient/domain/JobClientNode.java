package cn.uway.ucloude.uts.jobclient.domain;

import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;

/**
 * 任務客戶端節點
 * @author uway
 *
 */
public class JobClientNode extends Node {

	public JobClientNode() {
		this.setNodeType(NodeType.JOB_CLIENT);
		this.addListenNodeType(NodeType.JOB_TRACKER);
		this.addListenNodeType(NodeType.JOB_CLIENT);
		this.addListenNodeType(NodeType.MONITOR);
	}
	
}
