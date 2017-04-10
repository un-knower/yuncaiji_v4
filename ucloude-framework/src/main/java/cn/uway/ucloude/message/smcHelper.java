package cn.uway.ucloude.message;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.message.pack.AbsSmcPackage;
import cn.uway.ucloude.message.sender.EmailSender;
import cn.uway.ucloude.message.sender.SMGPSender;
import cn.uway.ucloude.message.sender.SMSSender;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.DateUtil.TimePattern;

public class smcHelper {
	private static Logger LOG = LoggerFactory.getLogger(smcHelper.class);
	
	public smcHelper(String iniFile) {
		SMCCfgSys.getInstance().init(iniFile);
	}
	
	public boolean sendSmsMsg(SMCData smcData) {
		SMSSender sender = new SMSSender();
		List<AbsSmcPackage> plist = sender.builderPackage(smcData);
		boolean result = false;
		if (plist != null && plist.size() > 0)
			result = sender.sendAll(smcData);
		else {
			LOG.warn(smcData + ", 发送用户to_user 无短信接收人");
			result = true;
		}
		
		return result;
	}
	
	public boolean sendSmgpMsg(SMCData smcData) {
		SMGPSender sender = new SMGPSender();
		boolean result = sender.sendAll(smcData);
		
		return result;
	}
	
	public boolean sendEmail(SMCData smcData) {
		EmailSender sender = new EmailSender();
		List<AbsSmcPackage>  plist = sender.builderPackage(smcData);
		boolean result = false;
		if (plist != null && plist.size() > 0)
			result = sender.sendAll(smcData);
		else {
			LOG.warn(smcData + ", 发送用户to_user 无邮件接收人");
			result = true;
		}
		return result;
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws InterruptedException {
		System.out.println("启动开始");
		if (args == null || args.length < 1)
			System.out.println("参数：空");
		else
			System.out.println("参数：" + Arrays.toString(args));
		
		System.out.println(DateUtil.formatNonException(new Date(),
				TimePattern.yyyyMMddHHmmssSSS));
		
		smcHelper helper = null;
		if (args.length > 0)
			helper = new smcHelper(args[0]);
		else
			helper = new smcHelper("e:/sg/smc.ini");
		
		boolean result = false;
		SMCData smcData = new SMCData();
		smcData.setId(668);
		
		
		if (false) {
			smcData.setToUsers("shig@uway.cn,sgang81plus@163.com");
			smcData.setSubject("测试邮件");
			smcData.setContent("这是测试内容．");
			smcData.setAttachmentfile("/home/shig/run.sh");
			result = helper.sendEmail(smcData);
		}
		
		if (false) {
			if (args.length > 1)
				smcData.setToUsers(args[1]);
			else
				smcData.setToUsers("18066108964");
			
			if (args.length > 2)
				smcData.setContent(args[2]);
			else
				smcData.setContent("Hi,不好意思，打扰了，这是一条测试短信.");
				
			result = helper.sendSmgpMsg(smcData);
		}
		
		if (true) {
			if (args.length > 1)
				smcData.setToUsers(args[1]);
			else
				smcData.setToUsers("18066108964");
			
			if (args.length > 2)
				smcData.setContent(args[2]);
			else
				smcData.setContent("Hi,不好意思，打扰了，这是一条测试短信.");
				
			result = helper.sendSmsMsg(smcData);
		}
		
		Thread.sleep(5*1000l);
		
		System.out.println(result);
	}
}
