package cn.uway.framework.task.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.task.DelayTask;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.TimeUtil;

/**
 * 任务数据库访问类
 * 
 * @author chenrongqiang @ 2014-3-29
 */
public class TaskDAO {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(TaskDAO.class);

	/**
	 * 分布式应用名称
	 */
	public static final String distributeAppName = "distIgpv4";
	
	/**
	 * 数据源对象
	 */
	private BasicDataSource datasource;
	
	/**
	 * 当前采集程序PID
	 */
	private int pid;

	/**
	 * 查询任务列表的SQL语句。
	 */
	private String sqlForGetTaskList;

	/**
	 * 查询补采任务列表的SQL语句。
	 */
	private String sqlForGetRTaskList;

	/**
	 * 插入补采表的SQL语句
	 */
	private String sqlForInsertRTaskRecords;

	/**
	 * 修改补采表的SQL语句
	 */
	private String sqlForUpdateRTaskRecords;

	/**
	 * 将任务表时间点改到下一周期的SQL语句。
	 */
	private String sqlForSetTaskDataTimeToNextPeriod;

	/** 查询DB输出表记录语句。 */
	private String sqlForGetDBExportRecords;

	/** 修改补采任务状态的sql语句。 */
	private String sqlForUpdateReTaskStatus;

	/** 根据正常任务ID和补采路径查询补采任务是否存在的SQL。 */
	private String sqlForTestReTaskExists;

	/** 查询延迟数据任务的SQL语句 */
	private String sqlForDelayDataList;

	/** 添加延迟数据任务SQL语句 */
	private String sqlForDelayDataInsert;

	/** 更新延迟任务到下一时间点SQL语句 */
	private String sqlForDelayDataNextTime;

	/** 删除已经过期超过一天的延迟任务 */
	private String sqlForDelayDataDelete;

	/** 一个月最大的分钟数 , 即44640分钟 */
	private final static int MONTH_MINEUTE = (31 * 24 * 60);

