package cn.uway.ucloude.uts.core.protocol.command;

import java.util.HashMap;
import java.util.Map;

import cn.uway.ucloude.annotation.NotNull;
import cn.uway.ucloude.annotation.Nullable;
import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.rpc.RpcCommandBody;
import cn.uway.ucloude.rpc.exception.RpcCommandFieldCheckException;

public class AbstractRpcCommandBody implements RpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2402544609189497022L;

	@Override
	public void checkFields() throws RpcCommandFieldCheckException {
		// TODO Auto-generated method stub
		
	}
	
	/**
     * 节点组 当前节点的 group(统一类型, 具有相同功能的节点group相同)
     */
    @NotNull
    private String nodeGroup;

    /**
     * NodeType 的字符串表示, 节点类型
     */
    @NotNull
    private String nodeType;

    /**
     * 当前节点的唯一标识
     */
    @NotNull
    private String identity;

    private Long timestamp = SystemClock.now();

    // 额外的参数
    @Nullable
    private Map<String, Object> extParams;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public Map<String, Object> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, Object> extParams) {
        this.extParams = extParams;
    }

    public void putExtParam(String key, Object obj) {
        if (this.extParams == null) {
            this.extParams = new HashMap<String, Object>();
        }
        this.extParams.put(key, obj);
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }


}
