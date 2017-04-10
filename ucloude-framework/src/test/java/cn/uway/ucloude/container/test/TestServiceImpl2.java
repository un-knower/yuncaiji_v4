package cn.uway.ucloude.container.test;

import cn.uway.ucloude.configuration.BasicConfiguration;

public class TestServiceImpl2 implements TestService {
	 public TestServiceImpl2() {
	        System.out.println("2222222");
	    }
	@Override
	public void sayHello(BasicConfiguration configuration) {
		// TODO Auto-generated method stub
		System.out.println("2");
	}

}
