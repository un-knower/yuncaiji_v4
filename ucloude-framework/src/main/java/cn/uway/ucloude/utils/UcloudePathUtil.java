package cn.uway.ucloude.utils;

import java.io.File;

import org.apache.commons.lang.StringUtils;


public class UcloudePathUtil {
	private static String rootPath = null;
	
	public static String getRootPath() {
		if (rootPath == null) {
			synchronized (UcloudePathUtil.class) {
				String path = System.getProperty("UTS_IGP_ROOT_PATH", "./");
				if (StringUtils.isEmpty(path))
					path = "./";
				
				File file = new File(path);
				if (!file.exists() || !file.isDirectory()) {
					file.mkdirs();
				}
				
				rootPath = path;
				
				if (!rootPath.endsWith("/"))
					rootPath += "/";
			}
		}
		
		return rootPath;
	}
	
	public static String makePath(String filePath) {
		if (filePath.startsWith(getRootPath()))
			return filePath;
		
		return getRootPath() + filePath;
	}
	
	public static String makeIgpConfPath(String filePath) {
		if (filePath.startsWith(getRootPath()))
			return filePath;
		
		return getRootPath() + "conf/igp/" + filePath;
	}
	
	public static String makeIgpTemplatePath(String filePath) {
		if (filePath.startsWith(getRootPath()))
			return filePath;
		
		return getRootPath() + "conf/igp/template/" + filePath;
	}
}
