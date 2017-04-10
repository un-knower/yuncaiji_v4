package cn.uway.usummary.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FTP工具。
 * 
 * @author ChenSijiang 2012-10-30
 */
public final class FTPUtil {

	private static final Logger log = LoggerFactory.getLogger(FTPUtil.class);

	private static FTPFileComparator ftpFileComparator = new FTPFileComparator();

	public static FTPFileComparator getFTPFileComparator() {
		return ftpFileComparator;
	}

	/**
	 * 登出并关闭FTP连接。
	 * 
	 * @param ftp
	 *            FTP连接。
	 * @return 未正常登出或关闭。
	 */
	public static boolean logoutAndCloseFTPClient(FTPClient ftp) {
		if (ftp == null) {
			log.warn("传入的FTPClient为null.");
			return false;
		}

		boolean bOk = true;

		try {
			if (!ftp.logout()) {
				log.warn("FTP登出返回false，reply={}", ftp.getReplyString());
				bOk = false;
			}
		} catch (IOException exLogout) {
			bOk = false;
			log.warn("FTP登出发生异常。", exLogout);
		} finally {
			try {
				ftp.disconnect();
			} catch (IOException exClose) {
				bOk = false;
				log.warn("FTP断开发生异常。", exClose);
			}
		}
		return bOk;
	}

	/**
	 * 根据采集路径，在FTP服务器上遍历出所有文件路径
	 * 
	 * @param gatherPath
	 * @param ftp
	 * @param level
	 *            分析到采集目录的第几层
	 * @return 返回本地编码的文件路径列表，下载的时候需要encode成ftp编码
	 */
	@Deprecated
	/*
	 * public static List<String> extractDirPath(String gatherPath, String charset, FTPClient ftp, int level) { log.debug("开始分析下载路径"); // 去掉结尾分隔符 if
	 * (gatherPath.endsWith("/")) { gatherPath = gatherPath.substring(0, gatherPath.length() - 1); } if (!gatherPath.startsWith("/")) { gatherPath =
	 * "/" + gatherPath; } List<String> dirList = new ArrayList<String>(); if (!gatherPath.contains("*")) { dirList.add(gatherPath); return dirList; }
	 * String[] splitPath = gatherPath.split("/"); dirList.add("/"); String originalPath = ""; if (level >= splitPath.length) { level =
	 * splitPath.length - 1; } for (int i = 1; i <= level; i++) { List<String> tempDirList = new LinkedList<String>(); String subDir = splitPath[i];
	 * originalPath += "/" + subDir; log.debug("开始分析下载第" + i + "层路径:" + originalPath); if (subDir == null || subDir.trim().isEmpty()) { continue; }
	 * for (String dir : dirList) { String currentPath = dir; if (dir.equals("/")) { currentPath = ""; } if (subDir.contains("*")) { String encodeDir
	 * = StringUtil.encodeFTPPath(currentPath + "/" + subDir, charset); if (subDir.equals("*")) { encodeDir = StringUtil.encodeFTPPath(currentPath,
	 * charset); } List<FTPFile> ftpDirList = null; if (i == splitPath.length - 1) { ftpDirList = FTPUtil.listFiles(ftp, encodeDir, charset); for
	 * (FTPFile file : ftpDirList) { currentPath = StringUtil.decodeFTPPath(file.getName(), charset); tempDirList.add(currentPath); log.debug("扫描到第" +
	 * i + "层路径:" + currentPath); } } else { ftpDirList = listDirectories(ftp, encodeDir); if (!subDir.equals("*")) { encodeDir =
	 * encodeDir.substring(0, encodeDir.lastIndexOf("/")); } for (FTPFile file : ftpDirList) { currentPath = encodeDir + "/" +
	 * FilenameUtils.getName(file.getName()); currentPath = StringUtil.decodeFTPPath(currentPath, charset); tempDirList.add(currentPath);
	 * log.debug("扫描到第" + i + "层路径:" + currentPath); } }
	 * 
	 * } else { currentPath += "/" + subDir; tempDirList.add(currentPath); log.debug("扫描到第" + i + "层路径:" + currentPath); } } dirList = tempDirList; }
	 * return dirList; }
	 */
	/**
	 * FTP执行LIST命令，获取文件列表。如果失败，将返回<code>null</code>.
	 * 
	 * @param ftp
	 *            FTP连接。
	 * @param path
	 *            路径。
	 * @return 文件列表。
	 */
	public static List<FTPFile> listDirectories(FTPClient ftp, String path) {
		if (ftp == null) {
			log.warn("ftp为null.");
			return null;
		}
		if (path == null) {
			log.warn("path为null.");
			return null;
		}

		FTPFile[] ftpFiles = null;

		try {
			ftpFiles = ftp.listDirectories(path);
		} catch (IOException e) {
			// 异常时，返回null，告知调用者listFiles失败，有可能是网络原因，可重试。
			log.warn("FTP listDirectories时发生异常。", e);
			return null;
		}

		// listFiles返回null或长度为0时，可认为确实无文件，即使重试，也是一样。
		// 所以此处正常返回，即返回一个长度为0的List.
		if (ftpFiles == null || ftpFiles.length == 0)
			return Collections.emptyList();

		// 正常化文件列表， 做四个处理：
		// 1、为null的FTPFile对象消除；
		// 2、文件名为null的FTP对象清除；
		// 3、文件名改名绝对路径；
		// 4、如果不是文件，跳过。
		List<FTPFile> list = new ArrayList<FTPFile>();
		for (FTPFile ff : ftpFiles) {
			if (ff == null || ff.getName() == null || ff.getName().trim().isEmpty() || !ff.isDirectory())
				continue;
			String filename = FilenameUtils.getName(ff.getName());
			String dir = FilenameUtils.getFullPath(path);
			ff.setName(dir + filename);
			list.add(ff);
		}

		Collections.sort(list, getFTPFileComparator());
		return list;
	}

