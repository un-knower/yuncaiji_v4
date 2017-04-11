package cn.uway.framework.accessor;

import java.io.InputStream;

/**
 * http流式接入输出对象
 * 
 * @author linp @ 2015-7-10
 */
public class HttpAccessOutObject extends StreamAccessOutObject {

	//该流的编码
	private String encode;

	/**
	 * 构造方法
	 */
	public HttpAccessOutObject() {
		super();
	}

	public HttpAccessOutObject(String rawAccessName, InputStream outObject, int len, String encode) {
		super(rawAccessName, outObject, len);
		this.encode = encode;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

}
