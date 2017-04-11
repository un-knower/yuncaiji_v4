package cn.uway.framework.task.worker.luc;

import java.io.InputStream;
import java.util.List;

import cn.uway.framework.task.GatherPathEntry;

/**
 * 朗讯EVDO切割器参数
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public class EvdoCutterParam {

	/**
	 * EVDO话单文件名
	 */
	private String evdoName;

	/**
	 * 任务ID，用于标记任务ID
	 */
	private long taskId;
	
	/**
	 * bscID，用于区分不同的任务存储在不同的目录下
	 */
	private long bscId;

	/**
	 * EVDOPCMD话单telnet文件流
	 */
	private InputStream inputstream;

	/**
	 * 采集路径容器<br>
	 * 拆分线程完成拆分后将需要提交采集的文件添加到pathEntries中。由外部进行提交<br>
	 */
	private List<GatherPathEntry> pathEntries;

	/**
	 * 是否历史文件<br>
	 * 拆分器中会对历史文件和实时文件采取两种不同的策略进行处理<br>
	 */
	private boolean historyFlag;

	/**
	 * 总共的文件行数<br>
	 * 只有historyFlag=true时生效<br>
	 */
	private int totalLineNum = 0;
	
	/**
	 * 文件大小   单位：k
	 */
	private long size = 0;

	/**
	 * 命令
	 */
	private String order;

	/**
	 * @return order
	 */
	public String getOrder() {
		return order;
	}

	/**
	 * @param order
	 */
	public void setOrder(String order) {
		this.order = order;
	}

	/**
	 * 
	 * @return the evdoName
	 */
	public String getEvdoName() {
		return evdoName;
	}

	/**
	 * @param evdoName
	 *            the evdoName to set
	 */
	public void setEvdoName(String evdoName) {
		this.evdoName = evdoName;
	}

	/**
	 * @return the taskId
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId
	 *            the taskId to set
	 */
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	
	/**
	 * @return bscId
	 */
	public long getBscId() {
		return bscId;
	}

	/**
	 * @param bscId
	 */
	public void setBscId(long bscId) {
		this.bscId = bscId;
	}

	/**
	 * @return the inputstream
	 */
	public InputStream getInputstream() {
		return inputstream;
	}

	/**
	 * @param inputstream
	 *            the inputstream to set
	 */
	public void setInputstream(InputStream inputstream) {
		this.inputstream = inputstream;
	}

	/**
	 * @return the pathEntries
	 */
	public List<GatherPathEntry> getPathEntries() {
		return pathEntries;
	}

	/**
	 * @param pathEntries
	 *            the pathEntries to set
	 */
	public void setPathEntries(List<GatherPathEntry> pathEntries) {
		this.pathEntries = pathEntries;
	}

	/**
	 * @return the historyFlag
	 */
	public boolean isHistoryFlag() {
		return historyFlag;
	}

	/**
	 * @param historyFlag
	 *            the historyFlag to set
	 */
	public void setHistoryFlag(boolean historyFlag) {
		this.historyFlag = historyFlag;
	}

	/**
	 * @return the totalLineNum
	 */
	public int getTotalLineNum() {
		return totalLineNum;
	}

	/**
	 * @param totalLineNum
	 *            the totalLineNum to set
	 */
	public void setTotalLineNum(int totalLineNum) {
		this.totalLineNum = totalLineNum;
	}

	
	/**
	 * @return File size
	 */
	public long getSize() {
		return size;
	}

	
	/**
	 * @param size
	 */
	public void setSize(long size) {
		this.size = size;
	}

}