	/**
	 * 获取正常任务列表
	 * 
	 * @return 正常任务列表
	 */
	public List<Task> execute(String sql, boolean isReCollect) {
		List<Task> tasks = new LinkedList<Task>();
		String pcName = getPcName();
		if (pcName == null || pcName.trim().isEmpty())
			return tasks;
		LOGGER.debug("查询" + (isReCollect ? "补采" : "") + "任务的SQL为：{}", sql);
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			LOGGER.debug("开始执行SQL={}，pcname={}", new Object[]{sql, pcName});
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pcName);
			pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				try {
					// 是否周期采集标识，1：周期采集，0：非周期采集
					int periodFlag = rs.getInt("is_period");
					// 加载周期性任务，包括补采任务
					if (periodFlag == Task.TYPE_PERIOD) {
						// 加载补采任务，补采表的任务都是周期性任务，非周期性任务不需要补采表
						if (isReCollect) {
							ReTask reTask = new ReTask();
							reTask.loadTask(rs);
							if (reTask.isReady())
								tasks.add(reTask);
							continue;
						}
						// 加载周期任务
						PeriodTask periodTask = new PeriodTask();
						periodTask.loadTask(rs);
						if (periodTask.isReady())
							tasks.add(periodTask);
						continue;
					}
					// 加载非周期正常采集任务
					Task task = new Task();
					task.loadTask(rs);
					tasks.add(task);
				} catch (Exception e) {
					LOGGER.error("查询任务异常：", e);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("查询任务异常:", e);
		} finally {
			DbUtil.close(rs, pstmt, conn);
		}
		return tasks;
	}

	/**
	 * 获取延迟数据任务列表
	 * 
	 * @return 延迟数据列表
	 */
	public List<Task> loadDelayTask(String sql) {
		List<Task> dtList = new ArrayList<Task>();
		String pcName = getPcName();
		if (pcName == null || pcName.trim().isEmpty())
			return dtList;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			LOGGER.debug("开始执行SQL={}，pcname={}", new Object[]{sql, pcName});
			Timestamp now = new Timestamp(new Date().getTime());
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pcName);
			pstmt.setTimestamp(2, now);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				try {
					DelayTask task = new DelayTask();
					task.loadTask(rs);
					int index = taskIndexInTaskList(task, dtList);
					if (index == -1) {
						dtList.add(task);
					} else {
						// 如果有两条相同(按task_id,data_time)的延迟任务，则保留最先创建的那条；
						DelayTask odTask = (DelayTask) dtList.get(index);
						if (odTask.getCreateTime().getTime() > task.getCreateTime().getTime()) {
							dtList.remove(index);
							dtList.add(task);
						}
					}
				} catch (Exception e) {
					LOGGER.error("查询任务异常：", e);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("查询任务异常:", e);
		} finally {
			DbUtil.close(rs, pstmt, conn);
		}
		return dtList;
	}

	public boolean insertNewDelayTask(Task task) {
		Timestamp currTime = new Timestamp(System.currentTimeMillis());
		LOGGER.debug("准备插入延迟数据任务记录，任务ID：{}，数据时间：{}，插入时间：{}。",
				new Object[]{task.getId(), TimeUtil.getDateString(task.getDataTime()), TimeUtil.getDateString(currTime)});
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = this.datasource.getConnection(); 
			ps = con.prepareStatement(sqlForDelayDataInsert);
			int idx = 1;
			// task_id
			ps.setLong(idx++, task.getId());
			// data_time
			ps.setTimestamp(idx++, new Timestamp(task.getDataTime().getTime()));
			// data_scan_curr_time
			ps.setTimestamp(idx++, new Timestamp(task.getDataTime().getTime() + task.getDelayDataScanPeriod() * 60 * 1000));
			// data_scan_end_time
			ps.setTimestamp(idx++, new Timestamp(task.getDataTime().getTime() + task.getDelayDataTimeDelay() * 60 * 1000));
			// create_time
			ps.setTimestamp(idx++, currTime);
			int effectCount = ps.executeUpdate();
			if (effectCount > 0)
				return true;
			LOGGER.warn("插入延迟数据任务失败，数据库返回的受影响行数为：{}", effectCount);
			return false;
		} catch (Exception e) {
			LOGGER.warn("插入延迟数据任务失败。", e);
			return false;
		}finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 修改延迟任务时间点为下一个时间点
	 * 
	 * @param task 延迟任务对象。
	 * @return 是否修改成功。
	 */
	public boolean skipDelayTaskNextTime(DelayTask task) {
		if (task == null)
			throw new NullPointerException("更新延迟任务时间失败,任务为空");
		String currDateTime = TimeUtil.getDateString(task.getDataScanCurrTime());
		Date nextTime = TimeUtil.nextTime(task.getDataScanCurrTime(), task.getDelayDataScanPeriod());
		String nextDateTime = TimeUtil.getDateString(nextTime);
		LOGGER.debug("准备修改任务时间点，延迟任务ID={},任务ID={},当前时间点={}", new Object[]{task.getDelayId(), task.getId(), currDateTime});
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sqlForDelayDataNextTime);
			ps.setTimestamp(1, new Timestamp(nextTime.getTime()));
			//ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			ps.setInt(2, task.getDelayId());
			int ret = ps.executeUpdate();
			if (ret > 0) {
				LOGGER.debug("修改延迟任务时间点成功，已将延迟任务id={}的时间点由{}改为下一执行时间点{}", new Object[]{task.getDelayId(), currDateTime, nextDateTime});
				return true;
			}
			LOGGER.error("修改延迟任务时间点失败");
			return false;
		} catch (SQLException e) {
			LOGGER.error("修改延迟任务时间点异常，ID=" + task.getDelayId(), e);
			return false;
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 删除以下三种情况的任务：<br/>
	 * 1.延迟任务(IGP_CFG_DELAY_DATA_TASK表中记录)在igp_cfg_task表中找不到对应记录; <br/>
	 * 2.取消延功能的任务；delay_data_time_delay、delay_data_scan_period为空的任务;<br/>
	 * 3.时效到期并且已经超过一天的任务；
	 * 
	 * @return 语句是否执行成功。
	 */
	public boolean deleteExpiredDelayTask() {
		Connection con = null;
		PreparedStatement ps = null;
		String pcName = getPcName();
		if (pcName == null || pcName.trim().isEmpty())
			return false;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sqlForDelayDataDelete);
			ps.setString(1, pcName);
			int ret = ps.executeUpdate();
			LOGGER.info("成功删除延迟任务{}条。", ret);
			return true;
		} catch (SQLException e) {
			LOGGER.error("删除延迟任务异常。", e);
			return false;
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 一次从数据库中查询出正常采集和补采任务 进入TaskQueue的任务没有补采的概念
	 * 
	 * @author chenrongqiang
	 * @return 待执行的任务列表
	 */
	public List<Task> loadTasks() {
		List<Task> taskList = execute(sqlForGetTaskList, false);
		List<Task> reTaskList = execute(sqlForGetRTaskList, true);
		if (reTaskList.size() > 0)
			taskList.addAll(reTaskList);

		List<Task> delayTaskList = loadDelayTask(sqlForDelayDataList);
		if (delayTaskList.size() > 0) {
			if (reTaskList.size() == 0) {
				taskList.addAll(delayTaskList);
			} else {
				for (Task task : delayTaskList) {
					// 此延迟任务在补采任务列表中是否存在同一任务号、同一时间点补采任务
					if (!taskInTaskList(task, reTaskList)) {
						taskList.add(task);
					}
				}
			}
		}
		LOGGER.debug("本次总共扫描到{}条待执行任务", taskList.size());
		return taskList;
	}

	/**
	 * 任务在任务列表中是否存在(按task_id,data_time)
	 * 
	 * @param task
	 * @param taskList
	 * @return 存在就返回true,不存在就返回false;
	 */
	private boolean taskInTaskList(Task task, List<Task> taskList) {
		return taskIndexInTaskList(task, taskList) >= 0;
	}

	/**
	 * 获取任务在任务列表中的索引(按task_id,data_time)
	 * 
	 * @param task
	 * @param taskList
	 * @return 如果存在就返回索引，不存在就返回－1
	 */
	private int taskIndexInTaskList(Task task, List<Task> taskList) {
		for (int i = 0; i < taskList.size(); i++) {
			Task newTask = taskList.get(i);
			if ((newTask.getId() == task.getId()) && newTask.getDataTime().equals(task.getId())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 根据正常任务ID，和补采路径，判断是否已经存在补采任务。
	 * 
	 * @param taskId
	 *            正常任务ID。
	 * @param gatherPath
	 *            补采路径。
	 * @return 是否已经存在补采任务。
	 */
	public boolean reTaskExists(long taskId, String gatherPath) {
		LOGGER.debug("准备判断补采任务是否存在，正常任务ID：{}，补采路径：{}，SQL:{}", new Object[]{taskId, gatherPath, sqlForTestReTaskExists});
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sqlForTestReTaskExists);
			ps.setLong(1, taskId);
			ps.setString(2, gatherPath);
			rs = ps.executeQuery();
			boolean b = false;
			if (rs.next()) {
				b = (rs.getInt(1) > 0);
			}
			LOGGER.info("判断补采任务成功，结果：{}。", (b ? "存在" : "不存在"));
			return b;
		} catch (Exception e) {
			LOGGER.warn("判断补采任务是否存在时出错。", e);
			return false;
		} finally {
			DbUtil.close(rs, ps, con);
		}
	}

	public boolean updateReTaskStatus(long reTaskId, int status, Date successDate) {
		LOGGER.debug("准备将补采任务{}的状态修改为{}，SQL语句为：{}", new Object[]{reTaskId, status, this.sqlForUpdateReTaskStatus});
		Connection con = null;
		PreparedStatement ps = null;
		int ret;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(this.sqlForUpdateReTaskStatus);
			ps.setInt(1, status);
			ps.setTimestamp(2, successDate != null ? new Timestamp(successDate.getTime()) : null);
			ps.setLong(3, reTaskId);
			ret = ps.executeUpdate();
			if (ret > 0) {
				LOGGER.debug("补采记录修改成功。");
				return true;
			}
			throw new Exception("SQL语句受影响行数为" + ret + "，可能补采记录被人为删除。");
		} catch (Exception ex) {
			LOGGER.warn("补采记录修改失败。", ex);
			return false;
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 修改周期性任务时间点为下一个时间点
	 * 
	 * @param task
	 *            周期性任务对象。
	 * @return 是否修改成功。
	 */
	public boolean skipTime(PeriodTask task) {
		if (task == null)
			throw new NullPointerException("更新任务时间失败,任务为空");
		String currDateTime = TimeUtil.getDateString(task.getDataTime());

		int period = task.getPeriodMinutes();
		String nextDateTime = "";
		if (period == MONTH_MINEUTE) {
			nextDateTime = TimeUtil.getDateString(TimeUtil.nextMonth(task.getDataTime(), 1));
		} else {
			nextDateTime = TimeUtil.getDateString(TimeUtil.nextTime(task.getDataTime(), task.getPeriodMinutes()));
		}

		LOGGER.debug("准备修改任务时间点，任务ID={}，当前时间点={}", new Object[]{task.getId(), currDateTime});
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sqlForSetTaskDataTimeToNextPeriod);
			ps.setLong(1, task.getId());
			int ret = ps.executeUpdate();
			if (ret > 0) {
				LOGGER.debug("修改任务时间点成功，已将任务id={}的时间点由{}改为下一执行时间点{}", new Object[]{task.getId(), currDateTime, nextDateTime});
				return true;
			}
			LOGGER.error("修改任务时间点失败");
			return false;
		} catch (SQLException e) {
			LOGGER.error("修改任务时间点异常，ID=" + task.getId(), e);
			return false;
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 插入一条补采记录。
	 * 
	 * @param taskId
	 *            任务编号。
	 * @param gatherPath
	 *            需要进行补采的采集路径。
	 * @param dataTime
	 *            补采的数据时间。
	 * @param cause
	 *            补采原因。
	 * @return 是否插入成功。
	 */
	public boolean insertIntoRTaskRecords(long taskId, String gatherPath, Date dataTime, String cause) {
		if (this.reTaskExists(taskId, gatherPath))
			return true;
		String pcName = getPcName();
		Timestamp currTime = new Timestamp(System.currentTimeMillis());
		LOGGER.debug("准备插入补采记录，任务ID：{}，补采原因：{}，采集路径：{}，数据时间：{}，插入时间：{}，补采机器名：{}。",
				new Object[]{taskId, cause, gatherPath, TimeUtil.getDateString(dataTime), TimeUtil.getDateString(currTime), pcName});
		try (Connection con = this.datasource.getConnection(); PreparedStatement ps = con.prepareStatement(sqlForInsertRTaskRecords)) {
			int idx = 1;
			ps.setLong(idx++, taskId);
			ps.setString(idx++, gatherPath);
			ps.setTimestamp(idx++, new Timestamp(dataTime.getTime()));
			ps.setString(idx++, pcName);
			ps.setTimestamp(idx++, currTime);
			ps.setString(idx++, cause);
			int effectCount = ps.executeUpdate();
			if (effectCount > 0)
				return true;
			LOGGER.warn("插入补采失败，数据库返回的受影响行数为：{}", effectCount);
			return false;
		} catch (Exception e) {
			LOGGER.warn("插入补采失败。", e);
			return false;
		}
	}

	/**
	 * 插入补采表
	 * 
	 * @param task
	 *            周期性任务对象。
	 * @return 是否修改成功。
	 */
	public boolean insertIntoRTaskRecords(PeriodTask task, String paths, String cause) {
		if (task == null)
			throw new NullPointerException("插入补采记录失败,任务为空");
		if (paths == null)
			throw new NullPointerException("插入补采记录失败,采集路径为空");
		String logStr = "任务ID=" + task.getId() + "，补采时间点=" + TimeUtil.getDateString(task.getDataTime());
		LOGGER.debug("准备插入补采记录，" + logStr);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sqlForInsertRTaskRecords);
			ps.setLong(1, task.getId());
			ps.setString(2, paths);
			ps.setTimestamp(3, new Timestamp(task.getDataTime().getTime()));
			ps.setString(4, task.getPcName());
			ps.setTimestamp(5, new Timestamp(new Date().getTime()));
			ps.setString(6, cause);
			ps.executeUpdate();
			LOGGER.debug("插入补采记录成功，" + logStr);
			return true;
		} catch (SQLException e) {
			LOGGER.error("插入补采记录异常，" + logStr, e);
			return false;
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 更新补采表
	 * 
	 * @param task
	 *            周期性任务对象。
	 * @return 是否修改成功。
	 */
	public boolean updateRTaskRecords(ReTask reTask) {
		if (reTask == null)
			throw new NullPointerException("插入补采记录失败,任务为空");
		String logStr = "补采任务ID=" + reTask.getrTaskId() + "，任务ID=" + reTask.getId() + "，补采时间点="
				+ TimeUtil.getDateString(reTask.getRegather_datetime());
		LOGGER.debug("准备修改补采记录，" + logStr);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sqlForUpdateRTaskRecords);
			ps.setString(1, reTask.getRegatherPath());
			ps.setInt(2, reTask.getTimes());
			ps.setInt(3, reTask.getStatus());
			ps.setTimestamp(4, reTask.getSuccessDate() == null ? null : new Timestamp(reTask.getSuccessDate().getTime()));
			ps.setString(5, reTask.getCause());
			ps.setLong(6, reTask.getrTaskId());
			ps.executeUpdate();
			LOGGER.debug("修改补采记录成功，" + logStr);
			return true;
		} catch (SQLException e) {
			LOGGER.error("修改补采记录异常，" + logStr, e);
			return false;
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 根据任务group_id查找db输出表记录
	 * 
	 * @return 正常任务列表
	 */
	public Set<String> getDBExportRecords(String groupIds) {
		String sql = this.sqlForGetDBExportRecords;
		LOGGER.debug("查询db输出表记录的SQL为：{}", sql);

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Set<String> set = null;
		try {
			sql = sql.replace("?", groupIds);
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			set = new HashSet<String>();
			while (rs.next()) {
				set.add(rs.getInt("data_type") + "-" + rs.getInt("export_template_id") + "-" + rs.getInt("group_id"));
			}
		} catch (SQLException e) {
			LOGGER.error("从汇总输出日志表中读取日志信息时异常。", e);
		} finally {
			DbUtil.close(rs, pstmt, conn);
		}

		LOGGER.debug("groupIds={},从db输出表读取信息条数为: {}", new Object[]{groupIds, set.size()});
		return set;
	}

	/**
	 * 获取我的机器名
	 * 
	 * @return 如果包含进程编号pid且pid大于0，则为"计算机名@进程编号"
	 */
	private String getPcName() {
		String instName = distributeAppName;
		if (this.pid > 0)
			instName += "@" + this.pid;
		return instName;
	}

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}
	
	public void setSqlForGetTaskList(String sqlForGetTaskList) {
		this.sqlForGetTaskList = sqlForGetTaskList;
	}

	public void setSqlForGetRTaskList(String sqlForGetRTaskList) {
		this.sqlForGetRTaskList = sqlForGetRTaskList;
	}

	public void setSqlForSetTaskDataTimeToNextPeriod(String sqlForSetTaskDataTimeToNextPeriod) {
		this.sqlForSetTaskDataTimeToNextPeriod = sqlForSetTaskDataTimeToNextPeriod;
	}

	public void setSqlForGetDBExportRecords(String sqlForGetDBExportRecords) {
		this.sqlForGetDBExportRecords = sqlForGetDBExportRecords;
	}

	public void setSqlForInsertRTaskRecords(String sqlForInsertRTaskRecords) {
		this.sqlForInsertRTaskRecords = sqlForInsertRTaskRecords;
	}

	public void setSqlForUpdateRTaskRecords(String sqlForUpdateRTaskRecords) {
		this.sqlForUpdateRTaskRecords = sqlForUpdateRTaskRecords;
	}

	public void setSqlForUpdateReTaskStatus(String sqlForUpdateReTaskStatus) {
		this.sqlForUpdateReTaskStatus = sqlForUpdateReTaskStatus;
	}

	public void setSqlForTestReTaskExists(String sqlForTestReTaskExists) {
		this.sqlForTestReTaskExists = sqlForTestReTaskExists;
	}

	public void setSqlForDelayDataList(String sqlForDelayDataList) {
		this.sqlForDelayDataList = sqlForDelayDataList;
	}

	public void setSqlForDelayDataInsert(String sqlForDelayDataInsert) {
		this.sqlForDelayDataInsert = sqlForDelayDataInsert;
	}

	public void setSqlForDelayDataNextTime(String sqlForDelayDataNextTime) {
		this.sqlForDelayDataNextTime = sqlForDelayDataNextTime;
	}

	public String getSqlForDelayDataDelete() {
		return sqlForDelayDataDelete;
	}

	public void setSqlForDelayDataDelete(String sqlForDelayDataDelete) {
		this.sqlForDelayDataDelete = sqlForDelayDataDelete;
	}
}
