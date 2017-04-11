package cn.uway.framework.connection.pool.ftp;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
//import org.apache.commons.pool.PoolableObjectFactory;

import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 标准FTP连接池
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public class BasicFTPClientPool implements FTPClientPool {

	private static final ILogger LOGGER = LoggerManager.getLogger(BasicFTPClientPool.class);

	/**
	 * FTP连接超时时间
	 */
	private static final long FTP_CONNECTION_MONITOR_TIMEOUT_MILLS = 30 * 60 * 1000;

	private GenericObjectPool<FTPClient> objPool;

	private PooledObjectFactory<FTPClient> objFactory;

	private FTPConnectionInfo connectInfo;

	/**
	 * 坏掉的ftp连接
	 */
	public static Map<Integer, Boolean> badFTPConnectsMap = new Hashtable<Integer, Boolean>();

	/**
	 * 构造方法。
	 * 
	 * @param connectInfo
	 *            FTP连接信息。
	 * @param maxConnection
	 *            FTP最大连接数量。
	 * @param maxWaitSecond
	 *            等待空亲FTP连接的最大时间（秒）。
	 * @throws Exception
	 *             创建连接池失败。
	 */
	public BasicFTPClientPool(FTPConnectionInfo connectInfo, int maxConnection, long maxWaitSecond) throws Exception {
		super();
		this.connectInfo = connectInfo;
		if (connectInfo.getMaxConnections() > 1) {
			this.objFactory = new FTPClientFactory(connectInfo);
			this.objPool = new GenericObjectPool<FTPClient>(this.objFactory);
			this.objPool.setMaxTotal(maxConnection);
			this.objPool.setMaxIdle(maxConnection);
			this.objPool.setMaxWaitMillis(maxWaitSecond * 1000);
			this.objPool.setTestOnBorrow(true);
			this.objPool.setTestWhileIdle(true); // 空闲时验证。
			this.objPool.setTimeBetweenEvictionRunsMillis(30 * 1000); // 空闲时验证的间隔时间。
			LOGGER.debug("[FTP_DEBUG]已创建FTP连接池，连接信息：{}，HashCode：{}", new Object[]{connectInfo, hashCode()});
			return;
		}
		LOGGER.debug("[FTP_DEBUG]使用单FTP连接，连接信息：{}", new Object[]{connectInfo});
	}

	@Override
	public void close() {
		if (this.objPool == null)
			return;
		try {
			this.objPool.close();
		} catch (Exception e) {
			LOGGER.warn("关闭连接池时异常。", e);
		} finally {
			this.objPool.clear();
		}
	}

	@Override
	public void setMaxConnection(int maxConnection) {
		if (this.objPool == null)
			return;
		this.objPool.setMaxTotal(maxConnection);
		this.objPool.setMaxIdle(maxConnection);
	}

	@Override
	public int getMaxConnection() {
		if (this.objPool == null)
			return 0;
		return this.objPool.getNumActive();
	}

	@Override
	public void setMaxWaitSecond(int maxWaitSecond) {
		if (this.objPool == null)
			return;
		this.objPool.setMaxWaitMillis(maxWaitSecond * 1000);
	}

	@Override
	public int getMaxWaitSecond() {
		if (this.objPool == null)
			return 0;
		return (int) (this.objPool.getMaxWaitMillis() / 1000);
	}

	@Override
	public int getCurrentActiveCount() {
		if (this.objPool == null)
			return 0;
		return this.objPool.getNumActive();
	}

	public int getCurrentIdelCount() {
		if (this.objPool == null)
			return 0;
		return this.objPool.getNumIdle();
	}

	@Override
	public FTPClient getFTPClient() throws Exception, NoSuchElementException {
		// 不使用ftp连接池。
		if (connectInfo.getMaxConnections() == 1) {
			LOGGER.debug("[FTP_DEBUG]使用单FTP连接.");
			FTPClient ftp = this.creareFtpClient();
			return ftp;
		}
		LOGGER.debug("[FTP_DEBUG]尝试获取FTP连接，ID：{}，IP：{}，当前连接数：{}，最大连接数：{}", new Object[]{connectInfo.getId(), connectInfo.getIp(),
				getCurrentActiveCount(), getMaxConnection()});
		ProxyFTPClient ftp = (ProxyFTPClient) this.objPool.borrowObject();
		ftp.setReturned(false);
		ftp.setMonitor(new FTPMonitor(ftp, FTP_CONNECTION_MONITOR_TIMEOUT_MILLS));
		ftp.getMonitor().startMonitor();
		LOGGER.debug("[FTP_DEBUG]已获取FTP连接，ID：{}，IP：{}，当前连接数：{}，最大连接数：{}", new Object[]{connectInfo.getId(), connectInfo.getIp(),
				getCurrentActiveCount(), getMaxConnection()});
		return ftp;
	}

	/**
	 * 创建一个ftp连接
	 * 
	 * @return
	 */
	private final FTPClient creareFtpClient() throws Exception, NoSuchElementException {
		FTPClient ftpCLient = new FTPClient();
		ftpCLient.setRemoteVerificationEnabled(false);
		ftpCLient.setReceiveBufferSize(64 * 1024);
		ftpCLient.setBufferSize(64 * 1024);
		ftpCLient.setDefaultTimeout(20 * 60 * 1000);
		ftpCLient.setConnectTimeout(20 * 60 * 1000);
		LOGGER.debug("正在连接到 - {}:{}", this.connectInfo.getIp(), this.connectInfo.getPort());
		ftpCLient.connect(this.connectInfo.getIp(), this.connectInfo.getPort());
		ftpCLient.setSoTimeout(5 * 60 * 1000);
		ftpCLient.setDataTimeout(20 * 60 * 1000);
		LOGGER.debug("正在进行安全验证 - {}:{}", this.connectInfo.getUserName(), this.connectInfo.getPassword());
		boolean loginResult = ftpCLient.login(this.connectInfo.getUserName(), this.connectInfo.getPassword());
		if (!loginResult)
			throw new Exception("FTP登录失败，连接信息：" + this.connectInfo + "，返回信息：" + ftpCLient.getReplyString());
		if (this.connectInfo.isPassiveFlag()) {
			ftpCLient.enterLocalPassiveMode();
			LOGGER.debug("进入FTP被动模式。ftp entering passive mode");
		} else {
			ftpCLient.enterLocalActiveMode();
			LOGGER.debug("进入FTP主动模式。ftp entering local mode");
		}
		ftpCLient.setFileType(FTPClient.BINARY_FILE_TYPE);
		LOGGER.debug("FTP连接创建：ID：{}，IP：{}", connectInfo.getIp(), this.connectInfo.getIp());
		return ftpCLient;
	}

	/**
	 * FTP连接代理（代理关闭动作，将关闭动作改为对象回池）。
	 * 
	 * @author chenrongqiang @ 2014-3-30
	 */
	class ProxyFTPClient extends FTPClient {

		/**
		 * 是否已经归还到池中了。
		 */
		boolean bReturned = false;

		/**
		 * IP地址
		 */
		String ip;

		Socket dataConnection;

		FTPMonitor monitor;

		ProxyFTPClient(String ip) {
			this.ip = ip;
		}

		public String getIp() {
			return ip;
		}

		public synchronized FTPMonitor getMonitor() {
			return monitor;
		}

		public synchronized void setMonitor(FTPMonitor monitor) {
			this.monitor = monitor;
		}

		public Socket getRawSocket() {
			return super._socket_;
		}

		public Socket getDataConnection() {
			return dataConnection;
		}

		@Override
		public Socket _openDataConnection_(int command, String arg) throws IOException {
			this.dataConnection = super._openDataConnection_(command, arg);
			return dataConnection;
		}

		public synchronized boolean isReturned() {
			return this.bReturned;
		}

		public synchronized void setReturned(boolean b) {
			this.bReturned = b;
		}

		@Override
		public void disconnect() throws IOException {
			this.returnToPool();
		}

		@Override
		public boolean logout() throws IOException {
			return this.returnToPool();
		}

		boolean returnToPool() throws IOException {
			boolean bReturn = false;
			try {
				LOGGER.debug("[FTP_DEBUG]尝试归还FTP连接，IP：{}，当前连接数：{}，最大连接数：{}，HashCode(Pool)：{}", new Object[]{connectInfo.getIp(),
						getCurrentActiveCount(), getMaxConnection(), BasicFTPClientPool.this.hashCode()});
				if (!isReturned()) {
					/**
					 * <pre>
					 * change:shig date:2014-7-22
					 * explain:设置已归还标识应该在returnObject之前调用，否则会有线程同步问题。
					 * 			假如当前线程调用returnObject返还完，另外线程立即将当前的ftpClient获取走，
					 * 			紧接着当前线程再将ftpClient状态设为"已归还"，
					 * 			那么获走该ftpClient的线程用完后将不能再将ftpClient归还到pool中.
					 * </pre>
					 */
					this.setReturned(true);

					// monitor end要放到returnObject前面，不然会有线程同步问题
					this.getMonitor().endMonitor();
					this.setMonitor(null);

					StringBuilder debugStr = new StringBuilder("[FTP_DEBUG]");
					int key = this.hashCode();
					Boolean value = BasicFTPClientPool.badFTPConnectsMap.get(key);
					if (value != null && value) {
						// 销毁
						BasicFTPClientPool.this.objPool.invalidateObject(this);
						BasicFTPClientPool.badFTPConnectsMap.remove(key);
						debugStr.append("已销毁");
					} else {
						BasicFTPClientPool.this.objPool.returnObject(this);
						debugStr.append("已归还");
					}
					debugStr.append("FTP连接，IP：{}，当前连接数：{}，当前空闲数：{}，最大连接数：{}，HashCode(Pool)：{}");
					LOGGER.debug(debugStr.toString(), new Object[]{connectInfo.getIp(), getCurrentActiveCount(), getCurrentIdelCount(),
							getMaxConnection(), BasicFTPClientPool.this.hashCode()});
					bReturn = true;
					// this.getMonitor().endMonitor();
					// this.setMonitor(null);
				} else {
					LOGGER.warn("[FTP_DEBUG]由于此FTP连接已经在池中，所以不再归还，IP：{}，当前连接数：{}，当前空闲数：{}，最大连接数：{}，HashCode(Pool)：{}",
							new Object[]{connectInfo.getIp(), getCurrentActiveCount(), getCurrentIdelCount(), getMaxConnection(),
									BasicFTPClientPool.this.hashCode()});
				}
			} catch (Exception e) {
				LOGGER.error("向池中归还FTP连接时异常。", e);
				// 如果在调用returnObject时出异常，还需要把已归还状态设置为false.
				if (!bReturn) {
					this.setReturned(false);
				}
			}
			return true;
		}

		void _realDisconnect() throws IOException {
			super.disconnect();
		}

		boolean _realLogout() throws IOException {
			return super.logout();
		}

		@Override
		protected void finalize() throws Throwable {
			if (getMonitor() != null) {
				LOGGER.warn("由于FTP连接中的监控未正确关闭，在finalize()中关闭，监控线程名：{}，FTP：{}", new Object[]{getMonitor().getThreadName(), hashCode()});
				getMonitor().endMonitor();
				setMonitor(null);
			}
			super.finalize();
		}
	}

	// FTP连接工厂。
	class FTPClientFactory implements PooledObjectFactory<FTPClient> {

		private FTPConnectionInfo connectInfo;

		/* so_timeout等超时时间。 */
		private static final int DEFAULT_TIMEOUT_MILLS = 20 * 60 * 1000;

		FTPClientFactory(FTPConnectionInfo connectInfo) {
			super();
			this.connectInfo = connectInfo;
		}

		@Override
		public PooledObject<FTPClient> makeObject() throws Exception {
			FTPClient ftp = new ProxyFTPClient(this.connectInfo.getIp());
			ftp.setReceiveBufferSize(64 * 1024);
			ftp.setBufferSize(64 * 1024);
			ftp.setDefaultTimeout(DEFAULT_TIMEOUT_MILLS);/* 要在connect之前调用。 */
			ftp.setConnectTimeout(DEFAULT_TIMEOUT_MILLS);/* 要在connect之前调用。 */
			ftp.connect(this.connectInfo.getIp(), this.connectInfo.getPort());
			ftp.setSoTimeout(5 * 60 * 1000);/* 要在connect之后调用。 */
			ftp.setDataTimeout(DEFAULT_TIMEOUT_MILLS);/* 要在connect之后调用。 */
			ftp.setKeepAlive(true);
			if (!ftp.login(this.connectInfo.getUserName(), this.connectInfo.getPassword()))
				throw new Exception("FTP登录失败，连接信息：" + this.connectInfo + "，返回信息：" + ftp.getReplyString());
			if (this.connectInfo.isPassiveFlag()) {
				ftp.enterLocalPassiveMode();
				LOGGER.debug("进入FTP被动模式。ftp entering passive mode");
			} else {
				ftp.enterLocalActiveMode();
				LOGGER.debug("进入FTP主动模式。ftp entering local mode");
			}
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE); /* 设为二进制传输模式。 */
			LOGGER.debug("[FTP_DEBUG]FTP连接创建：{}，HashCode：{}", this.connectInfo.getIp(), ftp != null ? ftp.hashCode() : "[null]");
			
			return new DefaultPooledObject<FTPClient>(ftp);
		}

		@Override
		public void destroyObject(PooledObject<FTPClient> p) throws Exception {
			FTPClient obj = p.getObject();
			if (obj == null)
				return;
			ProxyFTPClient ftp = (ProxyFTPClient) obj;
			try {
				if (!ftp._realLogout()) {
					LOGGER.warn("登出FTP时失败，返回信息：{}，FTP连接：{}", new Object[]{ftp.getReplyString(), this.connectInfo});
				}
			} catch (Exception e) {
				LOGGER.warn("登出FTP时异常，FTP连接：" + this.connectInfo, e);
			} finally {
				try {
					ftp._realDisconnect();
				} catch (Exception e) {
					LOGGER.warn("断开FTP连接时异常，FTP连接：" + this.connectInfo, e);
				}
			}
			LOGGER.debug("[FTP_DEBUG]FTP连接销毁，HashCode：{}", obj != null ? obj.hashCode() : "[null]");
		}

		@Override
		public boolean validateObject(PooledObject<FTPClient> p) {
			if (p == null) {
				LOGGER.warn("FTP验证失败，连接对象为null.");
				return false;
			}

			String cmd = this.connectInfo.getValidateCmd();
			if (cmd != null)
				cmd = cmd.trim();
			if (cmd == null || cmd.trim().isEmpty())
				cmd = FTPConnectionInfo.DEFAULT_FTP_VALIDATE_CMD;

			FTPClient ftp = p.getObject();
			try {
				boolean b = FTPReply.isPositiveCompletion(ftp.sendCommand(cmd));
				if (!b)
					LOGGER.warn("FTP验证失败，FTP响应为：{}", ftp.getReplyString());
				return b;
			} catch (IOException e) {
				LOGGER.warn("向FTP发送验证命令时异常，FTP连接：" + this.connectInfo, e);
				return false;
			}
		}

		@Override
		public void activateObject(PooledObject<FTPClient> p) throws Exception {
			// 无操作。
		}

		@Override
		public void passivateObject(PooledObject<FTPClient> p) throws Exception {
			// 无操作。
		}
	}
}
