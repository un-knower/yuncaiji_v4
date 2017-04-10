package cn.uway.usummary;

import java.io.File;

import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.uway.usummary.cache.CacheManager;
import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.util.StringUtil;
import cn.uway.usummary.ws.impl.DataAnalyzeServiceImpl;

public class Runner {
	
	private static Logger LOG = LoggerFactory.getLogger(Runner.class);
	
	private static String VERSION = "1.0.0.0";
	
	public static void main(String[] args) {
		Runner runner = new Runner();
		try{
			runner.init();
			runner.startWS();
		}catch(Exception e){
			LOG.error("程序启动发生异常，原因:",e);
		}
		
	}
	
	private void init(){
//		DOMConfigurator.configure("conf/log4j.xml");;
//		PropertyConfigurator.configure("conf/log4j.properties");
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		LoggerContext loggerContext = (LoggerContext) loggerFactory;
		JoranConfigurator jc = new JoranConfigurator();
		try {
			File f = new File("./conf/logback.xml");
			jc.setContext(loggerContext);
			jc.doConfigure(f);
		} catch (JoranException e) {
			LOG.error("U-Summary启动失败,原因：系统日志模块加载失败.", e);
		}
		LOG.info("开始启动U-Summary，程序版本号："+VERSION);
		AppContext.getBean("cacheManager", CacheManager.class).loadCache();
	}
	
	private void startWS(){
		String addr = AppContext.getBean("webserviceAddr", String.class);
		if(StringUtil.isEmpty(addr)){
			LOG.debug("WS地址没有配置，程序退出!");
			System.exit(0);
		}
		LOG.debug("开始启动WS!");
		JaxWsServerFactoryBean factory=new JaxWsServerFactoryBean();
        factory.setAddress(addr);
        factory.setServiceBean(new DataAnalyzeServiceImpl());
        factory.create();
//		Endpoint.publish(addr, new DataAnalyzeServiceImpl());
        LOG.debug("成功启动WS!");
	} 
}
