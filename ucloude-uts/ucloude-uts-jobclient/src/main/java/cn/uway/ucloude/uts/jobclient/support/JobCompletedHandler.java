package cn.uway.ucloude.uts.jobclient.support;

import java.util.List;

import cn.uway.ucloude.uts.core.domain.JobResult;

/**
 * 作業完成處理接口
 * @author uway
 *
 */
public interface JobCompletedHandler {
	/**
     * 处理返回结果
     */
    void onComplete(List<JobResult> jobResults);
}
