package cn.uway.framework.accessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.IoUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.utils.UcloudePathUtil;
import cn.uway.util.ExcelToCsvUtil;

/**
 * 基于数据流的FTP接入器（下载到本地，然后处理后返回文件流）。
 * 
 * @author sunt 2015-08-14
 */
public class FTPToLocalAccessor extends StreamFTPAccessor {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager
			.getLogger(FTPToLocalAccessor.class);

	// 把文件输出到本地目录
	private FileOutputStream fos = null;

	// 本地文件路径+名
	public String localPath = UcloudePathUtil.makePath("igp/igp_down_tmp/");

	/**
	 * 构造方法。
	 * 
	 * @param task
	 *            任务对象。
	 */
	public FTPToLocalAccessor() {
		File parentPath = new File(localPath);
		if (!parentPath.exists()) {
			parentPath.mkdirs();
		}
	}

	/**
	 * FTP接收，处理异常。
	 * 
	 * @param ftpPath
	 * @param ftpClient
	 * @return 文件输入流
	 */
	@Override
	protected InputStream download(String ftpPath, FTPClient ftpClient) {
		String localFile = localPath
				+ FilenameUtils.getName(StringUtil.decodeFTPPath(ftpPath, connInfo.getCharset()));
		// 下载到本地
		try {
			File file = new File(localFile);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file.getAbsolutePath());

			getFtpClient().retrieveFile(ftpPath, fos);
		} catch (IOException e) {
			LOGGER.error("FTP下载到本地异常：" + ftpPath, e);
			return null;
		} finally {
			if (null != fos) {
				IoUtil.closeQuietly(fos);
			}
		}
		// 返回文件流
		return getInputStream(localFile);
	}

	/**
	 * 处理下载的文件，并返回文件输入流
	 * 
	 * @param localFileName
	 * @return
	 */
	protected InputStream getInputStream(String localFileName) {
		InputStream in = null;
		ExcelToCsvUtil util;
		try {
			util = new ExcelToCsvUtil(localFileName, getTask(), localPath,
					"gbk");
			List<File> list = util.toCsv();
			if (list.size() > 0) {
				File csv = list.get(0);
				in = new FileInputStream(csv);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("excel转换为csv异常：文件未找到", e);
		} catch (Exception e) {
			LOGGER.error("excel转换为csv异常：未知异常", e);
		}
		return in;
	}
}
