package cn.uway.usummary.entity;

/**
 * 文件打包信息
 * 
 */
public class FilePackageInfo {

	/**
	 * 文件名,不一定是打包文件后缀名
	 */
	private String fileName;

	/**
	 * 文件扩展名
	 */
	private String fileExt;

	/**
	 * 默认不打包
	 */
	private boolean isFileExt = false;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	public boolean isFileExt() {
		return isFileExt;
	}

	public void setFileExt(boolean isFileExt) {
		this.isFileExt = isFileExt;
	}

}
