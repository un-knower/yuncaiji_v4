package cn.uway.usummary.parser.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.entity.RequestResult;
import cn.uway.usummary.entity.USummaryConfInfo;
import cn.uway.usummary.parser.AbstractParser;

public class DataAnalyzeParser extends AbstractParser{
	
	private static Logger LOG = LoggerFactory.getLogger(DataAnalyzeParser.class);

	public void access(USummaryConfInfo conf,RequestResult result) {
		super.access(conf, result);
		try{
			LOG.debug("开始执行sql："+conf.getSql());
			conn = datasource.getConnection();
			ps = conn.prepareStatement(conf.getSql());
			if(conf.getStorageType() == 4){
				ps.execute();
			}else{
				rs = ps.executeQuery();
				metaData = rs.getMetaData();
				columnNum = metaData.getColumnCount();
				
				// 当为文件输出时，设置列名
				if(conf.getStorageType() == 2 
						|| conf.getStorageType() == 3){
					headers = new ArrayList<String>(columnNum);
					for(int i=1; i<columnNum; i++){
						headers.add(metaData.getColumnName(i).toUpperCase());
					}
				}
			}
		}catch(Exception e){
			LOG.error("执行SQL时发生异常,sql="+conf.getSql(),e);
			result.setCode(0);
			result.setErrMsg("执行SQL时发生异常!");
		}
	}

	public boolean hasNext() throws Exception {
		return rs.next();
	}

	public Map<String, String> next() throws Exception {
		Map<String, String> map = new HashMap<String,String>();
		result.setTotalCount(++totalCount);
		for(int i=1; i<=columnNum; i++){
			map.put(metaData.getColumnName(i).toUpperCase(), rs.getString(metaData.getColumnName(i)));
		}
		return map;
	}
}
