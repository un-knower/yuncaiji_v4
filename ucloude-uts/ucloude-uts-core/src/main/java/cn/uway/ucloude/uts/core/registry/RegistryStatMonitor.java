package cn.uway.ucloude.uts.core.registry;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.UtsContext;

public class RegistryStatMonitor {
	private static final ILogger LOGGER = LoggerManager.getLogger(RegistryStatMonitor.class);
    private UtsContext context;
    private AtomicBoolean available = new AtomicBoolean(false);

    public RegistryStatMonitor(UtsContext context) {
        this.context = context;
    }

    public void setAvailable(boolean available) {
        this.available.set(available);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Registry {}", available ? "available" : "unavailable");
        }
        // 发布事件
        context.getEventCenter().publishAsync(new EventInfo(
                available ? EcTopic.REGISTRY_AVAILABLE : EcTopic.REGISTRY_UN_AVAILABLE));
    }

    public boolean isAvailable() {
        return this.available.get();
    }
}
