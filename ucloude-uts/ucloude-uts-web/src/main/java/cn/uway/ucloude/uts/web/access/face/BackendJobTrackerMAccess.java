package cn.uway.ucloude.uts.web.access.face;

import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.JobTrackerMDataPo;
import cn.uway.ucloude.uts.monitor.access.face.JobTrackerMAccess;
import cn.uway.ucloude.uts.web.request.MDataRequest;

public interface BackendJobTrackerMAccess extends JobTrackerMAccess {
	List<JobTrackerMDataPo> querySum(MDataRequest request);

    void delete(MDataRequest request);

    List<String> getJobTrackers();
}
