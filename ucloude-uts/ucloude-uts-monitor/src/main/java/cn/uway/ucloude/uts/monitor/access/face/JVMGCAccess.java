package cn.uway.ucloude.uts.monitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.JVMGCDataPo;

/**
 * @author uway
 */
public interface JVMGCAccess {

    void insert(List<JVMGCDataPo> pos);

}
