package cn.uway.ucloude.data.dataaccess.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import cn.uway.ucloude.data.dataaccess.DbHelper;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.model.ProtocalType;
import cn.uway.ucloude.data.dataaccess.model.ProtocalView;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.DESUtils;

public class ConnectionDAO {
	
	private static ILogger logger= LoggerManager.getLogger(ConnectionDAO.class); 
	
	/**
	 * 数据库驱动列表
	 */
	public static final Map<String,String> mapDBTypeDriver = new HashMap<String,String>();

	/**
	 * 加载驱动方法
	 */
	 static {
		mapDBTypeDriver.put("ORACLE", "oracle.jdbc.driver.OracleDriver");
		mapDBTypeDriver.put("SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		mapDBTypeDriver.put("OLEDB", "sun.jdbc.odbc.JdbcOdbcDriver");
		mapDBTypeDriver.put("MYSQL", "com.mysql.jdbc.Driver");
	}
	
	private static final ConnectionDAO DAO = new ConnectionDAO();

	public static ConnectionDAO getInstance(){
		return DAO;
	}
	
	public ConnectionDAO(){
		//initDbDriver();
	}
	
	public List<ProtocalView> getProtocalView(DataSource ds, ProtocalType type){
		List<ProtocalView> list = null;
		
		String sql = "select a.conn_key,b.user_name,b.pass_word,b.driver," +
					       "'jdbc:oracle:thin:@' || b.url || ':' || b.port || ':' || a.tns_name as url,"+
					       "b.connet_pool_maxactive,b.connet_pool_maxidle,b.connet_pool_maxwait,b.connet_pool_validatequery " +
					  "from ufa_connection_info a join ufa_connection_db_info b on a.conn_relate_id = b.id where a.type = ?";
		Connection conn = null;
		try{
			
			conn = ds.getConnection();
			DbHelper helper =new DbHelper();
			list = helper.query(conn, sql, new ResultSetHandler<List<ProtocalView>>(){

				@Override
				public List<ProtocalView> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<ProtocalView> views = new ArrayList<ProtocalView>();
					ProtocalView view = null;
					//DESUtils des = new DESUtils();
					while(rs.next())
					{
						view = new  ProtocalView();
						 view.setConnKey(rs.getString(1));
						 view.setUserName(DESUtils.decode(rs.getString(2)));
						 view.setPassWord(DESUtils.decode(rs.getString(3)));
						 view.setDriver(getDriver(rs.getString(4)));
						 view.setUrl(rs.getString(5));
						 view.setMaxActive(rs.getInt(6));
						 view.setMaxIdle(rs.getInt(7));
						 view.setMaxWait(rs.getInt(8));
						 view.setValidateQuery(rs.getString(9));
						 views.add(view);
						 logger.info(view.toString());
					}
					return views;
				}}, type.getValue());
			
		}catch(Exception ex){
			logger.info(ex);
		}
		finally{
			
			
	
		}
		return list;
		
	}
	
	public String getDriver(String key){
		if(mapDBTypeDriver.containsKey(key.toUpperCase()))
			return mapDBTypeDriver.get(key.toUpperCase());
		return mapDBTypeDriver.get("ORACLE");
	}

}
