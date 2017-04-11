package cn.uway.util.parquet.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import cn.uway.util.parquet.FNCreater;

public class DefaultNameCreater extends FNCreater {
	// "/user/hive/warehouse/st.db/stpar/hour=2016011409/29-dev1-0009.parq"
	private static String warehousePath = "/user/impala/lte_hd";
	private String tblName;
	private String partStr;
	private String fullPath;
	private Random r = new Random();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss-SSS");

	public DefaultNameCreater(String tblName, String partStr) {
		this.tblName = tblName;
		this.partStr = partStr;
		init();
	}

	protected void init() {
		StringBuilder sb = new StringBuilder();
		sb.append(warehousePath).append("/").append(tblName.toLowerCase());
		if(!StringUtils.isEmpty(partStr))
		   sb.append("/").append(partStr);
		
		sb.append("/%s-%d.parq");
		fullPath = sb.toString();
	}

	@Override
	public synchronized String getNewName() {
		return String.format(fullPath, sdf.format(new Date()) ,r.nextInt(100));
	}

}
