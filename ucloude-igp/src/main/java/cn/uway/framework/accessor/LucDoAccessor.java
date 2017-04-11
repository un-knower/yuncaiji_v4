package cn.uway.framework.accessor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.IoUtil;

/**
 * LucDo 本地文件方式接入器。
 * 
 * @author ChenSijiang 2012-11-1
 */
public class LucDoAccessor extends AbstractAccessor {

	private static final ILogger LOGGER = LoggerManager.getLogger(LucDoAccessor.class);

	/* 保存最后一次取得的流。 */
	private InputStream currIn;

	public LucDoAccessor() {
		super();
		LOGGER.debug("LucDoAccessor被实例化。");
	}

	@Override
	public boolean beforeAccess() {
		return super.beforeAccess();
	}

	@Override
	public void setConnectionInfo(cn.uway.framework.connection.ConnectionInfo connInfo) {

	}

	/**
	 * 此处的paths是listFiles后的结果，对每条进行下载即可。
	 */
	@Override
	public AccessOutObject access(GatherPathEntry path) throws Exception {
		this.startTime = new Date();
		String localFilePath = path.getPath();
		this.gatherObj = localFilePath;
		LOGGER.debug("开始处理：" + localFilePath);
		InputStream in = new GZIPInputStream(new FileInputStream(localFilePath), 64 * 1024);
		this.currIn = in;
		return this.toAccessOutObject(in, localFilePath, 0);
	}

	@Override
	public void close() {
		if (currIn != null) {
			// IoUtil.readFinish(currIn); //
			// 这里是将InputStream内容全部read完，否则FTP服务器会返回失败响应。
			IoUtil.closeQuietly(currIn);
		}
		super.close();
	}

	/**
	 * 将FTP数据流转换为AccessOutObject对象。
	 * 
	 * @param in 数据流。
	 * @param rawName 原始名称。
	 * @return AccessOutObject对象。
	 */
	public AccessOutObject toAccessOutObject(InputStream in, String rawName, int len) {
		StreamAccessOutObject out = new StreamAccessOutObject();
		out.setOutObject(in);
		out.setLen(len);
		out.setRawAccessName(rawName);
		return out;
	}

}
