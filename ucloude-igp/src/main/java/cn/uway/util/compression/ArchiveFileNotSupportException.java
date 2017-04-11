package cn.uway.util.compression;

import java.io.IOException;

/**
 * 压缩格式不支持的异常。
 * 
 * @author chensijiang 2014-8-11
 */
public class ArchiveFileNotSupportException extends IOException {

	/** 序列化ID。 */
	private static final long serialVersionUID = 422587685907313068L;

	/**
	 * 构造方法。
	 * 
	 * @param message
	 *            异常描述。
	 */
	public ArchiveFileNotSupportException(String message) {
		super(message);
	}

}
