package cn.uway.util.parquet.impl;

import cn.uway.util.parquet.FNCreater;

public class ConfigureNameCreater  extends FNCreater {
	// "/user/hive/warehouse/st.db/stpar/hour=2016011409/29-dev1-0009.parq"
	private static String warehousePath = "/user/impala/lte_hd";
	private String tblName;
	private String fullPath;

	public ConfigureNameCreater(String tblName) {
		this.tblName = tblName;
		init();
	}

	protected void init() {
		StringBuilder sb = new StringBuilder();
		sb.append(warehousePath).append("/")
		.append(tblName.toLowerCase()).append("/")
		.append(tblName.toLowerCase())
		.append("_tmp.parq");
		fullPath = sb.toString();
	}

	@Override
	public synchronized String getNewName() {
		return fullPath;
	}

}

