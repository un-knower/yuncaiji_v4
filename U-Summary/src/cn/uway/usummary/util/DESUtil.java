package cn.uway.usummary.util;



import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import cn.uway.usummary.context.AppContext;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class DESUtil {
	
	// 加密算法的参数接口，IvParameterSpec是它的一个实现
	private static AlgorithmParameterSpec iv = null;
	
	private static Key key = null;

	static  {
		try {
			// 获取秘钥"UWAY@SOF".getBytes();
			byte[] DESkey = AppContext.getBean("systemDesKey", String.class).getBytes("UTF-8");
			
			// 设置密钥参数
			DESKeySpec keySpec = new DESKeySpec(DESkey);
			
			// 设置向量
			byte[] DESIV = new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
			iv = new IvParameterSpec(DESIV);
			
			// 获得密钥工厂
		    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		    
		    // 得到密钥对象
		    key = keyFactory.generateSecret(keySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
	}        

	   public static String encode(String data) throws Exception {
		   // 得到加密对象Cipher
	       Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	       
	       // 设置工作模式为加密模式，给出密钥和向量
	       enCipher.init(Cipher.ENCRYPT_MODE, key, iv);
	       byte[] pasByte = enCipher.doFinal(data.getBytes("UTF-8"));
	       BASE64Encoder base64Encoder = new BASE64Encoder();
	       return base64Encoder.encode(pasByte);
	   }

	   public static String decode(String data) throws Exception {
	       Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	       deCipher.init(Cipher.DECRYPT_MODE, key, iv);
	       BASE64Decoder base64Decoder = new BASE64Decoder();
	       byte[] pasByte = deCipher.doFinal(base64Decoder.decodeBuffer(data));

	       return new String(pasByte, "UTF-8");
	   }
	   
	   
	   public static void main(String[] args) {
	       try {
	          
//	           DESUtil des = new DESUtil();//自定义密钥
//	           System.out.println("解密后的字符："+decode("fgkXyrEAGqCAvzvp2XTTUA=="));
//	           System.out.println(encode("js_bigdata"));
	           System.out.println(encode("ftpuser"));
	           System.out.println(encode("uwaysoft2009"));
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
	   }
}
