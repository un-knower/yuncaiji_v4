package cn.uway.util.compression;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.commons.io.IOUtils;

/**
 * 7-ZIP解压器。
 * 
 * @author chensijiang 2014-8-20
 */
class SevenZipExecutor implements Closeable {

	/** 原始数据流。 */
	private InputStream in;

	/** 通过原始数据流在本地生成的临时文件，因为7-ZIP库必须从本地文件进行随机读。 */
	private File tempFile;

	/** 给7-ZIP库使用的原始压缩包随机读写对象。 */
	private RandomAccessFile raf;

	/** 给7-ZIP库使用的预处理流。 */
	private RandomAccessFileInStream sevenZipInStream;

	/** 7-ZIP库获取到的压缩条目信息。 */
	private ISevenZipInArchive sevenZipArchive;

	/** 7-ZIP库获取到的解压缩接口。 */
	private ISimpleInArchive sevenZipInterface;

	/** 7-ZIP库获取到的压缩条目列表。 */
	private ISimpleInArchiveItem[] sevenZipItems;

	/** 当前正在读取的压缩条目索引。 */
	private int sevenZipItemsPos;

	/** 当前正在读取的压缩条目。 */
	private ISimpleInArchiveItem sevenZipCurrItem;

	/** 本地生成的临时文件流列表，需要在关闭时释放。 */
	private List<InputStream> localStreams;

	/** 本地生成的临时文件列表，需要在关闭时删除。 */
	private List<File> localFiles;

	/** 7-ZIP解压缓冲大小（字节）。 */
	private static final int BUF_SIZE_BYTES = 1024 * 10;

	/**
	 * 构造方法。
	 * 
	 * @param in
	 *            原始数据流。
	 * @throws IOException
	 *             初始化失败。
	 */
	public SevenZipExecutor(InputStream in) throws IOException {
		super();
		if (!SevenZipLoader.load())
			throw new IOException("无法加载所需要的7-ZIP库。");
		this.in = in;
		// 7-ZIP只能用本地文件，不能直接从流解压，所以生成临时文件。
		this.tempFile = File.createTempFile("igp3_", "_7z");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(this.tempFile);
			IOUtils.copy(in, out);
			out.flush();
		} finally {
			IOUtils.closeQuietly(out);
		}
		this.raf = new RandomAccessFile(this.tempFile, "r");
		this.sevenZipInStream = new RandomAccessFileInStream(this.raf);
		try {
			this.sevenZipArchive = SevenZip.openInArchive(null, this.sevenZipInStream);
			this.sevenZipInterface = this.sevenZipArchive.getSimpleInterface();
			this.sevenZipItems = this.sevenZipInterface.getArchiveItems();
		} catch (SevenZipException e) {
			throw new IOException(e);
		}
		this.localFiles = new ArrayList<File>();
		this.localStreams = new ArrayList<InputStream>();
	}

	/**
	 * 判断是否有下一个压缩条目。
	 * 
	 * @return 是否有下一个压缩条目。
	 * @throws IOException
	 *             I/O错误。
	 */
	public boolean hasNextEntry() throws IOException {
		return (this.sevenZipItemsPos < this.sevenZipItems.length);
	}

	/**
	 * 获取有下一个压缩条目。
	 * 
	 * @return 有下一个压缩条目。
	 * @throws IOException
	 *             I/O错误。
	 */
	public ArchiveFileEntry nextEntry() throws IOException {
		if (this.sevenZipItemsPos >= this.sevenZipItems.length)
			throw new IOException("已无下一条目。");
		ISimpleInArchiveItem item = this.sevenZipItems[this.sevenZipItemsPos++];
		this.sevenZipCurrItem = item;
		try {
			File localFile = File.createTempFile("igp3_", "_7z_entry");
			final OutputStream localOut = new FileOutputStream(localFile);
			ExtractOperationResult result = this.sevenZipCurrItem.extractSlow(new ISequentialOutStream() {

				@Override
				public int write(byte[] data) throws SevenZipException {
					int writeCount = 0;
					try {
						writeCount = (data.length >= BUF_SIZE_BYTES ? BUF_SIZE_BYTES : data.length);
						localOut.write(data, 0, writeCount);
					} catch (IOException e) {
					}
					return writeCount;
				}
			});
			IOUtils.closeQuietly(localOut);
			if (result != ExtractOperationResult.OK) {
				if (localFile != null)
					localFile.delete();
				throw new IOException("解压失败：" + result);
			}
			InputStream localIn = new FileInputStream(localFile);
			this.localFiles.add(localFile);
			this.localStreams.add(localIn);
			// 某些格式是没有解压后名称的。
			if (this.sevenZipArchive.getArchiveFormat() == ArchiveFormat.Z) {
				return new ArchiveFileEntry(null, false, 0, 0, true, localIn);
			} else {
				return new ArchiveFileEntry(item.getPath(), item.isFolder(), 0, 0, false, localIn);
			}
		} catch (Exception ex) {
			if (ex instanceof IOException)
				throw (IOException) ex;
			throw new IOException(ex);
		}
	}

	/**
	 * 根据一个文件头，判断是否能被7-ZIP解压。
	 * 
	 * @param signature
	 *            文件头。
	 * @return 是否能被7-ZIP解压。
	 */
	public static final boolean matches(byte[] signature) {
		boolean isZ = false, isRar = false, isSevenZip = false;
		int len = signature.length;

		// .Z:[1F 9D]
		if (len >= 2)
			isZ = ((signature[0] & 0xff) == 0x1f && (signature[1] & 0xff) == 0x9d);

		// RAR:[52 61 72 21]
		if (len >= 4)
			isRar = (signature[0] & 0xff) == 0x52 && (signature[1] & 0xff) == 0x61 && (signature[2] & 0xff) == 0x72 && (signature[3] & 0xff) == 0x21;

		// 7Z:[37 7a bc af]
		if (len >= 4)
			isSevenZip = (signature[0] & 0xff) == 0x37 && (signature[1] & 0xff) == 0x7a && (signature[2] & 0xff) == 0xbc
					&& (signature[3] & 0xff) == 0xaf;

		return (isZ || isRar || isSevenZip);
	}

	@Override
	public void close() {
		try {
			this.sevenZipInterface.close();
		} catch (Exception ex) {
		}
		try {
			this.sevenZipArchive.close();
		} catch (Exception ex) {
		}
		try {
			this.sevenZipInStream.close();
		} catch (Exception ex) {
		}
		if (this.raf != null)
			IOUtils.closeQuietly(this.raf);
		if (this.tempFile != null && this.tempFile.exists() && this.tempFile.isFile())
			this.tempFile.delete();
		for (InputStream in : this.localStreams)
			IOUtils.closeQuietly(in);
		for (File f : this.localFiles)
			f.delete();
		IOUtils.closeQuietly(this.in);
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

}
