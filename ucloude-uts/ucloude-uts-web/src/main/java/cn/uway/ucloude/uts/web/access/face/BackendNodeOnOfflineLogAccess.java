package cn.uway.ucloude.uts.web.access.face;
import java.util.List;

import cn.uway.ucloude.uts.web.access.domain.*;
import cn.uway.ucloude.uts.web.request.NodeOnOfflineLogQueryRequest;

public interface BackendNodeOnOfflineLogAccess {
    void insert(List<NodeOnOfflineLog> nodeOnOfflineLogs);

    List<NodeOnOfflineLog> select(NodeOnOfflineLogQueryRequest request);

    Long count(NodeOnOfflineLogQueryRequest request);

    void delete(NodeOnOfflineLogQueryRequest request);
}
