package cn.uway.framework.connection.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.TelnetConnectionInfo;
import cn.uway.framework.connection.dao.ConnectionInfoDAO;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;

/**
 * 基于数据库查询的连接信息DAO
 * 
 * @author chenrongqiang @ 2014-3-26
 */
public class DatabaseConnectionInfoDAO implements ConnectionInfoDAO {

	/**
	 * 根据ID查询连接信息的SQL语句
	 */
	private String sqlForGetConnById;

	/**
	 * 根据查询数据库连接的SQL语句
	 */
	private String sqlForGetDBConnById;

	/**
	 * 根据查询FTP连接的SQL语句
	 */
	private String sqlForGetFTPConnById;

	/**
	 * 根据查询Telnet连接的SQL语句
	 */
	private String sqlForGetTelnetConnById;

	/**
	 * 数据库连接池
	 */
	private BasicDataSource datasource;

	/**
	 * 日志
	 */
	private static ILogger LOGGER = LoggerManager.getLogger(DatabaseConnectionInfoDAO.class);

	/**
	 * @param sqlForGetConnById the sqlForGetConnById to set
	 */
	public void setSqlForGetConnById(String sqlForGetConnById) {
		this.sqlForGetConnById = sqlForGetConnById;
	}

	/**
	 * @param sqlForGetDBConnById the sqlForGetDBConnById to set
	 */
	public void setSqlForGetDBConnById(String sqlForGetDBConnById) {
		this.sqlForGetDBConnById = sqlForGetDBConnById;
	}

	/**
	 * @param sqlForGetFTPConnById the sqlForGetFTPConnById to set
	 */
	public void setSqlForGetFTPConnById(String sqlForGetFTPConnById) {
		this.sqlForGetFTPConnById = sqlForGetFTPConnById;
	}

