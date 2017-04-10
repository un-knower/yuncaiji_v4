package cn.uway.usummary.parser;

import java.util.Date;
import java.util.Map;

import cn.uway.usummary.entity.RequestResult;
import cn.uway.usummary.entity.USummaryConfInfo;
import cn.uway.usummary.warehouse.repository.BufferedMultiExportRepository;

public interface Parser {
	
	public void access(USummaryConfInfo conf,RequestResult result);
	
	public boolean hasNext() throws Exception;
	
	public Map<String,String> next() throws Exception ;
	
	public BufferedMultiExportRepository createRepository(Date dataTime);
	
	public void close();
}
