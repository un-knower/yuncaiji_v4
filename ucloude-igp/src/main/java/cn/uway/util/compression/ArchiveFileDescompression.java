package cn.uway.util.compression;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

/**
 * 解压缩的实现，注意，此类中的操作，不会关闭初始化传入的数据流，调用此类的{@linkplain #close()}方法，仅仅是释放一些内部资源。
 * 
 * @author chensijiang 2014-8-11
 */
public class ArchiveFileDescompression implements ArchiveDescompression {

	/** 压缩格式：zip。 */
	public static final int ARCHIVE_TYPE_ZIP = 0;

	/** 压缩格式：gzip。 */
	public static final int ARCHIVE_TYPE_GZ = 1;

	/** 压缩格式：tar。 */
	public static final int ARCHIVE_TYPE_TAR = 2;

	/** 压缩格式：7-ZIP所支持的格式。 */
	public static final int ARCHIVE_TYPE_7Z = 3;

	/**
	 * 原始数据流，需要使用带缓存的流，这里要求是{@linkplain BufferedInputStream}，原因是需要通过{@linkplain InputStream#mark(int)}与{@linkplain InputStream#reset()}
	 * 预先读取文件头，以猜测压缩格式。
	 */
	private BufferedInputStream rawStream;

	/** 记录是否已经初始化。 */
	private boolean bInitial;

	/**
	 * 原始数据流的压缩格式。
	 * 
	 * @see #ARCHIVE_TYPE_ZIP
	 * @see #ARCHIVE_TYPE_GZIP
	 * @see #ARCHIVE_TYPE_TAR
	 * @see #ARCHIVE_TYPE_7Z
	 * */
	private int rawStreamArchiveType;

	/** Apache实现的压缩包解压流，用于包装原始数据流。 */
	private ArchiveInputStream archiveInputStream;

	/** 当前迭代到的被压缩条目，使用Apache的实现。 */
	private ArchiveEntry currEntry;

	/** 原始数据流是gzip格式时，此处存放唯一的gzip流，供{@linkplain #nextEntry()}返回。返回一次后，便设为<code>null</code>，虽然gzip流中只会有一个文件，也遵循hasNextEntry/nextEntry的原则。 */
	private InputStream gzStream;

	private SevenZipExecutor sevenZipStream;

	/**
	 * 构造方法。注意，传入的原始数据流，不会被关闭，应由流的最初创建者关闭。
	 * 
	 * @param rawStream
	 *            待解压的原始数据流。
	 * @throws ArchiveFileNotSupportException
	 *             压缩格式不支持。
	 * @throws IOException
	 *             出现I/O错误。
	 */
	public ArchiveFileDescompression(BufferedInputStream rawStream) throws ArchiveFileNotSupportException, IOException {
		super();
		this.rawStream = rawStream;
		// 猜测压缩格式。
		this.rawStreamArchiveType = guessArchiveType(rawStream);
		if (this.rawStreamArchiveType == -1)
			throw new ArchiveFileNotSupportException("不支持的压缩格式。");

	}

	@Override
	public boolean hasNextEntry() throws IOException {

		// 初始化。
		this.initDescompression();

		// 首先，先判断是否是gz，是gz的话，this.gzStream不会为null（this.initDescompression()中得到值），直接返回true.
		// 然后，非gz情况，Apache原始流不会为null，它是在this.initDescompression()中创建的，调用其getNextEntry()方法，获取条目信息，保存到this.currEntry，供nextEntry()使用。
		// 如果两者都为null，意味着条目已经被迭代完。
		if (this.gzStream != null) {
			return true;
		}

		// 7-ZIP情况，使用其独特的解压器。
		if (this.sevenZipStream != null) {
			return this.sevenZipStream.hasNextEntry();
		}

		if (this.archiveInputStream != null) {
			this.currEntry = this.archiveInputStream.getNextEntry();
			return (this.currEntry != null);
		}
		return false;
	}

