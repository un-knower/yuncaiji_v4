package cn.uway.util.parquet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.parquet.impl.FileNameDateUtil;

/**
 * 多分区处理起来太麻烦，暂时降级为只支持单分区
 * @author sunt
 *
 */
public class PartPool {
	private static ILogger LOG = LoggerManager.getLogger(PartPool.class);
	// fn,part
	private static HashMap<String, String> partPool = new HashMap<String, String>();
	
	/**
	 * 拼接分区字符串
	 * 
	 * @param fn
	 *            文件名
	 * @param cityId
	 *            城市id
	 * @return
	 */
	public static synchronized String getPart(String fn, String cityId) {
		// 文件名+城市id
		String partKey = fn + cityId;
		if (!partPool.containsKey(partKey)) {
			// 主要分文件型 或数据库类型分区处理, 如果是数据库类型则判断是否以hadoop_db开头
			// hadoop_db_partition_hour:%%Y-%M-%%D%%H%%m%%S ,
			// hadoop_db_partition_day:%%Y-%M-%%D%%H%%m%%S ,
			StringBuilder sb = new StringBuilder();
			String dateStr = FileNameDateUtil.getFileDate(fn);

			// hadoop输出
			if (partKey.toLowerCase().startsWith("hadoop_db")) {
				String partionConf = partKey.toLowerCase();
				String info = "hadoop_db_partition_";
				int index = partionConf.indexOf(info) + info.length();

				String partitionType = partionConf.substring(index,
						partionConf.lastIndexOf("="));

				if ("year".equals(partitionType)) {
					sb.append("year=" + dateStr.substring(0, 4));
				}
				if ("month".equals(partitionType)) {
					sb.append("year=" + dateStr.substring(0, 4));
					sb.append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)));
					
				}
				if ("day".equals(partitionType)) {
					sb.append("year=" + dateStr.substring(0, 4));
					sb.append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)));
					sb.append("/day=")
					.append(Integer.parseInt(dateStr.substring(6, 8)));
					
				}
				if ("hour".equals(partitionType)) {
					sb.append("year=" + dateStr.substring(0, 4));
					sb.append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)));
					sb.append("/day=")
					.append(Integer.parseInt(dateStr.substring(6, 8)));
					sb.append("/hour=")
					.append(Integer.parseInt(dateStr.substring(8, 10)));
				}
				if ("minute".equals(partitionType)) {
					sb.append("year=" + dateStr.substring(0, 4));
					sb.append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)));
					sb.append("/day=")
					.append(Integer.parseInt(dateStr.substring(6, 8)));
					sb.append("/hour=")
					.append(Integer.parseInt(dateStr.substring(8, 10)));
					sb.append("minute/=")
					.append(Integer.parseInt(dateStr.substring(10, 12)));
				}

			} else {
				sb.append("year=" + dateStr.substring(0, 4)).append("/month=")
						.append(Integer.parseInt(dateStr.substring(4, 6)))
						.append("/day=")
						.append(Integer.parseInt(dateStr.substring(6, 8)))
						.append("/hour=")
						.append(Integer.parseInt(dateStr.substring(8, 10)))
						.append("/city_id=").append(cityId);
			}
			LOG.debug(partKey+" ,partitions:"+sb.toString());
			partPool.put(partKey, sb.toString());
		}
		return partPool.get(partKey);
	}
	
	/**
	 * 拼接分区字符串
	 * 
	 * @param fn
	 *            文件名
	 * @return
	 */
	public static synchronized String getPart(String fn,int p) {
		// 文件名
		String partKey = fn;
		if (!partPool.containsKey(partKey)) {
			String dateStr = FileNameDateUtil.getFileDate(fn);
			String partition = convertToString(p);
			StringBuilder sb = new StringBuilder();
			switch(partition)
			{
				case "year":
				sb.append("year=" + dateStr.substring(0, 4));
				break;
				case "month":
					sb.append("year=" + dateStr.substring(0, 4)).append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)));
					break;
				case "day":
					sb.append("year=" + dateStr.substring(0, 4)).append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)))
					.append("/day=")
					.append(Integer.parseInt(dateStr.substring(6, 8)));
					break;
				case "hour":
					sb.append("year=" + dateStr.substring(0, 4)).append("/month=")
					.append(Integer.parseInt(dateStr.substring(4, 6)))
					.append("/day=")
					.append(Integer.parseInt(dateStr.substring(6, 8)))
					.append("/hour=")
					.append(Integer.parseInt(dateStr.substring(8, 10)));
					break;
				case "none":
					sb.append("");
			}
			partPool.put(partKey, sb.toString());
		}
		return partPool.get(partKey);
	}

	private static String convertToString(int partition){
		String part = "hour";
		switch(partition)
		{
		case 1 :
			part =  "year";
			break;
		case 2 : 
			part =  "month";
			break;
		case 3 : 
			part =  "day";
			break;
		case 4 : 
			part =  "hour";
			break;
		case 0 : 
			part =  "none";
		}
		return part;
	}
	
	/**
	 * 清除fn的所有分区
	 * 
	 * @param fn
	 */
	public synchronized static void removeAll(String fn) {
		List<String> remKeys = new ArrayList<String>();
		for (String partKey : partPool.keySet()) {
			if (partKey.startsWith(fn)) {
				remKeys.add(partKey);
			}
		}
		for (String key : remKeys) {
			partPool.remove(key);
		}
		LOG.debug("partPool.size:{}",partPool.size());
	}
}
