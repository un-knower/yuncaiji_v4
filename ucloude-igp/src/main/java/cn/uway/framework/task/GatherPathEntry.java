package cn.uway.framework.task;

import java.sql.Timestamp;
import java.util.Date;

import cn.uway.framework.task.worker.TaskWorkerFactory;
import cn.uway.util.StringUtil;

/**
 * 采集路径实体类
 * <p>
 * 描述单一的采集对象.
 * </p>
 * 
 * @author MikeYang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 * @see GatherPathDescriptor
 */
public class GatherPathEntry {

	private String path; // 单一路径

	private String convertedPath;

	private long size;

	private Date dateTime; // 文件时间

	private boolean isLast;// lucDo 是否是最后一个文件，*55.EVDOPCMD

	private Date ftpFileTimestamp;
	
	/**
	 * 子包文件后缀过滤器
	 */
	private String packSubFileSuffixFilter;
	
	/**
	 * 文件规则表达式
	 */
	private String fileRuleExpression;
	
	public GatherPathEntry() {
		super();
	}

	/**
	 * 构造方法。
	 * 
	 * @param path
	 *            单一路径。
	 */
	public GatherPathEntry(String path) {
		super();
		
		int posSubFileSuffix = path.indexOf("::");
		if (posSubFileSuffix>0) {
			int posLeftExpressionBracket = path.indexOf("${");
			if (posLeftExpressionBracket>0) {
				int posRightExpressionBracket = path.indexOf("}", posLeftExpressionBracket);
				if (posRightExpressionBracket > 0) {
					this.fileRuleExpression = path.substring(posLeftExpressionBracket+2, posRightExpressionBracket).trim();
				}
				
				this.path = path.substring(0, posSubFileSuffix).trim();
			} else {
				if (posSubFileSuffix < (path.length()-2)) {
					this.packSubFileSuffixFilter = path.substring(posSubFileSuffix+2).trim();
				}
				this.path = path.substring(0, posSubFileSuffix).trim();
			}
		} else {
			this.path = path;
		}
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * 获取单一路径
	 */
	public String getPath() {
		return path;
	}

	public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	public Date getFtpFileTimestamp() {
		return ftpFileTimestamp;
	}

	public void setFtpFileTimestamp(Date ftpFileTimestamp) {
		this.ftpFileTimestamp = ftpFileTimestamp;
	}

	public String getConvertedPath(Date dataTime, int workType) {
		if (this.convertedPath != null)
			return this.convertedPath;
		
		if(TaskWorkerFactory.isLogCltInsert(workType))
		{
			//按IGP1中的方式转路径。
			this.convertedPath = StringUtil.ParseFilePath (this.path, new Timestamp(dataTime.getTime()));
		}
		else
		{
			this.convertedPath = StringUtil.convertCollectPath(this.path, dataTime);
		}
		
		
		return convertedPath;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	
	public String getPackSubFileSuffixFilter() {
		return packSubFileSuffixFilter;
	}

	public void setPackSubFileSuffixFilter(String packSubFileFilter) {
		this.packSubFileSuffixFilter = packSubFileFilter;
	}
	
	public String getFileRuleExpression() {
		return fileRuleExpression;
	}
	
	public void setFileRuleExpression(String fileRuleExpression) {
		this.fileRuleExpression = fileRuleExpression;
	}

	@Override
	public String toString() {
		return "GatherPathEntry [path=" + path + ", size=" + size
				+ ", packSubFileFilter=" + packSubFileSuffixFilter + "]";
	}
	
	
}
