package cn.uway.ucloude.uts.tasktracker.runner;

/**
 * Job Runner 的工厂类
 * @author uway
 *
 */
public interface RunnerFactory {

    public JobRunner newRunner();
}
