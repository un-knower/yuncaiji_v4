package cn.uway.usummary.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.dao.USummaryDao;
import cn.uway.usummary.entity.ExporterArgs;
import cn.uway.usummary.entity.RequestResult;
import cn.uway.usummary.entity.USummaryConfInfo;
import cn.uway.usummary.util.DbUtil;
import cn.uway.usummary.warehouse.repository.BufferedMultiExportRepository;

public abstract class AbstractParser implements Parser{
	
	private static Logger LOG = LoggerFactory.getLogger(AbstractParser.class);
	
	protected USummaryConfInfo conf;
	
	protected RequestResult result;
	
	protected USummaryDao usummaryDao;
	
	protected Connection conn;
	
	protected PreparedStatement ps;
	
	protected ResultSet rs;
	
	protected List<String> headers;
	
	/**
	 * 采集表元信息
	 */
	protected ResultSetMetaData metaData;
	
	/**
	 * 采集字段数
	 */
	protected int columnNum;
	
	protected long totalCount;
	
	protected ExporterArgs exportInfo;
	
	/**
	 * 数据库连接池
	 */
	protected BasicDataSource datasource;
	
	public void access(USummaryConfInfo conf,RequestResult result) {
		this.conf = conf;
		this.result = result;
		try{
			if(conf.getStorageType() != 4){
				exportInfo = usummaryDao.queryExportInfo(conf.getGroupId(), conf.getStorageType());
			}
		}catch(Exception e){
			result.setCode(0);
			result.setErrMsg("从数据库获取输出信息失败!");
			LOG.error("从数据库获取输出信息失败:",e);
		}
	}
	
	public BufferedMultiExportRepository createRepository(Date dataTime){
		exportInfo.setDataTime(dataTime);
		exportInfo.setHeaders(headers);
		exportInfo.setSqlNum(conf.getSqlNum());
		exportInfo.setStorageType(conf.getStorageType());
		return new BufferedMultiExportRepository(exportInfo);
	}

	public boolean hasNext() throws Exception{
		// TODO Auto-generated method stub
		return false;
	}

	public Map<String, String> next()  throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void close() {
		DbUtil.close(rs, ps, conn);
	}

	public USummaryDao getUsummaryDao() {
		return usummaryDao;
	}

	public void setUsummaryDao(USummaryDao usummaryDao) {
		this.usummaryDao = usummaryDao;
	}

	public BasicDataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}
	
}
