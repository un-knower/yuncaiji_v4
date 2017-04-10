package cn.uway.ucloude.uts.web.access.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.web.access.domain.LockType;
import cn.uway.ucloude.uts.web.access.domain.LoginConfigInfo;
import cn.uway.ucloude.uts.web.access.domain.LoginLockInfo;
import cn.uway.ucloude.uts.web.access.face.LoginConfigAccess;

/**
 * @author Uway-M3
 *
 */
public class DbLoginConfigAccess extends JdbcAbstractAccess implements LoginConfigAccess {

	public DbLoginConfigAccess() {
		super(ExtConfigKeys.CONNECTION_KEY);
	}

	/**
	 * 获取用户锁定记录
	 * 
	 * @param userId
	 * @param createTime
	 * @return
	 */
	@Override
	public LoginLockInfo getLoginLockInfo(int userId, String ip, Date createTime) {
		LoginLockInfo lli = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table("CASP_LOGIN_LOCK").where("USER_ID = ?", userId)
				.and("CREATE_DATE >= ?", new java.sql.Timestamp(createTime.getTime())).andOnNotEmpty("login_ip = ?", ip)
				.orderBy("CREATE_DATE", OrderByType.DESC).single(new ResultSetHandler<LoginLockInfo>() {
					@Override
					public LoginLockInfo handle(ResultSet rs) throws SQLException {
						LoginLockInfo lli = null;
						if (rs.next()) {
							lli = readLoginLock(rs);
						}
						return lli;
					}
				});
		return lli;
	}

	@Override
	public boolean AddLoginLockInfo(LoginLockInfo loginInfo) {
		try {
			getSqlTemplate().insert(
					"insert into CASP_LOGIN_LOCK(login_lock_id,user_id,user_lock_code,pwd_error_counter,user_lock_timeout,login_ip,lock_date,create_date,update_date,remark) values(seq_casp_login_lock.nextval,?,?,?,?,?,?,?,?,?)",
					loginInfo.getUserId(), loginInfo.getUserLockCode(), loginInfo.getPwdErrorCounter(),
					loginInfo.getUserLockTimeout(), loginInfo.getLoginIp(),
					loginInfo.getLockDate() == null ? null : new java.sql.Timestamp(loginInfo.getLockDate().getTime()),
					new java.sql.Timestamp(loginInfo.getCreateDate().getTime()), loginInfo.getUpdateDate() == null
							? null : new java.sql.Timestamp(loginInfo.getUpdateDate().getTime()),
					loginInfo.getRemark());
			return true;
		} catch (Exception e) {
			LoggerManager.getLogger(this.getClass()).error("新增用户密码错误记录异常：" + e.toString());
			return false;
		}
	}

	@Override
	public boolean UpdateLoginLockInfo(LoginLockInfo loginInfo) {
		try {
			SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getUpdateSql().update().table("CASP_LOGIN_LOCK")
					.set("pwd_error_counter", loginInfo.getPwdErrorCounter())
					.set("update_date", new java.sql.Timestamp(loginInfo.getUpdateDate().getTime()))
					.where("login_lock_id = ?", loginInfo.getLoginLockId()).doUpdate();
			return true;
		} catch (Exception e) {
			LoggerManager.getLogger(this.getClass()).error("更新用户密码错误记录异常：" + e.toString());
			return false;
		}
	}

	/**
	 * 获取系统登录配置
	 * 
	 * @return
	 */
	@Override
	public LoginConfigInfo getLoginCinfigInfo() {
		LoginConfigInfo lc = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table("CASP_LOGIN_SETTING").single(new ResultSetHandler<LoginConfigInfo>() {
					@Override
					public LoginConfigInfo handle(ResultSet rs) throws SQLException {
						LoginConfigInfo lc = null;
						if (rs.next()) {
							lc = readLoginConfig(rs);
						}
						return lc;
					}
				});
		return lc;
	}

	private LoginConfigInfo readLoginConfig(ResultSet rs) throws SQLException {
		LoginConfigInfo lc = new LoginConfigInfo();
		lc.setIsEnabledIP(rs.getInt("is_enabled_ip"));
		lc.setIsEnabledMac(rs.getInt("is_enabled_mac"));
		lc.setLoginIntervalCount(rs.getInt("login_interval_count"));
		lc.setLoginSettingID(rs.getInt("login_setting_id"));
		lc.setMender(rs.getString("mender"));
		lc.setPwdErrorCount(rs.getInt("pwd_error_count"));
		lc.setRemark(rs.getString("remark"));
		lc.setUpdateDate(
				rs.getTimestamp("update_date") == null ? null : new Date(rs.getTimestamp("update_date").getTime()));
		lc.setUserLockCode(LockType.getLockType(rs.getInt("user_lock_code")));
		lc.setUserLockTimeout(rs.getInt("user_lock_timeout"));
		return lc;
	}

	private LoginLockInfo readLoginLock(ResultSet rs) throws SQLException {
		LoginLockInfo lli = new LoginLockInfo();
		lli.setCreateDate(rs.getTimestamp("create_date"));
		lli.setLockDate(rs.getTimestamp("lock_date"));
		lli.setLoginIp(rs.getString("login_ip"));
		lli.setLoginLockId(rs.getInt("login_lock_id"));
		lli.setPwdErrorCounter(rs.getInt("pwd_error_counter"));
		lli.setRemark(rs.getString("remark"));
		lli.setUpdateDate(rs.getTimestamp("update_date"));
		lli.setUserId(rs.getInt("user_id"));
		lli.setUserLockCode(rs.getString("user_lock_code"));
		lli.setUserLockTimeout(rs.getInt("user_lock_timeout"));
		return lli;
	}

}