	/**
	 * 根据采集路径，在FTP服务器上遍历出所有文件路径
	 * 
	 * @param gatherPath
	 * @param ftp
	 * @return 返回本地编码的文件路径列表，下载的时候需要encode成ftp编码
	 */
	public static List<String> extractDirPath(String gatherPath, String charset, FTPClient ftp) {
		gatherPath = gatherPath.replaceAll("[\\\\]", "/");
		if (!gatherPath.startsWith("/")) {
			gatherPath = "/" + gatherPath;
		}
		if (!gatherPath.endsWith("/")) {
			gatherPath = gatherPath.substring(0, gatherPath.lastIndexOf('/') + 1);
		}

		List<String> dirList = new ArrayList<String>();
		if (!gatherPath.contains("*")) {
			try {
				String encodePath = StringUtil.encodeFTPPath(gatherPath, charset);
				if (ftp.changeWorkingDirectory(encodePath)) {
					log.debug(">>扫描到符合通匹符规则的子文件夹：{}", gatherPath);
					dirList.add(gatherPath);
				}
			} catch (IOException e) {
			}

			return dirList;
		}

		String[] splitPath = gatherPath.split("/");
		String path = "";

		// 循环从第2个开始，因为第一个是/,
		for (int i = 1; i < splitPath.length; ++i) {
			String subDir = splitPath[i];
			if (subDir == null || subDir.trim().isEmpty()) {
				continue;
			}

			if (subDir.contains("*")) {
				boolean bNeedMatch = false;
				String encodeDir = path + "/";
				String matchPath = null;
				if (!subDir.equals("*")) {
					// encodeDir = path + "/" + subDir;
					matchPath = path + "/" + subDir;
					bNeedMatch = true;
				}

				// log.debug("开始在FTP路径：\"{}\" 下查找子件夹...", encodeDir);
				encodeDir = StringUtil.encodeFTPPath(encodeDir, charset);
				List<FTPFile> ftpDirList = listDirectories(ftp, encodeDir);
				if (ftpDirList != null) {
					for (FTPFile ftpDir : ftpDirList) {
						String dirName = StringUtil.decodeFTPPath(ftpDir.getName(), charset);
						String subPath = dirName;
						for (int j = i + 1; j < splitPath.length; ++j) {
							subPath += "/";
							subPath += splitPath[j];
						}
						subPath += "/";
						if (!bNeedMatch || wildCardMatch(matchPath, subPath, "*")) {
							dirList.addAll(extractDirPath(subPath, charset, ftp));
						}
					}
				}
				// 碰到有通匹符的，直接通过递归将扫描到的目录加入到tempDirList中，后面的目录就不需要再去管了，由递归去完成。
				return dirList;
			} else {
				path += "/" + subDir;
			}
		}

		// 实际应用中，理论上代码不会执行到此处
		if (path.length() < 1)
			path = "/";

		try {
			if (ftp.changeWorkingDirectory(path)) {
				log.debug(">>扫描到符合通匹符规则的子文件夹：{}", path);
				dirList.add(path);
			}
		} catch (IOException e) {
		}

		return dirList;
		// int maxLevel = getPathMaxLevel(gatherPath);
		// return extractDirPath(gatherPath, charset, ftp, maxLevel);
	}

