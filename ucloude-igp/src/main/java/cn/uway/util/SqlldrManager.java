package cn.uway.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.warehouse.exporter.template.SqlldrColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.SqlldrTableTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 管理连接 目前采用每个线程都自己轮询锁的方式，可能比较耗性能。 待优化：建议尝试使用Listener方式解决此问题（20150918）
 * 
 * @author linp
 * 
 */
public class SqlldrManager {

	private static final ILogger LOGGER = LoggerManager
			.getLogger(SqlldrManager.class); // 日志

	private static class SqlldrManagerHolder {

		private static SqlldrManager instance = new SqlldrManager();
	}
	
	// 紧急文件尺寸，优先获取sqlldr执行资源，默认45M
	private static final String URGENT_FILE_SIZE_STR= AppContext.getBean("sqlldrUrgentFileSize", String.class);
	public long urgentFileSize = 45;

	private AtomicBoolean sqlldrDataFileWriterFlag = new AtomicBoolean(true);

	/**
	 * 采用serviceName+userName作为key,管理sqlldr同时操作相同数据库的量
	 */
	private Map<String, SqlldrConManager> dbMap = new HashMap<String, SqlldrConManager>();

	/**
	 * 采用采用url+userName+table名作为key,唯一确定数据库表建立的clt表信息,作为缓存
	 */
	private Map<String, String> cltMap = new HashMap<String, String>();

	/**
	 * key为driver|url|userName|password 与dbTableMap一致，好操作
	 */
	private Map<String, Map<String, SqlldrTableTemplateBean>> tableTempMap = new HashMap<String, Map<String, SqlldrTableTemplateBean>>();

	private static class RunnerWaiter {

		public Condition waitCond;

		public boolean urgent;
	};

	// 私有化构造方法
	private SqlldrManager() {
		try {
			if(URGENT_FILE_SIZE_STR != null && URGENT_FILE_SIZE_STR.indexOf("$") < 0) {
				urgentFileSize= Integer.valueOf(URGENT_FILE_SIZE_STR);
			}
			LOGGER.debug("SQLDR文件尺寸优先阀值:{}", urgentFileSize);
		} catch (NumberFormatException e) {
			LOGGER.error("FILE_SIZE TYPE IS NOT NUMBER ",e);
		}
	}

	public static SqlldrManager getInstance() {
		return SqlldrManagerHolder.instance;
	}

