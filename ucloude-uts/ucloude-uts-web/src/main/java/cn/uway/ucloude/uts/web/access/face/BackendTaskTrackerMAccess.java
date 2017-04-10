package cn.uway.ucloude.uts.web.access.face;



import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.TaskTrackerMDataPo;
import cn.uway.ucloude.uts.minitor.access.face.TaskTrackerMAccess;
import cn.uway.ucloude.uts.web.admin.vo.NodeInfo;
import cn.uway.ucloude.uts.web.request.MDataRequest;

/**
 * @author uway
 */
public interface BackendTaskTrackerMAccess extends TaskTrackerMAccess{

    List<TaskTrackerMDataPo> querySum(MDataRequest request);

    void delete(MDataRequest request);

    List<NodeInfo> getTaskTrackers();
}
