package cn.uway.framework.task;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.framework.solution.GatherSolution;
import cn.uway.util.DbUtil;

/**
 * * 任务抽象类
 * <p>
 * 不区分任务类型，为任务的顶级基类.<br>
 * 其下分为周期性任务{@link PeriodTask}和非周期性任务{@link NoPeriodTask}。
 * </p>
 * 
 * @author chenrongqiang @ 2014-3-29
 */
public class Task{

	/**
	 * 周期性任务。
	 */
	public static final int TYPE_PERIOD = 1;

	/**
	 * 非周期性任务。
	 */
	public static final int TYPE_NOPERIOD = 0;

	/**
	 * 任务开始运行时间
	 */
	private Date beginRuntime;

	/**
	 * 任务编号
	 */
	protected long id;

	/**
	 * 任务名
	 */
	protected String name;

	/**
	 * 任务附加信息，主要包含网元等相关信息
	 */
	protected ExtraInfo extraInfo;

	/**
	 * 任务描述
	 */
	protected String description;

	/**
	 * 数据源连接ID
	 */
	protected int connectionId;

	/**
	 * 采集解决方案编号
	 */
	protected long solutionId;

	protected GatherSolution gatherSolutionInfo;

	/**
	 * 超时时间，单位分钟
	 */
	protected int timeoutMinutes;

	/**
	 * 采集路径描述
	 */
	protected GatherPathDescriptor gatherPathDescriptor;

	/**
	 * 任务归属机器名
	 */
	protected String pcName;

	/**
	 * 任务归属分组编号
	 */
	protected long groupId;

	/**
	 * 数据时间
	 */
	protected Date dataTime;

	/**
	 * 数据结束时间
	 */
	protected Date endDataTime;

	/**
	 * 任务执行前的shell脚本
	 */
	protected String shellBefore;

	/**
	 * 任务执行后的shell脚本
	 */
	protected String shellAfter;

	/**
	 * 执行shell脚本的超时时间
	 */
	protected int shellTimeout;
	
	/**
	 * 采集周期
	 */
	protected int period;

	/**
	 * 解析模板配置
	 */
	private String parserTemplates;

	/**
	 * 输出模板配置
	 */
	private String exportTemplates;

	/**
	 * 记录一个任务在一个周期的运行过程中产生的所有的补采信息，在运行结束后，统一提交到补采表。
	 * */
	private List<ReTask> regatherInfoStatics = new ArrayList<ReTask>();
	
	/**
	 * 时间处理函数
	 */
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 任务类型 <br>
	 * 0 - 单任务单job, <br>
	 * 1 - 单任务多job, <br>
	 * 2 - 单FTP任务多job
	 */
	protected int workerType;
	
	protected int sqlForDelayDataList;

	
	protected String cityIdWcdr;
	
	/**
	 * 延迟任务扫描周期
	 */
	protected int delayDataScanPeriod;

	/**
	 * 延迟任务扫描结束时延
	 */
	protected int delayDataTimeDelay;
	
	public String getCityIdWcdr() {
		return cityIdWcdr;
	}

	
	public void setCityIdWcdr(String cityIdWcdr) {
		this.cityIdWcdr = cityIdWcdr;
	}

	/**
	 * 获取任务编号
	 */
	public long getId(){
		return id;
	}

	public void setId(long id){
		this.id = id;
	}

	/**
	 * 获取任务名称
	 */
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	/**
	 * 获取任务描述信息
	 */
	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

	/**
	 * 获取任务归属的机器名
	 */
	public String getPcName(){
		return pcName;
	}

	public void setPcName(String pcName){
		this.pcName = pcName;
	}

	/**
	 * 获取任务超时时间,单位分钟
	 */
	public int getTimeoutMinutes(){
		return timeoutMinutes;
	}

	public void setTimeoutMinutes(int timeoutMinutes){
		this.timeoutMinutes = timeoutMinutes;
	}

	/**
	 * 获取任务采集路径描述对象
	 * 
	 * @return {@link GatherPathDescriptor}
	 */
	public GatherPathDescriptor getGatherPathDescriptor(){
		return gatherPathDescriptor;
	}

	public void setGatherPathDescriptor(GatherPathDescriptor gatherPathDescriptor){
		this.gatherPathDescriptor = gatherPathDescriptor;
	}

	/**
	 * 获取任务归属的分组编号
	 */
	public long getGroupId(){
		return groupId;
	}

	public void setGroupId(long groupId){
		this.groupId = groupId;
	}

	public Date getDataTime(){
		return dataTime;
	}

	public void setDataTime(Date dataTime){
		this.dataTime = dataTime;
	}

	public Date getEndDataTime(){
		return endDataTime;
	}

	public void setEndDataTime(Date endDataTime){
		this.endDataTime = endDataTime;
	}

	public int getWorkerType(){
		return workerType;
	}

	public void setWorkerType(int workerType){
		this.workerType = workerType;
	}

	/**
	 * @return the beginRuntime
	 */
	public Date getBeginRuntime(){
		return beginRuntime;
	}

	/**
	 * @param beginRuntime the beginRuntime to set
	 */
	public void setBeginRuntime(Date beginRuntime){
		this.beginRuntime = beginRuntime;
	}

	public String getParserTemplates(){
		return parserTemplates;
	}

	public void setParserTemplates(String parserTemplates){
		this.parserTemplates = parserTemplates;
	}

	public String getExportTemplates(){
		return exportTemplates;
	}

	public void setExportTemplates(String exportTemplates){
		this.exportTemplates = exportTemplates;
	}

