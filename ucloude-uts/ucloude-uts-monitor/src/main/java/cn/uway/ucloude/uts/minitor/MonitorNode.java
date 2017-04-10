package cn.uway.ucloude.uts.minitor;

import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;

/**
 * @author magic.s.g.xie
 */
public class MonitorNode extends Node {

    public MonitorNode() {
        this.setNodeType(NodeType.MONITOR);
    }
}
