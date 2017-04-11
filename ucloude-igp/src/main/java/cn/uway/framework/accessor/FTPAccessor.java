package cn.uway.framework.accessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.NoSuchElementException;

import org.apache.commons.net.ftp.FTPClient;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.pool.ftp.FTPClientPool;
import cn.uway.framework.connection.pool.ftp.FTPClientPoolFactory;
import cn.uway.framework.connection.pool.ftp.KeyedFTPClientPoolMgr;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.Task;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.FTPUtil;
import cn.uway.ucloude.utils.IoUtil;
import cn.uway.ucloude.utils.StringUtil;

/**
 * FTP方式接入器。
 * 
 * @author ChenSijiang 2012-11-1
 */
public class FTPAccessor extends AbstractAccessor {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(FTPAccessor.class);

	/**
	 * FTP连接数据源对象
	 */
	protected FTPConnectionInfo connInfo;

	/**
	 * FTP连接池管理器，以FTP的IP为主键，存放及获取FTP连接池。
	 */
	private KeyedFTPClientPoolMgr<Integer> poolMgr;

	/**
	 * FTP连接池工厂。
	 */
	private FTPClientPoolFactory poolFactory;

	/**
	 * FTP连接池。
	 */
	private FTPClientPool ftpPool;

	/**
	 * FTP连接。
	 */
	private FTPClient ftpClient;

	/**
	 * 保存最后一次取得的流。
	 */
	private InputStream currIn;

	@Override
	public void setConnectionInfo(ConnectionInfo connInfo) {
		if (!(connInfo instanceof FTPConnectionInfo))
			throw new IllegalArgumentException("错误的连接信息.请配置有效的FTP连接配置信息");
		this.connInfo = (FTPConnectionInfo) connInfo;
	}

	/**
	 * 检查FTP连接池管理器中是否已有针对此IP的FTP连接池，如果没有，先创建<br>
	 * 注：如果FTP连接信息配置的连接数量为1，则不使用连接池<br>
	 */
	@Override
	public boolean beforeAccess() {
		// int connNum = connInfo.getMaxConnections();
		// if (connNum == 1)
		// return true;
		this.ftpPool = this.getPoolMgr().getFTPClientPool(connInfo.getId());
		if (this.ftpPool == null) {
			try {
				this.ftpPool = getPoolFactory().create(connInfo);
				this.getPoolMgr().putFTPClientPool(connInfo.getId(), this.ftpPool);
			} catch (Exception e) {
				LOGGER.error("创建FTP连接池异常。", e);
				return false;
			}
		}
		return true;
	}

