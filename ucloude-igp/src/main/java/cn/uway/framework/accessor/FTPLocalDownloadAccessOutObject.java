package cn.uway.framework.accessor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.IoUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.util.ExcelToCsvUtil;
import cn.uway.util.compression.ArchiveDescompression;
import cn.uway.util.compression.ArchiveFileDescompression;
import cn.uway.util.compression.ArchiveFileEntry;
import cn.uway.util.compression.ArchiveFileNotSupportException;

/**
 * FTP输出对象，针对FTP落地下载的方式使用，描述单个下载好的文件。其特征是支持解压缩，可通过{@linkplain #isArchiveFile()}判断是否是压缩文件，然后通过{@linkplain #getDecompressedFile(boolean, boolean)}
 * 方法获取解压后的文件列表，因为压缩包中可能有多个文件，所以是文件列表。另外，包含原始FTP路径信息，可在{@linkplain #getDecompressedFile(boolean, boolean)}中得到原始的FTP路径等信息。
 * 
 * @author chensijiang 2014-8-12
 */
public class FTPLocalDownloadAccessOutObject extends AccessOutObject {

	/** 日志记录器。 */
	private static final ILogger LOG = LoggerManager.getLogger(FTPLocalDownloadAccessOutObject.class);

	/** 下载到本地的文件。 */
	private File localFile;

	/** 设置的ftp编码。 */
	private String charset;

	/** 获取下载到本地的文件名(需解码，解决乱码问题)。 */
	private String decodedLocalFile;

	/** 原始采集路径信息。 */
	private GatherPathEntry gatherPathInfo;

	/** 保存已解压的文件列表。 */
	private List<File> descompressedFiles;

	/** 保存已转为CSV的文件列表。 */
	private List<File> csvFiles;

	/**
	 * 构造方法。
	 * 
	 * @param localFile
	 *            下载到本地的文件。
	 * @param gatherPathInfo
	 *            原始采集路径信息。
	 */
	public FTPLocalDownloadAccessOutObject(File localFile, GatherPathEntry gatherPathInfo, String charset) {
		super();
		this.localFile = localFile;
		this.charset = charset;
		this.decodedLocalFile = StringUtil.decodeFTPPath(localFile.getAbsolutePath(), charset);
		this.gatherPathInfo = gatherPathInfo;
		this.descompressedFiles = new ArrayList<>(8);
		this.csvFiles = new ArrayList<File>(8);
	}

	/**
	 * 获取下载到本地的文件。
	 * 
	 * @return 下载到本地的文件。
	 */
	public File getLocalFile() {
		return localFile;
	}

	/**
	 * 获取下载到本地的文件(解码后的)。
	 * 
	 * @return 下载到本地的文件。
	 */
	public String getDecodedLocalFile() {
		return decodedLocalFile;
	}

	/**
	 * 获取解压后的文件列表，虽然本类描述的是一个下载好的文件，但如果是压缩文件，解压后可能是多个文件，所以此处使用文件列表返回。 文件的解压是延迟进行的，即在调用本方法后，才会进行解压，再次调用时，然后的是上次解压后的文件列表。
	 * 
	 * @param deleteRaw
	 *            解压成功后，是否删除压缩包。
	 * @param keepDir
	 *            是否在解压时，在本地保持压缩包中的目录结构。
	 * @return 解压后的文件列表。
	 * @throws IOException
	 *             I/O错误。
	 * @throws ArchiveFileNotSupportException
	 *             不支持解压的文件格式。
	 * @see #isArchiveFile()
	 */
	public List<File> getDecompressedFile(boolean deleteRaw, boolean keepDir) throws IOException, ArchiveFileNotSupportException {
		return this.getDecompressedFile(deleteRaw, keepDir, null);
	}

