package cn.uway.usummary.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.entity.ConnectionInfo;
import cn.uway.usummary.entity.DBExportInfo;
import cn.uway.usummary.entity.ExporterArgs;
import cn.uway.usummary.entity.FtpExportInfo;
import cn.uway.usummary.entity.USummaryConfInfo;
import cn.uway.usummary.util.DESUtil;
import cn.uway.usummary.util.DbUtil;

public class USummaryDao {
	
	private static Logger LOG = LoggerFactory.getLogger(USummaryDao.class);
	
	/**
	 * 数据库连接池
	 */
	private BasicDataSource datasource;
	
	private String sqlForLoadConf;
	
	private String sqlForQueryConfById;
	
	private String sqlForDBExport;
	
	private String sqlForFTPExport;
	
	/**
	 * 加载is_used=1的配置数据
	 * @return
	 */
	public Map<Long,USummaryConfInfo> loadConf()
	{
		Map<Long,USummaryConfInfo> map = new HashMap<Long,USummaryConfInfo>();
		USummaryConfInfo conf = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			LOG.debug("执行缓存加载,SQL="+sqlForLoadConf);
			conn = datasource.getConnection();
			ps = conn.prepareStatement(sqlForLoadConf);
			rs = ps.executeQuery();
			while(rs.next()){
				conf = new USummaryConfInfo();
				conf.setSqlNum(rs.getLong("sqlnum"));
				conf.setSql(DbUtil.ClobParse(rs.getClob("sql")));
				conf.setIsPlaceholder(rs.getInt("is_placeholder"));
				conf.setOperationType(rs.getInt("operation_type"));
				conf.setStorageType(rs.getInt("storage_type"));
				conf.setGroupId(rs.getInt("group_id"));
				map.put(conf.getSqlNum(), conf);
			}
		}catch(Exception e){
			LOG.error("从配置表usummary_cfg_conf加载数据失败,sql="+sqlForLoadConf,e);
			map = null;
		}finally{
			DbUtil.close(rs, ps, conn);
		}
		return map;
	}
	
	/**
	 * 根据sql编号查询信息
	 * @param sqlNum
	 * @return
	 */
	public USummaryConfInfo queryConfById(Long sqlNum)
	{
		USummaryConfInfo conf = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			LOG.debug("根据sqlNum获取配置信息,SQL="+sqlForQueryConfById);
			conn = datasource.getConnection();
			ps = conn.prepareStatement(sqlForQueryConfById);
			ps.setLong(1, sqlNum);
			rs = ps.executeQuery();
			if(rs.next()){
				conf = new USummaryConfInfo();
				conf.setSqlNum(rs.getLong("sqlnum"));
				conf.setSql(DbUtil.ClobParse(rs.getClob("sql")));
				conf.setIsPlaceholder(rs.getInt("is_placeholder"));
				conf.setOperationType(rs.getInt("operation_type"));
				conf.setStorageType(rs.getInt("storage_type"));
				conf.setGroupId(rs.getInt("group_id"));
			}
		}catch(Exception e){
			LOG.error("从配置表usummary_cfg_conf查询"+sqlNum+"数据失败,SQL="+sqlForQueryConfById,e);
			conf = null;
		}finally{
			DbUtil.close(rs, ps, conn);
		}
		return conf;
	}
	
	/**
	 * 根据sql编号查询信息
	 * @param sqlNum
	 * @return
	 * @throws Exception 
	 */
	public ExporterArgs queryExportInfo(Integer groupId,Integer storageType) throws Exception
	{
		ExporterArgs exportInfo = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = null;
		try{
			sql = this.sqlForDBExport;
			if(storageType == 2 
					|| storageType == 3){
				sql = this.sqlForFTPExport;
			}
			conn = datasource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setLong(1, groupId);
			rs = ps.executeQuery();
			if(rs.next()){
				exportInfo = new ExporterArgs();
				loadDB(exportInfo, rs, storageType);
				loadFtp(exportInfo, rs, storageType);
				loadPublic(exportInfo, rs, storageType);
			}
		}catch(Exception e){
			LOG.error("根据"+groupId+"获取输出信息失败,sql="+sql,e);
			throw e;
		}finally{
			DbUtil.close(rs, ps, conn);
		}
		return exportInfo;
	}
	
	private void loadPublic(ExporterArgs exportInfo, ResultSet rs, Integer storageType) throws Exception{
		if(storageType == 1
				|| storageType == 3){
			ConnectionInfo connInfo = new ConnectionInfo();
			connInfo.setId(rs.getInt("dbid"));
			connInfo.setUrl(rs.getString("url"));
			connInfo.setDriver(rs.getString("driver"));
			connInfo.setUserName(DESUtil.decode(rs.getString("user_name")));
			connInfo.setPassWord(DESUtil.decode(rs.getString("pass_word")));
			connInfo.setDescription("description");
			connInfo.setMaxwait(rs.getInt("connet_pool_maxwait"));
			connInfo.setMaxactive(rs.getInt("connet_pool_maxactive"));
			connInfo.setMaxidle(rs.getInt("connet_pool_maxidle"));
			connInfo.setValidateQuery(rs.getString("connet_pool_validatequery"));
			connInfo.setPort(rs.getInt("port"));
			connInfo.setPassiveFlag(rs.getInt("passive") == 1 ? true : false);
			exportInfo.setConnInfo(connInfo);
		}
	}
	
	private void loadDB(ExporterArgs exportInfo, ResultSet rs, Integer storageType) throws SQLException{
		if(storageType == 1){
			DBExportInfo dbExpInfo = new DBExportInfo();
			dbExpInfo.setId(rs.getLong("id"));
			dbExpInfo.setTableName(rs.getString("table_name"));
			dbExpInfo.setBatchNum(rs.getInt("batch_num"));
			exportInfo.setDbExpInfo(dbExpInfo);
		}
	}
	
	private void loadFtp(ExporterArgs exportInfo, ResultSet rs, Integer storageType) throws SQLException{
		if(storageType == 2 
				|| storageType == 3){
			FtpExportInfo ftpExpInfo = new FtpExportInfo();
			ftpExpInfo.setId(rs.getLong("id"));
			ftpExpInfo.setExportFileName(rs.getString("export_filename"));
			ftpExpInfo.setExportPath(rs.getString("export_path"));
			ftpExpInfo.setBatchNum(rs.getInt("batch_num"));
			ftpExpInfo.setSplitChar(rs.getString("split_char"));
			ftpExpInfo.setExportHeader(rs.getInt("export_header"));
			ftpExpInfo.setEncode(rs.getString("encode"));
			ftpExpInfo.setAdditionParams(rs.getString("addition_params"));
			ftpExpInfo.setCompressFormat(rs.getString("compress_format"));
			exportInfo.setFtpExpInfo(ftpExpInfo);
		}
	}

	public BasicDataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	public String getSqlForLoadConf() {
		return sqlForLoadConf;
	}

	public void setSqlForLoadConf(String sqlForLoadConf) {
		this.sqlForLoadConf = sqlForLoadConf;
	}

	public String getSqlForQueryConfById() {
		return sqlForQueryConfById;
	}

	public void setSqlForQueryConfById(String sqlForQueryConfById) {
		this.sqlForQueryConfById = sqlForQueryConfById;
	}

	public String getSqlForDBExport() {
		return sqlForDBExport;
	}

	public void setSqlForDBExport(String sqlForDBExport) {
		this.sqlForDBExport = sqlForDBExport;
	}

	public String getSqlForFTPExport() {
		return sqlForFTPExport;
	}

	public void setSqlForFTPExport(String sqlForFTPExport) {
		this.sqlForFTPExport = sqlForFTPExport;
	}
	
	
}
