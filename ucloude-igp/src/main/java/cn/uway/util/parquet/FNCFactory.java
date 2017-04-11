package cn.uway.util.parquet;

import cn.uway.util.parquet.impl.ConfigureNameCreater;
import cn.uway.util.parquet.impl.DefaultNameCreater;

public class FNCFactory {

	/**
	 * 创建文件名生成类
	 * 
	 * @param tblName
	 *            表名
	 * @param partStr
	 *            分区字符串
	 * @return
	 */
	public static FNCreater getCreater(String tblName,
			String partStr,int ctType) {
		switch (ctType) {
		case 1:
			return new ConfigureNameCreater(tblName);
		default:
			return new DefaultNameCreater(tblName, partStr);
		}
	}
}
