package cn.uway.framework.status;

import java.util.Date;

import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.util.StringUtil;

/**
 * GatherObjStatus
 * 
 * @author chenrongqiang 2012-11-13
 */
public class Status {

	/**
	 * 增加采集开始状态是为了避免重复采集
	 */
	public final static int JOB_COMMIT = 0;	//提交任务
	
	public final static int GATHER_BEGIN = 1; // 采集开始

	public final static int DATA_ACCESS = 2; // 数据接入开始
	
	public final static int DATA_PARSE = 3; // 数据解析开始
	
	public final static int DATA_EXPORT = 4; // 数据入库开始
	
	public final static int DATA_ACCESS_FAIL = -1; // 数据接入失败
	
	public final static int DATA_PARSE_FAIL = -2; // 数据解析失败
	
	public final static int DATA_EXPORT_FAIL = -3; // 数据入库失败
	
	public final static int EXPORT_START = 1;	//开始输出
	
	public final static int EXPORT_ERROR = -1;	//输出错误
	
	public final static int FINISH_SUCCESS = 9; // 数据采集或入库成功

	private long id;

	private volatile String gatherObj; // 采集对象

	private volatile String subGatherObj; // 采集子对象

	private long taskId; // 任务编号

	private volatile Date dataTime; // 数据时间

	private volatile Date accessStartTime; // 开始接入时间

	private volatile Date accessEndTime; // 完成接入时间

	private volatile String accessCause; // 接入失败原因

	private volatile Date parseStartTime; // 开始解析时间

	private volatile Date parseEndTime; // 完成解析时间

	private volatile String parseCause; // 解析失败原因

	private volatile Date warehouseStartTime; // 入库开始时间

	private volatile Date warehouseEndTime; // 入库结束时间

	private volatile String warehousePoint; // 入库点

	private volatile String warehouseCause; // 入库失败原因

	private volatile int status; // 状态

	private volatile String pcName; // 采集计算机名称

	private volatile String gatherNum; // 采集条数

	private volatile int exportStatus = 0; // warehouse输出状态 0表示未全部输出 1表示全部输出

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGatherObj() {
		return gatherObj;
	}

	public String getSubGatherObj() {
		return subGatherObj;
	}