	/**
	 * FTP接入器核心工作方法<br>
	 * 1、创建FTP连接<br>
	 * 2、创建文件读取流<br>
	 * 3、组装接入结果对象.
	 */
	@Override
	public AccessOutObject access(GatherPathEntry path) throws Exception {
		this.startTime = new Date();
		try {
			if (this.ftpClient == null) {
				// 获取FTP连接。
				LOGGER.debug("[FTPAccessor]等待获取FTP连接......KEY={}，当前池中活动连接数={}，最大连接数={}", new Object[]{connInfo.getId(),
						this.getFtpPool().getCurrentActiveCount(), this.getFtpPool().getMaxConnection()});
				this.ftpClient = this.getFtpPool().getFTPClient();
				LOGGER.debug("[FTPAccessor]获取FTP连接成功，KEY={}，当前池中活动连接数={}，最大连接数={}", new Object[]{connInfo.getId(),
						this.getFtpPool().getCurrentActiveCount(), this.getFtpPool().getMaxConnection()});
			}
		} catch (NoSuchElementException e) {
			// 异常处理，目前，有一个连接获取失败，所有路径都不再处理。
			throw new Exception("从FTP连接池中获取连接失败，等待超时，无可用的空闲连接。连接池最大连接数量：" + this.getFtpPool().getMaxConnection() + "，当前已用连接数量："
					+ this.getFtpPool().getCurrentActiveCount() + "，最大等待时间（秒）：" + this.getFtpPool().getMaxWaitSecond(), e);
		} catch (Exception e) {
			throw new Exception("从FTP连接池中获取连接时发生异常", e);
		}
		String ftpPath = path.getPath();
		/*
		 * 日志乱码的根源所在，之前这里默认转换为UTF-8格式，但是JVM默认的是GBK格式，<br/> 所以在FtpClient拿回来的路径是以GBK格式为基础的ISO-8859-1编码集，因此在 <br/> 程序中造成乱码.<br>
		 * 
		 * @author Niow 2014-6-11
		 */
		
		String decodedFTPPath = StringUtil.decodeFTPPath(ftpPath, connInfo.getCharset());
		LOGGER.debug("开始下载：{}", decodedFTPPath);
		InputStream in = this.downRemoteFile(ftpPath);
		// 如果多次重试均无法获取FTP连接流 则直接异常退出
		if (in == null)
			throw new Exception(connInfo.getRetryTimes() + "次重试下载失败,放弃此文件：" + decodedFTPPath);
		LOGGER.debug("获取{}FTP文件流成功", decodedFTPPath);
		this.currIn = in;
		return this.toAccessOutObject(in, getTask().getWorkerType() == 9 ? ftpPath : decodedFTPPath, path.getSize(), path, getTask());
	}

	@Override
	public void close() {
		if (currIn != null) {
			// 这里是将InputStream内容全部read完，否则FTP服务器会返回失败响应。
			IoUtil.readFinish(currIn);
			IoUtil.closeQuietly(currIn);
		}
		if (this.ftpClient != null) {
			LOGGER.debug("读取FTP响应……");
			// 读取FTP服务器响应字符串。
			this.completePendingCommand(this.ftpClient);

			int connNum = connInfo.getMaxConnections();
			if (connNum > 1) {
				// 此处的ftpClient实际上是ProxyFTPClient，重写了logout和disconnect方法，logoutAndCloseFTPClient方法只会将连接放入连接池
				LOGGER.debug("读取FTP响应完成：{}", (this.ftpClient.getReplyString() != null ? this.ftpClient.getReplyString().trim() : "[null]"));
				LOGGER.debug("[FTPAccessor]准备将FTP连接归还入池，ID=：{}，最大连接数：{}，当前连接数：{}", new Object[]{connInfo.getId(),
						this.getFtpPool().getMaxConnection(), this.getFtpPool().getCurrentActiveCount()});
				FTPUtil.logoutAndCloseFTPClient(this.ftpClient);// 归还到池中。
				LOGGER.debug("[FTPAccessor]FTP连接归还入池，ID=：{}，最大连接数：{}，当前连接数：{}", new Object[]{connInfo.getId(), this.getFtpPool().getMaxConnection(),
						this.getFtpPool().getCurrentActiveCount()});
			} else {
				// 此处是真正的ftpClient，logoutAndCloseFTPClient方法会关闭它的.
				LOGGER.debug("读取FTP响应完成：{}", (this.ftpClient.getReplyString() != null ? this.ftpClient.getReplyString().trim() : "[null]"));
				FTPUtil.logoutAndCloseFTPClient(this.ftpClient);
				LOGGER.debug("[FTPAccessor] FTP单连接已经被直接关掉，ID=：{}", connInfo.getId());
			}
			/** 这个地方必须要置空，因为ftpclient可以外部set进来，所以用完不仅要关掉，还要置空，否则单连接就会有问题 */
			this.ftpClient = null;
		}
		super.close();
	}

	/**
	 * 获取FTP连接池管理器。
	 * 
	 * @return FTP连接池管理器。
	 */
	public KeyedFTPClientPoolMgr<Integer> getPoolMgr() {
		return poolMgr;
	}

