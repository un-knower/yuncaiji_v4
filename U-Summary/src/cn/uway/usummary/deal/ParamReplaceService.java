package cn.uway.usummary.deal;

import java.util.Map;

import cn.uway.usummary.util.DateUtil;

public abstract class ParamReplaceService {
	
	public abstract String replace(String sql,Map<String,String> map);
	
	public abstract Integer storageType();
	
	protected String autoReplace(String sql){
		sql = sql.replaceAll("\\{CURRENTTIME\\}", DateUtil.getCurrentTime());
		sql = sql.replaceAll("\\{CURRENTHOUR\\}", DateUtil.getCurrentHour());
		sql = sql.replaceAll("\\{CURRENTDAY\\}", DateUtil.getCurrentDay());
		sql = sql.replaceAll("\\{PASTTIME\\}", DateUtil.getPastTime());
		sql = sql.replaceAll("\\{PASTHOUR\\}", DateUtil.getPastHour());
		sql = sql.replaceAll("\\{PASTDAY\\}", DateUtil.getPastDay());
		sql = sql.replaceAll("\\{CURRENTWEEK\\}", DateUtil.getCurrentWeek());
		sql = sql.replaceAll("\\{PASTWEEK\\}", DateUtil.getPastWeek());
		sql = sql.replaceAll("\\{CURRENTMONTH\\}", DateUtil.getCurrentMonth());
		sql = sql.replaceAll("\\{PASTMONTH\\}", DateUtil.getPastMonth());
		sql = sql.replaceAll("\\{YEAR\\}", DateUtil.getYear());
		sql = sql.replaceAll("\\{MONTH\\}", DateUtil.getMonth());
		sql = sql.replaceAll("\\{DAY\\}", DateUtil.getDay());
		sql = sql.replaceAll("\\{YESTERDAY\\}", DateUtil.getYesterday());
		sql = sql.replaceAll("\\{HOUR\\}", DateUtil.getHour());
		return sql;
	}
}