	/**
	 * 计算路径的最大层级数
	 * 
	 * @param path
	 * @return
	 */
	public static int getPathMaxLevel(String gatherPath) {
		if (gatherPath.endsWith("/")) {
			gatherPath = gatherPath.substring(0, gatherPath.length() - 1);
		}
		if (!gatherPath.startsWith("/")) {
			gatherPath = "/" + gatherPath;
		}
		String[] split = gatherPath.split("/");
		return split.length - 1;
	}

	/**
	 * FTP执行LIST命令，递归的获取文件列表。如果失败，将返回<code>null</code>. 注意，之支持一级目录有星号的递归。
	 * 
	 * @param ftp
	 *            FTP连接。
	 * @param path
	 *            路径。
	 * @return 文件列表。
	 * @throws IOException
	 *             操作失败。
	 */
	public static List<FTPFile> listFilesRecursive_(FTPClient ftp, String path, String ftpCharset) throws IOException {
		// 目录无通配符的情况，直接返回。
		String parentPath = FilenameUtils.getFullPath(path);
		if (!parentPath.contains("*") && !parentPath.contains("?"))
			return listFiles(ftp, path, ftpCharset);

		String[] spPath = path.split("/");
		String namePart = FilenameUtils.getName(path);
		String wildDir = "";

		List<String> parsedDirs = new ArrayList<String>();
		String currFullDir = "/";
		for (int i = 0; i < spPath.length; i++) {
			String dir = spPath[i];
			if (dir == null || dir.trim().isEmpty() || dir.equals(namePart))
				continue;
			if (dir.contains("*") || dir.contains("?")) {
				wildDir = dir;
				FTPFile[] dirs = ftp.listDirectories(currFullDir + "/" + wildDir);
				for (FTPFile ftpDir : dirs) {
					if (ftpDir == null || ftpDir.getName() == null)
						continue;
					if (FilenameUtils.wildcardMatch(ftpDir.getName(), dir))
						parsedDirs.add(ftpDir.getName());
				}
				break;
			} else {
				currFullDir += (dir + "/");
			}
		}
		
		List<FTPFile> files = new ArrayList<FTPFile>();

		for (int i = 0; i < parsedDirs.size(); i++) {
			if (parsedDirs.get(i) == null)
				continue;

			String oneDir = path.replace("/" + wildDir + "/", "/" + parsedDirs.get(i) + "/");
			List<FTPFile> tmp = listFilesRecursive(ftp, oneDir, ftpCharset);
			if (tmp != null)
				files.addAll(tmp);
		}

		Collections.sort(files, getFTPFileComparator());

		return files;
	}

	/**
	 * FTP执行LIST命令，递归的获取文件列表。如果失败，将返回<code>null</code>. 注意，之支持一级目录有星号的递归。
	 * 
	 * @param ftp
	 *            FTP连接。
	 * @param path
	 *            路径。
	 * @return 文件列表。
	 * @throws IOException
	 *             操作失败。
	 */
	public static List<FTPFile> listFilesRecursive(FTPClient ftp, String path, String ftpCharset) throws IOException {
		// 目录无通配符的情况，直接返回。
		String parentPath = FilenameUtils.getFullPath(path);
		if (!parentPath.contains("*") && !parentPath.contains("?"))
			return listFiles(ftp, path, ftpCharset);

		String fileExt = path.substring(parentPath.length());
		List<String> parsedDirs = extractDirPath(parentPath, ftpCharset, ftp);
		log.debug("本次一共搜索到{}个符合通匹符规则:\"{}\"的子文件夹，igp将开始逐一在这些子文件夹下，查找符合规则的所有文件。 fileExt=\"{}\"", new Object[]{parsedDirs.size(), parentPath, fileExt});

		List<FTPFile> files = new ArrayList<FTPFile>();
		for (int i = 0; i < parsedDirs.size(); i++) {
			String subPath = parsedDirs.get(i);
			if (!subPath.endsWith("/"))
				subPath += "/";
			subPath += fileExt;

			List<FTPFile> tmp = listFilesRecursive_(ftp, subPath, ftpCharset);
			if (tmp != null)
				files.addAll(tmp);
		}
		Collections.sort(files, getFTPFileComparator());
		return files;
	}

