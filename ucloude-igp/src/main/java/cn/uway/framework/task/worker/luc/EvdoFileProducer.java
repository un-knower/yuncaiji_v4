package cn.uway.framework.task.worker.luc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import cn.uway.util.FileUtil;
import cn.uway.util.IoUtil;

/**
 * EVDO拆分后的生成类<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public class EvdoFileProducer {

	/**
	 * 需要创建的文件名<br>
	 * 注意：文件名包含完整的路径
	 */
	private String cuttedFileName;

	/**
	 * 写入过程中的临时文件对象<br>
	 */
	private File tempFile;

	/**
	 * 文件输出流<br>
	 */
	protected BufferedWriter writer;

	/**
	 * 构造方法<br>
	 * 
	 * @param cutterdFileName
	 */
	public EvdoFileProducer(String cuttedFileName) throws IOException {
		this.cuttedFileName = cuttedFileName;
		checkFile();
		// 先创建临时文件<br>
		tempFile = new File(cuttedFileName + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		GZIPOutputStream gzout = new GZIPOutputStream(fos, 4 * 1024 * 1024);
		OutputStreamWriter outwriter = new OutputStreamWriter(gzout);
		// 每次缓存4M后再写文件<br>
		writer = new BufferedWriter(outwriter, 4 * 1024 * 1024);
	}

	/**
	 * 检查文件、.tmp、.ok是否存在,如存在则删除<br>
	 * 
	 * @return 是否可以创建当前文件
	 */
	private boolean checkFile() {
		if (FileUtil.exists(cuttedFileName))
			return FileUtil.removeFile(cuttedFileName);
		if (FileUtil.exists(cuttedFileName + ".tmp"))
			return FileUtil.removeFile(cuttedFileName + ".tmp");
		if (FileUtil.exists(cuttedFileName + ".OK"))
			return FileUtil.removeFile(cuttedFileName + ".OK");
		return true;
	}

	/**
	 * 向外输出一行记录<br>
	 * 
	 * @param line
	 * @throws IOException
	 */
	public void write(String line) throws IOException {
		writer.write(line);
		writer.newLine();
		writer.flush();
	}

	/**
	 * 结束写入<br>
	 * 1、关闭流<br>
	 * 2、重命名文件<br>
	 * 3、返回成功标记<br>
	 * 
	 * @return 是否结束成功<br>
	 */
	public boolean commit() {
		close();
		return tempFile.renameTo(new File(cuttedFileName));
	}

	/**
	 * 关闭方法<br>
	 * 只能在外部发生异常的时候调用,不会重命名文件<br>
	 * 
	 * @return
	 */
	public void close() {
		IoUtil.closeQuietly(writer);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		EvdoFileProducer evdoFileProducer = new EvdoFileProducer("F:/test.gz");
		for (int i = 0; i < 100; i++) {
			evdoFileProducer.write(i + "");
		}
		Thread.sleep(10000);
		evdoFileProducer.commit();
	}
}
