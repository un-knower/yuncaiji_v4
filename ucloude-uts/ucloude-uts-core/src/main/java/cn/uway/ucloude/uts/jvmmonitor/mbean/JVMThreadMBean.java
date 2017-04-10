package cn.uway.ucloude.uts.jvmmonitor.mbean;

import java.math.BigDecimal;

public interface JVMThreadMBean {
	int getDaemonThreadCount();

    int getThreadCount();

    long getTotalStartedThreadCount();

    int getDeadLockedThreadCount();

    BigDecimal getProcessCpuTimeRate();
}
