package cn.uway.usummary.util;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DbUtil{

	private static final Logger log = LoggerFactory.getLogger(DbUtil.class);
	
	private static final String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
	
	public static String getDriver(String type){
		String driver = CustomerPropertyConfigurer.getProperties().get(type);
		return driver == null?DEFAULT_DRIVER:driver;
	}
	
	public static String getUrl(String ip,int id){
		String url = CustomerPropertyConfigurer.getProperties().get("DATABASE.URL."+id);
		if(StringUtil.isNotEmpty(url)){
			url = url.replace("{ip}", ip);
		}
		return url;
	}

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

	private DbUtil(){
		super();
	}

	public static String jdbcDriver(String url){
		if(StringUtils.isEmpty(url))
			return null;
		if(url.trim().startsWith("jdbc:oracle"))
			return "oracle.jdbc.driver.OracleDriver";
		return null;
	}

	/**
	 * 将clob转换为String
	 * @param clob
	 * @return
	 * @throws Exception
	 */
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
	
}
