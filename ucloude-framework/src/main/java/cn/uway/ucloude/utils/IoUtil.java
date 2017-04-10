package cn.uway.ucloude.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.omg.CORBA_2_3.portable.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IO工具类。
 * 
 * @author ChenSijiang 2012-10-28
 */
public final class IoUtil {

	private static final Logger log = LoggerFactory.getLogger(IoUtil.class);

	/**
	 * 针对<code>java.io.Closeable</code>接口的关闭，忽略IO异常。
	 * 
	 * @param io
	 *            <code>java.io.Closeable</code>接口。
	 */
	public static void closeQuietly(Closeable io) {
		if (io == null)
			return;
		try {
			io.close();
		} catch (IOException e) {
			log.warn("关闭IO时发生了异常。", e);
		}
	}

	/**
	 * 将流的数据全部读完。
	 * 
	 * @param in
	 *            流。
	 * @return 是否出现了异常。
	 * @throws NullPointerException
	 *             流为<code>null</code>时。
	 */
	public static boolean readFinish(InputStream in) {
		if (in == null)
			throw new NullPointerException("in");
		try {
			byte[] buff = new byte[1024];
			while (in.read(buff) > -1) {
			}
		} catch (Exception e) {
			// log.warn("[readFinish]读取InputStream异常。", e);
			return false;
		}
		return true;
	}

	private IoUtil() {
	}
	
	public static String readContentFromReader(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ( (line = reader.readLine()) != null ) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}
	
	public static String readContentFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] datas = new byte[1024];
		int len = 0;
		while ( (len = is.read(datas, 0, datas.length)) != -1 ) {
			baos.write(datas, 0, len);
		}
		return baos.toString();
	}
	
	public static byte[] readBytesFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] datas = new byte[1024];
		int len = 0;
		while ( (len = is.read(datas, 0, datas.length)) != -1 ) {
			baos.write(datas, 0, len);
		}
		return baos.toByteArray();
	}
	
	public static void closeInputStream(InputStream is) {
		try {
			if ( is != null ) {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeOuputStream(OutputStream os) {
		try {
			if ( os != null ) {
				os.flush();
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
