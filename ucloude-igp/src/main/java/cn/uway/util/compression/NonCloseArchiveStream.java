package cn.uway.util.compression;

import java.io.IOException;
import java.io.InputStream;

/**
 * 不会将被装饰流关闭的InputStream，作为一个代理，避免调用者将返回的解压流关闭，导致上级流也被关闭。
 * 
 * @author chensijiang 2014-8-11
 */
class NonCloseArchiveStream extends InputStream {

	/** 被代理的解压流。 */
	private InputStream wrap;

	/**
	 * 构造方法。
	 * 
	 * @param wrap
	 *            被代理的解压流。
	 */
	NonCloseArchiveStream(InputStream wrap) {
		super();
		this.wrap = wrap;
	}

	@Override
	public int read() throws IOException {
		return this.wrap.read();
	}

	@Override
	public void close() throws IOException {
		// 不关闭。
	}
}
