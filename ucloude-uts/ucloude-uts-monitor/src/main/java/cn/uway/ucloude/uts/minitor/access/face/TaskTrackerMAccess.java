package cn.uway.ucloude.uts.minitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.TaskTrackerMDataPo;

/**
 * @author uway
 */
public interface TaskTrackerMAccess {

    void insert(List<TaskTrackerMDataPo> pos);

}