	/**
	 * @param ftp
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static List<FTPFile> listFilesRecursive(FTPClient ftp, String path) throws IOException {
		return listFilesRecursive_(ftp, path, "");
	}

	/**
	 * FTP执行LIST命令，获取文件列表。如果失败，将返回<code>null</code>.
	 * 
	 * @param ftp
	 *            FTP连接。
	 * @param path
	 *            路径。
	 * @return 文件列表。
	 */
	public static List<FTPFile> listFiles(FTPClient ftp, String path, String ftpCharset) throws IOException {
		if (ftp == null) {
			log.warn("ftp为null.");
			return null;
		}
		if (path == null) {
			log.warn("path为null.");
			return null;
		}
		
		// 对于有包含双个通匹符的，采用本地匹配文件扩展名
		String fileMatcher = null;
		int nLastFileSperator = path.lastIndexOf("/");
		if (nLastFileSperator > 0 && (path.contains("**") || path.contains("??"))) {
			fileMatcher = path.substring(nLastFileSperator+1);
			fileMatcher = fileMatcher.replace("**", "*");
			fileMatcher = fileMatcher.replace("??", "?");
			path = path.substring(0, nLastFileSperator+1);
			
			log.debug("采用本地匹配文件的扩展名.");
		}

		FTPFile[] ftpFiles = null;
		String encodePath = null;
		encodePath = StringUtil.encodeFTPPath(path, ftpCharset);
		ftpFiles = ftp.listFiles(encodePath);

		// listFiles返回null或长度为0时，可认为确实无文件，即使重试，也是一样。
		// 所以此处正常返回，即返回一个长度为0的List.
		if (ftpFiles == null || ftpFiles.length == 0)
			return Collections.emptyList();
		
		// 正常化文件列表， 做四个处理：
		// 1、为null的FTPFile对象消除；
		// 2、文件名为null的FTP对象清除；
		// 3、文件名改名绝对路径；
		// 4、如果不是文件，跳过。
		List<FTPFile> list = new ArrayList<FTPFile>();
		for (FTPFile ff : ftpFiles) {
			/**
			 * 有ftp服务器上的文件是连接，此时仍需要下载
			 */
			if (ff == null || ff.getName() == null || ff.getName().trim().isEmpty() 
					|| (!ff.isFile() && !ff.isSymbolicLink())) {
				//log.debug("ignore file:{} isfile:{} isSymbolicLink:{} type:{}", new Object[]{ff.getName(), ff.isFile(), ff.isSymbolicLink(), ff.getType()});
				continue;
			}
			
			String dir = FilenameUtils.getFullPath(encodePath);
			if (dir.contains("*") || dir.contains("?")) {
				// 例如WCDMA爱立信性能，LIST使用的路径，目录部分是有*号的。
				// 直接用LIST结果里的文件名就行了，它带有目录。
				list.add(ff);
			} else {
				// 为了不影响后续业务处理，做最小改动，此处仅仅避免getName时因文件名中的“\”而错误截位。
//				log.debug("FilenameUtils.getName:ff.toString:"+ff.toString());
//				log.debug("FilenameUtils.getName:before:"+ff.getName());
				String filename = FilenameUtils.getName(StringUtil.decodeFTPPath(ff.getName(), ftpCharset));
//				log.debug("FilenameUtils.getName:after:"+filename);
				ff.setName(dir + StringUtil.encodeFTPPath(filename, ftpCharset));
				
				// 对文件扩展名通匹符进行匹配
				if (fileMatcher != null) {
					if (! FilenameUtils.wildcardMatch(ff.getName(), fileMatcher)) {
						//log.debug("ignore file:{} fileMatcher:{}", new Object[]{ff.getName(),fileMatcher});
						continue;
					}
				}
				
				list.add(ff);
			}
		}
		
		Collections.sort(list, getFTPFileComparator());
		return list;
	}

