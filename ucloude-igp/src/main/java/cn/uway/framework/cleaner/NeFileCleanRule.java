package cn.uway.framework.cleaner;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class NeFileCleanRule implements CleanRule {

	// 文件清理多久以前的文件，单位：分钟
	private Integer filesCleanTime;
	public void setFilesCleanTime(Integer filesCleanTime) {
		this.filesCleanTime = filesCleanTime;
	}

	/**
	 * 日志
	 */
	protected static ILogger LOGGER = LoggerManager.getLogger(NeFileCleanRule.class); // 日志

	@Override
	public boolean cleanable(List<String> localFile) {
		if (localFile == null || localFile.size() == 0)
			return false;
		String pattern = "yyyyMMdd";
		List<String> keepList = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		for (String path : localFile) {
			String fileName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
			String name = fileName.substring(0, pattern.length());
			Date fileDate;
			try {
				fileDate = sdf.parse(name);
				long timestamp = System.currentTimeMillis() - fileDate.getTime();
				if (timestamp < filesCleanTime * 60 * 1000) {
					keepList.add(path);
				}
			} catch (ParseException e) {
				LOGGER.error("解析日期出错", e);
				return false;
			}
		}
		if (keepList.size() > 0) {
			localFile.removeAll(keepList);
		}
		return true;
	}

}
