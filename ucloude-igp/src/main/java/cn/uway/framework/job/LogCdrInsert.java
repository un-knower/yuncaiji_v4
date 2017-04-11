package cn.uway.framework.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.context.AppContext;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;

/**
 * 向LOG_CDR_INSERT表记录日志。
 * 
 * @author ChenSijiang 2012-11-27
 */
public class LogCdrInsert {

	private static final ILogger log = LoggerManager.getLogger(LogCdrInsert.class);

	private static final String INSERT_SQL = "insert into log_cdr_insert (city_id,omc_id,bsc_id,tablename,data_time,insert_start_time,insert_end_time,"
			+ "insert_succ_num,insert_fail_num,insert_all_num,vendor,province_id,filename,is_cal) values " + "(?,?,?,?,?,?,?,?,?,?,?,?,?,0)";

	private BasicDataSource connPool = (BasicDataSource) AppContext.getBean("jdbcConnection", DataSource.class);

	private int provinceId = Integer.parseInt(AppContext.getBean("provinceId", String.class));

	private static LogCdrInsert instance = new LogCdrInsert();

	public static LogCdrInsert getInstance() {
		return instance;
	}

	public synchronized void insert(long cityId, int omcId, int bscId, String tableName, Date dataTime, Date startTime, Date endTime,
			long insertSuccNum, long insertFailNum, long insertAllNum, String vendor, String filename) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			log.debug("准备获取Oracle连接……当前oracle连接数：{}，最大oracle连接数：{}", new Object[]{connPool.getNumActive(), connPool.getNumActive()});
			con = this.connPool.getConnection();
			log.debug("Oracle连接获取成功，当前oracle连接数：{}，最大oracle连接数：{}", new Object[]{connPool.getNumActive(), connPool.getNumActive()});
			ps = con.prepareStatement(INSERT_SQL);
			int index = 1;
			ps.setLong(index++, cityId);
			ps.setInt(index++, omcId);
			ps.setInt(index++, bscId);
			ps.setString(index++, tableName != null ? tableName.toUpperCase() : tableName);
			ps.setTimestamp(index++, new Timestamp(dataTime.getTime()));
			ps.setTimestamp(index++, new Timestamp(startTime.getTime()));
			ps.setTimestamp(index++, new Timestamp(endTime.getTime()));
			ps.setLong(index++, insertSuccNum);
			ps.setLong(index++, insertFailNum);
			ps.setLong(index++, insertAllNum);
			ps.setString(index++, vendor);
			ps.setInt(index++, this.provinceId);
			ps.setString(index++, filename);
			ps.execute();
		} catch (Exception e) {
			log.warn("插入log_cdr_insert表异常，dataTime=" + dataTime, e);
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	public void setConnPool(DataSource connPool) {
		this.connPool = (BasicDataSource) connPool;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	private LogCdrInsert() {
		super();
	}
}