	@Override
	public ArchiveFileEntry nextEntry() throws IOException {

		// 是gz流时，包装为ArchiveFileEntry返回，然后将this.gzStream设为null。
		// 因为这样的话，再次调用hasNextEntry()就会返回false，gz条目只会有一个，迭代一次即可。
		if (this.gzStream != null) {
			ArchiveFileEntry afe = new ArchiveFileEntry(null, false, 0, 0, true, this.gzStream);
			this.gzStream = null;
			return afe;
		}

		// 7-ZIP情况，使用其独特的解压器。
		if (this.sevenZipStream != null) {
			return this.sevenZipStream.nextEntry();
		}

		// 非gz情况获取相关信息填充到ArchiveFileEntry并返回。
		if (this.currEntry != null) {
			long archSize = 0;
			// 仅zip存在压缩后大小，但这个方法似乎也是返回0或-1，暂时先调用它。
			if (this.currEntry instanceof ZipArchiveEntry) {
				((ZipArchiveEntry) this.currEntry).getCompressedSize();
			}
			return new ArchiveFileEntry(this.currEntry.getName(), this.currEntry.isDirectory(), archSize, this.currEntry.getSize(), false,
					new NonCloseArchiveStream(this.archiveInputStream));
		}
		throw new IOException("已无下一个压缩条目。");
	}

	/**
	 * 通过文件流读取文件头，以猜测一个数据流的压缩格式。如果无法得知压缩格式，将会返回-1. 注意，此方法不会关闭数据流。
	 * 
	 * @param in
	 *            数据流。
	 * @return 压缩格式编号。
	 * @throws IOException
	 *             出现I/O错误。
	 * @see #ARCHIVE_TYPE_ZIP
	 * @see #ARCHIVE_TYPE_GZIP
	 * @see #ARCHIVE_TYPE_TAR
	 * @see #ARCHIVE_TYPE_7Z
	 */
	public static int guessArchiveType(BufferedInputStream in) throws IOException {
		// 至少读取12字节。
		int headSize = 12;
		byte[] head = new byte[headSize];
		in.mark(head.length);
		int count;
		try {
			count = in.read(head);
		} finally {
			in.reset();
		}
		if (ZipArchiveInputStream.matches(head, count)) {
			return ARCHIVE_TYPE_ZIP;
		}
		if (GzipCompressorInputStream.matches(head, count)) {
			return ARCHIVE_TYPE_GZ;
		}

		// 尝试tar，文件头要读较多字节。
		int tarHeadLen = TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN;
		head = new byte[tarHeadLen];
		try {
			count = in.read(head);
		} finally {
			in.reset();
		}
		if (TarArchiveInputStream.matches(head, count)) {
			return ARCHIVE_TYPE_TAR;
		}

		if (SevenZipExecutor.matches(head)) {
			return ARCHIVE_TYPE_7Z;
		}

		return -1;
	}

	@Override
	public void close() throws IOException {
		this.currEntry = null;
		this.gzStream = null;
		this.archiveInputStream = null;
		this.rawStream = null;
		this.rawStreamArchiveType = -1;
		this.bInitial = false;
		if (this.sevenZipStream != null)
			this.sevenZipStream.close();
	}

	/**
	 * 初始化解压操作。
	 * 
	 * @throws IOException
	 *             出现I/O错误。
	 */
	private void initDescompression() throws IOException {
		// 仅初始化一次。
		if (this.bInitial) {
			return;
		}

		// 根据猜测到的压缩格式，创建相应的Apache解压流实现。
		switch (this.rawStreamArchiveType) {
			case ARCHIVE_TYPE_ZIP :
				this.archiveInputStream = new ZipArchiveInputStream(this.rawStream);
				break;
			case ARCHIVE_TYPE_GZ :
				this.gzStream = new GzipCompressorInputStream(this.rawStream);
				break;
			case ARCHIVE_TYPE_TAR :
				this.archiveInputStream = new TarArchiveInputStream(this.rawStream);
				break;
			case ARCHIVE_TYPE_7Z :
				this.sevenZipStream = new SevenZipExecutor(this.rawStream);
				break;
			default :
				throw new IOException("不支持的压缩格式。");
		}
		this.bInitial = true;
	}

	 public static void main(String[] args) throws Exception {
	 BufferedInputStream b = new BufferedInputStream(new FileInputStream("/Users/chensijiang/Downloads/Downloads - 副本.7z"));
	 ArchiveFileDescompression d = new ArchiveFileDescompression(b);
	 while (d.hasNextEntry()) {
	 ArchiveFileEntry en = d.nextEntry();
	 System.out.println(en);
	 FileOutputStream ou = new FileOutputStream("/Users/chensijiang/Downloads/" + en.getEntryName());
	 IOUtils.copy(en.getDescompressedStream(), ou);
	 ou.close();
	 }
	 d.close();
	 b.close();
	 }

}
