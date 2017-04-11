package cn.uway.framework.db;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerManager;
//
//import cn.uway.des.DESDecryptor;

public class IGPBasicDataSource extends BasicDataSource{
//	private static final ILogger log = LoggerManager.getLogger(IGPBasicDataSource.class);
//
//	private BasicDataSource basedatasource;
//	
//	public Connection getConnection() throws SQLException{
//		try {
//			return basedatasource.getConnection();
//		} catch (SQLException e) {
//			throw new SQLException(e);
//		}
//	}
//
//	public void setBasedatasource(BasicDataSource basedatasource) {
//		this.basedatasource = basedatasource;
//		if("login".equalsIgnoreCase(this.basedatasource.getUsername()) && "login".equalsIgnoreCase(this.basedatasource.getPassword())){
//			String[] nameAndPwd;
//			nameAndPwd = getUserNameAndPassword(this.basedatasource.getUrl(),"IGPV3");
//			if(nameAndPwd != null && nameAndPwd.length > 1){
//				this.basedatasource.setUsername(nameAndPwd[0]);
//				this.basedatasource.setPassword(nameAndPwd[1]);
//			}
//		}
//	}
//
//	private static String[] getUserNameAndPassword(String dburl, String name){
//		String sql = "select max(LOGIN_INFO) as LOGIN_INFO from login.TB_LOGIN_INFO where project_name='"+name+"'";
//		Connection con = null;
//		PreparedStatement st = null;
//		ResultSet rs = null;
//		try {
//			Class.forName("oracle.jdbc.driver.OracleDriver");
//			con = DriverManager.getConnection(dburl, "login", "login");
//			
//			String loginInfo = null;
//			st = con.prepareStatement(sql);
//			log.debug("query for - " + sql);
//			rs = st.executeQuery();
//			if (rs.next()) {
//				loginInfo = rs.getString("LOGIN_INFO");
//				log.debug("query ok - " + sql);
//
//			} else {
//				throw new Exception("select出来的记录数为0");
//			}
//
//			DESDecryptor desDecryptor = new DESDecryptor();
//			String result = desDecryptor.desDecrypt(loginInfo, "UWAY@SOFT2009");
//			if (desDecryptor.getLastException() != null) {
//				throw desDecryptor.getLastException();
//			}
//			result = result.toLowerCase().replace("user id=", "").replace("password=", "");
//			String[] split = result.split(";");
//			return split;
//		} catch (Throwable e) {
//			 log.error("在login.TB_LOGIN_INFO同义词中获取数据库账户时出现异常:" + sql, e);
//		} finally {
//			if(rs != null){
//				try {
//					rs.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//			if(st != null){
//				try {
//					st.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//			if(con != null){
//				try {
//					con.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return null;
//	}
//
//	public synchronized void close() throws SQLException {
//		basedatasource.close();
//	}
	
}
