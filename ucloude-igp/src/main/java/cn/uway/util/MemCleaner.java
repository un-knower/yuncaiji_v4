package cn.uway.util;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class MemCleaner extends Thread {

	private static final ILogger log = LoggerManager.getLogger(MemCleaner.class);

	private double threoldPercent = 80;

	private long periodMills = 1 * 60 * 1000;

	public MemCleaner() {
		super("内存清理");
		super.setDaemon(true);
		super.setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		log.info("内存清理线程启动，清理间隔：{}毫秒。", this.periodMills);
		while (true) {
			double maxMemory = Runtime.getRuntime().maxMemory() / 1024. / 1024.;
			double totalMemory = Runtime.getRuntime().totalMemory() / 1024. / 1024.;
			double freeMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
			double usedMemory = totalMemory - freeMemory;
			double usedRate = usedMemory / totalMemory * 100.;
			log.info("【清理前】当前JAVA虚拟机已分配到的内存：{}MB，已用内存：{}MB，配置的最大虚拟机内存{}MB，空闲内存：{}MB，使用率{}%.", new Object[]{totalMemory, usedMemory, maxMemory,
					freeMemory, usedRate});
			if (usedRate >= threoldPercent) {
				System.gc();
				maxMemory = Runtime.getRuntime().maxMemory() / 1024. / 1024.;
				totalMemory = Runtime.getRuntime().totalMemory() / 1024. / 1024.;
				freeMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
				usedMemory = totalMemory - freeMemory;
				log.info("【清理后】当前JAVA虚拟机已分配到的内存：{}MB，已用内存：{}MB，配置的最大虚拟机内存{}MB，空闲内存：{}MB，使用率{}%.", new Object[]{totalMemory, usedMemory, maxMemory,
						freeMemory, usedRate});
			} else {
				log.debug("由于JVM内存使用率低于{}%，此次不执行清理。", threoldPercent);
			}

			try {
				Thread.sleep(periodMills);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void setPeriodMills(long periodMills) {
		this.periodMills = periodMills;
	}

	public void setThreoldPercent(double threoldPercent) {
		this.threoldPercent = threoldPercent;
	}
}
