package cn.uway.ucloude.uts.monitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.JVMMemoryDataPo;

/**
 * @author uway
 */
public interface JVMMemoryAccess {

    void insert(List<JVMMemoryDataPo> pos);

}
