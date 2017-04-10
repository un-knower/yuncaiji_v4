package cn.uway.ucloude.uts.minitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JVMMemoryDataPo;

/**
 * @author uway
 */
public interface JVMMemoryAccess {

    void insert(List<JVMMemoryDataPo> pos);

}
