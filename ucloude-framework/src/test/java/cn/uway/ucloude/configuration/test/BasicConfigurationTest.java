package cn.uway.ucloude.configuration.test;

import org.junit.Test;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.configuration.BasicConfiguration;

public class BasicConfigurationTest {
	@Test
	public void testgetParameters(){
		BasicConfiguration configuration = new BasicConfiguration();
		System.out.println(configuration.getParameter("netty.frame.length.max",UCloudeConstants.DEFAULT_BUFFER_SIZE));
	}
}