	/**
	 * 从数据库结果集中组装任务对象
	 * 
	 * @param rs
	 * @throws Exception
	 */
	public void loadTask(final ResultSet rs) throws Exception{
		this.setId(rs.getLong("task_id"));
		this.setName(rs.getString("task_name"));
		this.setWorkerType(rs.getInt("worker_type"));
		// 任务附加信息
		ExtraInfo extraInfo = new ExtraInfo(rs.getInt("city_id"), rs.getInt("omc_id"), rs.getInt("bsc_id"),
				rs.getInt("net_type"));
		this.setExtraInfo(extraInfo);
		ResultSetMetaData resultSetMetaData = rs.getMetaData();
		GatherPathDescriptor gatherPathDescriptor = null;
		int index = findCloumnIndex(resultSetMetaData, "gather_path");
		if(-1 != index
				&& (resultSetMetaData.getColumnType(index) == Types.BLOB || resultSetMetaData.getColumnType(index) == Types.CLOB)){
			gatherPathDescriptor = new GatherPathDescriptor(DbUtil.ClobParse(rs.getClob("gather_path")));
		}else{
			gatherPathDescriptor = new GatherPathDescriptor(rs.getString("gather_path"));
		}
		this.setGatherPathDescriptor(gatherPathDescriptor);
		this.setDataTime(new Date(rs.getTimestamp("data_time").getTime()));
		this.setEndDataTime(rs.getTimestamp("end_data_time"));
		this.setSolutionId(rs.getLong("solution_id"));
		this.setConnectionId(rs.getInt("conn_id"));
		this.setShellBefore(rs.getString("shell_before_gather"));
		this.setShellAfter(rs.getString("shell_after_gather"));
		this.setShellTimeout(rs.getInt("shell_timeout"));
		this.setTimeoutMinutes(rs.getInt("timeout"));
		this.setPcName(rs.getString("pc_name"));
		this.setGroupId(rs.getLong("group_id"));
		this.setDescription(rs.getString("task_description"));
		this.setParserTemplates(rs.getString("paser_templates"));
		this.setExportTemplates(rs.getString("export_templates"));
		this.setPeriod(rs.getInt("period"));
		this.setDelayDataScanPeriod(rs.getInt("delay_data_scan_period"));
		this.setDelayDataTimeDelay(rs.getInt("delay_data_time_delay"));
	}

	private int findCloumnIndex(ResultSetMetaData resultSetMetaData, String name) throws Exception{
		int count = resultSetMetaData.getColumnCount();
		for(int i = 1; i <= count; i++){
			String columnName = resultSetMetaData.getColumnName(i);
			if(columnName.equalsIgnoreCase(name)){
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return the solutionId
	 */
	public long getSolutionId(){
		return solutionId;
	}

	/**
	 * @param solutionId the solutionId to set
	 */
	public void setSolutionId(long solutionId){
		this.solutionId = solutionId;
	}

	public GatherSolution getGatherSolutionInfo(){
		return gatherSolutionInfo;
	}

	public void setGatherSolutionInfo(GatherSolution gatherSolutionInfo){
		this.gatherSolutionInfo = gatherSolutionInfo;
	}

	public String getDateString(Date date){
		if(date == null)
			return "";
		return this.dateFormat.format(date);
	}

	/**
	 * @return the shellBefore
	 */
	public String getShellBefore(){
		return shellBefore;
	}

	/**
	 * @param shellBefore the shellBefore to set
	 */
	public void setShellBefore(String shellBefore){
		this.shellBefore = shellBefore;
	}

	/**
	 * @return the shellAfter
	 */
	public String getShellAfter(){
		return shellAfter;
	}

	/**
	 * @param shellAfter the shellAfter to set
	 */
	public void setShellAfter(String shellAfter){
		this.shellAfter = shellAfter;
	}

	/**
	 * @return the shellTimeout
	 */
	public int getShellTimeout(){
		return shellTimeout;
	}

	/**
	 * @return the connectionID
	 */
	public int getConnectionId(){
		return connectionId;
	}

	/**
	 * @param connectionID the connectionID to set
	 */
	public void setConnectionId(int connectionId){
		this.connectionId = connectionId;
	}

	/**
	 * @param shellTimeout the shellTimeout to set
	 */
	public void setShellTimeout(int shellTimeout){
		this.shellTimeout = shellTimeout;
	}

	/**
	 * @return the extraInfo
	 */
	public ExtraInfo getExtraInfo(){
		return extraInfo;
	}

	/**
	 * @param extraInfo the extraInfo to set
	 */
	public void setExtraInfo(ExtraInfo extraInfo){
		this.extraInfo = extraInfo;
	}

	
	
	public List<ReTask> getRegatherInfoStatics() {
		return regatherInfoStatics;
	}
	
	public void addRegatherInfoStatics(ReTask regatherInfo) {
		this.regatherInfoStatics.add(regatherInfo);
	}
	
	public int getPeriod() {
		return period;
	}
	
	public void setPeriod(int period) {
		this.period = period;
	}

	public int getDelayDataScanPeriod() {
		return delayDataScanPeriod;
	}

	public void setDelayDataScanPeriod(int delayDataScanPeriod) {
		if(delayDataScanPeriod<=0)
			return;
		this.delayDataScanPeriod = delayDataScanPeriod;
	}

	public int getDelayDataTimeDelay() {
		return delayDataTimeDelay;
	}

	public void setDelayDataTimeDelay(int delayDataTimeDelay) {
		if(delayDataTimeDelay<=0)
			return;
		this.delayDataTimeDelay = delayDataTimeDelay;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataTime == null) ? 0 : dataTime.hashCode());
		result = prime * result + (int)(id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Task other = (Task)obj;
		if(dataTime == null){
			if(other.dataTime != null)
				return false;
		}else if(!dataTime.equals(other.dataTime))
			return false;
		if(id != other.id)
			return false;
		return true;
	}

}
