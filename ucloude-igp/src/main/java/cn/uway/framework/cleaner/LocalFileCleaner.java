package cn.uway.framework.cleaner;

import java.util.LinkedList;
import java.util.List;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileUtil;

/**
 * 本地文件清理器<br>
 * 通过指定的通配符扫描本地文件,并且根据指定的清理规则清理数据<br>
 * 可以配置清理规则<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public class LocalFileCleaner extends FileCleaner {

	/**
	 * 清理规则<br>
	 */
	private CleanRule cleanRule;

	/**
	 * 需要清理的文件夹路径
	 */
	private String cleanPath;

	/**
	 * 需要清理的文件匹配符,支持命令行方式的文件操作 如*.txt格式
	 */
	private List<String> mappings;

	/**
	 * 日志
	 */
	protected static ILogger LOGGER = LoggerManager.getLogger(LocalFileCleaner.class); // 日志

	/**
	 * 设置清理规则
	 * 
	 * @param cleanRule
	 *            the cleanRule to set
	 */
	public void setCleanRule(CleanRule cleanRule) {
		this.cleanRule = cleanRule;
	}

	/**
	 * 设置需要清理的文件夹目录
	 * 
	 * @param cleanPath
	 *            the cleanPath to set
	 */
	public void setCleanPath(String cleanPath) {
		this.cleanPath = cleanPath;
	}

	/**
	 * 设置清理的文件的匹配符
	 * 
	 * @param mappings
	 *            the mappings to set
	 */
	public void setMappings(List<String> mappings) {
		this.mappings = mappings;
	}

	/**
	 * 是否可以被清理<br>
	 * 
	 * @param localFile
	 * @return 是否清理成功<br>
	 */
	public void clean() {
		if (!FileUtil.exists(cleanPath))
		{
			LOGGER.debug("路径={}不存在，本次不执行清理", cleanPath);
			return;
		}
		List<String> matchFiles = listFiles();
		int listFileNum = matchFiles.size();
		int deletedNum = 0;
		if (cleanRule != null && !cleanRule.cleanable(matchFiles)) {
			return;
		}
		for (String matchFile : matchFiles) {
			if (FileUtil.removeFile(matchFile))
				deletedNum++;
		}
		LOGGER.debug("本次共扫描到{}个文件。执行删除{}个文件。", listFileNum, deletedNum);
	}

	/**
	 * 扫描本地文件<br>
	 * 
	 * @param path
	 *            指定的目录
	 * @param mappings
	 *            文件名需要匹配的正则表达式
	 * @return
	 */
	private List<String> listFiles() {
		if (mappings == null || mappings.isEmpty())
			return FileUtil.getFileNames(cleanPath);
		List<String> fileList = new LinkedList<String>();
		for (int i = 0; i < mappings.size(); i++) {
			List<String> files = FileUtil.getFileNames(cleanPath, mappings.get(i));
			if (files != null && !files.isEmpty())
				fileList.addAll(files);
		}
		return fileList;
	}
}
