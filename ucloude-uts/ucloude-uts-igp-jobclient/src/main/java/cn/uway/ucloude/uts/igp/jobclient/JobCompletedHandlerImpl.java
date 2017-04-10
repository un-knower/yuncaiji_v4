package cn.uway.ucloude.uts.igp.jobclient;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.domain.JobResult;
import cn.uway.ucloude.uts.jobclient.support.JobCompletedHandler;

/**
 * @author magic.s.g.xie
 */
public class JobCompletedHandlerImpl implements JobCompletedHandler {

    @Override
    public void onComplete(List<JobResult> jobResults) {
        // 任务执行反馈结果处理
        if (CollectionUtil.isNotEmpty(jobResults)) {
            for (JobResult jobResult : jobResults) {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 任务执行完成：" + jobResult);
            }
        }
    }
}
