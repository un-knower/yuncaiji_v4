package cn.uway.usummary.deal.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.cache.CacheManager;
import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.deal.ParamProcessingService;
import cn.uway.usummary.deal.ParamReplaceService;
import cn.uway.usummary.entity.RequestResult;
import cn.uway.usummary.entity.USummaryConfInfo;
import cn.uway.usummary.util.DateUtil;
import cn.uway.usummary.util.JSONUtil;
import jodd.util.StringUtil;

public class AnalyzeParamServiceImpl implements ParamProcessingService{
	
	private static Logger LOG = LoggerFactory.getLogger(AnalyzeParamServiceImpl.class);
	
	private static CacheManager manager = AppContext.getBean("cacheManager", CacheManager.class);
	
	private Long sqlNum;
	
	private String placeholder;
	
	private RequestResult result;
	
	private USummaryConfInfo conf;
	
	private Map<String,String> map;
	
	private static List<ParamReplaceService> prs;
	
	static{
		prs = new ArrayList<ParamReplaceService>();
		prs.add(new AutoReplaceServiceImpl());
		prs.add(new PlaceholderReplaceServiceImpl());
	}
	
	public AnalyzeParamServiceImpl(){
		
	}
	
	public AnalyzeParamServiceImpl(Long sqlNum, String placeholder,RequestResult result){
		this.sqlNum = sqlNum;
		this.placeholder = placeholder;
		this.result = result;
	}
	
	public boolean checkParam() {
		if(sqlNum == null){
			result.setCode(0);
			result.setErrMsg("SQL编号不能为空!");
			LOG.debug("SQL编号不能为空!");
			return false;
		}
		if(sqlNum.longValue() <= 0){
			result.setCode(0);
			result.setErrMsg("SQL编号必须大于0!");
			LOG.debug("SQL编号必须大于0!");
			return false;
		}
		conf = manager.get(sqlNum);
		if(conf == null){
			result.setCode(0);
			result.setErrMsg("查询不到对应的SQL编号或未启用!");
			LOG.debug("查询不到对应的SQL编号或未启用!");
			return false;
		}
		if(StringUtil.isEmpty(conf.getSql())){
			result.setCode(0);
			result.setErrMsg("SQL未配置，请在表中配置SQL!");
			LOG.debug("SQL未配置，请在表中配置SQL!");
			return false;
		}
		if((conf.getIsPlaceholder() == 2 || conf.getIsPlaceholder() == 3) 
				&& StringUtil.isEmpty(placeholder)){
			result.setCode(0);
			result.setErrMsg("SQL中的占位符的值不能为空!");
			LOG.debug("SQL中的占位符的值不能为空!");
			return false;
		}
		if(conf.getIsPlaceholder() == 1 
				&& StringUtil.isNotEmpty(placeholder)){
			result.setCode(2);
			result.setErrMsg("SQL中的占位符自动替换，不需要传参!");
			LOG.debug("SQL中的占位符自动替换，不需要传参!");
		}else if(conf.getIsPlaceholder() == 0 
				&& StringUtil.isNotEmpty(placeholder)){
			result.setCode(2);
			result.setErrMsg("SQL中没有占位符，不需要传参!");
			LOG.debug("SQL中没有占位符，不需要传参!");
		}
		try{
			// 转换参数
			map = JSONUtil.covertStringToMap(placeholder);
		}catch(Exception e){
			result.setCode(0);
			result.setErrMsg("占位符参数转换失败，请检查格式!");
			LOG.debug("占位符参数转换失败，请检查格式!");
			return false;
		}
		return true;
	}

	public USummaryConfInfo paramProcessing() {
		String sql = conf.getSql();
		// SQL中占位符的替换
		for(ParamReplaceService pr: prs){
			if(sql.indexOf("{") < 0){
				break;
			}
			sql = pr.replace(sql, map);
		}
		conf.setSql(sql);
		if(conf.getSql().indexOf("{") >= 0){
			result.setCode(0);
			result.setErrMsg("SQL中存在占位符没有被替换!");
			LOG.debug("SQL中存在占位符没有被替换!");
			return null;
		}
		return conf;
	}

	@Override
	public Date getDataTime() throws Exception {
		String dataTime = map == null?null:map.get("DATATIME");
		if(StringUtils.isEmpty(dataTime)){
			return new Date();
		}
		return DateUtil.parseDate(dataTime);
	}

}
