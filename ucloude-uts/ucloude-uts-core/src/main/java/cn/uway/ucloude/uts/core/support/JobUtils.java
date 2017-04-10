package cn.uway.ucloude.uts.core.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.support.bean.BeanCopier;
import cn.uway.ucloude.support.bean.BeanCopierFactory;
import cn.uway.ucloude.support.bean.PropConverter;
import cn.uway.ucloude.utils.BeanUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;

public class JobUtils {
	private static final BeanCopier<Job, Job> JOB_BEAN_COPIER;
    private static final BeanCopier<JobPo, JobPo> JOB_PO_BEAN_COPIER;

    static {
        Map<String, PropConverter<?, ?>> jobPoConverterMap = new ConcurrentHashMap<String, PropConverter<?, ?>>(1);
        // 目前只有这个 extParams和 internalExtParams 不是基本类型, 为了不采用流的方式, 从而提升性能
        jobPoConverterMap.put("extParams", new PropConverter<JobPo, Map<String, String>>() {
            @Override
            public Map<String, String> convert(JobPo jobPo) {
                return BeanUtil.copyMap(jobPo.getExtParams());
            }
        });
        jobPoConverterMap.put("internalExtParams", new PropConverter<JobPo, Map<String, String>>() {
            @Override
            public Map<String, String> convert(JobPo jobPo) {
                return BeanUtil.copyMap(jobPo.getInternalExtParams());
            }
        });
        JOB_PO_BEAN_COPIER = BeanCopierFactory.createCopier(JobPo.class, JobPo.class, jobPoConverterMap);

        Map<String, PropConverter<?, ?>> jobConverterMap = new ConcurrentHashMap<String, PropConverter<?, ?>>(1);
        // 目前只有这个 extParams不是基本类型, 为了不采用流的方式, 从而提升性能
        jobConverterMap.put("extParams", new PropConverter<Job, Map<String, String>>() {
            @Override
            public Map<String, String> convert(Job job) {
                return BeanUtil.copyMap(job.getExtParams());
            }
        });
        JOB_BEAN_COPIER = BeanCopierFactory.createCopier(Job.class, Job.class, jobConverterMap);
    }

    public static long getRepeatNextTriggerTime(JobPo jobPo) {
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(ExtConfigKeys.FIRST_FIRE_TIME));
        long now = SystemClock.now();
        long remainder = (now - firstTriggerTime) % jobPo.getRepeatInterval();
        if (remainder == 0) {
            return now;
        }
        return now + (jobPo.getRepeatInterval() - remainder);
    }

    public static boolean isRelyOnPrevCycle(JobPo jobPo) {
        return jobPo.getRelyOnPrevCycle() == null || jobPo.getRelyOnPrevCycle();
    }

    public static String generateJobId() {
        return StringUtil.generateUUID();
    }

    public static String generateExeSeqId(JobPo jobPo){
        return String.valueOf(jobPo.getTriggerTime());
    }

    public static Job copy(Job source) {
        Job job = new Job();
        JOB_BEAN_COPIER.copyProps(source, job);
        return job;
    }

    public static JobPo copy(JobPo source) {
        JobPo jobPo = new JobPo();
        JOB_PO_BEAN_COPIER.copyProps(source, jobPo);
        return jobPo;
    }
}
