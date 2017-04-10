package cn.uway.ucloude.uts.tasktracker.runner;

/**
 * 实现这个类可以自定义在中断时候的操作
 * @author uway
 *
 */
public interface InterruptibleJobRunner {
	 /**
     * 当任务被cancel(中断)的时候,调用这个
     */
    void interrupt();
}
