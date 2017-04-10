package cn.uway.ucloude.container.test;

import cn.uway.ucloude.configuration.BasicConfiguration;

public class TestServiceImpl implements TestService {

    public TestServiceImpl() {
        System.out.println("1111111");
    }

    @Override
    public void sayHello(BasicConfiguration config) {
        System.out.println("1");
    }

}
