package cn.uway.ucloude.uts.jvmmonitor.mbean;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.MathContext;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class JVMThread implements JVMThreadMBean {

	private final static ILogger LOGGER = LoggerManager.getLogger(JVMThread.class);

    private volatile long lastCPUTime;
    private volatile long lastCPUUpTime;
	private OperatingSystemMXBean OperatingSystem;
    private RuntimeMXBean Runtime;

    private static final JVMThread instance = new JVMThread();

    public static JVMThread getInstance() {
        return instance;
    }

    private ThreadMXBean threadMXBean;

	private JVMThread() {
        threadMXBean = ManagementFactory.getThreadMXBean();
        OperatingSystem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Runtime = ManagementFactory.getRuntimeMXBean();

        try {
            lastCPUTime = OperatingSystem.getProcessCpuTime();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getProcessCpuTimeRate() {
		long cpuTime = OperatingSystem.getProcessCpuTime();
        long upTime = Runtime.getUptime();

        long elapsedCpu = cpuTime - lastCPUTime;
        long elapsedTime = upTime - lastCPUUpTime;

        lastCPUTime = cpuTime;
        lastCPUUpTime = upTime;

        BigDecimal cpuRate;
        if (elapsedTime <= 0) {
            return new BigDecimal(0);
        }

        float cpuUsage = elapsedCpu / (elapsedTime * 10000F);
        cpuRate = new BigDecimal(cpuUsage, new MathContext(4));

        return cpuRate;
    }

    @Override
    public int getDaemonThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }

    @Override
    public int getThreadCount() {
        return threadMXBean.getThreadCount();
    }

    @Override
    public long getTotalStartedThreadCount() {
        return threadMXBean.getTotalStartedThreadCount();
    }

    @Override
    public int getDeadLockedThreadCount() {
        try {
            long[] deadLockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadLockedThreadIds == null) {
                return 0;
            }
            return deadLockedThreadIds.length;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


}
