package cn.uway.ucloude.uts.web.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupGetReq;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupPo;
import cn.uway.ucloude.uts.web.access.domain.NodeOnOfflineLog;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;
import cn.uway.ucloude.uts.web.request.NodeGroupRequest;
import cn.uway.ucloude.uts.web.request.NodeOnOfflineLogQueryRequest;
import cn.uway.ucloude.uts.web.request.NodeQueryRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author magic.s.g.xie
 */
@RestController
@RequestMapping("/node")
public class NodeApi extends AbstractMVC {

	@Autowired
	private BackendAppContext appContext;

	@RequestMapping(value = "node-list-get", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody Pagination<Node> getNodeList(NodeQueryRequest request) {
		// RestfulResponse response = new RestfulResponse();
		Pagination<Node> paginationRsp = appContext.getBackendRegistrySrv().getOnlineNodes(request);

		return paginationRsp;
	}

	@RequestMapping("registry-re-subscribe")
	public RestfulResponse reSubscribe() {
		RestfulResponse response = new RestfulResponse();

		appContext.getBackendRegistrySrv().reSubscribe();

		response.setSuccess(true);
		return response;
	}

	@RequestMapping(value = "node-group-all", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody List<NodeGroupPo> getNodeGroups(NodeGroupRequest request) {

		NodeGroupGetReq nodeGroupGetReq = new NodeGroupGetReq();
		nodeGroupGetReq.setNodeGroup(request.getNodeGroup());
		nodeGroupGetReq.setNodeType(request.getNodeType());
		List<NodeGroupPo> paginationRsp = appContext.getNodeGroupStore().getNodeGroups(nodeGroupGetReq);

		return paginationRsp;
	}

	@RequestMapping("node-group-get")
	public @ResponseBody Pagination<NodeGroupPo> getNodeGroup(NodeGroupRequest request) {
		RestfulResponse response = new RestfulResponse();
		NodeGroupGetReq nodeGroupGetReq = new NodeGroupGetReq();
		nodeGroupGetReq.setNodeGroup(request.getNodeGroup());
		nodeGroupGetReq.setNodeType(request.getNodeType());
		Pagination<NodeGroupPo> paginationRsp = appContext.getNodeGroupStore().getNodeGroup(nodeGroupGetReq);

		return paginationRsp;
	}

	@RequestMapping("node-group-add")
	public RestfulResponse addNodeGroup(NodeGroupRequest request) {
		RestfulResponse response = new RestfulResponse();
		appContext.getNodeGroupStore().addNodeGroup(request.getNodeType(), request.getNodeGroup());
		if (NodeType.TASK_TRACKER.equals(request.getNodeType())) {
			appContext.getExecutableJobQueue().createQueue(request.getNodeGroup());
		} else if (NodeType.JOB_CLIENT.equals(request.getNodeType())) {
			appContext.getJobFeedbackQueue().createQueue(request.getNodeGroup());
		}
		response.setSuccess(true);
		return response;
	}

	@RequestMapping("node-group-del")
	public RestfulResponse delNodeGroup(NodeGroupRequest request) {
		RestfulResponse response = new RestfulResponse();
		appContext.getNodeGroupStore().removeNodeGroup(request.getNodeType(), request.getNodeGroup());
		if (NodeType.TASK_TRACKER.equals(request.getNodeType())) {
			appContext.getExecutableJobQueue().removeQueue(request.getNodeGroup());
		} else if (NodeType.JOB_CLIENT.equals(request.getNodeType())) {
			appContext.getJobFeedbackQueue().removeQueue(request.getNodeGroup());
		}
		response.setSuccess(true);
		return response;
	}

	@RequestMapping("node-onoffline-log-get")
	public @ResponseBody Pagination<NodeOnOfflineLog> delNodeGroup(NodeOnOfflineLogQueryRequest request) {
		Pagination<NodeOnOfflineLog> response = new Pagination<NodeOnOfflineLog>();;
		Long results = appContext.getBackendNodeOnOfflineLogAccess().count(request);
		if (results > 0) {
			response.setData(appContext.getBackendNodeOnOfflineLogAccess().select(request));
		} else {
			response.setData(null);
		}
		return response;
	}
}
