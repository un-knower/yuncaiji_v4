package cn.uway.ucloude.uts.minitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JVMGCDataPo;

/**
 * @author uway
 */
public interface JVMGCAccess {

    void insert(List<JVMGCDataPo> pos);

}
