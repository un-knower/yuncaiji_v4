package cn.uway.util.hdfs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * HDFS工具类
 * 
 */
public class HdfsUtils {
	private static ILogger LOG = LoggerManager.getLogger(HdfsUtils.class);

	/**
	 * 构造函数声明为私有类型，防止实例化
	 */
	private HdfsUtils() {
	}

	/**
	 * 判断HDFS文件是否存在
	 * 
	 * @param hdfs
	 *            HDFS文件系统
	 * @param path
	 *            路径
	 * @return 如果存在返回true，否则返回false
	 */
	public static boolean isValidFile(final FileSystem hdfs, final String path) {
		try {
			FileStatus[] status = hdfs.listStatus(new Path(path));
			// 可用文件的条件：文件状态非空，不是目录，并且快大小大于0
			return (status.length == 1 && !status[0].isDirectory() && status[0]
					.getBlockSize() > 0);
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean isValidDir(final FileSystem hdfs, final String path) {
		try {
			return hdfs.isDirectory(new Path(path));
			// FileStatus[] status = hdfs.listStatus(new Path(path));
			// return (status.length > 0 && status[0].isDirectory());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取文件系统
	 * 
	 * @param conf
	 * @return
	 */
	public static DistributedFileSystem getFileSystem(Configuration conf) {
		try {
			return (DistributedFileSystem) FileSystem.get(conf);
		} catch (IOException e) {
			LOG.error("getFileSystem: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 判断本地文件是否存在
	 * 
	 * @param fileName
	 *            本地文件名
	 * @return 存在返回true，否则返回false
	 */
	public static boolean isFileExist(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	/**
	 * 列出HDFS上的目录信息
	 * 
	 * @param path
	 *            路径
	 * @return
	 */
	public static Map<String, Long> lsDir(FileSystem fs, String path) {
		Map<String, Long> pathInfo = new HashMap<String, Long>();
		try {
			Path dfsPath = new Path(path);
			if (!fs.exists(dfsPath))
				return pathInfo;
			FileStatus[] fileStatus = fs.listStatus(dfsPath);
			for (FileStatus fileStat : fileStatus) {
				pathInfo.put(fileStat.getPath().getName(), fileStat.getLen());
			}
		} catch (IOException e) {
			;
		}

		return pathInfo;
	}

}
