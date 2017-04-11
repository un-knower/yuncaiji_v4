package cn.uway.framework.accessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件列表型数据接入输出对象
 * 
 * @author MikeYang
 * @Date 2012-10-30
 * @version 1.0
 * @since 3.0
 */
public class FileListAccessOutObject extends AccessOutObject {

	private List<String> fileNames; // 文件名列表

	/**
	 * 构造方法
	 */
	public FileListAccessOutObject() {
		super();
		fileNames = new ArrayList<String>();
	}

	/**
	 * 获取文件名列表
	 */
	public List<String> getFileNames() {
		return fileNames;
	}

	/**
	 * 添加一组文件名
	 * 
	 * @param fileNames
	 *            文件名列表
	 */
	public void setFileNames(List<String> fileNames) {
		this.fileNames.addAll(fileNames);
	}

	/**
	 * 添加文件名
	 * 
	 * @param fileName
	 *            文件名
	 */
	public void setFileName(String fileName) {
		this.fileNames.add(fileName);
	}
}
