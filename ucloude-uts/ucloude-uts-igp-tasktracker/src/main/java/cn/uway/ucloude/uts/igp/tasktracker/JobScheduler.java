package cn.uway.ucloude.uts.igp.tasktracker;


import org.springframework.beans.factory.annotation.Autowired;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.spring.tasktracker.JobRunnerItem;
import cn.uway.ucloude.uts.spring.tasktracker.UTS;
import cn.uway.ucloude.uts.tasktracker.Result;
import cn.uway.ucloude.uts.tasktracker.logger.BizLogger;
import cn.uway.ucloude.uts.tasktracker.runner.TaskLoggerFactory;

/**
 * @author magic.s.g.xie
 */
@UTS
public class JobScheduler {

    private static final ILogger LOGGER = LoggerManager.getLogger(JobScheduler.class);



    @JobRunnerItem(shardValue = "111")
    public Result runJob1(Job job) throws Throwable {
        try {
            Thread.sleep(1000L);


            LOGGER.info("runJob1 我要执行：" + job);
            BizLogger bizLogger = TaskLoggerFactory.getBizLogger();
            // 会发送到 UTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_LATER, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }

    @JobRunnerItem(shardValue = "222")
    public void runJob2() throws Throwable {
        try {

            LOGGER.info("runJob2 我要执行");
            BizLogger bizLogger = TaskLoggerFactory.getBizLogger();
            // 会发送到 UTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");
        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
        }
    }

}
