package cn.uway.framework.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.context.AppContext;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.TimeUtil;

/**
 * <p>
 * 记录数据库日志，用于前台显示 。<br />
 * 数据库中对应的表是: LOG_CLT_INSERT <br />
 * 其中的字段： <br />
 * OMCID omcid <br />
 * CLT_TBNAME clt原始表名<br />
 * STAMPTIME 入库几点的数据<br />
 * VSYSDATE 当前系统时间<br />
 * INSERT_COUNTNUM 入库条数<br />
 * IS_CAL 是否汇总,默认为0<br />
 * TASKID 任务编号<br />
 * </p>
 * 
 * @author yuy 2013.12.28
 */
public final class DBLogger {

	
	/**
	 * oracle log_clt_insert 入库脚本
	 */
	private static String INSERT_SQL = "INSERT INTO LOG_CLT_INSERT" + " (OMCID,CLT_TBNAME,STAMPTIME,VSYSDATE,INSERT_COUNTNUM,IS_CAL,TASKID,FILE_NAME"
			+ ") VALUES " + "(?,UPPER(?),?,sysdate,?,0,?,?)";

	/**
	 * mysql log_clt_insert 入库脚本
	 */
	String INSERT_SQL_MYSQL = "INSERT INTO LOG_CLT_INSERT" + " (OMCID,CLT_TBNAME,STAMPTIME,VSYSDATE,INSERT_COUNTNUM,IS_CAL,TASKID,FILE_NAME"
			+ ") VALUES " + "(?,UPPER(?),?,?,?,0,?,?)";

	private static final ILogger logger = LoggerManager.getLogger(DBLogger.class);

	private BasicDataSource connPool = (BasicDataSource) AppContext.getBean("jdbcConnection", DataSource.class);

	private static final DBLogger INSTANCE = new DBLogger();

	/**
	 * 构造方法。
	 */
	private DBLogger() {
		super();
	}

	public synchronized static DBLogger getInstance() {
		return INSTANCE;
	}

	/**
	 * 记录一条数据库日志到LOG_CLT_INSERT表中。
	 * 
	 * @param omcId
	 *            omcIdjp
	 * @param tableName
	 *            clt原始表名
	 * @param stampTime
	 *            入库时间
	 * @param count
	 *            入库条数
	 * @param taskID
	 *            任务编号
	 */
	public synchronized void insert(long taskID, int omcid, String tableName, Date stampTime, long insertSuccNum, String fileName) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			// logger.debug("准备获取Oracle连接……当前oracle连接数：{}，最大oracle连接数：{}",
			// new Object[]{connPool.getNumActive(),connPool.getMaxActive()});
			con = this.connPool.getConnection();
			// logger.debug("Oracle连接获取成功，当前oracle连接数：{}，最大oracle连接数：{}",
			// new Object[]{connPool.getNumActive(),connPool.getMaxActive()});

			String sql = new String(INSERT_SQL);

			//根据驱动名来确定是哪个数据库
			String driverClassName = connPool.getDriverClassName();
			if (driverClassName.toLowerCase().contains("mysql")) {
				sql = new String(INSERT_SQL_MYSQL);
			}

			ps = con.prepareStatement(sql);
			con.setAutoCommit(false);
			int index = 1;
			ps.setLong(index++, omcid);
			ps.setString(index++, tableName);

			ps.setTimestamp(index++, new Timestamp(stampTime.getTime()));

			if (driverClassName.toLowerCase().contains("mysql")) {
				Date sysdate=new Date();
				String  sysDateTmp=TimeUtil.getDateString(new Timestamp(sysdate.getTime()));
				ps.setTimestamp(index++, new Timestamp(TimeUtil.getDate(sysDateTmp).getTime()));
			}

			ps.setLong(index++, insertSuccNum);
			ps.setLong(index++, taskID);
			ps.setString(index++, fileName);
			ps.execute();
			con.commit();
		} catch (Exception e) {
			logger.warn("插入log_clt_insert表异常，dataTime=" + stampTime + ", tablename: " + tableName, e);
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	public synchronized void insertMinute(long taskID, int omcid, String tableName, Date stampTime, long insertSuccNum, String fileName) {
		try {
			String tmp = TimeUtil.getDateString_yyyyMMddHHmm(stampTime);
			Date date = new Date(stampTime.getTime());

			date = TimeUtil.getyyyyMMddHHmmDate(tmp);
			insert(taskID, omcid, tableName, date, insertSuccNum, fileName);
		} catch (ParseException e) {
			logger.warn("插入log_clt_insert 表异常，dataTime=" + stampTime + ", tablename: " + tableName, e);
		}

	}

}
