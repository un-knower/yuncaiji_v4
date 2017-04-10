package cn.uway.ucloude.uts.spring.tasktracker;



import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;

/**
 * @author magic.s.g.xie
 */
public class JobRunnerHolder {

    private static final ILogger LOGGER = LoggerManager.getLogger(JobRunnerHolder.class);

    private static final Map<String, JobRunner> JOB_RUNNER_MAP = new ConcurrentHashMap<String, JobRunner>();

    static void add(String shardValue, JobRunner jobRunner) {
        JOB_RUNNER_MAP.put(shardValue, jobRunner);
    }

    public static JobRunner getJobRunner(String shardValue) {
        return JOB_RUNNER_MAP.get(shardValue);
    }

    public static void addUTSBean(Object bean) {
        Class<?> clazz = bean.getClass();
        Method[] methods = clazz.getMethods();
        if (methods != null && methods.length > 0) {
            for (final Method method : methods) {
                if (method.isAnnotationPresent(JobRunnerItem.class)) {
                    JobRunnerItem jobRunnerItem = method.getAnnotation(JobRunnerItem.class);
                    String shardValue = jobRunnerItem.shardValue();
                    if (StringUtil.isEmpty(shardValue)) {
                        LOGGER.error(clazz.getName() + ":" + method.getName() + " " + JobRunnerItem.class.getName() + " shardValue can not be null");
                        continue;
                    }
                    JobRunnerHolder.add(shardValue, JobRunnerBuilder.build(bean, method, method.getParameterTypes()));
                }
            }
        }
    }
}
