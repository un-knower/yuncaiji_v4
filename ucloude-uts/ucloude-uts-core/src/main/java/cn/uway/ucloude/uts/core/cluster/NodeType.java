package cn.uway.ucloude.uts.core.cluster;

import cn.uway.ucloude.utils.StringUtil;

public enum NodeType {
	All(-1,"所有"),
	// job tracker
    JOB_TRACKER(0,"作业调度"),
    // task tracker
    TASK_TRACKER(1,"作业执行"),
    // client
    JOB_CLIENT(2,"作业提交"),
    // monitor
    MONITOR(3,"监控"),
    
    BACKEND(4, "后端");
	
	private final int value;
	public int getValue() {
		return value;
	}

	public String getText() {
		return Text;
	}

	private final String Text;
	NodeType( int value,String Text){
		this.value = value;
		this.Text = Text;
	}
	
	public static NodeType getNodeType(int nodeType){
		NodeType[] nodeTypes = NodeType.values();
		for(NodeType item:nodeTypes){
			if(item.getValue() == nodeType )
				return item;
		}
		return NodeType.All;
	}
}
