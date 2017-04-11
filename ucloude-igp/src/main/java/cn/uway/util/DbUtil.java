package cn.uway.util;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 数据库工具。
 * 
 * @author ChenSijiang 2012-10-29
 */
public final class DbUtil{

	private static final ILogger log = LoggerManager.getLogger(DbUtil.class);

	/**
	 * 关闭数据库连接及相关资源。
	 * 
	 * @param rs 结果集。
	 * @param stm 语句对象。
	 * @param conn 连接。
	 */
	public static void close(ResultSet rs, Statement stm, Connection conn){
		if(rs != null){
			try{
				rs.close();
			}catch(Exception ex){
				log.warn("关闭ResultSet时发生异常。", ex);
			}
		}
		if(stm != null){
			try{
				stm.close();
			}catch(Exception ex){
				log.warn("关闭Statement时发生异常。", ex);
			}
		}
		if(conn != null){
			try{
				conn.close();
			}catch(Exception ex){
				log.warn("关闭Connection时发生异常。", ex);
			}
		}

	}

	public static boolean tableExists(Connection con, String tableName, long taskId) throws SQLException{
		if(StringUtil.isEmpty(tableName)){
			return false;
		}
		if(con == null)
			return true;

		String prefix = taskId < 0 ? "" : (taskId + " - ");

		String sql = "select 1 from " + tableName + " where 1=2";
		Statement st = null;
		ResultSet rs = null;
		try{
			if(!con.isClosed()){
				st = con.createStatement();
				st.setQueryTimeout(1800);
				rs = st.executeQuery(sql);
			}
		}catch(SQLException e){
			int code = e.getErrorCode();
			// 表或视图不存在，oracle错误码为924，sysbase与sqlserver的错误码为208
			if(code == 942 || code == 208){
				log.debug(prefix + "表或视图不存在,测试语句:" + sql + ",出现的异常信息:" + e.getMessage().trim());
				return false;
			}
			log.debug(prefix + "测试表或视图是否存在时,发生异常,测试语句:" + sql + ",出现的异常信息:" + e.getMessage().trim());
			return true;
		}catch(Exception e){
			log.debug(prefix + "测试表或视图是否存在时,发生异常,测试语句:" + sql + ",出现的异常信息:" + e.getMessage().trim());
			return true;
		}finally{
			try{
				if(rs != null){
					rs.close();
				}
				if(st != null){
					st.close();
				}
			}catch(Exception e){}
		}
		return true;
	}

	private DbUtil(){
		super();
	}

	public static String jdbcDriver(String url){
		if(StringUtil.isEmpty(url))
			return null;
		if(url.trim().startsWith("jdbc:oracle"))
			return "oracle.jdbc.driver.OracleDriver";
		return null;
	}

	/* 在Oracle 数据库中将Clob转换成String类型 */
	public static String ClobParse(Clob clob) throws Exception{
		if(clob == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		java.io.Reader is = clob.getCharacterStream();
		java.io.BufferedReader br = new java.io.BufferedReader(is);
		String s = null;
		// 当到达最后一行readLine的时候,s为空退出
		while((s = br.readLine()) != null){
			sb.append(s);
			// content += s;
		}
		br.close();
		is.close();
		// content = clob.getSubString((long)1,(int)clob.length());
		return sb.toString();
	}
	
	/**
	 * 删除表。
	 * 
	 * @param con
	 *            数据库连接。
	 * @param tableName
	 *            表名。
	 * @return 是否成功。
	 */
	public static boolean dropTable(Connection con, String tableName) {
		Statement st = null;
		try {
			st = con.createStatement();
			st.executeUpdate("drop table " + tableName + " purge");
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			close(null, st, null);
		}

	}

	/**
	 * 截断表（不触动触发器）。
	 * 
	 * @param con
	 *            数据库连接。
	 * @param tableName
	 *            表名。
	 * @return 是否截断成功。
	 */
	public static boolean truncateTable(Connection con, String tableName) {
		Statement st = null;
		try {
			st = con.createStatement();
			st.executeUpdate("truncate table " + tableName);
			return true;
		} catch (Exception e) {
			log.error("截断表时异常：" + tableName, e);
			return false;
		} finally {
			close(null, st, null);
		}

	}
	
	/**
	 * 清空表（触动触发器）。
	 * 
	 * @param con
	 *            数据库连接。
	 * @param tableName
	 *            表名。
	 * @return 是否截断成功。
	 */
	public static boolean deleteTable(Connection con, String tableName) {
		Statement st = null;
		try {
			st = con.createStatement();
			st.executeUpdate("delete from " + tableName);
			return true;
		} catch (Exception e) {
			log.error("清空表时异常：" + tableName, e);
			return false;
		} finally {
			close(null, st, null);
		}
		
	}

	/**
	 * 备份表。
	 * 
	 * @param con
	 *            数据库连接。
	 * @param tableName
	 *            待备份的表名。
	 * @param bakTableName
	 *            备份表名。
	 * @return 是否备份成功。
	 */
	public static boolean backupTable(Connection con, String tableName, String bakTableName) {
		Statement st = null;
		try {
			st = con.createStatement();
			st.executeUpdate("create table " + bakTableName + " as (select * from " + tableName + ")");
			return true;
		} catch (Exception e) {
			log.error("备份表时异常，TableName：" + tableName + "，BakTableName：" + bakTableName, e);
			return false;
		} finally {
			close(null, st, null);
		}
	}
	
	/**
	 * 备份表，添加一个insert_time字段。
	 * 
	 * @param con
	 *            数据库连接。
	 * @param columns
	 *            包含列
	 * @param insertTime
	 *            插入时间，格式yyyy-mm-dd hh24:mi:ss
	 * @param tableName
	 *            待备份的表名。
	 * @param bakTableName
	 *            备份表名。
	 * @return 是否备份成功。
	 */
	public static boolean backupTableWithTime(Connection con, List<ColumnTemplateBean> columns, String insertTime, String tableName, String bakTableName) {
		Statement st = null;
		StringBuffer fieldName = new StringBuffer();
		for (ColumnTemplateBean column : columns) {
			fieldName.append(column.getColumnName()).append(",");
		}
		StringBuffer sb = new StringBuffer("INSERT INTO ");
		sb.append(bakTableName).append("(").append(fieldName.toString())
		 .append("INSERT_TIME").append(") ")
		 .append("SELECT ").append(fieldName.toString())
		 .append("TO_DATE('").append(insertTime).append("','yyyy-mm-dd hh24:mi:ss') AS INSERT_TIME")
		 .append(" FROM ").append(tableName);
		try {
			st = con.createStatement();
			st.executeUpdate(sb.toString());
			return true;
		} catch (Exception e) {
			log.error("备份表时异常，TableName：" + tableName + "，BakTableName：" + bakTableName, e);
			return false;
		} finally {
			close(null, st, null);
		}
	}
}
