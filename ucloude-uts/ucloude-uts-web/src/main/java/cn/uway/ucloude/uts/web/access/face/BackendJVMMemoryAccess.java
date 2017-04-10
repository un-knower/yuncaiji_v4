package cn.uway.ucloude.uts.web.access.face;



import java.util.List;

import cn.uway.ucloude.uts.minitor.access.domain.JVMMemoryDataPo;
import cn.uway.ucloude.uts.minitor.access.face.JVMMemoryAccess;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;

/**
 * @author uway
 */
public interface BackendJVMMemoryAccess extends JVMMemoryAccess{

    void delete(JvmDataRequest request);

    List<JVMMemoryDataPo> queryAvg(MDataRequest request);
}
