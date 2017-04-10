package cn.uway.ucloude.uts.web.cluster;

import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class BackendNode extends Node {

    public BackendNode() {
        this.setNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.TASK_TRACKER);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.MONITOR);
    }
}