	public void getWriteFileRight() {
		// 获取文件写入权限，只有获取了，才能写入文件
		while (!sqlldrDataFileWriterFlag.compareAndSet(true, false)) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean realseWriteFileRight() {
		return sqlldrDataFileWriterFlag.compareAndSet(false, true);
	}

	public void setDbTable(String key, SqlldrTableTemplateBean tableBean) {
		Map<String, SqlldrTableTemplateBean> beanMap = tableTempMap.get(key);
		if (beanMap == null) {
			Map<String, SqlldrTableTemplateBean> tableMap = new HashMap<String, SqlldrTableTemplateBean>();
			tableMap.put(tableBean.getTableName(), tableBean);
			tableTempMap.put(key, tableMap);
		} else {
			if (beanMap.get(tableBean.getTableName()) == null) {
				beanMap.put(tableBean.getTableName(), tableBean);
			}
		}
	}

	public synchronized void createTableClt() {
		for (String key : tableTempMap.keySet()) {
			StringBuilder sbd = new StringBuilder();
			Connection conn = null;
			Statement st = null;
			ResultSet rs = null;
			Set<String> settmp = tableTempMap.get(key).keySet();
			// driver url userName password
			String[] info = key.split("\\|");
			try {
				Class.forName(info[0]);
				conn = DriverManager.getConnection(info[1], info[2], info[3]);
				String sql = "select COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,NULLABLE　from user_tab_columns where table_name = UPPER('%s')";
				st = conn.createStatement();
				for (String tableName : settmp) {
					rs = st.executeQuery(String.format(sql, tableName));
					Map<String, String> map = new HashMap<String, String>();
					sbd.setLength(0);
					sbd.append(" ");
					while (rs.next()) {
						String columnName = rs.getString("COLUMN_NAME");
						// 模板未配置该字段
						if (tableTempMap.get(key).get(tableName).getColumns()
								.get(columnName) == null) {
							continue;
						}
						String dataType = rs.getString("DATA_TYPE");
						if ("DATE".equals(dataType)
								|| "TIMESTAMP".equals(dataType)) {
							map.put(columnName,
									" Date '"
											+ tableTempMap.get(key)
													.get(tableName)
													.getColumns()
													.get(columnName)
													.getFormat() + "'");
						} else if ("VARCHAR2".equals(dataType)
								|| "CHAR".equals(dataType)) {
							int data_length = Integer.valueOf(rs
									.getString("DATA_LENGTH"));
							if (data_length > 255) {
								map.put(columnName, " CHAR(" + data_length
										+ ")");
							}
						}
					}
					for (SqlldrColumnTemplateBean ctBean : tableTempMap
							.get(key).get(tableName).getColumnsList()) {
						String format = map.get(ctBean.getColumnName()) == null
								? ""
								: map.get(ctBean.getColumnName());
						sbd.append(ctBean.getColumnName()).append(format)
								.append(",");
					}
					cltMap.put(info[1] + info[2] + tableName,
							sbd.substring(0, sbd.length() - 1).trim());
				}
			} catch (Exception e) {
				LOGGER.error("连接数据库异常,key:" + key, e);
			} finally {
				DbUtil.close(rs, st, conn);
			}
		}
	}

	public String getTableClt(String key) {
		return cltMap.get(key);
	}

	public void showTableClt() {
		StringBuilder sbd = new StringBuilder();
		for (Map.Entry<String, String> entry : cltMap.entrySet()) {
			sbd.append("key:").append(entry.getKey()).append("cltStr:")
					.append(entry.getValue()).append("\n");
		}
		LOGGER.debug("exist table clt: " + sbd.toString());
	}

	public SqlldrResult toRunSqlldr(int limitConn, SqlldrRunner sqlRunner) {
		String key = sqlRunner.getServiceName() + sqlRunner.getUserName();
		SqlldrResult result = null;
		SqlldrConManager manager = null;
		synchronized (this) {
			manager = dbMap.get(key);
			if (manager == null) {
				manager = new SqlldrConManager(key, limitConn, 0);
				dbMap.put(key, manager);
			}
		}

		// 文件大于45M的，争取sqlldr执行资源的能力要强
		File file = new File(sqlRunner.getBadpath().replace(".bad", ".txt"));
		boolean bUrgent = file.length() / (1024 * 1024) > urgentFileSize;
		long timeWait = 1000;
		while (!manager.acquireRunnerPermission(bUrgent)) {
			try {
				TimeUnit.MILLISECONDS.sleep(timeWait);
				if (timeWait < 50)
					timeWait = 50;

			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		result = sqlRunner.runSqlldr();
		manager.releaseRunnerPermission();
		return result;
	}

	public String getConManagerInfo(String serviceName) {
		SqlldrConManager conManager = dbMap.get(serviceName);
		if (conManager != null) {
			return conManager.toString();
		}
		return "";
	}

	public static class SqlldrConManager {

		private String serviceName; // 为oracle配置文件ora中的别名+username

		private int limitConn;

		private int currentConn;

		private Lock lock = new ReentrantLock();

		private LinkedList<RunnerWaiter> lstWaitThreadConds = new LinkedList<RunnerWaiter>();

		public SqlldrConManager() {
		};

		public SqlldrConManager(String serviceName, int limitConn,
				int currentCon) {
			this.serviceName = serviceName;
			this.limitConn = limitConn;
			this.currentConn = currentCon;
		};

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public int getLimitConn() {
			return limitConn;
		}

		public int getCurrentConn() {
			try {
				lock.lock();
				return currentConn;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 如果是紧急的，可以允许超限3个
		 * 
		 * @param bUrgent
		 * @return
		 */
		public boolean acquireRunnerPermission(boolean bUrgent) {
			try {
				lock.lock();
				if (hasIdleResoure(bUrgent)) {
					++this.currentConn;
					return true;
				} else {
					RunnerWaiter waiter = addWaiterConnd(bUrgent);
					waiter.waitCond.await();

					++this.currentConn;
					return true;
				}
			} catch (InterruptedException e) {

			} finally {
				lock.unlock();
			}

			return false;
		}

		public void releaseRunnerPermission() {
			try {
				lock.lock();
				--currentConn;

				// 通知最近在等待的入库线程开始工作
				if (lstWaitThreadConds.size() > 0) {
					RunnerWaiter waiter = lstWaitThreadConds.get(0);
					if (hasIdleResoure(waiter.urgent)) {
						lstWaitThreadConds.remove(0);
						waiter.waitCond.signal();
					}
				}
			} finally {
				lock.unlock();
			}
		}

		private RunnerWaiter addWaiterConnd(boolean bUrgent) {
			try {
				// lock是重入锁，不会死锁的
				lock.lock();
				int index = 0;

				// 带有紧急标志的，优先入库（紧急优先，排除时间优先)
				if (bUrgent) {
					for (RunnerWaiter waiter : lstWaitThreadConds) {
						if (!waiter.urgent)
							break;

						++index;
					}
				}

				RunnerWaiter waiter = new RunnerWaiter();
				waiter.waitCond = lock.newCondition();
				waiter.urgent = bUrgent;

				if (index < 1)
					lstWaitThreadConds.add(waiter);
				else
					lstWaitThreadConds.add(index, waiter);

				return waiter;
			} finally {
				lock.unlock();
			}
		}

		private boolean hasIdleResoure(boolean bUrgent) {
			try {
				// lock是重入锁，不会死锁的
				lock.lock();
				if (currentConn < limitConn
						|| (bUrgent && currentConn < limitConn + 6)) {
					return true;
				}

				return false;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public String toString() {
			return "serviceName:" + serviceName + " limitConn:" + limitConn
					+ " currentConn:" + currentConn;
		}
	}

	// public static class SqlldrTestRunner extends Thread {
	// public int threadIndex;
	// public SqlldrConManager manager;
	//
	// @Override
	// public void run() {
	// boolean bUrgent = ((threadIndex % 18) == 0);
	// for (int i=1; i<10; ++i) {
	// if (!manager.acquireRunnerPermission(bUrgent)) {
	// System.out.println("error, can't accuire runner permission.");
	// return;
	// }
	//
	// try {
	// // do something....
	// Thread.sleep(10*(threadIndex+1));
	//
	// manager.releaseRunnerPermission();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// System.out.println("thread:" + threadIndex + " run finished.");
	// }
	// }
	//
	// public static void main(String[] args) {
	// SqlldrConManager manager = new SqlldrManager.SqlldrConManager("ssdf", 20,
	// 0);
	//
	// for (int i=0; i<100; ++i) {
	// SqlldrTestRunner runner = new SqlldrTestRunner();
	// runner.manager = manager;
	// runner.threadIndex = i;
	//
	// runner.start();
	// }
	//
	// System.out.println("main thread run finished.");
	// }
}
