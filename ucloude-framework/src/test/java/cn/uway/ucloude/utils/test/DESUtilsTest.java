package cn.uway.ucloude.utils.test;

import java.security.SecureRandom;
import java.sql.Timestamp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.junit.Test;

import cn.uway.ucloude.utils.DESUtils;
import cn.uway.ucloude.utils.StringUtil;

public class DESUtilsTest {
	@Test
	public void testDes(){
//		String str = "测试内容";
//		//密码，长度要是8的倍数
//		String password = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";
//
//		//System.out.println(DESUtils.decode("/oznyRP7/KpM6z9v/qoIdw=="));
//		byte[] result = DESUtilsTest.encrypt(str.getBytes(),password);
//		System.out.println("加密后："+new String(result));
//		
//		//直接将如上内容解密
//		try {
//			byte[] decryResult = DESUtilsTest.decrypt(result, password);
//			System.out.println("解密后："+new String(decryResult));
//		} catch (Exception e1) {
//		e1.printStackTrace();
//		}
		
		System.out.println(DESUtils.decode("/oznyRP7/KpM6z9v/qoIdw=="));
		Timestamp ts = new Timestamp(1487757081258L);
	}
	
	/**
	* 加密
	* @param datasource byte[]
	* @param password String
	* @return byte[]
	*/
	public static byte[] encrypt(byte[] datasource, String password) { 
	try{
	SecureRandom random = new SecureRandom();
	DESKeySpec desKey = new DESKeySpec(password.getBytes());
	//创建一个密匙工厂，然后用它把DESKeySpec转换成
	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	SecretKey securekey = keyFactory.generateSecret(desKey);
	//Cipher对象实际完成加密操作
	Cipher cipher = Cipher.getInstance("DES");
	//用密匙初始化Cipher对象
	cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
	//现在，获取数据并加密
	//正式执行加密操作
	return cipher.doFinal(datasource);
	}catch(Throwable e){
	e.printStackTrace();
	}
	return null;
	}
	/**
	* 解密
	* @param src byte[]
	* @param password String
	* @return byte[]
	* @throws Exception
	*/
	public static byte[] decrypt(byte[] src, String password) throws Exception {
	// DES算法要求有一个可信任的随机数源
	SecureRandom random = new SecureRandom();
	// 创建一个DESKeySpec对象
	DESKeySpec desKey = new DESKeySpec(password.getBytes());
	// 创建一个密匙工厂
	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	// 将DESKeySpec对象转换成SecretKey对象
	SecretKey securekey = keyFactory.generateSecret(desKey);
	// Cipher对象实际完成解密操作
	Cipher cipher = Cipher.getInstance("DES");
	// 用密匙初始化Cipher对象
	cipher.init(Cipher.DECRYPT_MODE, securekey, random);
	// 真正开始解密操作
	return cipher.doFinal(src);
	}
}
