package cn.uway.ucloude.uts.spring.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author magic.s.g.xie
 */
@ConfigurationProperties(prefix = "ucloude.uts.jobtracker")
public class JobTrackerProperties extends cn.uway.ucloude.uts.core.properties.JobTrackerProperties {

    /**
     * 监听端口
     */
    private Integer listenPort;

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }
}
