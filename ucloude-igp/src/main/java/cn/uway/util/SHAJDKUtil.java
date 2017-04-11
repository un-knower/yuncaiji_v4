package cn.uway.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * http://jingyan.baidu.com/article/2fb0ba40a2ef2b00f3ec5f74.html<br>
 * SHA 加密工具<br>
 * 2016年11月25日 10:02
 */
public class SHAJDKUtil {

	public static final String SHAUtilAGORITHM_1 = "SHA";

	public static final String SHAUtilAGORITHM_256 = "SHA-256";

	public static final String SHAUtilAGORITHM_384 = "SHA-384";

	public static final String SHAUtilAGORITHM_512 = "SHA-512";

	public static String convertByteToHexStr(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		String tempHex = "";
		for (int i = 0; i < bytes.length; i++) {
			tempHex = Integer.toHexString(bytes[i] & 0xff);
			if (tempHex.length() < 2) {
				result.append("0").append(tempHex);
			} else {
				result.append(tempHex);
			}
		}
		return result.toString();
	}

	/**
	 * jdk 自带的类实现
	 * 
	 * @param message
	 *            消息
	 * @param algorithmStr
	 *            摘要算法
	 * @throws NoSuchAlgorithmException
	 */
	public static String jdksha(String message, String algorithmStr) throws NoSuchAlgorithmException {
		String algorithmString = "SHA-384";
		if (!StringUtil.isEmpty(algorithmStr)) {
			algorithmString = algorithmStr;
		}
		String shaEncod = "";
		try {
			MessageDigest sha384Digest = MessageDigest.getInstance(algorithmString);
			byte[] sha1Encode = sha384Digest.digest(message.getBytes());
			shaEncod = convertByteToHexStr(sha1Encode);
		} catch (NoSuchAlgorithmException e) {
			throw new NoSuchAlgorithmException("没有这种算法 ： MessageDigest.getInstance(" + algorithmStr + ")");
		}
		return shaEncod;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {
		String message = "18011112222";
		// String message = "最后我们在main方法内调用加密方法。对以上编写的加密方法进行测试。" + "首先我们定义要加密的字符串为：jdksha，" + "然后分别调用以上编写的不同加密方式的加密方法，具体实现方式如下图所示。";
		System.out.println(jdksha(message, SHAUtilAGORITHM_1));
	}
}
