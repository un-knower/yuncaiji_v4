package cn.uway.ucloude.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;


public class AdaptiveInputStream extends InputStream {
	public static class CompressionFileEntry {
		public InputStream inputStream;
		public String fileName;
		
		public CompressionFileEntry(InputStream inputStream, String fileName) {
			this.inputStream = inputStream;
			this.fileName = fileName;
		}
	};
	
	
	public final static String ZIPFILESUFFIX = ".zip";
	public final static String GZFILESUFFIX = ".gz";
	public final static String TARFILESUFFIX = ".tar";
	public final static String BZ2FILESUFFIX = ".bz2";
	
	private String rawFileName;
	private InputStream rawStream;
	
	
	private AdaptiveInputStream subAdaptiveInputStream;
	private ArchiveInputStream archiveFileInputStream;
	private final boolean isArachiveFile;
	private boolean isEnumeratedCompleted = false;
	
	public AdaptiveInputStream(InputStream inputStream, String fileName) throws IOException {
		this.rawFileName = fileName;
		this.rawStream = inputStream;
		this.subAdaptiveInputStream = null;
		this.archiveFileInputStream = null;
		
		String fileSuffix = fileName;
		if (fileName.lastIndexOf('.')>0) {
			fileSuffix = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
		}
		if (fileSuffix.endsWith(ZIPFILESUFFIX)) {
			archiveFileInputStream = new ZipArchiveInputStream(rawStream);
			isArachiveFile = true;
		} else if (fileSuffix.endsWith(TARFILESUFFIX)) {
			archiveFileInputStream = new TarArchiveInputStream(rawStream);
			isArachiveFile = true;
		} else if (fileSuffix.endsWith(GZFILESUFFIX)) {
			String analysisFileName = fileName.substring(0, fileName.length()-GZFILESUFFIX.length());
			isArachiveFile = true;
			//gzip只包含一个文件，不需要遍历，所以此处给subAdaptiveInputStream赋值即可
			subAdaptiveInputStream = new AdaptiveInputStream(new GZIPInputStream(rawStream), analysisFileName);
		} else if (fileSuffix.endsWith(BZ2FILESUFFIX)) {
			String analysisFileName = fileName.substring(0, fileName.length()-BZ2FILESUFFIX.length());
			isArachiveFile = true;
			//bz2只包含一个文件，不需要遍历，所以此处给subAdaptiveInputStream赋值即可
			subAdaptiveInputStream = new AdaptiveInputStream(new BZip2CompressorInputStream(rawStream), analysisFileName);
		} else {
			isArachiveFile = false;
		}
	}
	
	@Override
	public int read() throws IOException {
		return this.rawStream.read();
	}

	@Override
	public int available() throws IOException {
		return this.rawStream.available();
	}

	@Override
	public void close() throws IOException {
		/**
		 * explain:
		 * <pre>
		 *  为避免在子压缩文件，客户使用时，误关闭源流，此处将close方法置空，
		 *  确定要关闭，可调用forceClose()
		 *  </pre>
		 *  
		 *  super.close();
		 */
	}

	@Override
	public synchronized void mark(int readlimit) {
		this.rawStream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return this.rawStream.markSupported();
	}

	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		return this.rawStream.read(arg0, arg1, arg2);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.rawStream.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		this.rawStream.reset();
	}

	@Override
	public long skip(long arg0) throws IOException {
		return this.rawStream.skip(arg0);
	}
	
	/**
	 * 	强制关闭流 
	 * @throws IOException
	 */

	public void forceClose() throws IOException{
		if (this.rawStream != null) {
			this.rawStream.close();
		}
	}
	
	/**
	 * 从流中抽取子文件流
	 * @return
	 * @throws IOException
	 */
	public CompressionFileEntry getNextEntry() throws IOException {
		// 如果当前的文件流非包含多文件的流，直接返回
		if (!this.isArachiveFile) {
			if (!this.isEnumeratedCompleted) {
				CompressionFileEntry nextFileEntry = new CompressionFileEntry(this, this.rawFileName); 
				this.isEnumeratedCompleted = true;
				return nextFileEntry;
			}
		} else if (this.isArachiveFile) {
			//　先查看下能否从子压缩包中抽取子文件
			if (this.subAdaptiveInputStream != null) {
				CompressionFileEntry nextFileEntry = this.subAdaptiveInputStream.getNextEntry();
				if (nextFileEntry != null) {
					return nextFileEntry;
				}
				
				// 如果子压缩包没有文件了，标识置空
				this.subAdaptiveInputStream = null;
			}
			
			if (this.archiveFileInputStream == null)
				return null;
			
			// 从当前的压缩流中，抽取子文件
			ArchiveEntry entry = null;
			while ((entry = archiveFileInputStream.getNextEntry()) != null) {
				if (entry.isDirectory())
					continue;
					
				String subEntryFileName = entry.getName();
				subAdaptiveInputStream = new AdaptiveInputStream(archiveFileInputStream, subEntryFileName);
				return subAdaptiveInputStream.getNextEntry();
			}
		}

		return null;
	}
	
	public boolean isArachiveFile() {
		return this.isArachiveFile;
	}
	
	public static void main(String[] args) throws IOException {
		FileInputStream ifs = new FileInputStream("/home/shig/temp/0808/0808.tar.gz");
		
		byte buff[] = new byte[4096];
		
		AdaptiveInputStream afs = new AdaptiveInputStream(ifs, "/home/shig/temp/0808/0808.tar.gz");
		AdaptiveInputStream.CompressionFileEntry entry = null;
		while ((entry = afs.getNextEntry()) != null) {
			String fileName = entry.fileName;
			InputStream is = entry.inputStream;
			String targetFile = "/home/shig/temp/0808/" + fileName;
			FileOutputStream ofs = new FileOutputStream(targetFile);
			while (true) {
				int nRead = is.read(buff);
				if (nRead <0)
					break;
				
				ofs.write(buff, 0, nRead);
			}
			ofs.close();
			is.close();
			
			System.out.println("file:" + entry.fileName);
		}
		afs.close();
		afs.forceClose();
	}
	
	
}
