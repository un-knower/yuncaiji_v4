package cn.uway.ucloude.uts.access.test;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import cn.uway.ucloude.uts.monitor.access.domain.JobClientMDataPo;
import cn.uway.ucloude.uts.web.access.db.DbBackendAccessFactory;
import cn.uway.ucloude.uts.web.access.face.BackendJobClientMAccess;
import cn.uway.ucloude.uts.web.request.MDataRequest;

public class DbMonitorAccessTest {
	@Test
	public void testJobClientAccess() {
		BackendJobClientMAccess access = new DbBackendAccessFactory().getBackendJobClientMAccess();
		MDataRequest request = new MDataRequest();
		//[1489334400000,1489334400000,2]
		request.setNodeType(2);
		request.setStartTime(1489334400000L);
		request.setEndTime(1489334400000L);
		List< JobClientMDataPo> result = access.querySum(request);
		System.out.println(result.isEmpty());
	}
}
