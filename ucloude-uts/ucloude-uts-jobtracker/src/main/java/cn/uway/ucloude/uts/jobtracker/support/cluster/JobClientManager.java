package cn.uway.ucloude.uts.jobtracker.support.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.ucloude.common.ConcurrentHashSet;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.loadbalance.LoadBalance;
import cn.uway.ucloude.uts.jobtracker.channel.ChannelWrapper;
import cn.uway.ucloude.uts.jobtracker.domain.JobClientNode;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobClientManager {
	private static final ILogger LOGGER = LoggerManager.getLogger(JobClientManager.class);
	
	private final ConcurrentHashMap<String /*Node Group*/,Set<JobClientNode>> NODE_MAP = new ConcurrentHashMap<String, Set<JobClientNode>>();
	
	private JobTrackerContext context;
	private LoadBalance loadBalance;
	public JobClientManager(JobTrackerContext context){
		this.context = context;
		this.loadBalance = ServiceFactory.load(LoadBalance.class, context.getConfiguration(), ExtConfigKeys.JOB_CLIENT_SELECT_LOADBALANCE);
	}
	
	public Set<String> getNodeGroups(){
		return NODE_MAP.keySet();
	}
	
	public void addNode(Node node){
		 //  channel 可能为 null
        ChannelWrapper channel = context.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
        Set<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());

        if (jobClientNodes == null) {
            jobClientNodes = new ConcurrentHashSet<JobClientNode>();
            Set<JobClientNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), jobClientNodes);
            if (oldSet != null) {
                jobClientNodes = oldSet;
            }
        }

        JobClientNode jobClientNode = new JobClientNode(node.getGroup(), node.getIdentity(), channel);
        LOGGER.info("add JobClient node:{}", jobClientNode);
        jobClientNodes.add(jobClientNode);

        // create feedback queue
        context.getJobFeedbackQueue().createQueue(node.getGroup());
        context.getNodeGroupStore().addNodeGroup(NodeType.JOB_CLIENT, node.getGroup());
	}
	
	
	 /**
     * 删除节点
     */
    public void removeNode(Node node) {
        Set<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());
        if (jobClientNodes != null && jobClientNodes.size() != 0) {
            for (JobClientNode jobClientNode : jobClientNodes) {
                if (node.getIdentity().equals(jobClientNode.getIdentity())) {
                    LOGGER.info("remove JobClient node:{}", jobClientNode);
                    jobClientNodes.remove(jobClientNode);
                }
            }
        }
    }

    /**
     * 得到 可用的 客户端节点
     */
    public JobClientNode getAvailableJobClient(String nodeGroup) {

        Set<JobClientNode> jobClientNodes = NODE_MAP.get(nodeGroup);

        if (CollectionUtil.isEmpty(jobClientNodes)) {
            return null;
        }

        List<JobClientNode> list = new ArrayList<JobClientNode>(jobClientNodes);

        while (list.size() > 0) {

            JobClientNode jobClientNode = loadBalance.select(list, null);

            if (jobClientNode != null && (jobClientNode.getChannel() == null || jobClientNode.getChannel().isClosed())) {
                ChannelWrapper channel = context.getChannelManager().getChannel(jobClientNode.getNodeGroup(), NodeType.JOB_CLIENT, jobClientNode.getIdentity());
                if (channel != null) {
                    // 更新channel
                    jobClientNode.setChannel(channel);
                }
            }

            if (jobClientNode != null && jobClientNode.getChannel() != null && !jobClientNode.getChannel().isClosed()) {
                return jobClientNode;
            } else {
                list.remove(jobClientNode);
            }
        }
        return null;
    }

}
