package cn.uway.framework.parser.file.templet;

public class HttpTemplet extends Templet {

	public String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "HttpTemplet [id=" + id + ", url=" + url + ", dataName=" + dataName + ", dataType=" + dataType + "]";
	}
}