	/**
	 * 设置FTP连接池管理器。
	 * 
	 * @param poolMgr
	 *            FTP连接池管理器。
	 */
	public void setPoolMgr(KeyedFTPClientPoolMgr<Integer> poolMgr) {
		this.poolMgr = poolMgr;
	}

	/**
	 * 获取FTP连接池工厂。
	 * 
	 * @return FTP连接池工厂。
	 */
	public FTPClientPoolFactory getPoolFactory() {
		return poolFactory;
	}

	/**
	 * 设置FTP连接池工厂。
	 * 
	 * @param poolFactory
	 *            FTP连接池工厂。
	 */
	public void setPoolFactory(FTPClientPoolFactory poolFactory) {
		this.poolFactory = poolFactory;
	}

	/**
	 * 将FTP数据流转换为AccessOutObject对象。
	 * 
	 * @param in
	 *            数据流。
	 * @param rawName
	 *            原始名称。
	 * @return AccessOutObject对象。
	 */
	public AccessOutObject toAccessOutObject(InputStream in, String rawName, long len, GatherPathEntry gatherPathInfo, Task task) {
		return null;
	}

	/**
	 * 获取FTP连接池。
	 * 
	 * @return FTP连接池。
	 */
	public FTPClientPool getFtpPool() {
		return ftpPool;
	}

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	protected void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	/**
	 * FTP 下载过程，包括重试。
	 */
	private InputStream downRemoteFile(String ftpPath) {
		InputStream in = download(ftpPath, ftpClient);
		if (in != null) {
			return in;
		}
		LOGGER.warn("FTP下载失败，开始重试，文件：{}，reply={}", new Object[]{ftpPath, ftpClient.getReplyString() != null ? ftpClient.getReplyString().trim() : ""});
		for (int i = 0; i < connInfo.getRetryTimes(); i++) {
			try {
				Thread.sleep(connInfo.getRetryDelaySecond());
			} catch (InterruptedException e) {}
			LOGGER.debug("第{}次重试下载，准备重新创建连接……", i + 1);
			this.completePendingCommand(ftpClient);
			FTPUtil.logoutAndCloseFTPClient(ftpClient);
			try {
				ftpClient = this.getFtpPool().getFTPClient();
				LOGGER.debug("第{}次重试下载，重新创建连接成功。", i + 1);
			} catch (Exception e) {
				LOGGER.debug("第" + (i + 1) + "次重试下载，重新创建连接失败。", e);
			}
			in = download(ftpPath, ftpClient);
			if (in != null) {
				LOGGER.debug("第{}次重试下载成功。", i + 1);
				break;
			}
		}
		return in;

	}

	/**
	 * FTP接收，处理异常。
	 * 
	 * @param ftpPath
	 * @param ftpClient
	 * @return FTP文件下载流
	 */
	protected InputStream download(String ftpPath, FTPClient ftpClient) {
		InputStream in = null;
		try {
			in = getFtpClient().retrieveFileStream(ftpPath);
		} catch (IOException e) {
			LOGGER.error("FTP下载异常：" + ftpPath, e);
		}
		return in;

	}

	/**
	 * 从FTP读取了流之后，需要读取FTP响应消息，否则下次操作时将会失败。
	 * 
	 * @param ftpClient
	 * @return 是否操作成功
	 */
	private boolean completePendingCommand(FTPClient ftpClient) {
		try {
			//林鹏：  参考http://zhouzaibao.iteye.com/blog/354266
			//处理原因：出现completePendingCommand()同步调用时间5分钟，然后抛出异常
			//建议如果出现问题，可以将下面注释的sendNoOp()方法打开，进行尝试
			//ftpClient.sendNoOp();
			if (!ftpClient.completePendingCommand()) {
				LOGGER.warn("FTP失败响应：{}", ftpClient.getReplyString());
				return false;
			}
			return true;
		} catch (IOException e) {
			LOGGER.error("获取FTP响应异常。", e);
			return false;
		}
	}
}