	public long getTaskId() {
		return taskId;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public Date getAccessStartTime() {
		return accessStartTime;
	}

	public Date getAccessEndTime() {
		return accessEndTime;
	}

	public String getAccessCause() {
		return accessCause;
	}

	public Date getParseStartTime() {
		return parseStartTime;
	}

	public Date getParseEndTime() {
		return parseEndTime;
	}

	public String getParseCause() {
		return parseCause;
	}

	public Date getWarehouseStartTime() {
		return warehouseStartTime;
	}

	public Date getWarehouseEndTime() {
		return warehouseEndTime;
	}

	/**
	 * 该信息在warehouse中会被修改
	 * 
	 * @return warehousePoint
	 */
	public String getWarehousePoint() {
		return warehousePoint;
	}

	public String getWarehouseCause() {
		return warehouseCause;
	}

	public int getStatus() {
		return status;
	}

	public String getPcName() {
		return pcName;
	}

	public void setGatherObj(String gatherObj) {
		this.gatherObj = gatherObj;
	}

	public void setSubGatherObj(String subGatherObj) {
		this.subGatherObj = subGatherObj;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	public void setAccessStartTime(Date accessStartTime) {
		this.accessStartTime = accessStartTime;
	}

	public void setAccessEndTime(Date accessEndTime) {
		this.accessEndTime = accessEndTime;
	}

	public void setAccessCause(String accessCause) {
		this.accessCause = accessCause;
	}

	public void setParseStartTime(Date parseStartTime) {
		this.parseStartTime = parseStartTime;
	}

	public void setParseEndTime(Date parseEndTime) {
		this.parseEndTime = parseEndTime;
	}

	public void setParseCause(String parseCause) {
		this.parseCause = parseCause;
	}

	public void setWarehouseStartTime(Date warehouseStartTime) {
		this.warehouseStartTime = warehouseStartTime;
	}

	public void setWarehouseEndTime(Date warehouseEndTime) {
		this.warehouseEndTime = warehouseEndTime;
	}

	public void setWarehousePoint(String warehousePoint) {
		this.warehousePoint = warehousePoint;
	}

	public void setWarehouseCause(String warehouseCause) {
		this.warehouseCause = warehouseCause;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setPcName(String pcName) {
		this.pcName = pcName;
	}

	public boolean isError() {
		return this.status == DATA_ACCESS_FAIL || status == DATA_PARSE_FAIL || status == DATA_EXPORT_FAIL;
	}

	@Override
	public String toString() {
		return "id=" + id + "gatherObj=" + gatherObj + "subGatherObj=" + subGatherObj + "taskId=" + taskId + "dataTime=" + dataTime
				+ "accessStartTime=" + accessStartTime + "accessEndTime=" + accessEndTime + "accessCause=" + accessCause + "parseStartTime="
				+ parseStartTime + "parseEndTime=" + parseEndTime + "parseCause=" + parseCause + "warehouseStartTime=" + warehouseStartTime
				+ "warehouseEndTime=" + warehouseEndTime + "warehousePoint=" + warehousePoint + "warehouseCause=" + warehouseCause + "status="
				+ status + "pcName=" + pcName + "gatherNum=" + gatherNum + "exportStatus=" + exportStatus;
	}

	public String getGatherNum() {
		return gatherNum;
	}

	public void setGatherNum(String gatherNum) {
		this.gatherNum = gatherNum;
	}

	public int getExportStatus() {
		return exportStatus;
	}

	public void setExportStatus(int exportStatus) {
		this.exportStatus = exportStatus;
	}

	public void init() {
		this.status = Status.GATHER_BEGIN;
	}

	public void initDataAccess() {
		this.status = Status.DATA_ACCESS;
	}

	public void initDataParse() {
		this.status = Status.DATA_PARSE;
	}

	public void initDataExport() {
		this.status = Status.DATA_EXPORT;
	}

	/**
	 * <pre>
	 * 	同步更新状态表，因为入库线程和解码线程分属不同的线程，
	 * 	如果非同步情况下，解码线程虽然将status改成了9，
	 * 	但入库线程并不能立刻刷新status的值，还会用5的值，重写到数据库，
	 * 	这样会导致文件状态不正确，而不重复采集
	 * </pre>
	 * 
	 * @param dao
	 * @param id
	 */
	public synchronized void updateBySynchronized(StatusDAO dao, long id) {
		dao.updateUnsynchronized(this, id);
	}

	public synchronized void updateExportStatusBySynchronized(StatusDAO dao, long id, int exportStatus) {
		dao.updateExportStatusUnsynchronized(id, exportStatus);
	}

	/**
	 * 断点信息处理
	 * 
	 * 加锁，解决断点同步问题 --modifyed by yuy 2014.9.19
	 * 
	 * @return
	 */
	public synchronized void breakPointProcess(int exportId, long breakPoint) {
		String oldWarehouseBreakPoint = this.getWarehousePoint();
		if (oldWarehouseBreakPoint == null || oldWarehouseBreakPoint.trim().isEmpty()) {
			this.setWarehousePoint(exportId + ":" + breakPoint + ";");
			return;
		}
		// 正则表达式问题，前面加个分号 --modifyed by yuy 2014.9.19
		String regex = ";" + exportId + ":\\d*";
		String pattern = StringUtil.getPattern(";" + this.getWarehousePoint(), regex);
		if (pattern == null || pattern.trim().isEmpty()) {
			oldWarehouseBreakPoint = oldWarehouseBreakPoint + exportId + ":" + breakPoint + ";";
			this.setWarehousePoint(oldWarehouseBreakPoint);
			return;
		}
		oldWarehouseBreakPoint = (";" + oldWarehouseBreakPoint).replace(pattern, ";" + exportId + ":" + breakPoint);
		this.setWarehousePoint(oldWarehouseBreakPoint.substring(1));
		return;
	}
}
