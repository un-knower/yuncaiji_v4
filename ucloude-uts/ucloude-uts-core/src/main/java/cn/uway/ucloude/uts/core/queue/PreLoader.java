package cn.uway.ucloude.uts.core.queue;

import cn.uway.ucloude.uts.core.queue.domain.JobPo;

/**
 * 任务预加载
 * @author uway
 *
 */
public interface PreLoader {
	public JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity);

	/**
	 * 如果taskTrackerNodeGroup为空，那么load所有的
	 */
	public void load(String taskTrackerNodeGroup);

	/**
	 * 加载某个任务并放置第一个
	 */
	public void loadOne2First(String taskTrackerNodeGroup, String jobId);
}
