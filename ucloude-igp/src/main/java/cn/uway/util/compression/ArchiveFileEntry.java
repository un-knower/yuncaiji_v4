package cn.uway.util.compression;

import java.io.InputStream;

/**
 * 压缩包中的一个被压缩条目的信息。
 * 
 * @author chensijiang 2014-8-11
 */
public class ArchiveFileEntry {

	/**
	 * 被压缩条目的名称，如果压缩包是gzip格式，则没有名称， 因为按照规范，gzip压缩包中只会有一个文件，并且gzip压缩包的文件名是什么，那么解压后便是什么，只是将“.gz”的扩展名移除。 所以，对于调用者而言，解压后的名字是已知的。
	 * */
	private String entryName;

	/** 该压缩条目是否是目录。 */
	private boolean isDirectory;

	/** 该条目的大小（字节），此项内容可能为0或-1，取决于解压实现。 */
	private long archiveSizeBytes;

	/** 该条目的解压后大小（字节），此项内容可能为0或-1，取决于解压实现。 */
	private long decompressedSizeBytes;

	/** 该条目是否没有名字，例如GZ文件，解压后以压缩包名字命名，只是把“.gz”后缀去除。 */
	private boolean isNonameEntry;

	/**
	 * 解压数据流，即从此流中读取的数据，是已被解压后的。 也存在一种可能，解压后也许会是另一个被压缩的流，例如一个zip压缩包中又放了一个gzip文件，那么从zip压缩包中获取到的流，不能直接使用，应当作gzip压缩包再次进行解压。
	 * */
	private InputStream descompressedStream;

	/**
	 * 构造方法。
	 * 
	 * @param entryName
	 *            被压缩条目的名称。
	 * @param isDirectory
	 *            该压缩条目是否是目录。
	 * @param archiveSizeBytes
	 *            该条目的大小（字节）。
	 * @param decompressedSizeBytes
	 *            该条目的解压后大小（字节）。
	 * @param isNonameEntry
	 *            该条目是否没有名字。
	 * @param descompressedStream
	 *            解压数据流。
	 */
	public ArchiveFileEntry(String entryName, boolean isDirectory, long archiveSizeBytes, long decompressedSizeBytes, boolean isNonameEntry,
			InputStream descompressedStream) {
		super();
		this.entryName = entryName;
		this.isDirectory = isDirectory;
		this.archiveSizeBytes = archiveSizeBytes;
		this.decompressedSizeBytes = decompressedSizeBytes;
		this.isNonameEntry = isNonameEntry;
		this.descompressedStream = descompressedStream;
	}

	/**
	 * 获取被压缩条目的名称，如果压缩包是gzip格式，则没有名称， 因为按照规范，gzip压缩包中只会有一个文件，并且gzip压缩包的文件名是什么，那么解压后便是什么，只是将“.gz”的扩展名移除。 所以，对于调用者而言，解压后的名字是已知的。
	 * 
	 * @return 被压缩条目的名称。
	 * @throws IllegalStateException
	 *             该条目是gzip格式，无条目名称。
	 * @see #isNonameEntry()
	 */
	public String getEntryName() throws IllegalStateException {
		if (this.isNonameEntry())
			throw new IllegalStateException("该条目是gzip格式，无条目名称。");
		return entryName;
	}

	/**
	 * 判断该压缩条目是否是目录。
	 * 
	 * @return 该压缩条目是否是目录。
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * 获取该条目的大小（字节），此项内容可能为0或-1，取决于解压实现。
	 * 
	 * @return 条目的大小（字节）。
	 */
	public long getArchiveSizeBytes() {
		return archiveSizeBytes;
	}

	/**
	 * 获取该条目的解压后大小（字节），此项内容可能为0或-1，取决于解压实现。
	 * 
	 * @return 该条目的解压后大小（字节），此项内容可能为0或-1，取决于解压实现。
	 */
	public long getDecompressedSizeBytes() {
		return decompressedSizeBytes;
	}

	/**
	 * 判断该条目是否没有名字，例如GZ文件，解压后以压缩包名字命名，只是把“.gz”后缀去除。 如果是，意味着{@linkplain #getEntryName()}的返回将会是<code>null</code>。
	 * 
	 * @return 该条目是否没有名字。
	 * @see #getEntryName()
	 */
	public boolean isNonameEntry() {
		return isNonameEntry;
	}

	/**
	 * 获取解压数据流，即从此流中读取的数据，是已被解压后的。 也存在一种可能，解压后也许会是另一个被压缩的流，例如一个zip压缩包中又放了一个gzip文件，那么从zip压缩包中获取到的流，不能直接使用，应当作gzip压缩包再次进行解压。
	 * 
	 * @return 解压数据流。
	 */
	public InputStream getDescompressedStream() {
		return descompressedStream;
	}

	@Override
	public String toString() {
		return "ArchiveFileEntry [entryName=" + entryName + ", isDirectory=" + isDirectory + "]";
	}

}