	/**
	 * @param sqlForGetTelnetConnById the sqlForGetTelnetConnById to set
	 */
	public void setSqlForGetTelnetConnById(String sqlForGetTelnetConnById) {
		this.sqlForGetTelnetConnById = sqlForGetTelnetConnById;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public ConnectionInfo getConnectionInfo(int connId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sqlForGetConnById);
			pstmt.setInt(1, connId);
			rs = pstmt.executeQuery();
			if (rs.next())
				return getConnectionInfo(rs);
			LOGGER.error("查询连接信息失败，连接配置不存在，ID={}", connId);
			return null;
		} catch (SQLException e) {
			LOGGER.error("查询连接信息失败.连接ID={}", connId, e);
			return null;
		} finally {
			DbUtil.close(rs, pstmt, conn);
		}
	}

	private ConnectionInfo getConnectionInfo(ResultSet rs) {
		try {
			int connType = rs.getInt("conn_type");
			if (!ConnectionInfo.validate(connType))
				throw new IllegalArgumentException("非法的连接类型配置:" + connType);
			if (connType == ConnectionInfo.CONNECTION_TYPE_DB)
				return getDatabaseConnInfo(rs);
			if (connType == ConnectionInfo.CONNECTION_TYPE_FTP || connType == ConnectionInfo.CONNECTION_TYPE_SFTP || connType == ConnectionInfo.CONNECTION_TYPE_HDFS)
				return getFTPConnInfo(rs);
			if (connType == ConnectionInfo.CONNECTION_TYPE_TELNET)
				return getTelnetConnInfo(rs);
			return null;
		} catch (Exception e) {
			LOGGER.error("查询连接信息失败.", e);
			return null;
		}
	}

	/**
	 * 设置connection信息的公共信息:<br>
	 * 
	 * @param connInfo
	 * @param rs
	 */
	private void setValues(ConnectionInfo connInfo, ResultSet rs) throws SQLException {
		connInfo.setId(rs.getInt("id"));
		connInfo.setIp(rs.getString("ip"));
		connInfo.setConnType(rs.getInt("conn_type"));
		connInfo.setUserName(rs.getString("user_name"));
		connInfo.setPassword(rs.getString("user_pwd"));
		connInfo.setDescription(rs.getString("description"));
		connInfo.setVendor(rs.getString("vendor"));
	}

	/**
	 * 查询数据库的连接信息<br>
	 * 
	 * @param rs
	 * @return 数据库连接信息 @see{ConnectionInfo}
	 */
	private ConnectionInfo getDatabaseConnInfo(ResultSet rs) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet subrs = null;
		int connId = 0;
		try {
			connId = rs.getInt("conn_relate_id");
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sqlForGetDBConnById);
			pstmt.setInt(1, connId);
			subrs = pstmt.executeQuery();
			if (subrs.next()) {
				DatabaseConnectionInfo connInfo = new DatabaseConnectionInfo();
				// 设置公共属性
				this.setValues(connInfo, rs);
				connInfo.setDriver(subrs.getString("driver"));
				connInfo.setUrl(subrs.getString("url"));
				return connInfo;
			}
			LOGGER.error("查询连接信息失败，数据库连接配置不存在，ID={}", connId);
			return null;
		} catch (SQLException e) {
			LOGGER.error("查询连接信息失败.数据库连接ID={}", connId, e);
			return null;
		} finally {
			DbUtil.close(subrs, pstmt, conn);
		}
	}

	private ConnectionInfo getFTPConnInfo(ResultSet rs) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet subrs = null;
		int connId = 0;
		try {
			connId = rs.getInt("conn_relate_id");
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sqlForGetFTPConnById);
			pstmt.setInt(1, connId);
			subrs = pstmt.executeQuery();
			if (subrs.next()) {
				FTPConnectionInfo connInfo = new FTPConnectionInfo(rs.getInt("conn_type"));
				// 设置公共属性
				this.setValues(connInfo, rs);
				connInfo.setPort(subrs.getInt("port"));
				connInfo.setCharset(subrs.getString("ftp_charset"));
				connInfo.setPassiveFlag(subrs.getInt("passive") == 1 ? true : false);
				connInfo.setBreakpointEnableFlag(subrs.getInt("breakpoint") == 0 ? false : true);
				connInfo.setMaxConnections(subrs.getInt("max_connections"));
				connInfo.setMaxWaitSecond(subrs.getInt("max_wait_second"));
				connInfo.setValidateCmd(subrs.getString("validate_cmd"));
				connInfo.setRetryTimes(subrs.getInt("retry_times"));
				connInfo.setRetryDelaySecond(subrs.getInt("retry_delay_second"));
				return connInfo;
			}
			LOGGER.error("查询连接信息失败，FTP连接配置不存在，ID={}", connId);
			return null;
		} catch (SQLException e) {
			LOGGER.error("查询连接信息失败.FTP连接ID={}", connId, e);
			return null;
		} finally {
			DbUtil.close(subrs, pstmt, conn);
		}
	}

	private ConnectionInfo getTelnetConnInfo(ResultSet rs) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet subrs = null;
		int connId = 0;
		try {
			connId = rs.getInt("conn_relate_id");
			conn = datasource.getConnection();
			pstmt = conn.prepareStatement(sqlForGetTelnetConnById);
			pstmt.setInt(1, connId);
			subrs = pstmt.executeQuery();
			if (subrs.next()) {
				TelnetConnectionInfo connInfo = new TelnetConnectionInfo();
				// 设置公共属性
				this.setValues(connInfo, rs);
				connInfo.setPort(subrs.getInt("port"));
				connInfo.setLoginSign(subrs.getString("login_sign"));
				connInfo.setTermType(subrs.getString("term_type"));
				connInfo.setTimeoutMinutes(subrs.getInt("timeout"));
				return connInfo;
			}
			LOGGER.error("查询连接信息失败，Telnet连接配置不存在，ID={}", connId);
			return null;
		} catch (SQLException e) {
			LOGGER.error("查询连接信息失败，Telnet连接ID={}", connId, e);
			return null;
		} finally {
			DbUtil.close(subrs, pstmt, conn);
		}
	}
}
