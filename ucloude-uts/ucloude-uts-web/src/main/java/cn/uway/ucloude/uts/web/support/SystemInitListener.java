package cn.uway.ucloude.uts.web.support;


import org.apache.log4j.PropertyConfigurator;

import cn.uway.ucloude.compiler.AbstractCompiler;
import cn.uway.ucloude.data.dataaccess.DataSourceProvider;
import cn.uway.ucloude.filehandling.FileUtils;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonFactory;
import cn.uway.ucloude.utils.PlatformUtils;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.minitor.MonitorAgentStartup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author magic.s.g.xie
 */
public class SystemInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String confPath = servletContextEvent.getServletContext().getInitParameter("uts.admin.config.path");
        if (StringUtil.isNotEmpty(confPath)) {
            System.out.println("uts.admin.config.path : " + confPath);
        }
        DataSourceProvider.initialDataSource(confPath);

        
        AppConfigurer.load(confPath);

        String compiler = AppConfigurer.getProperty("configs." + ExtConfigKeys.COMPILER);
        if (StringUtil.isNotEmpty(compiler)) {
            AbstractCompiler.setCompiler(compiler);
        }

        String jsonAdapter = AppConfigurer.getProperty("configs." + ExtConfigKeys.UTS_JSON);
        if (StringUtil.isNotEmpty(jsonAdapter)) {
            JsonFactory.setJSONAdapter(jsonAdapter);
        }

        String loggerAdapter = AppConfigurer.getProperty("configs." + ExtConfigKeys.UTS_LOGGER);
        if (StringUtil.isNotEmpty(loggerAdapter)) {
            LoggerManager.setLoggerAdapter(loggerAdapter);
        }

        String log4jPath = confPath + "/log4j.properties";
        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }

        boolean monitorAgentEnable = Boolean.valueOf(AppConfigurer.getProperty("uts.monitorAgent.enable", "true"));
        if (monitorAgentEnable) {
            String utsMonitorCfgPath = confPath;
            if (StringUtil.isEmpty(utsMonitorCfgPath)) {
            	utsMonitorCfgPath = this.getClass().getResource("/").getPath();
                if (PlatformUtils.isWindows()) {
                    // 替换window下空格问题
                	utsMonitorCfgPath = utsMonitorCfgPath.replaceAll("%20", " ");
                }
            }
            MonitorAgentStartup.start(utsMonitorCfgPath);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MonitorAgentStartup.stop();
    }
}
