package cn.uway.ucloude.uts.web.access.face;



import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JVMThreadDataPo;
import cn.uway.ucloude.uts.minitor.access.face.JVMThreadAccess;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;

/**
 * @author uway
 */
public interface BackendJVMThreadAccess extends JVMThreadAccess {

    void delete(JvmDataRequest request);

    List<JVMThreadDataPo> queryAvg(MDataRequest request);

}
