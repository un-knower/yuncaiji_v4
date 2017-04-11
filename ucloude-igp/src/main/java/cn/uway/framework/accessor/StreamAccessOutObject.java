package cn.uway.framework.accessor;

import java.io.InputStream;

/**
 * 流式接入输出对象
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public class StreamAccessOutObject extends AccessOutObject {

	/**
	 * 输出对象
	 */
	private InputStream outObject;

	private long len;

	/**
	 * 构造方法
	 */
	public StreamAccessOutObject() {
		super();
	}

	public StreamAccessOutObject(String rawAccessName, InputStream outObject, int len) {
		this.outObject = outObject;
		this.len = len;
	}

	/**
	 * 获取数据接入后的输出对象
	 * 
	 * @return {@link InputStream}
	 */
	public InputStream getOutObject() {
		return outObject;
	}

	public void setOutObject(InputStream outObject) {
		this.outObject = outObject;
	}

	public void setLen(long len) {
		this.len = len;
	}

	public long getLen() {
		return len;
	}

}
