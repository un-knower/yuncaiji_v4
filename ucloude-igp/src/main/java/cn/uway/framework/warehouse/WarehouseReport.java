package cn.uway.framework.warehouse;

import java.util.Date;
import java.util.Map;

/**
 * WarehouseReport 仓库报表
 * 
 * @author chenrongqiang 2012-11-8
 */
public class WarehouseReport {

	/**
	 * 开始时间
	 */
	private Date startTime;

	/**
	 * 结束时间
	 */
	private Date endTime;

	/**
	 * 总条数
	 */
	private long total;

	/**
	 *  成功条数
	 */
	private long succ;

	/**
	 *  失败条数
	 */
	private long fail;

	/**
	 *  分发至输出模块记录条数 多个数据类型用;隔开
	 */
	private String distributedNum;

	/**
	 *  失败原因
	 */
	private String cause;
	
	private Map<String, TableReport> tableInfo;

	/*getters and setters*/
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getSucc() {
		return succ;
	}

	public void setSucc(long succ) {
		this.succ = succ;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getDistributedNum() {
		return distributedNum;
	}

	public void setDistributedNum(String distributedNum) {
		this.distributedNum = distributedNum;
	}
	
	public Map<String, TableReport> getTableInfo() {
		return tableInfo;
	}
	
	public void setTableInfo(Map<String, TableReport> tableInfo) {
		this.tableInfo = tableInfo;
	}


	public static class TableReport{
		/**
		 * 开始时间
		 */
		private Date startTime;

		/**
		 * 结束时间
		 */
		private Date endTime;

		/**
		 * 总条数
		 */
		private long total;

		/**
		 *  成功条数
		 */
		private long succ;

		/**
		 *  失败条数
		 */
		private long fail;

		/**
		 *  分发至输出模块记录条数 多个数据类型用;隔开
		 */
		private String distributedNum;

		/**
		 *  失败原因
		 */
		private String cause;
		
		/**
		 * 数据类型，从模板中获取
		 */
		private int dataType;
		
		/**
		 * 入库表名
		 */
		private String tableName;
		

		/*getters and setters*/
		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}

		public long getTotal() {
			return total;
		}

		public void setTotal(long total) {
			this.total = total;
		}

		public long getSucc() {
			return succ;
		}

		public void setSucc(long succ) {
			this.succ = succ;
		}

		public long getFail() {
			return fail;
		}

		public void setFail(long fail) {
			this.fail = fail;
		}

		public String getCause() {
			return cause;
		}

		public void setCause(String cause) {
			this.cause = cause;
		}

		public String getDistributedNum() {
			return distributedNum;
		}

		public void setDistributedNum(String distributedNum) {
			this.distributedNum = distributedNum;
		}

		
		public int getDataType() {
			return dataType;
		}

		
		public void setDataType(int dataType) {
			this.dataType = dataType;
		}

		
		public String getTableName() {
			return tableName;
		}

		
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
	}
}
