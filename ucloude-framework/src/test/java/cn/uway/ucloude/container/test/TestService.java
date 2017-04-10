package cn.uway.ucloude.container.test;

import cn.uway.ucloude.configuration.BasicConfiguration;
import cn.uway.ucloude.container.SPI;

@SPI(key = "test.type", dftValue = "test1")
public interface TestService {
	public void sayHello(BasicConfiguration configuration);
}
