package cn.uway.ucloude.uts.monitor.cmd;



import java.util.List;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.serialize.TypeReference;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.core.cmd.HttpCmdParamNames;
import cn.uway.ucloude.uts.core.domain.monitor.JobClientMData;
import cn.uway.ucloude.uts.core.domain.monitor.JobTrackerMData;
import cn.uway.ucloude.uts.core.domain.monitor.MData;
import cn.uway.ucloude.uts.core.domain.monitor.MNode;
import cn.uway.ucloude.uts.core.domain.monitor.TaskTrackerMData;
import cn.uway.ucloude.uts.monitor.MonitorAppContext;

/**
 * 监控数据添加CMD
 *
 * @author uway
 */
public class MDataAddHttpCmd implements HttpCmdProcessor {

    private static final ILogger LOGGER = LoggerManager.getLogger(MDataAddHttpCmd.class);

    private MonitorAppContext appContext;

    public MDataAddHttpCmd(MonitorAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfiguration().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_ADD_M_DATA;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String mNodeJson = request.getParam(HttpCmdParamNames.M_NODE);
        if (StringUtil.isEmpty(mNodeJson)) {
            return HttpCmdResponse.newResponse(false, "mData is empty");
        }
        MNode mNode = JsonConvert.deserialize(mNodeJson, new TypeReference<MNode>() {
        }.getType());

        HttpCmdResponse response = paramCheck(mNode);
        if (response != null) {
            return response;
        }

        String mDataJson = request.getParam(HttpCmdParamNames.M_DATA);
        if (StringUtil.isEmpty(mDataJson)) {
            return HttpCmdResponse.newResponse(false, "mData is empty");
        }
        try {
            assert mNode != null;
            List<MData> mDatas = getMDataList(mNode.getNodeType(), mDataJson);
            appContext.getMDataSrv().addMDatas(mNode, mDatas);
        } catch (Exception e) {
            LOGGER.error("Add Monitor Data error: " + JsonConvert.serialize(request), e);
            return HttpCmdResponse.newResponse(false, "Add Monitor Data error: " + e.getMessage());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add Monitor Data success, mNode=" + mNodeJson + ", mData=" + mDataJson);
        }

        return HttpCmdResponse.newResponse(true, "Add Monitor Data success");
    }


    private List<MData> getMDataList(NodeType nodeType, String mDataJson) {
        List<MData> mDatas = null;
        if (NodeType.TASK_TRACKER == nodeType) {
            mDatas = JsonConvert.deserialize(mDataJson, new TypeReference<List<TaskTrackerMData>>() {
            }.getType());
        } else if (NodeType.JOB_TRACKER == nodeType) {
            mDatas = JsonConvert.deserialize(mDataJson, new TypeReference<List<JobTrackerMData>>() {
            }.getType());
        } else if (NodeType.JOB_CLIENT == nodeType) {
            mDatas = JsonConvert.deserialize(mDataJson, new TypeReference<List<JobClientMData>>() {
            }.getType());
        }
        return mDatas;
    }

    private HttpCmdResponse paramCheck(MNode mNode) {
        if (mNode == null) {
            return HttpCmdResponse.newResponse(false, "mNode is empty");
        }

        NodeType nodeType = mNode.getNodeType();
        if (nodeType == null || !(nodeType == NodeType.JOB_CLIENT || nodeType == NodeType.TASK_TRACKER || nodeType == NodeType.JOB_TRACKER)) {
            return HttpCmdResponse.newResponse(false, "nodeType error");
        }
        if (StringUtil.isEmpty(mNode.getNodeGroup())) {
            return HttpCmdResponse.newResponse(false, "nodeGroup is empty");
        }
        if (StringUtil.isEmpty(mNode.getIdentity())) {
            return HttpCmdResponse.newResponse(false, "identity is empty");
        }
        return null;
    }

}
