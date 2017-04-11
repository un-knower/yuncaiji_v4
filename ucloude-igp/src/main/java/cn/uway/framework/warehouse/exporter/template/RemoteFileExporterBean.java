package cn.uway.framework.warehouse.exporter.template;


/**
 * DatabaseExporterBean 文件输出目标配置<br>
 * 1、配置来源于IGP_CFG_File_EXPORT
 * 
 */
public class RemoteFileExporterBean extends ExporterBean{
	private int connID;
	
	private String exportPath;
	
	private String exportFileName;
	
	private String compressFormat;
	
	private String splitChar;
	
	private boolean exportHeader;
	
	private String encode;
	
	private String additionParams;
	
	/**
	 * 批量提交条数
	 */
	private int batchNum;

	public void setBatchNum(int batchNum){
		this.batchNum = batchNum;
	}

	public int getBatchNum(){
		return batchNum;
	}
	
	public int getConnID() {
		return connID;
	}

	
	public void setConnID(int connID) {
		this.connID = connID;
	}
	
	public String getExportPath() {
		return exportPath;
	}
	
	public void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}
	
	public String getExportFileName() {
		return exportFileName;
	}
	
	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}
	
	public String getCompressFormat() {
		return compressFormat;
	}
	
	public void setCompressFormat(String compressType) {
		this.compressFormat = compressType;
	}

	public String getSplitChar() {
		return splitChar;
	}

	public void setSplitChar(String splitChar) {
		this.splitChar = splitChar;
	}
	
	public boolean isExportHeader() {
		return exportHeader;
	}

	public void setExportHeader(boolean exportHeader) {
		this.exportHeader = exportHeader;
	}
	
	public String getEncode() {
		return encode;
	}

	
	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getAdditionParams() {
		return additionParams;
	}
	
	public void setAdditionParams(String additionParams) {
		this.additionParams = additionParams;
	}

}
