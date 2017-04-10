package cn.uway.usummary.entity;

public class FtpExportInfo {
	
	private Long id;
	
	private int batchNum;
	
	private String exportFileName;
	
	private String exportPath;

	private String splitChar;
	
	private Integer exportHeader;
	
	private String encode;
	
	private String additionParams;
	
	private String compressFormat;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public String getExportPath() {
		return exportPath;
	}

	public void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}

	public String getSplitChar() {
		return splitChar;
	}

	public void setSplitChar(String splitChar) {
		this.splitChar = splitChar;
	}

	public Integer getExportHeader() {
		return exportHeader;
	}

	public void setExportHeader(Integer exportHeader) {
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

	public String getCompressFormat() {
		return compressFormat;
	}

	public void setCompressFormat(String compressFormat) {
		this.compressFormat = compressFormat;
	}

	public int getBatchNum() {
		return batchNum;
	}

	public void setBatchNum(int batchNum) {
		this.batchNum = batchNum;
	}
	
	public boolean isExportHeader(){
		return 1 == this.exportHeader;
	}
}
