package cn.uway.ucloude.uts.startup;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import cn.uway.ucloude.data.dataaccess.DataSourceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author magic.s.g.xie
 */
public class JettyContainer {

    public static void main(String[] args) {
        try {
            String confPath = args[0];

            confPath = confPath.trim();

            Properties conf = new Properties();
            InputStream is = new FileInputStream(new File(confPath + "/conf/ucloude-uts-web.cfg"));
            conf.load(is);
            String port = conf.getProperty("port");
            if (port == null || port.trim().equals("")) {
                port = "8083";
            }

            Server server = new Server(Integer.parseInt(port));
            WebAppContext webapp = new WebAppContext();
            webapp.setWar(confPath + "/war/ucloude-uts-web.war");
            Map<String, String> initParams = new HashMap<String, String>();
            initParams.put("uts.admin.config.path", confPath + "/conf");
            webapp.setInitParams(initParams);
            server.setHandler(webapp);
            server.setStopAtShutdown(true);
            server.start();
            System.out.println("uts.admin.config.path："+confPath + "/conf");
            System.out.println("ucloude-uts-web started. http://" + NetUtils.getLocalHost() + ":" + port + "/index.htm");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
