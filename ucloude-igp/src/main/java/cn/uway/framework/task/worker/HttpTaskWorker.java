package cn.uway.framework.task.worker;

import java.io.InputStream;
import java.util.Date;

import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

public class HttpTaskWorker extends AbstractTaskWorker {
	
//	private HttpConnectionInfo sourceConnectionInfo;
	
	/**
	 * http 读取流
	 */
	InputStream inputStream = null;

	public HttpTaskWorker(Task task) {
		super(task);
//		if (!(connInfo instanceof HttpConnectionInfo))
//			throw new IllegalArgumentException("连接信息不正确，错误的连接类型");
//		sourceConnectionInfo = (HttpConnectionInfo) connInfo;
	}

	@Override
	public void beforeWork() {
		//将任务中时间通配符用采集时间代替，然后加入到采集对象pathEntries中去，其中对象的path为带参url请求
		GatherPathDescriptor gatherPaths = task.getGatherPathDescriptor();
		if(task instanceof PeriodTask){
			//结束时间  减去一秒钟，到59:59
			int minutes = ((PeriodTask) task).getPeriodMinutes();
			Date endDate = TimeUtil.nextTime(task.getDataTime(), minutes);
			endDate = new Date(endDate.getTime() - 1 * 1000);
			//要求数据库中配置的路径，每个路径都必须包含起始时间与结束时间通配，日期搭配为起始日期在前，结束日期在后，并相间隔
			String rawData = gatherPaths.getRawData();
			while(rawData.lastIndexOf("%%Y") > -1){
				//替换开始时间
				rawData = StringUtil.convertCollectPathFirstDate(rawData, task.getDataTime());
				//替换结束时间
				rawData = StringUtil.convertCollectPathFirstDate(rawData, endDate);
			}
			gatherPaths = new GatherPathDescriptor(rawData);
		}
		pathEntries = gatherPaths.getPaths();
	}

	@Override
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		return true;
	}

	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		return true;
	}

	/**
	 * 获取任务并发线程数，取采集对象数、系统配置最大并发数的最小值
	 */
	@Override
	protected int getMaxConcurentJobThreadCount() {
		return Math.min(systemMaxJobConcurrent, pathEntries.size());
	}

}
