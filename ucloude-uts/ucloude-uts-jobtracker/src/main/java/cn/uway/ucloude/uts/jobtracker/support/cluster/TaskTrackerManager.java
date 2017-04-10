package cn.uway.ucloude.uts.jobtracker.support.cluster;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.ucloude.common.ConcurrentHashSet;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.jobtracker.channel.ChannelWrapper;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.domain.TaskTrackerNode;

/**
 * Task Tracker 管理器 (对 TaskTracker 节点的记录 和 可用线程的记录)
 * @author uway
 *
 */
public class TaskTrackerManager {
	private static final ILogger logger = LoggerManager.getLogger(TaskTrackerManager.class);
	/**
	 * 单例
	 */
	private final ConcurrentHashMap<String/*nodeGroup*/,Set<TaskTrackerNode>> NODE_MAP = new ConcurrentHashMap<String, Set<TaskTrackerNode>>();
	
	private JobTrackerContext context;
	
	public TaskTrackerManager(JobTrackerContext context){
		this.context = context;
	}
	
	 /**
     * get all connected node group
     */
    public Set<String> getNodeGroups() {
        return NODE_MAP.keySet();
    }

    
    public void addNode(Node node){
    	ChannelWrapper channel = context.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
    	Set<TaskTrackerNode> taskTrackerNodes  = NODE_MAP.get(node.getGroup());
    	
    	if(taskTrackerNodes == null){
    		taskTrackerNodes = new ConcurrentHashSet<TaskTrackerNode>();
            Set<TaskTrackerNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), taskTrackerNodes);
            if (oldSet != null) {
                taskTrackerNodes = oldSet;
            }
    	}
    	
    	TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getGroup(),
                node.getThreads(), node.getIdentity(), channel);
    	logger.info("Add TaskTracker node:{}", taskTrackerNode);
        taskTrackerNodes.add(taskTrackerNode);
        
        context.getExecutableJobQueue().createQueue(node.getGroup());
        context.getNodeGroupStore().addNodeGroup(NodeType.TASK_TRACKER, node.getGroup());

    }
	
    
    public void removeNode(Node node){
    	 Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());
         if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
             TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getIdentity());
             taskTrackerNode.setNodeGroup(node.getGroup());
             logger.info("Remove TaskTracker node:{}", taskTrackerNode);
             taskTrackerNodes.remove(taskTrackerNode);
         }
    }
    
    public TaskTrackerNode getTaskTrackerNode(String nodeGroup, String identity){
    	Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);
        if (taskTrackerNodes == null || taskTrackerNodes.size() == 0) {
            return null;
        }

        for (TaskTrackerNode taskTrackerNode : taskTrackerNodes) {
            if (taskTrackerNode.getIdentity().equals(identity)) {
                if (taskTrackerNode.getChannel() == null || taskTrackerNode.getChannel().isClosed()) {
                    // 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
                    ChannelWrapper channel = context.getChannelManager().getChannel(
                            taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                    if (channel != null) {
                        // 更新channel
                        taskTrackerNode.setChannel(channel);
                        logger.info("update node channel , taskTackerNode={}", taskTrackerNode);
                        return taskTrackerNode;
                    }
                } else {
                    // 只有当channel正常的时候才返回
                    return taskTrackerNode;
                }
            }
        }
        return null;
    }
    
    /**
     * 更新节点的 可用线程数
     * @param timestamp        时间戳, 只有当 时间戳大于上次更新的时间 才更新可用线程数
     */
    public void updateTaskTrackerAvailableThreads(
            String nodeGroup,
            String identity,
            Integer availableThreads,
            Long timestamp) {

        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);

        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            for (TaskTrackerNode trackerNode : taskTrackerNodes) {
                if (trackerNode.getIdentity().equals(identity) && (trackerNode.getTimestamp() == null || trackerNode.getTimestamp() <= timestamp)) {
                    trackerNode.setAvailableThread(availableThreads);
                    trackerNode.setTimestamp(timestamp);
                    if (logger.isDebugEnabled()) {
                    	logger.debug("更新节点线程数: {}", trackerNode);
                    }
                }
            }
        }
    }
}
