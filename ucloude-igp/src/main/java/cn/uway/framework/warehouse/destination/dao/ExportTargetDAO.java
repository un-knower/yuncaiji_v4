package cn.uway.framework.warehouse.destination.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.log.ImportantLogger;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.ExporterBean;
import cn.uway.framework.warehouse.exporter.template.RemoteFileExporterBean;
import cn.uway.util.DbUtil;

/**
 * 查询数据库输出连接配置
 * 
 * @author chenrongqiang
 */
public abstract class ExportTargetDAO {

	private BasicDataSource datasource;
	
	// 数据库输出配置
	protected String loadDbExportTargetSQLForMysql;

	protected String loadDbExportTargetSQLForOracle;
	
	// 文件输出配置
	protected String loadFileExportTargetSQLForMysql;
	
	protected String loadFileExportTargetSQLForOracle;

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	public abstract String getDBExportSQL();
	public abstract String getRemoteFileExportSQL();
	
	public void setLoadDbExportTargetSQLForMysql(String loadDbExportTargetSQLForMysql) {
		this.loadDbExportTargetSQLForMysql = loadDbExportTargetSQLForMysql;
	}

	public void setLoadDbExportTargetSQLForOracle(String loadDbExportTargetSQLForOracle) {
		this.loadDbExportTargetSQLForOracle = loadDbExportTargetSQLForOracle;
	}
	
	public void setLoadFileExportTargetSQLForMysql(
			String loadFileExportTargetSQLForMysql) {
		this.loadFileExportTargetSQLForMysql = loadFileExportTargetSQLForMysql;
	}
	
	public void setLoadFileExportTargetSQLForOracle(
			String loadFileExportTargetSQLForOracle) {
		this.loadFileExportTargetSQLForOracle = loadFileExportTargetSQLForOracle;
	}

	/**
	 * 从数据库加载数据库输出配置
	 */
	public Map<String, ExporterBean> loadDbExportTargetTemplet() {
		Map<String, ExporterBean> dbExportTagetBeans = new HashMap<String, ExporterBean>();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(getDBExportSQL());
			rs = statement.executeQuery();
			while (rs.next()) {
				DatabaseExporterBean dbTempletBean = new DatabaseExporterBean();
				DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
				connectionInfo.setId(rs.getInt("ID"));
				connectionInfo.setDriver(rs.getString("DRIVER"));
				connectionInfo.setUrl(rs.getString("URL"));
				connectionInfo.setUserName(rs.getString("USER_NAME"));
				connectionInfo.setPassword(rs.getString("USER_PWD"));
				connectionInfo.setMaxActive(rs.getInt("MAX_ACTIVE"));
				connectionInfo.setMaxWait(rs.getInt("MAX_WAIT"));
				connectionInfo.setDescription(rs.getString("DESCRIPTION"));
				dbTempletBean.setConnectionInfo(connectionInfo);
				dbTempletBean.setOpenFlag(true);
				dbTempletBean.setBatchNum(rs.getInt("BATCH_NUM"));
				dbExportTagetBeans.put(rs.getString("KEY_NAME"), dbTempletBean);
			}
		} catch (SQLException e) {
			ImportantLogger.getLogger().error("加载数据库输出配置失败,程序即将退出", e);
			System.exit(0);
		} finally {
			DbUtil.close(rs, statement, conn);
		}
		return dbExportTagetBeans;
	}
	
	/**
	 * 从数据库加载数据库输出配置
	 */
	public Map<String, ExporterBean> loadRemoteFileExportTargetTemplet() {
		Map<String, ExporterBean> fileExportTagetBeans = new HashMap<String, ExporterBean>();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(getRemoteFileExportSQL());
			rs = statement.executeQuery();
			while (rs.next()) {
				RemoteFileExporterBean remoteFileExportBean = new RemoteFileExporterBean();
				
				remoteFileExportBean.setConnID(rs.getInt("id"));
				remoteFileExportBean.setExportPath(rs.getString("export_path"));
				remoteFileExportBean.setExportFileName(rs.getString("export_filename"));
				remoteFileExportBean.setCompressFormat(rs.getString("compress_format"));
				remoteFileExportBean.setSplitChar(rs.getString("split_char"));
				remoteFileExportBean.setExportHeader(true);
				if (1 != rs.getInt("export_header")) {
					remoteFileExportBean.setExportHeader(false);
				}
				remoteFileExportBean.setEncode(rs.getString("encode"));
				remoteFileExportBean.setAdditionParams(rs.getString("addition_params"));
				
				remoteFileExportBean.setOpenFlag(true);
				remoteFileExportBean.setBatchNum(rs.getInt("batch_num"));
				fileExportTagetBeans.put(rs.getString("key_name"), remoteFileExportBean);
			}
		} catch (SQLException e) {
			ImportantLogger.getLogger().error("加载文件输出配置失败,程序即将退出.", e);
			System.exit(0);
		} finally {
			DbUtil.close(rs, statement, conn);
		}
		return fileExportTagetBeans;
	}

}
