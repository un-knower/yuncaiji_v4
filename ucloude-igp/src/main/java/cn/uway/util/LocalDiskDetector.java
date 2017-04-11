package cn.uway.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.warehouse.exporter.template.ExportTargetTempletContext;
import cn.uway.framework.warehouse.exporter.template.FileExporterBean;

/**
 * 本地磁盘空间检测.会对export_config.xml中配置的输出路径进行空间检查<br>
 * 注：只会检查输出节点中已打开的节点<br>
 * 
 * @author chenrongqiang @ 2014-2-6
 */
public class LocalDiskDetector {

	/**
	 * 最小的空闲磁盘空间
	 */
	private Integer minFreeDisk = AppContext.getBean("minFreeDisk", Integer.class);

	private static LocalDiskDetector instance = new LocalDiskDetector();

	private LocalDiskDetector() {
		super();
	}

	public static LocalDiskDetector getInstance() {
		return instance;
	}

	/**
	 * 磁盘空间检测<br>
	 * 此操作会进行线程同步.检测已配置的输出目的地磁盘空间是否足够<br>
	 * 一旦磁盘空间不足,则线程锁不会释放,直到磁盘空间有闲余为止<br>
	 */
	public synchronized void detect() {
		// 如果为0，则表示不检测
		if (minFreeDisk == 0)
			return;
		// 重新创建一个集合,访问并发修改
		List<FileExporterBean> localExports = new LinkedList<FileExporterBean>(ExportTargetTempletContext.getInstance().getFileExportBeans());
		if (localExports == null || localExports.isEmpty())
			return;
		// 已检测过的磁盘路径临时缓存
		Set<String> checkedPaths = new HashSet<String>();
		Iterator<FileExporterBean> iterator = localExports.iterator();
		while (iterator.hasNext()) {
			String path = getPath(iterator.next());
			if (checkedPaths.contains(path))
				continue;
			File local = new File(path);
			// 目录不存在，则表示目前没有使用，不执行检查
			if (!local.exists())
				continue;
			// 没检查过的则放入到临时缓存中
			checkedPaths.add(path);
			// 如果磁盘空间检测不通过，则线程一直挂起
			while (!usable(local)) {
				try {
					// 10秒检测一次磁盘空间
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * 检查是否有冗余空间
	 * 
	 * @param local
	 *            配置的本地输出磁盘目录
	 * @return 磁盘空间是否还有冗余
	 */
	private boolean usable(File local) {
		long usable = local.getUsableSpace();
		return usable / 1024 / 1024 > minFreeDisk;
	}

	private static String getPath(FileExporterBean exportTargetBean) {
		String path = exportTargetBean.getPath();
		if (path.contains("%"))
			path = path.substring(0, path.indexOf("%"));
		int index = path.indexOf("/");
		return index < 0 ? path : path.substring(0, index);
	}

	public static void main(String[] args) throws IOException {
		new LocalDiskDetector().detect();
	}

}
