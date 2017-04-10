package cn.uway.ucloude.uts.web.access.face;



import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JobClientMDataPo;
import cn.uway.ucloude.uts.minitor.access.face.JobClientMAccess;
import cn.uway.ucloude.uts.web.admin.vo.NodeInfo;
import cn.uway.ucloude.uts.web.request.MDataRequest;

/**
 * @author uway
 */
public interface BackendJobClientMAccess extends JobClientMAccess {

    void delete(MDataRequest request);

    List<JobClientMDataPo> querySum(MDataRequest request);

    List<NodeInfo> getJobClients();
}
