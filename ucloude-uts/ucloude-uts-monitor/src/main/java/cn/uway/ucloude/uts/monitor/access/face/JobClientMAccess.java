package cn.uway.ucloude.uts.monitor.access.face;


import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.JobClientMDataPo;

/**
 * @author uway
 */
public interface JobClientMAccess {

    void insert(List<JobClientMDataPo> jobTrackerMDataPos);

}
