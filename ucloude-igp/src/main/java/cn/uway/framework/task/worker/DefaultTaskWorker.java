package cn.uway.framework.task.worker;

import java.util.Date;
import java.util.List;

import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.util.StringUtil;

/**
 * DefaultTaskWorker
 * 
 * @author chenrongqiang 2012-12-5
 */
public class DefaultTaskWorker extends AbstractTaskWorker {

	public DefaultTaskWorker(Task task) {
		super(task);
	}

	protected int getMaxConcurentJobThreadCount() {
		int maxConcurrentJobThreadNum = this.pathEntries.size();
		maxConcurrentJobThreadNum = Math.min(maxConcurrentJobThreadNum, this.systemMaxJobConcurrent);
		return maxConcurrentJobThreadNum;
	}

	public void beforeWork() {
		// 正常采集 task开始运行前不需要做任何操作
		GatherPathDescriptor gatherPaths = task.getGatherPathDescriptor();
		if (task instanceof PeriodTask) {
			String rawData = gatherPaths.getRawData();
			Date dataTime = task.getDataTime();
			// 补采
			if (task instanceof ReTask) {
				dataTime = ((ReTask) task).getRegather_datetime();
			}
			rawData = StringUtil.convertCollectPath(rawData, dataTime);
			gatherPaths = new GatherPathDescriptor(rawData);
		}
		List<GatherPathEntry> sourceEnrties = gatherPaths.getPaths();
		for (GatherPathEntry sourceEntry : sourceEnrties) {
			if (checkGatherObject(sourceEntry.getPath(), sourceEntry.getDateTime())) {
				pathEntries.add(sourceEntry);
			}
		}
	}

	@Override
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		return true;
	}

	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		return true;
	}
}
