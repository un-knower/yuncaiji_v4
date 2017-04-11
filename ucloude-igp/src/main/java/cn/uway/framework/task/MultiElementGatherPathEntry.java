package cn.uway.framework.task;

import java.util.List;

/**
 * 一个采集对象包含多个采集路径<br>
 * 使用场景:多个文件需要打包在一起处理 ，多个文件看成在一个事务中处理
 * 
 * @author chenrongqiang @ 2013-4-29
 */
public class MultiElementGatherPathEntry extends GatherPathEntry {

	protected List<String> gatherPaths;

	protected List<String> convertedPaths;
	
	// 是否补采任务
	protected boolean repairTask;

	public MultiElementGatherPathEntry() {
		super();
	}

	public List<String> getGatherPaths() {
		return gatherPaths;
	}

	public void setGatherPaths(List<String> gatherPaths) {
		this.gatherPaths = gatherPaths;
	}

	public List<String> getConvertedPaths() {
		return convertedPaths;
	}

	public void setConvertedPaths(List<String> convertedPaths) {
		this.convertedPaths = convertedPaths;
	}

	/**
	 * @param path
	 */
	public MultiElementGatherPathEntry(String path) {
		super(path);
	}

	public MultiElementGatherPathEntry(List<String> gatherPaths) {
		this.gatherPaths = gatherPaths;
	}
	
	public boolean isRepairTask() {
		return repairTask;
	}

	public void setRepairTask(boolean repairTask) {
		this.repairTask = repairTask;
	}
}
