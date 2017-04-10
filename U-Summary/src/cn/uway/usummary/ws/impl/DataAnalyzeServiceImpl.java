package cn.uway.usummary.ws.impl;

import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.deal.impl.AnalyzeParamServiceImpl;
import cn.uway.usummary.entity.RequestResult;
import cn.uway.usummary.entity.USummaryConfInfo;
import cn.uway.usummary.entity.WarehouseReport;
import cn.uway.usummary.parser.Parser;
import cn.uway.usummary.warehouse.repository.BufferedMultiExportRepository;
import cn.uway.usummary.ws.DataAnalyzeService;

@WebService(endpointInterface = "cn.uway.usummary.ws.DataAnalyzeService", serviceName = "DataAnalyzeService")
public class DataAnalyzeServiceImpl implements DataAnalyzeService{
	
	private static Logger LOG = LoggerFactory.getLogger(DataAnalyzeServiceImpl.class);
		
	public String DataAnalyze(@WebParam(name = "sqlNum")String sqlNum, @WebParam(name = "placeholder") String placeholder) {
		LOG.debug("进入数据分析接口,sqlNum="+sqlNum+",placeholder="+placeholder);
		RequestResult result = new RequestResult();
		boolean exceptionFlag = false;
		Parser parser = null;
		BufferedMultiExportRepository repository = null;
		try{
			AnalyzeParamServiceImpl param = new AnalyzeParamServiceImpl(Long.parseLong(sqlNum),placeholder,result);
			if(!param.checkParam()){
				return JSON.toJSONString(result);
			}
			USummaryConfInfo conf = param.paramProcessing();
			if(conf == null){
				return JSON.toJSONString(result);
			}
			parser = AppContext.getBean("dataAnalyzeParser", Parser.class);
			parser.access(conf, result);
			if(result.getCode() == 0){
				return JSON.toJSONString(result);
			}
			// impala的插入操作
			if(conf.getStorageType() == 4){
				return JSON.toJSONString(result);
			}
			Map<String,String> record = null;
			while(parser.hasNext()){
				record = parser.next();
				if(repository == null){
					repository = parser.createRepository(param.getDataTime());
				}
				repository.transport(record);
			}
			
		}catch(Exception e){
			exceptionFlag = true;
			LOG.error("数据处理过程中发生错误，原因：",e);
			result.setCode(0);
			result.setErrMsg("数据处理过程中发生错误!");
		}finally{
			if(parser != null){
				parser.close();
			}
			if(repository != null){
				repository.commit(exceptionFlag);
			}
		}
		try{
			if(repository  != null){
				WarehouseReport report = repository.getReport();
				if(report.getErrCode() == 0){
					result.setCode(report.getErrCode());
					result.setErrMsg(report.getCause());
				}
			}
		}catch(Exception e){
			result.setCode(0);
			result.setErrMsg("获取exporter的输出结果失败!");
			LOG.error("获取exporter的输出结果失败!",e);
		}
		String rs = JSON.toJSONString(result);
		LOG.debug("执行结果："+rs);
		return rs;
	}

}
