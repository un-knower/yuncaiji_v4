package cn.uway.framework.connection.pool.ftp;

import java.io.IOException;

import cn.uway.framework.connection.pool.ftp.BasicFTPClientPool.ProxyFTPClient;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

class FTPMonitor implements Runnable {

	private static final ILogger log = LoggerManager.getLogger(FTPMonitor.class);

	private Thread th;

	private ProxyFTPClient ftp;

	private long timeoutMills;

	public FTPMonitor(ProxyFTPClient ftp, long timeoutMills) {
		super();
		this.ftp = ftp;
		this.timeoutMills = timeoutMills;
		this.th = new Thread(this, "FTP连接监控-" + ftp.getIp());
		this.th.setPriority(Thread.MIN_PRIORITY);
		this.th.setDaemon(true);
	}

	@Override
	public void run() {
		log.debug("监控开始，FTP：{}，超时时间：{}毫秒。", new Object[]{this.ftp.hashCode(), this.timeoutMills});
		try {
			Thread.sleep(this.timeoutMills);
			try {
				if (this.ftp.getDataConnection() != null)
					this.ftp.getDataConnection().close();
			} finally {
				if (this.ftp.getRawSocket() != null)
					this.ftp.getRawSocket().close();
			}
			log.warn("由于到达超时时间，已强行关闭FTP的socket连接，FTP：{}，超时时间：{}毫秒。", new Object[]{this.ftp.hashCode(), this.timeoutMills});
		} catch (IOException e) {
			log.error("监控异常，未能关闭FTP的socket连接，FTP：" + this.ftp.hashCode() + "，超时时间：" + this.timeoutMills + "毫秒。", e);
		} catch (InterruptedException e) {
			log.debug("监控正常结束，FTP：{}，超时时间：{}毫秒。", new Object[]{this.ftp.hashCode(), this.timeoutMills});
		}
	}

	public void startMonitor() {
		if (this.th.getState() == Thread.State.NEW)
			this.th.start();
	}

	public void endMonitor() {
		if (this.th.getState() != Thread.State.NEW)
			this.th.interrupt();
	}

	public String getThreadName() {
		return this.th.getName();
	}
}
