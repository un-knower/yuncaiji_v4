package cn.uway.usummary.export.impl;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.cache.impl.BlockingCacher;
import cn.uway.usummary.entity.ExporterArgs;
import cn.uway.usummary.export.AbstractExporter;
import cn.uway.usummary.util.DateUtil;
import cn.uway.usummary.util.DbUtil;
import jodd.util.StringUtil;
import oracle.sql.CLOB;

public class JDBCExporter extends AbstractExporter{
	
	private static Logger LOG = LoggerFactory.getLogger(JDBCExporter.class);
	
	private Connection conn;
	
	private PreparedStatement ps;
	
	private Map<String,String> colTypeMap;
	
	private List<String> columns;
	
	private String sql;
	
	private List<Map<String,String>> cacheRecords;
	
	public JDBCExporter(ExporterArgs exportInfo){
		super(exportInfo);
		init(exportInfo);
	}
	
	
	public void init(ExporterArgs exportInfo){
		this.batchNum = exportInfo.getDbExpInfo().getBatchNum();
		this.cacher = new BlockingCacher(createCacherName(), exportInfo.getRepository());
		// 创建JDBC连接
		try{
			Class.forName(DbUtil.getDriver(exportInfo.getConnInfo().getDriver()));
			String url = DbUtil.getUrl(exportInfo.getConnInfo().getUrl(),exportInfo.getConnInfo().getId());
			conn = DriverManager.getConnection(url, exportInfo.getConnInfo().getUserName(), exportInfo.getConnInfo().getPassWord());
		}catch(Exception e){
			LOG.error("获取输出数据源连接失败,原因：",e);
			this.exporterInitErrorFlag = true;
			this.errorCode = 0;
			this.cause = "获取输出数据源连接失败!";
			return;
		}
		
		// 获取输出表字段名称与类型并构造SQL
		ResultSet rs = null;
		ResultSetMetaData metaData = null;
		StringBuffer col = new StringBuffer();
		StringBuffer val = new StringBuffer(" values(");
		col.append("insert into ")
			.append(exportInfo.getDbExpInfo().getTableName())
			.append("(");
		try{
			ps = conn.prepareStatement("select * from "+exportInfo.getDbExpInfo().getTableName()+" where 1=0");
			rs = ps.executeQuery();
			metaData = rs.getMetaData();
			colTypeMap = new TreeMap<String,String>();
			columns = new ArrayList<String>(metaData.getColumnCount());
			for(int i=1; i<=metaData.getColumnCount(); i++){
				if(i == metaData.getColumnCount()){
					col.append(metaData.getColumnName(i).toUpperCase())
					.append(")");
					val.append("?)");
				}else{
					col.append(metaData.getColumnName(i).toUpperCase())
						.append(",");
					val.append("?,");
				}
				columns.add(metaData.getColumnName(i).toUpperCase());
				colTypeMap.put(metaData.getColumnName(i).toUpperCase(), metaData.getColumnTypeName(i).toUpperCase());
			}
			sql = col.append(val.toString()).toString();
			this.cacheRecords = new ArrayList<Map<String,String>>(this.batchNum);
		}catch(Exception e){
			LOG.error("获取输出表字段信息失败，原因：",e);
			this.exporterInitErrorFlag = true;
			this.errorCode = 0;
			this.cause = "获取输出表字段信息失败!";
		}finally{
			DbUtil.close(rs, ps, null);
		}
		
	}
	
	protected String createCacherName() {
		return new StringBuilder().append(exportInfo.getSqlNum())
				.append("_").append(System.currentTimeMillis()).toString();
	}
		
	public void export(List<Map<String,String>> records) throws Exception{
		for (Map<String,String> record : records) {
			this.current++;
			if (cacheRecords.size() <= batchNum - 1) {
				cacheRecords.add(record);
			} else {
				exportBatch(cacheRecords);
				cacheRecords = new ArrayList<Map<String,String>>(batchNum);
				cacheRecords.add(record);
			}
		}
	}
	
	private void exportBatch(List<Map<String,String>> records) throws Exception{
		long count = 0;
		try{
			ps = conn.prepareStatement(sql);
			int index = 1;
			for(Map<String,String> map : records){
				index = 1;
				count++;
				for(String colName: columns){
					covertColumn(map.get(colName),colTypeMap.get(colName),index,ps);
					index++;
				}
				ps.addBatch();
			}
			long startTime = System.currentTimeMillis();
			ps.executeBatch();
			this.totalTime += System.currentTimeMillis() - startTime;
			this.succ += count;
		}catch(Exception e){
			this.fail += count;
			LOG.error("批量插入数据失败,sql="+this.sql,e);
			throw new Exception("批量插入数据失败!");
		}finally{
			DbUtil.close(null, ps, null);
		}
	}
	
	private void covertColumn(String value,String colType,int index,PreparedStatement ps) throws Exception{
		if(colType == null){
			ps.setString(index, value);
			return;
		}
		switch(colType){
			case "DATE":
				if(StringUtil.isEmpty(value)){
					ps.setTimestamp(index, null);
					return;
				}
				ps.setTimestamp(index, new Timestamp(DateUtil.parseDate(value).getTime()));
				break;
			case "CLOB":
				if(StringUtil.isEmpty(value)){
					ps.setClob(index, CLOB.empty_lob());
					return;
				}
				StringReader reader = new StringReader(value);
				ps.setCharacterStream(index, reader);
				break;
			default:
				ps.setString(index, value);
		}
	}
	
	public void close(){
		try {
			// 可能还有未提交的内容，最后要提交一下。
			if (cacheRecords != null && cacheRecords.size() > 0) {
				this.exportBatch(cacheRecords);
			}
		} catch (Exception e) {
			this.fail += cacheRecords.size();
			this.errorCode = 0;
			this.cause = "批量插入数据失败!";
			LOG.error("批量插入数据失败!", e);
		} finally{
			DbUtil.close(null, ps, conn);
		}
		LOG.debug("【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，入库失败{}条数据"
				,new Object[]{this.exportInfo.getDbExpInfo().getTableName(),this.totalTime/1000L,this.succ,this.fail});
		
	}

}
