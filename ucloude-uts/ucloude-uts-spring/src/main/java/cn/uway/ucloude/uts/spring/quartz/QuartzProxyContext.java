package cn.uway.ucloude.uts.spring.quartz;

/**
 * @author magic.s.g.xie
 */
class QuartzProxyContext {

    private QuartzUTSConfig quartzUTSConfig;
    private QuartzUTSProxyAgent agent;

    public QuartzProxyContext(QuartzUTSConfig quartzUTSConfig, QuartzUTSProxyAgent agent) {
        this.quartzUTSConfig = quartzUTSConfig;
        this.agent = agent;
    }

    public QuartzUTSConfig getQuartzUTSConfig() {
        return quartzUTSConfig;
    }

    public QuartzUTSProxyAgent getAgent() {
        return agent;
    }
}
