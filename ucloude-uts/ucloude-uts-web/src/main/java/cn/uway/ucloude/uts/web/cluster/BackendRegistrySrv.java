package cn.uway.ucloude.uts.web.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.registry.AbstractRegistry;
import cn.uway.ucloude.uts.core.registry.NotifyEvent;
import cn.uway.ucloude.uts.core.registry.NotifyListener;
import cn.uway.ucloude.uts.core.registry.Registry;
import cn.uway.ucloude.uts.core.registry.RegistryFactory;
import cn.uway.ucloude.uts.web.access.domain.NodeOnOfflineLog;
import cn.uway.ucloude.uts.web.request.NodeQueryRequest;

public class BackendRegistrySrv {
	 private static final ILogger LOGGER = LoggerManager.getLogger(BackendRegistrySrv.class);
	    private BackendAppContext context;
	    private Registry registry;
	    private NotifyListener notifyListener;

	    public BackendRegistrySrv(BackendAppContext context) {
	        this.context = context;
	    }

	    private void subscribe() {

	        if (registry instanceof AbstractRegistry) {
	            ((AbstractRegistry) registry).setNode(context.getNode());
	        }
	        registry.subscribe(context.getNode(), notifyListener);
	    }

	    public void reSubscribe() {
	        // 取消订阅
	        registry.unsubscribe(context.getNode(), notifyListener);
	        // 清空内存数据
	        context.getNodeMemCacheAccess().clear();
	        // 重新订阅
	        subscribe();
	    }

	    public Pagination<Node> getOnlineNodes(NodeQueryRequest request) {
	        return context.getNodeMemCacheAccess().pageSelect(request);
	    }

	    /**
	     * 记录节点上下线日志
	     */
	    private void addLog(NotifyEvent event, List<Node> nodes) {
	        List<NodeOnOfflineLog> logs = new ArrayList<NodeOnOfflineLog>(nodes.size());

	        for (Node node : nodes) {
	            NodeOnOfflineLog log = new NodeOnOfflineLog();
	            log.setLogTime(new Date());
	            log.setEvent(event == NotifyEvent.ADD ? "ONLINE" : "OFFLINE");

	            log.setClusterName(node.getClusterName());
	            log.setCreateTime(new Date(node.getCreateTime()));
	            log.setGroup(node.getGroup());
	            log.setHostName(node.getHostName());
	            log.setIdentity(node.getIdentity());
	            log.setIp(node.getIp());
	            log.setPort(node.getPort());
	            log.setThreads(node.getThreads());
	            log.setNodeType(node.getNodeType());
	            log.setHttpCmdPort(node.getHttpCmdPort());

	            logs.add(log);
	        }

	        context.getBackendNodeOnOfflineLogAccess().insert(logs);
	    }

	    public void start() throws Exception {

	        registry = RegistryFactory.getRegistry(context);

	        notifyListener = new NotifyListener() {
	            @Override
	            public void notify(NotifyEvent event, List<Node> nodes) {
	                if (CollectionUtil.isEmpty(nodes)) {
	                    return;
	                }
	                switch (event) {
	                    case ADD:
	                    	context.getNodeMemCacheAccess().addNode(nodes);
	                        LOGGER.info("ADD NODE " + nodes);
	                        break;
	                    case REMOVE:
	                    	context.getNodeMemCacheAccess().removeNode(nodes);
	                        LOGGER.info("REMOVE NODE " + nodes);
	                        break;
	                }
	                // 记录日志
	                addLog(event, nodes);
	            }
	        };

	        subscribe();
	    }
}
