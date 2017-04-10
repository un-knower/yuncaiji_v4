package cn.uway.ucloude.uts.jobtracker.support;

import java.util.List;

import cn.uway.ucloude.uts.core.domain.JobRunResult;

public interface ClientNotifyHandler<T extends JobRunResult> {
	
	/**
     * 通知成功的处理
     */
    void handleSuccess(List<T> jobResults);

    /**
     * 通知失败的处理
     */
    void handleFailed(List<T> jobResults);

}
