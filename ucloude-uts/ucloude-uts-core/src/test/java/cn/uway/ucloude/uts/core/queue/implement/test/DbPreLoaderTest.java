package cn.uway.ucloude.uts.core.queue.implement.test;

import cn.uway.ucloude.ec.injvm.InjvmEventCenter;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.PreLoader;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.implement.DbPreLoader;

public class DbPreLoaderTest {
	@org.junit.Test
	public void test() {
		PreLoader dbPreLoader=new DbPreLoader(ExtConfigKeys.CONNECTION_KEY,300,0.2,100,"JT_192.168.15.161_6108_20170220093535122_1",new InjvmEventCenter());
		JobPo po = dbPreLoader.take("igp_tasktracker", "TT_192.168.15.161_10416_20170220093341669_1");
		//、、PreLoader loader = new DbPre
	}
}
