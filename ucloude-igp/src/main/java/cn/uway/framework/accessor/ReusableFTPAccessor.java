package cn.uway.framework.accessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;

import cn.uway.framework.connection.pool.ftp.FTPClientPool;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.IoUtil;
import cn.uway.ucloude.utils.StringUtil;

/**
 * 可重用的FTP接入器。主要是将FTP连接由每次创建，改为传入使用。即调用者需要事先调用{@linkplain #setReusableFTPClient(FTPClient)}方法，设置FTP连接，然后FTP接入才可正常工作，此类本身不会创建FTP连接。
 * 另一个改变是接入的输出对象为下载到本地的文件，而非一个InputStream。
 * 
 * @author chensijiang 2014-8-12
 */
public class ReusableFTPAccessor extends FTPAccessor {

	/** 日志记录器。 */
	private static final ILogger LOGGER = LoggerManager.getLogger(ReusableFTPAccessor.class);

	/** 供父类使用的代理FTP池，参考{@linkplain DummyFTPClientPool}的描述。 */
	private DummyFTPClientPool dummyPool;

	@Override
	public boolean beforeAccess() {
		return true;
	}

	/**
	 * 设置需要使用的FTP连接。
	 * 
	 * @param ftp
	 *            FTP连接。
	 */
	public void setReusableFTPClient(FTPClient ftp) {
		super.setFtpClient(ftp);
	}

	@Override
	public FTPClientPool getFtpPool() {
		// 重写父类的getFtpPool()方法，因为父类是从FTP池获取连接的，
		// 这里使父类获取一个假的FTP池，池中获取连接的方法，
		// 获取到的是调用者传入的连接（通过setReusableFTPClient(FTPClient ftp)方法传入的）。
		if (this.dummyPool == null)
			this.dummyPool = new DummyFTPClientPool();
		return this.dummyPool;
	}

	// 此处将FTP接入到的Socket流下载到本地磁盘。
	@Override
	public AccessOutObject toAccessOutObject(InputStream in, String ftpPath, long len, GatherPathEntry gatherPathInfo, Task task) {
		// 下载的根目录。
		File baseDir = new File(AppContext.getBean("tempFileDir", String.class));
		if (!baseDir.exists() && !baseDir.isDirectory() && !baseDir.mkdirs()) {
			throw new RuntimeException("基准临时目录[" + baseDir + "]不存在，并且程序尝试自动创建时失败，请检查当前操作系统用户是否拥有足够权限。");
		}

		// 下载的子目录，即接在baseDir后面的那部分路径。
		StringBuilder subDir = new StringBuilder();
		if (task instanceof ReTask) {
			subDir.append(File.separator).append(task.getId()).append("-").append(((ReTask) task).getrTaskId());
		} else {
			subDir.append(File.separator).append(task.getId());
		}
		subDir.append(File.separator).append(FilenameUtils.getFullPath(gatherPathInfo.getPath()));

		// 拼接成完成的下载目录路径，下载的根目录+下载的子目录。
		baseDir = new File(baseDir, subDir.toString());
		if (!baseDir.exists() && !baseDir.mkdirs()) {
			throw new RuntimeException("临时目录[" + baseDir + "]不存在，并且程序尝试自动创建时失败，请检查当前操作系统用户是否拥有足够权限。");
		}

		// 下载到本地磁盘的文件名，完整路径。
		File localFile = new File(baseDir, FilenameUtils.getName(ftpPath));

		// 以下开始写入到本地磁盘。
		LOGGER.debug("开始下载，文件本地下载路径：{}", StringUtil.decodeFTPPath(localFile.getAbsolutePath(), connInfo.getCharset()));
		long time = System.currentTimeMillis();
		FileOutputStream out = null;
		long count = 0;
		boolean suc = false;
		try {
			out = new FileOutputStream(localFile);
			count = IOUtils.copy(in, out);
			suc = true;
		} catch (IOException ex) {
			LOGGER.error("下载文件时出现I/O错误。", ex);
		} finally {
			IoUtil.closeQuietly(out);
			// 这个FTP的SocketInputStream要关闭，否则在某些FTP服务器上，读取不到下一个响应。
			IoUtil.closeQuietly(in);
			time = System.currentTimeMillis() - time;
			// 避免除数为0.
			if (time == 0)
				time = 1;
		}

		if (suc) {
			LOGGER.debug(
					"文件下载成功，文件大小：{}MB，字节数：{}，耗时{}秒，速度：{}KB/秒。",
					new Object[]{String.format("%.4f", count / 1024 / 1024.), count, String.format("%.3f", time / 1000.),
							String.format("%.3f", count / 1024. / (time / 1000.))});
		} else {
			LOGGER.debug("文件下载失败，耗时{}秒。", String.format("%.3f", time / 1000.));
			return null;
		}

		return new FTPLocalDownloadAccessOutObject(localFile, gatherPathInfo, connInfo.getCharset());
	}

	@Override
	public void close() {
		// 什么都不做。
	}

	/**
	 * 一个简单的FTP连接池代理，作用是修改{@linkplain #getFTPClient()}方法，使其直接使用调用者通过{@linkplain ReusableFTPAccessor#setReusableFTPClient(FTPClient)}方法传入的FTP连接。
	 * 
	 * @author chensijiang 2014-8-12
	 */
	protected class DummyFTPClientPool implements FTPClientPool {

		@Override
		public void setMaxConnection(int maxConnection) {
		}

		@Override
		public int getMaxConnection() {
			return 0;
		}

		@Override
		public void setMaxWaitSecond(int maxWaitSecond) {
		}

		@Override
		public int getMaxWaitSecond() {
			return 0;
		}

		@Override
		public int getCurrentActiveCount() {
			return 0;
		}

		@Override
		public FTPClient getFTPClient() throws Exception, NoSuchElementException {
			// 或取ReusableFTPAccessor的getFtpClient()方法返回的连接，实际getFtpClient()方法并未被重写。
			// 但调用者要使用setReusableFTPClient(FTPClient)方法传入连接，而它调用的是与getFtpClient()配套的setFtpClient()方法，
			// 所以此处用getFtpClient()方法会获取到调用者传入的FTP连接。
			return ReusableFTPAccessor.this.getFtpClient();
		}

		@Override
		public void close() {
		}

	}
}
