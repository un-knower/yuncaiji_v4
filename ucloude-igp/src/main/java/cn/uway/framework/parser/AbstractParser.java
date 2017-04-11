package cn.uway.framework.parser;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.AbstractJob;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;

/**
 * AbstractParser
 * 
 * @author chenrongqiang 2012-11-3
 */
public abstract class AbstractParser implements Parser{
	
	//国际码
	protected String icode =AppContext.getBean("international_code", String.class);

	//电话号码匹配
	protected Pattern phonePtn = Pattern.compile("^86(106\\d{10}+|\\d{11}+)|^(85[23]\\d{8}+)");
	
	/**
	 * 解析模板字段
	 */
	protected String templates;

	/**
	 * 失败原因
	 */
	protected String cause;

	/**
	 * 接入开始时间
	 */
	protected Date startTime;

	// 接入结束时间
	protected Date endTime;

	/**
	 * 解析后总条数
	 */
	protected long totalNum;

	/**
	 * 无效记录数
	 */
	protected long invalideNum;

	/**
	 * 解析失败总条数
	 */
	protected long parseFailNum;

	/**
	 * 解析成功总条数
	 */
	protected long parseSucNum;

	/**
	 * 当前解析条数
	 */
	protected long currentNum;

	/**
	 * 当前解析对象数据时间
	 */
	public Date currentDataTime;

	/**
	 * 原始数据行数。
	 */
	protected long fileLines = -1;

	/**
	 * 获取数据时间成功标志
	 */
	protected boolean dataTimeFlag = false;

	/**
	 * 任务信息
	 */
	public Task task;

	// 当前解码任务的job
	protected AbstractJob job;

	public String getTemplates(){
		return templates;
	}

	public void setTemplates(String templates){
		this.templates = templates;
	}

	public Date getCurrentDataTime(){
		if(task == null)
			return null;
		if(task instanceof PeriodTask){
			if(task instanceof ReTask)
				return ((ReTask)task).getRegather_datetime();
			return task.getDataTime();
		}
		return currentDataTime;
	}

	public void setDataTime(Date dataTime){
		this.currentDataTime = dataTime;
	}

	public Date getStartTime(){
		return startTime;
	}

	public Date getEndTime(){
		return endTime;
	}

	public long getTotalNum(){
		return totalNum;
	}

	public long getInvalideNum(){
		return invalideNum;
	}

	public long getParseFailNum(){
		return parseFailNum;
	}

	public long getParseSucNum(){
		return parseSucNum;
	}

	public long getCurrentNum(){
		return currentNum;
	}

	@Override
	public ParserReport getReport(){
		ParserReport parserReport = new ParserReport();
		parserReport.setStartTime(startTime);
		parserReport.setEndTime(endTime);
		parserReport.setTotalNum(totalNum);
		parserReport.setInvalideNum(invalideNum);
		parserReport.setParseFailNum(parseFailNum);
		parserReport.setParseSucNum(parseSucNum);
		parserReport.setFileLines(fileLines);
		parserReport.setCause(cause);
		return parserReport;
	}

	public void destory(){}

	@Override
	public void before(){
		if(this.startTime == null){
			this.startTime = new Date();
		}
	}

	public void after() throws Exception{}

	@Override
	public void setCurrentJob(AbstractJob currJob){
		this.job = currJob;
	}

	@Override
	public AbstractJob getCurrentJob(){
		return this.job;
	}

	@Override
	public void init(JobParam jobParam){
		this.task = jobParam.getTask();
		this.startTime = new Date();
	}

	@Override
	public void afterClose(){
		if(this.endTime != null){
			this.endTime = new Date();
		}
	}
	
	@Override
	public boolean canAccess(String fileName) {
		return true;
	}
	
	/**
	 * 根据指定的数据类型，创建对应的输出字段数据Map;
	 * 
	 * @param dataType 数据类型
	 * @return 如果输出模板配置正确，将输出ArrayMap,否则输出HashMap
	 */
	public Map<String,String> createExportPropertyMap(int dataType){
		return job.createExportPropertyMap(dataType);
	}
	
	public String getPhoneNum(String pStr){
		if (pStr == null)
			return null;
		
		int cutLength = pStr.length();
		for (; cutLength>0; --cutLength) {
			char c = pStr.charAt(cutLength - 1);
			if (c >= '0' && c <= '9') 
				break;
		}
		
		
		int startPos = 0;
		if (pStr.charAt(0) == '8' && pStr.charAt(1) == '6')
			startPos = 2;
		
		return pStr.substring(startPos, cutLength);
	}
}
