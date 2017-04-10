package cn.uway.ucloude.uts.startup.jobclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import cn.uway.ucloude.filehandling.FileUtils;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.startup.jobtracker.CfgException;

public class JobClientCfgLoader {
	public static JobClientCfg load(String confPath) throws CfgException {
		String cfgPath = confPath + "/ucloude.uts.jobclient.cfg";
	    String log4jPath = confPath + "/log4j.properties";
	     
	    Properties conf = new Properties();
        File file = new File(cfgPath);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CfgException("can not find " + cfgPath);
        }
        try {
            conf.load(is);
        } catch (IOException e) {
            throw new CfgException("Read " + cfgPath + " error.", e);
        }
        
        JobClientCfg cfg = new JobClientCfg();
        
        String registryAddress = conf.getProperty("ucloude.uts.jobclient.registry-address");
        if (StringUtil.isEmpty(registryAddress)) {
            throw new CfgException("registryAddress can not be null.");
        }
        cfg.setRegistryAddress(registryAddress);
        
        String clusterName = conf.getProperty("ucloude.uts.jobclient.cluster-name");
        if (StringUtil.isEmpty(clusterName)) {
            throw new CfgException("clusterName can not be null.");
        }
        cfg.setClusterName(clusterName);

        
        String useRetryClient = conf.getProperty("ucloude.uts.jobclient.use-retry-client");
        boolean isUserRetryClient = true;
        if (StringUtil.isEmpty(useRetryClient)) {
        	isUserRetryClient = false;
        }
        cfg.setUseRetryClient(isUserRetryClient);
        String springXmlPaths = conf.getProperty("ucloude.uts.jobclient.spring-Xml-Paths");
        if (StringUtil.isNotEmpty(springXmlPaths)) {
            // 都好分割
            String[] tmpArr = springXmlPaths.split(",");
            if (tmpArr.length > 0) {
                String[] springXmlPathArr = new String[tmpArr.length];
                for (int i = 0; i < tmpArr.length; i++) {
                    springXmlPathArr[i] = StringUtil.trim(tmpArr[i]);
                }
                cfg.setSpringXmlPaths(springXmlPathArr);
            }
        }
        Map<String, String> configs = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : conf.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("ucloude.uts.jobclient.configs.")) {
                String value = entry.getValue() == null ? null : entry.getValue().toString();
                configs.put(key.replace("ucloude.uts.jobclient.configs.", ""), value);
            }
        }

        cfg.setConfigs(configs);

        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }
		return cfg;
	}
}
