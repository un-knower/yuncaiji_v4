package cn.uway.usummary.deal.impl;

import java.util.Map;

import cn.uway.usummary.deal.ParamReplaceService;

public class AutoReplaceServiceImpl extends ParamReplaceService{

	@Override
	public String replace(String sql, Map<String, String> map) {
		return this.autoReplace(sql);
	}

	@Override
	public Integer storageType() {
		return 1;
	}

}
