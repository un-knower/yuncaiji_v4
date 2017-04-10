package cn.uway.ucloude.uts.web.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.minitor.access.domain.MDataPo;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.support.Builder;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;
import cn.uway.ucloude.uts.web.request.MDataRequest;

import java.util.List;

/**
 * @author magic.s.g.xie
 */
@RestController
public class MonitorApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;
    
    

    @RequestMapping(value = "/monitor/monitor-data-get", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody List<? extends MDataPo> monitorDataGet(MDataRequest request) {
        RestfulResponse response = new RestfulResponse();
//        if (request.getNodeType() == null) {
//            return Builder.build(false, "nodeType can not be null.");
//        }
//        if (request.getStartTime() == null || request.getEndTime() == null) {
//            return Builder.build(false, "Search time range must be input.");
//        }
        if (StringUtil.isNotEmpty(request.getIdentity())) {
            request.setNodeGroup(null);
        }

        List<? extends MDataPo> rows = null;
        switch (request.getNodeType()) {
            case JOB_CLIENT:
                rows = appContext.getBackendJobClientMAccess().querySum(request);
                break;
            case JOB_TRACKER:
                rows = appContext.getBackendJobTrackerMAccess().querySum(request);
                break;
            case TASK_TRACKER:
                rows = appContext.getBackendTaskTrackerMAccess().querySum(request);
                break;
        }
//        response.setSuccess(true);
//        response.setRows(rows);
//        response.setResults(CollectionUtil.sizeOf(rows));
        return rows;
    }

    @RequestMapping(value = "/monitor/jvm-monitor-data-get", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody List<? extends MDataPo> jvmMDataGet(MDataRequest request) {
//        RestfulResponse response = new RestfulResponse();
//        if (request.getJvmType() == null) {
//            return Builder.build(false, "jvmType can not be null.");
//        }
//        if (request.getStartTime() == null || request.getEndTime() == null) {
//            return Builder.build(false, "Search time range must be input.");
//        }
        if (StringUtil.isNotEmpty(request.getIdentity())) {
            request.setNodeGroup(null);
        }

        List<? extends MDataPo> rows = null;
        switch (request.getJvmType()) {
            case GC:
                rows = appContext.getBackendJVMGCAccess().queryAvg(request);
                break;
            case MEMORY:
                rows = appContext.getBackendJVMMemoryAccess().queryAvg(request);
                break;
            case THREAD:
                rows = appContext.getBackendJVMThreadAccess().queryAvg(request);
                break;
        }
//        response.setSuccess(true);
//        response.setRows(rows);
//        response.setResults(CollectionUtil.sizeOf(rows));
        return rows;
    }

}
