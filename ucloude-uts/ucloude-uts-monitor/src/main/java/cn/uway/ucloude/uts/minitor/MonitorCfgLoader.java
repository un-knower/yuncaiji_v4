package cn.uway.ucloude.uts.minitor;



import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import cn.uway.ucloude.filehandling.FileUtils;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.utils.StringUtil;

/**
 * @author uway
 */
public class MonitorCfgLoader {

    public static MonitorCfg load(String confPath) {

        String cfgPath = confPath + "/ucloude-uts-monitor.cfg";
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

        MonitorCfg cfg = new MonitorCfg();
        try {
            String registryAddress = conf.getProperty("registryAddress");
            Assert.hasText(registryAddress, "registryAddress can not be null.");
            cfg.setRegistryAddress(registryAddress);

            String clusterName = conf.getProperty("clusterName");
            Assert.hasText(clusterName, "clusterName can not be null.");
            cfg.setClusterName(clusterName);

            String bindIp = conf.getProperty("bindIp");
            if (StringUtil.isNotEmpty(bindIp)) {
                cfg.setBindIp(bindIp);
            }

            String identity = conf.getProperty("identity");
            if (StringUtil.isNotEmpty(identity)) {
                cfg.setBindIp(identity);
            }

            Map<String, String> configs = new HashMap<String, String>();
            for (Map.Entry<Object, Object> entry : conf.entrySet()) {
                String key = entry.getKey().toString();
                if (key.startsWith("configs.")) {
                    String value = entry.getValue() == null ? null : entry.getValue().toString();
                    configs.put(key.replace("configs.", ""), value);
                }
            }

            cfg.setConfigs(configs);
        } catch (Exception e) {
            throw new CfgException(e);
        }

        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }

        return cfg;
    }

}
