package cn.uway.ucloude.uts.monitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.TaskTrackerMDataPo;

/**
 * @author uway
 */
public interface TaskTrackerMAccess {

    void insert(List<TaskTrackerMDataPo> pos);

}
