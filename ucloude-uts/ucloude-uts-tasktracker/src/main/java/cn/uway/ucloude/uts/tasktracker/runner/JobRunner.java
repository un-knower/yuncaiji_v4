package cn.uway.ucloude.uts.tasktracker.runner;

import cn.uway.ucloude.uts.tasktracker.Result;

/**
 * 任务执行者要实现的接口
 * @author uway
 *
 */
public interface JobRunner {
    /**
     * 执行任务
     * 抛出异常则消费失败, 返回null则认为是消费成功
     */
	Result run(JobContext context) throws Throwable;
}
