package cn.uway.util.compression;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于加载7-ZIP动态库。
 * 
 * @author chensijiang 2014-8-20
 */
final class SevenZipLoader {

	/** 日志记录器。 */
	private static final Logger LOG = LoggerFactory.getLogger(SevenZipLoader.class);

	/** 是否加载成功。 */
	private static boolean loadSuc;

	/**
	 * 加载7-ZIP库。
	 * 
	 * @return 是否加载成功。
	 */
	public synchronized static final boolean load() {
		if (loadSuc)
			return loadSuc;
		try {
			SevenZip.initSevenZipFromPlatformJAR();
			loadSuc = true;
			LOG.debug("7-ZIP解压库补始化成功，平台：{}", SevenZip.getUsedPlatform());
		} catch (SevenZipNativeInitializationException e) {
			LOG.error("7-ZIP解压库补始化失败。", e);
			loadSuc = false;
		}

		return loadSuc;
	}
}
