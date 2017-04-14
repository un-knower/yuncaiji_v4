package cn.uway.ucloude.uts.monitor.access.face;

import java.util.List;

import cn.uway.ucloude.uts.monitor.access.domain.JVMThreadDataPo;

/**
 * @author uway
 */
public interface JVMThreadAccess {

    void insert(List<JVMThreadDataPo> pos);

}
