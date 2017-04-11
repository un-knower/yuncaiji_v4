package cn.uway.framework.warehouse.exporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.util.DbUtil;

//insert into A select from B
//truncate B
//insert into B
//update B set x=xx where y=yy:必须更新、选择性更新
//delete from A where z=zz
//
public class JdbcBatchBackUpExporter extends JdbcBatchExporter{
	private String insertTime;
	private String tableName;
	private String bakTableName;
	public JdbcBatchBackUpExporter(DbExportTemplateBean dbExportTempletBean,
			ExporterArgs exporterArgs) {
		super(dbExportTempletBean, exporterArgs);
	}

	/**
	 * 创建数据库链接(先调用父类方法，然后添加自己的业务：备份表)
	 */
	@Override
	protected void initJdbc(ExporterArgs exporterArgs) {
		super.initJdbc(exporterArgs);
		
		insertTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		tableName = dbExportTempletBean.getTable().getTableName();
		bakTableName = tableName+"_HIS";
		DbUtil.backupTableWithTime(con, columns, insertTime, tableName, bakTableName);
		DbUtil.truncateTable(con, tableName);
	}
	
	@Override
	protected void afterClose(){
		StringBuffer sb = new StringBuffer();
		
		List<String> pkFN = new ArrayList<String>();
		pkFN.add("INFO_NUM");
		
		List<String> keepFN = new ArrayList<String>();
		keepFN.add("PLAN_ID");
		keepFN.add("ISSUE_TIME");
		
		List<String> updateFN = new ArrayList<String>();
		updateFN.add("UPDATE_TIME");
		
		List<String> compareFN = new ArrayList<String>();
		compareFN.addAll(pkFN);
		compareFN.add("NET_TYPE_ID");
		compareFN.add("NET_TYPE_NAME");
		compareFN.add("SITE_NAME");
		compareFN.add("SITE_TYPE");
		compareFN.add("LONGITUDE");
		compareFN.add("LATITUDE");
		compareFN.add("RADIUS");
		compareFN.add("PROBLEM_DESC");
		compareFN.add("PROGRESS_STATUS");
		compareFN.add("INVALID_TIME");
		compareFN.add("EFFECTIVE_TIME");
		compareFN.add("COVERAREA");
		compareFN.add("BUILDING_NAME");
		compareFN.add("REGION_NAME");
		compareFN.add("QUJU");
		compareFN.add("ZERENGUISHU");
		compareFN.add("CITY_NAME");
		compareFN.add("CITY_ID");
		compareFN.add("MAINTENANCE_UNIT");
		compareFN.add("BUILDING_CODE");
		compareFN.add("DUANJU");
		
		sb.append(" bt.insert_time=to_date('").append(insertTime).append("','yyyy-mm-dd hh24:mi:ss')");
		String insertTimeStr = sb.toString();
		
		sb.setLength(0);
		for (String str : pkFN) {
			sb.append("( t.").append(str).append("=").append("bt.").append(str)
			.append(" or (t.").append(str).append(" is null and bt.").append(str).append(" is null)").append(") and ");
		}
		sb.append(insertTimeStr);
		// 唯一标识字段
		String pkStr = sb.toString();
		
		sb.setLength(0);
		for (String str : keepFN) {
			sb.append(" t.").append(str).append(",");
		}
		// 需要保持的字段t
		String keepStr_t = sb.deleteCharAt(sb.length()-1).toString();
		sb.setLength(0);
		for (String str : keepFN) {
			sb.append(" bt.").append(str).append(",");
		}
		// 需要保持的字段bt
		String keepStr_bt = sb.deleteCharAt(sb.length()-1).toString();
		
		sb.setLength(0);
		for (String str : updateFN) {
			sb.append(" t.").append(str).append(",");
		}
		// 更新时间字段t
		String updateStr_t = sb.deleteCharAt(sb.length()-1).toString();
		sb.setLength(0);
		for (String str : updateFN) {
			sb.append(" bt.").append(str).append(",");
		}
		// 更新时间字段bt
		String updateStr_bt = sb.deleteCharAt(sb.length()-1).toString();
		
		sb.setLength(0);
		for (String str : compareFN) {
			sb.append("( t.").append(str).append("=").append("bt.").append(str)
			.append(" or (t.").append(str).append(" is null and bt.").append(str).append(" is null)").append(") and ");
		}
		sb.append(insertTimeStr);
		// 一般比较字段
		String compareStr = sb.toString();
		
		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
		DatabaseConnectionInfo connectionInfo = dbTargetBean.getConnectionInfo();
		Connection con=null;
		Statement st = null;
		try {
			Class.forName(connectionInfo.getDriver());
			con = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUserName(), connectionInfo.getPassword());
			st = con.createStatement();

			sb.setLength(0);
			sb.append("update ").append(tableName)
			.append(" t set (").append(keepStr_t).append(")=(select ").append(keepStr_bt).append(" from ")
			.append(bakTableName).append(" bt where ").append(pkStr).append(")")
			.append(" where exists (select 1 from ").append(bakTableName).append(" bt where ").append(pkStr).append(")");
			st.executeUpdate(sb.toString());
			
			sb.setLength(0);
			sb.append("update ").append(tableName)
			.append(" t set (").append(updateStr_t).append(")=(select ").append(updateStr_bt).append(" from ")
			.append(bakTableName).append(" bt where ").append(compareStr).append(")")
			.append(" where exists (select 1 from ").append(bakTableName).append(" bt where ").append(compareStr).append(")");
			st.executeUpdate(sb.toString());
			
			sb.setLength(0);
			sb.append("delete from ").append(bakTableName)
			.append(" bt where exists (select 1 from ").append(tableName).append(" t where ").append(compareStr).append(")");
			st.executeUpdate(sb.toString());
		} catch (Exception e) {
			LOGGER.error("备份时，数据库连接创建失败。", e);
		}finally {
			DbUtil.close(null, st, con);
		}
	}
	
}
