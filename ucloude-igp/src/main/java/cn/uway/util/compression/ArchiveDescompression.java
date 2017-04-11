package cn.uway.util.compression;

import java.io.Closeable;
import java.io.IOException;

/**
 * 压缩包解压接口，针对一个压缩包进行解压操作。流程是通过{@linkplain #hasNextEntry()}判断压缩包中是否存在可用的压缩条目，如果有，通过{@linkplain #nextEntry()}获取。
 * 
 * <pre>
 * ArchiveDescompression ad = ...;
 * while (ad.hasNextEntry()) {
 * 	ArchiveFileEntry entry = ad.nextEntry();
 * 	....使用entry....
 * }
 * </pre>
 * 
 * @see ArchiveFileEntry
 * @author chensijiang 2014-8-11
 */
public interface ArchiveDescompression extends Closeable {

	/**
	 * 判断压缩包中是否存在下一个被压缩条目。
	 * 
	 * @return 压缩包中是否存在下一个被压缩条目。
	 * @throws IOException
	 *             I/O错误。
	 */
	boolean hasNextEntry() throws IOException;

	/**
	 * 获取压缩包中的下一个被压缩条目。前提是压缩包中存在可用条目，需要通过{@linkplain #hasNextEntry()}方法判断。
	 * 
	 * @return 压缩包中的下一个被压缩条目。
	 * @throws IOException
	 *             I/O错误，或者已无下一个压缩条目。
	 * @see #hasNextEntry()
	 */
	ArchiveFileEntry nextEntry() throws IOException;

}
