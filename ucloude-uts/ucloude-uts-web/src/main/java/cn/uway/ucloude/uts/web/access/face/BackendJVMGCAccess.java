package cn.uway.ucloude.uts.web.access.face;


import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JVMGCDataPo;
import cn.uway.ucloude.uts.minitor.access.face.JVMGCAccess;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;

/**
 * @author uway.
 */
public interface BackendJVMGCAccess extends JVMGCAccess {

    void delete(JvmDataRequest request);

    List<JVMGCDataPo> queryAvg(MDataRequest request);
}
