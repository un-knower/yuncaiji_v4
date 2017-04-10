package cn.uway.ucloude.uts.tasktracker.runner;

import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.tasktracker.domain.Response;

public interface RunnerCallback {
    /**
     * 执行完成, 可能是成功, 也可能是失败
     * @return 如果有新的任务, 那么返回新的任务过来
     */
    public JobMeta runComplete(Response response);
}
