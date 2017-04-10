package cn.uway.ucloude.data.test;

import java.util.Date;

import org.junit.Test;

//import cn.uway.ucloude.data.DBFactory;
//import cn.uway.ucloude.data.DBProxy;

public class DbProxyTest {
	@Test
	public void testDbProxy(){
		
		WorkOrderDao dao = new WorkOrderDao("4");
		WorkOrder order = dao.getWorkOrder(5887);
		System.out.println("order:" + order.getOrderNo());
		WorkOrderDao dao1 = new WorkOrderDao("1");
		WorkOrder order1 = dao.getWorkOrder(5888);
		System.out.println("order:" + order1.getOrderNo());
		System.out.println(new Date());
	}
}