	/**
	 * 获取解压后的文件列表，虽然本类描述的是一个下载好的文件，但如果是压缩文件，解压后可能是多个文件，所以此处使用文件列表返回。 文件的解压是延迟进行的，即在调用本方法后，才会进行解压，再次调用时，然后的是上次解压后的文件列表。
	 * 
	 * @param deleteRaw
	 *            解压成功后，是否删除压缩包。
	 * @param keepDir
	 *            是否在解压时，在本地保持压缩包中的目录结构。
	 * @param speciBaseDir
	 *            指定解压缩的根目录，如果传入<code>null</code>，则在压缩文件所在目录进行解压。
	 * @return 解压后的文件列表。
	 * @throws IOException
	 *             I/O错误。
	 * @throws ArchiveFileNotSupportException
	 *             不支持解压的文件格式。
	 * @see #isArchiveFile()
	 */
	public List<File> getDecompressedFile(boolean deleteRaw, boolean keepDir, File speciBaseDir) throws IOException, ArchiveFileNotSupportException {
		// 判断是否已经解压过，解压过，直接返回保存的结果。
		if (!this.descompressedFiles.isEmpty())
			return Collections.unmodifiableList(this.descompressedFiles);

		// 解压的根文件，解压到哪个目录。
		File baseDir = (speciBaseDir != null ? speciBaseDir : this.getLocalFile().getParentFile());

		// 开始解压操作。

		// 压缩包的文件流。
		InputStream in = null;
		// 压缩包的文件流，包一层Buffered流，因为要使用mark/reset操作。
		BufferedInputStream bufStream = null;
		long time = System.currentTimeMillis();
		boolean succ = false;
		try {
			in = new FileInputStream(this.getLocalFile());
			bufStream = new BufferedInputStream(in);
			LOG.debug("开始解压文件：{}", this.getDecodedLocalFile());
			List<File> result = descompression(this.getLocalFile().getName(), bufStream, baseDir, keepDir);
			this.descompressedFiles.addAll(result);
			succ = true;
			return Collections.unmodifiableList(this.descompressedFiles);
		} finally {
			IOUtils.closeQuietly(bufStream);
			IOUtils.closeQuietly(in);
			LOG.debug("解压耗时{}秒。", (System.currentTimeMillis() - time) / 1000.);
			if (deleteRaw) {
				if (succ) {
					if (this.getLocalFile().delete()) {
						LOG.debug("删除原始文件成功：{}", this.getDecodedLocalFile());
					} else {
						LOG.warn("删除原始文件失败：{}", this.getDecodedLocalFile());
					}
				} else {
					LOG.warn("由于解压失败，将不会删除原始文件。");
				}

			}
		}
	}

	/**
	 * 获取原始采集路径信息。
	 * 
	 * @return 原始采集路径信息。
	 */
	public GatherPathEntry getGatherPathInfo() {
		return gatherPathInfo;
	}

