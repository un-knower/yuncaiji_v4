package cn.uway.ucloude.uts.monitor.access.face;


import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.JobTrackerMDataPo;

/**
 * @author uway
 */
public interface JobTrackerMAccess {

    void insert(List<JobTrackerMDataPo> jobTrackerMDataPos);

}
