package cn.uway.ucloude.uts.minitor.access.face;


import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JobClientMDataPo;

/**
 * @author uway
 */
public interface JobClientMAccess {

    void insert(List<JobClientMDataPo> jobTrackerMDataPos);

}