	/**
	 * 判断是否是压缩文件。
	 * 
	 * @return 是否是压缩文件。
	 */
	public boolean isArchiveFile() {
		if (!this.getLocalFile().exists())
			return false;
		InputStream in = null;
		BufferedInputStream bufStream = null;
		try {
			in = new FileInputStream(this.getLocalFile());
			bufStream = new BufferedInputStream(in);
			return (ArchiveFileDescompression.guessArchiveType(bufStream) != -1);
		} catch (IOException ex) {
			LOG.warn("判断文件是否是压缩文件时出错。", ex);
			return false;
		} finally {
			IOUtils.closeQuietly(bufStream);
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * 判断是否是Excel文件，包括.xls和.xlsx，这个判断是通过文件名来判断的，而不是文件头。
	 * 
	 * @return 是否是Excel文件。
	 */
	public boolean isExcelFileByFileName() {
		if (this.getLocalFile() != null) {
			String filename = this.getLocalFile().getName().toLowerCase().trim();
			return (filename.endsWith(".xls") || filename.endsWith(".xlsx"));
		}
		return false;
	}

	/**
	 * 将excel文件转为一批csv文件。
	 * 
	 * @return 一批csv文件。
	 * @throws Exception
	 *             本地文件不是excel文件，或其它错误。
	 */
	public List<File> excelFileExtraCsvFiles() throws Exception {
		if (this.csvFiles != null && !this.csvFiles.isEmpty())
			return Collections.unmodifiableList(this.csvFiles);
		if (!this.isExcelFileByFileName())
			throw new Exception("本地文件不是excel文件。");
		ExcelToCsvUtil util = new ExcelToCsvUtil(getLocalFile(), getTask(), this.getLocalFile().getParentFile().getAbsolutePath(), null);
		List<File> list = util.toCsv();
		this.csvFiles.addAll(list);
		return Collections.unmodifiableList(list);
	}

	/**
	 * 解压缩操作，此方法会自我递归调用，因为压缩包中可能又有压缩包，这种情况会自我递归调用，直到不再有嵌套的压缩包。
	 * 
	 * @param archiveName
	 *            压缩包的名字，无目录部分，只是文件名加扩展名。
	 * @param bufStream
	 *            压缩包的输入流。
	 * @param baseDir
	 *            解压的根目录，即解压到哪个目录。
	 * @param keepDir
	 *            是否在解压时，在本地保持压缩包中的目录结构。
	 * @return 解压后的文件列表。
	 * @throws IOException
	 *             I/O错误。
	 */
	@SuppressWarnings("resource")
	private final List<File> descompression(String archiveName, BufferedInputStream bufStream, File baseDir, boolean keepDir) throws IOException {
		List<File> list = new ArrayList<File>();
		// 解压器。
		ArchiveDescompression archiveDescompression = null;
		try {
			archiveDescompression = new ArchiveFileDescompression(bufStream);
			// 迭代压缩包中的每个条目。
			while (archiveDescompression.hasNextEntry()) {
				ArchiveFileEntry entry = archiveDescompression.nextEntry();
				// 是目录时不用处理，压缩包中的带目录的文件，在文件名上就已经体现了目录。
				// 比如会有名为“hello/a.txt”的文件条目，也会有名为“hello”的目录条目，
				// “hello/a.txt”中已经可知道有hello目录了，名为“hello”的目录条目当然就不需要处理了。
				if (entry.isDirectory()) {
					continue;
				}
				// 压缩包是gzip时，没有条目名，需要用压缩包的名字，去掉最后的".gz"。
				String entryFileName;
				if (entry.isNonameEntry()) {
					entryFileName = getGzipRawName(archiveName);
				} else {
					entryFileName = entry.getEntryName();
				}

				// OSX系统产生的特殊内容。
				if (entryFileName.startsWith("__MACOSX")) {
					continue;
				}

				// 压缩包中某个条目的数据流，注意，不能关闭，关闭会导致上级流也关闭。
				InputStream descompressedStream = entry.getDescompressedStream();
				BufferedInputStream bufDescompressedStream = new BufferedInputStream(descompressedStream);

				// 输出文件的完整路径。
				StringBuilder outFilePath = new StringBuilder();
				outFilePath.append(baseDir.getAbsolutePath()).append(File.separator);
				outFilePath.append(keepDir ? entryFileName : FilenameUtils.getName(entryFileName));
				File outFile = new File(outFilePath.toString());
				File outFileDir = outFile.getParentFile();
				if ((!outFileDir.exists() || !outFileDir.isDirectory()) && !outFileDir.mkdirs()) {
					throw new IOException("无法为文件" + outFile + "创建目录：" + outFileDir + "。");
				}

				// 压缩包中的条目又是压缩包，需要递归调用本方法。
				if (ArchiveFileDescompression.guessArchiveType(bufDescompressedStream) != -1) {
					List<File> recuResult = descompression(
							FilenameUtils.getName(entry.isNonameEntry() ? getGzipRawName(archiveName) : entry.getEntryName()),
							bufDescompressedStream, outFileDir, keepDir);
					list.addAll(recuResult);
					continue;
				}
				LOG.debug("正将{}解压到{}...", new Object[]{archiveName, StringUtil.decodeFTPPath(outFile.getAbsolutePath(), charset)});
				OutputStream out = null;
				try {
					out = new FileOutputStream(outFile);
					IOUtils.copy(bufDescompressedStream, out);
					LOG.debug("解压成功。");
					list.add(outFile);
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
			return list;
		} finally {
			IoUtil.closeQuietly(archiveDescompression);
		}
	}

	/**
	 * 获取gzip压缩包的原始文件名，即没压缩的文件的名字，最后的“.gz”去掉，如果没有，原样返回。
	 * 
	 * @param gzipFileName
	 *            gzip压缩包的文件名。
	 * @return gzip压缩包的原始文件名
	 */
	private static final String getGzipRawName(String gzipFileName) {
		if (gzipFileName == null)
			return gzipFileName;
		if (gzipFileName.toLowerCase().endsWith(".gz")) {
			return gzipFileName.substring(0, gzipFileName.length() - 3);
		} else {
			return FilenameUtils.getBaseName(gzipFileName);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