	/**
	 * FTP 下载过程，包括重试。
	 */
	public static InputStream download(String ftpPath, FTPClient ftpClient) {
		InputStream in = retrNoEx(ftpPath, ftpClient);
		if (in != null) {
			return in;
		}
		log.warn("FTP下载失败，开始重试，文件：{}，reply={}", new Object[]{ftpPath, ftpClient.getReplyString() != null ? ftpClient.getReplyString().trim() : ""});
		for (int i = 0; i < 3; i++) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				log.warn("FTP 下载重试过程中线程被中断。", e);
				return null;
			}
			log.debug("第{}次重试下载。", i + 1);
			completePendingCommandNoEx(ftpClient);
			in = retrNoEx(ftpPath, ftpClient);
			if (in != null) {
				log.debug("第{}次重试下载成功。", i + 1);
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
	 * @return
	 */
	public static InputStream retrNoEx(String ftpPath, FTPClient ftpClient) {
		InputStream in = null;
		try {
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			in = ftpClient.retrieveFileStream(ftpPath);
		} catch (IOException e) {
			log.error("FTP下载异常：" + ftpPath, e);
		}

		return in;
	}

	/**
	 * 从FTP读取了流之后，需要读取FTP响应消息，否则下次操作时将会失败
	 */
	public static boolean completePendingCommandNoEx(FTPClient ftpClient) {
		boolean b = true;
		try {
			/**
			 * 这里要判断服务器端口是否已经响应完成，如果是，不能再调用completePendingCommand，否则阻塞，直至超时。
			 * 
			 * @author yuy <br>
			 *         2014.11.14
			 */
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()) && !FTPReply.isNegativePermanent(ftpClient.getReplyCode())) {
				b = ftpClient.completePendingCommand();
			} else {
				log.warn("FTP已经响应：{}", ftpClient.getReplyString());
			}
			if (!b)
				log.warn("FTP失败响应：{}", ftpClient.getReplyString());
		} catch (Exception e) {
			log.error("获取FTP响应异常。", e);
			return false;
		}

		return b;
	}

	/**
	 * FTP服务器linux默认FTP，windows默认GBK<br>
	 * 通过FEAT命令查看是否支持UTF8模式，如果支持则设置发送OPTS UTF8 ON命令，并返回这只UTF-8编码集<br>
	 * 如果不支持UTF8模式，则查看FTP服务器的系统类型<br>
	 * 如果是WINDOWS则默认返回GBK<br>
	 * 如果不是windows则默认返回UTF-8
	 * 
	 * @author Niow 2014-7-8
	 * 
	 * @param ftp
	 *            登陆后的FTPClient
	 * @return 服务端编码集
	 * @throws IOException
	 */
	public static String autoSetCharset(FTPClient ftp) throws IOException {
		ftp.feat();
		String replay = ftp.getReplyString();
		if (replay.toUpperCase().contains("UTF8")) {
			ftp.sendCommand("OPTS UTF8", "ON");
			return "UTF-8";
		}
		ftp.sendCommand("SYST");
		replay = ftp.getReplyString();
		if (replay.toUpperCase().contains("WINDOWS")) {
			return "GBK";
		}
		return "UTF-8";
	}

	private FTPUtil() {
		super();
	}

	private static class FTPFileComparator implements Comparator<FTPFile> {

		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;
			String name1 = (o1.getName() != null ? o1.getName() : "");
			String name2 = (o2.getName() != null ? o2.getName() : "");
			return name1.compareTo(name2);
		}
	}

	/**
	 * 通配符匹配
	 * 
	 * @param src
	 *            带通配符的字符串
	 * @param dest
	 *            不带通配符的字符串
	 * @param wildCard
	 *            通配符
	 * @return
	 */
	private static boolean wildCardMatch(String src, String dest, String wildCard) {
		String[] fieldName = StringUtil.split(src, wildCard);
		int start = -1;
		boolean flag = true;
		for (int n = 0; n < fieldName.length; n++) {
			if ("".equals(fieldName[n]))
				continue;
			int index = dest.indexOf(fieldName[n]);
			if (index > start) {
				start = index;
				continue;
			} else {
				flag = false;
				break;
			}
		}
		return flag;
	}
	/**
	 * 根据指定的ftp路径查询ftp文件列表，以文件名的日期做自然排序后取日期最大的文件名
	 * @param ftp
	 *            FTPClient 对象
	 * @param path
	 *            ftp上的文件的路径
	 * @param order
	 *            是正序(文件名中日期的自然顺序，按日期从小到大)，还是倒序(按日期从大到小)<b>
	 *            要正确书写为 "desc" 才倒序,不是真正意义的倒序
	 * @return filePath
	 * */
	public static String getOrderTopFile(FTPClient ftp, String path,String order) throws IOException {
		List<FTPFile> remoteFiles = FTPUtil.listFilesRecursive(ftp, path);
		if (remoteFiles == null || remoteFiles.isEmpty()) {
			log.warn("服务器上无匹配文件,remotePath= " + path);
		}
		List<String> remoteFileName = new ArrayList<String>();
		for (FTPFile ftpFile : remoteFiles) {
			remoteFileName.add(ftpFile.getName());
		}
		Collections.sort(remoteFileName);
		if (!remoteFileName.isEmpty()){
			if(!"desc".equalsIgnoreCase(order)){
				return remoteFileName.get(remoteFileName.size() - 1);
			}
			return remoteFileName.get(0);
		}
		return null;
	}
}
