package cn.uway.framework.warehouse.exporter.template;

/**
 * FileExportTargetBean 文件输出目的地配置 对应于export_config.xml中的配置
 * 
 * @author chenrongqiang 2012-11-12
 */
public class FileExporterBean extends ExporterBean {

	/**
	 * 输出文件路径配置
	 */
	private String path;

	/**
	 * 输出文件名
	 */
	private String fileName;

	/**
	 * 是否压缩
	 */
	private boolean zipFlag;

	/**
	 * 压缩格式
	 */
	private String compressFormat;

	/** 是否输出表头，默认不输出。 */
	private boolean isExportHeader;
	
	/**
	 * 文件输出默认是,分割
	 */
	private String split ="," ; 
	
	

	
	public String getSplit() {
		return split;
	}

	
	public void setSplit(String split) {
		this.split = split;
	}

	public String getPath() {
		return path;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isZipFlag() {
		return zipFlag;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setZipFlag(boolean zipFlag) {
		this.zipFlag = zipFlag;
	}

	public String getCompressFormat() {
		return compressFormat;
	}

	public void setCompressFormat(String compressFormat) {
		this.compressFormat = compressFormat;
	}

	public void setExportHeader(boolean isExportHeader) {
		this.isExportHeader = isExportHeader;
	}

	public boolean isExportHeader() {
		return isExportHeader;
	}

	@Override
	public String toString() {
		return "FileExporterBean [path=" + path + ", fileName=" + fileName + ", zipFlag=" + zipFlag + ", compressFormat=" + compressFormat
				+ ", isExportHeader=" + isExportHeader + "]";
	}
}
