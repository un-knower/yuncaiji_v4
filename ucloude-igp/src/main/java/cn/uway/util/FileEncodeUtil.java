package cn.uway.util;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 代码页探测器
 * 需添加jar包：antlr-2.7.4.jar、chardet-1.0.jar、jargs-1.0.jar、cpdetector_1.0.10.jar
 * 
 * @author sunt
 *
 */
public class FileEncodeUtil {
	/** 探测器代理 **/
	private static CodepageDetectorProxy detector;

	/**
	 * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
	 * 
	 * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法
	 * 
	 * 加进来，如ParsingDetector、 JChardetFacade、ASCIIDetector、UnicodeDetector。
	 * 
	 * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
	 * 
	 * 字符集编码。
	 */
	static {
		detector = CodepageDetectorProxy.getInstance();
		// JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码测定。
		// 所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以再多加几个探测器，
		// 比如下面的ASCIIDetector、UnicodeDetector等。
		detector.add(JChardetFacade.getInstance());
		// ASCIIDetector用于ASCII编码测定
		detector.add(ASCIIDetector.getInstance());
		// UnicodeDetector用于Unicode家族编码的测定
		detector.add(UnicodeDetector.getInstance());
	}

	/**
	 * 探测
	 * 
	 * @param file
	 * @return charset
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Charset detector(File file) throws MalformedURLException,
			IOException {
		return detector.detectCodepage(file.toURI().toURL());
	}

	/**
	 * 探测
	 * 
	 * @param url
	 * @return charset
	 * @throws IOException
	 */
	public static Charset detector(URL url) throws IOException {
		return detector.detectCodepage(url);
	}

	/**
	 * 探测
	 * 
	 * @param inputStream
	 * @param length
	 *            长度应小于流的可用长度
	 * @return charset
	 * @throws IOException
	 */
	public static Charset detector(InputStream inputStream, int length)
			throws IOException {
		return detector.detectCodepage(inputStream, length);
	}
}
