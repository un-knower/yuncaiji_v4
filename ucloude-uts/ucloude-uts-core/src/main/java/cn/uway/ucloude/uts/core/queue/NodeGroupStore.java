package cn.uway.ucloude.uts.core.queue;

import java.util.List;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupGetReq;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupPo;

/**
 * 节点分组管理
 * @author uway
 *
 */
public interface NodeGroupStore {
	/**
     * 添加 NodeGroup
     */
    void addNodeGroup(NodeType nodeType, String name);

    /**
     * 移除 NodeGroup
     */
    void removeNodeGroup(NodeType nodeType, String name);

    /**
     * 得到某个nodeType 的所有 nodeGroup
     */
    List<NodeGroupPo> getNodeGroup(NodeType nodeType);

    /**
     * 分页查询
     */
    Pagination<NodeGroupPo> getNodeGroup(NodeGroupGetReq request);
    
    /**
     * <p>
     * 	获取所有的节点分组信息
     * </P>
     * 
     * @param request
     * @return
     */
    List<NodeGroupPo> getNodeGroups(NodeGroupGetReq request);
}
