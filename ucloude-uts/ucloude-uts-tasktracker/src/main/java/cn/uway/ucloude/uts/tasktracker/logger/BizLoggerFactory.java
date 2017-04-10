package cn.uway.ucloude.uts.tasktracker.logger;



import java.util.concurrent.ConcurrentHashMap;
import cn.uway.ucloude.Globle;
import cn.uway.ucloude.common.Environment;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.rpc.RpcClientDelegate;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

/**
 * @author magic.s.g.xie
 */
public class BizLoggerFactory {

    private static final ConcurrentHashMap<String, BizLogger> BIZ_LOGGER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, BizLogger>();

    /**
     * 保证一个TaskTracker只能有一个Logger, 因为一个jvm可以有多个TaskTracker
     */
    public static BizLogger getLogger(Level level, RpcClientDelegate remotingClient, TaskTrackerContext appContext) {

        // 单元测试的时候返回 Mock
        if (Environment.UNIT_TEST == Globle.getEnvironment()) {
            return new MockBizLogger(level);
        }

        String key = appContext.getConfiguration().getIdentity();
        BizLogger logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
        if (logger == null) {
            synchronized (BIZ_LOGGER_CONCURRENT_HASH_MAP) {
                logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
                if (logger != null) {
                    return logger;
                }
                logger = new BizLoggerImpl(level, remotingClient, appContext);

                BIZ_LOGGER_CONCURRENT_HASH_MAP.put(key, logger);
            }
        }
        return logger;
    }

}
