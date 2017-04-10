package cn.uway.ucloude.container.test;


import org.junit.Test;

import cn.uway.ucloude.container.ServiceFactory;

public class ServiceFactoryTest {
	@Test
	public void testServiceFactory(){
//	 Set<String> sets =	ServiceFactory.getServiceProviders(TestService.class);
//	 for(String key : sets)
//	 {
//		 TestService service = ServiceFactory.load(TestService.class, key);
//		 service.sayHello(null);
//	 }
		TestService service = ServiceFactory.loadDefault(TestService.class);
		service.sayHello(null);
	}
}
