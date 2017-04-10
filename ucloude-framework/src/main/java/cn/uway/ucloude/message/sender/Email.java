package cn.uway.ucloude.message.sender;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此类用于邮件的发送 Mail
 * 
 * @author ltp Dec 3, 2009
 */
public class Email {

	private final static Logger LOG = LoggerFactory.getLogger(Email.class);

	/** 发件方式 - 普通发送 */
	public final static int TO = 0;

	/** 发件方式 - 抄送 */
	public final static int CC = 1;

	/** 发件方式 - 密件抄送 */
	public final static int BCC = 2;

	/** 邮件相关信息 - SMTP 服务器 */
	private String mailSMTPHost;

	/** 邮件相关信息 - 邮件用户名 */
	private String mailUser;

	/** 邮件相关信息 - 密码 */
	private String mailPassword;

	/** 邮件相关信息 - 发件人邮件地址 */
	private String mailFromAddress;

	/** 邮件相关信息 - 邮件主题 */
	private String mailSubject = "";

	/** 邮件相关信息 - 邮件发送地址 */
	private Address[] mailTOAddress;

	/** 邮件相关信息 - 邮件抄送地址 */
	private Address[] mailCCAddress;

	/** 邮件相关信息 - 邮件密件抄送地址 */
	private Address[] mailBCCAddress;

	/** 邮件相关信息 - 邮件正文(复合结构) */
	private MimeMultipart mailBody;

	public Email() {
		mailBody = new MimeMultipart();
	}

	/**
	 * 设置 SMTP 服务器
	 * 
	 * @param strSMTPHost
	 *            邮件服务器名称或 IP
	 * @param strUser
	 *            邮件用户名
	 * @param strPassword
	 *            密码
	 */
	public void setSMTPHost(String strSMTPHost, String strUser,
			String strPassword) {
		if (strSMTPHost != null && !"".equals(strSMTPHost)) {
			this.mailSMTPHost = strSMTPHost;
		}
		if (strUser != null && !"".equals(strUser)) {
			this.mailUser = strUser;
		}
		if (strPassword != null && !"".equals(strPassword)) {
			this.mailPassword = strPassword;
		}
	}

	/**
	 * 设置邮件发送地址
	 * 
	 * @param strFromAddress
	 *            邮件发送地址
	 */
	public void setFromAddress(String strFromAddress) {
		if (strFromAddress != null && !strFromAddress.equals("")) {
			this.mailFromAddress = strFromAddress;
		}
	}

	/**
	 * 设置邮件目的地址
	 * 
	 * @param strAddress
	 *            邮件目的地址列表, 不同的地址可用;号分隔
	 * @param iAddressType
	 *            邮件发送方式 (TO 0, CC 1, BCC 2) 常量已在本类定义
	 * @throws AddressException
	 */
	public void setAddress(String[] strAddress, int iAddressType)
			throws AddressException {
		int len = 0;
		if (strAddress != null && (len = strAddress.length) > 0) {
			switch (iAddressType) {
				case Email.TO : {
					mailTOAddress = new Address[len];
					for (int i = 0; i < len; i++) {
						mailTOAddress[i] = new InternetAddress(strAddress[i]);
					}
				}
					break;
				case Email.CC : {

					mailCCAddress = new Address[len];
					for (int i = 0; i < len; i++) {
						mailCCAddress[i] = new InternetAddress(strAddress[i]);
					}

				}
					break;
				case Email.BCC : {
					mailBCCAddress = new Address[len];
					for (int i = 0; i < len; i++) {
						mailBCCAddress[i] = new InternetAddress(strAddress[i]);
					}

				}
					break;
				default :
					break;
			}
		}
	}

	/**
	 * 设置邮件主题
	 * 
	 * @param strSubject
	 *            邮件主题
	 */
	public void setSubject(String strSubject) {
		if (strSubject != null && !"".equals(strSubject)) {
			this.mailSubject = strSubject;
		}
	}

	/**
	 * 设置邮件文本正文
	 * 
	 * @param strTextBody
	 *            邮件文本正文
	 * @throws MessagingException
	 */
	public void setTextBody(String strTextBody) throws MessagingException {
		if (strTextBody != null && !"".equals(strTextBody)) {
			MimeBodyPart mimebodypart = new MimeBodyPart();
			mimebodypart.setText(strTextBody, "GBK");
			mailBody.addBodyPart(mimebodypart);
		}
	}

	/**
	 * 设置邮件超文本正文
	 * 
	 * @param strHtmlBody
	 *            邮件超文本正文
	 * @throws MessagingException
	 */
	public void setHtmlBody(String strHtmlBody) throws MessagingException {
		if (strHtmlBody != null && !"".equals(strHtmlBody)) {
			MimeBodyPart mimebodypart = new MimeBodyPart();
			mimebodypart.setDataHandler(new DataHandler(strHtmlBody,
					"text/html;charset=GBK"));
			mailBody.addBodyPart(mimebodypart);
		}
	}

	/**
	 * 设置邮件正文外部链接 URL, 信体中将包含链接所指向的内容
	 * 
	 * @param strURLAttachment
	 *            邮件正文外部链接 URL
	 * @throws MessagingException
	 * @throws MalformedURLException
	 */
	public void setURLAttachment(String strURLAttachment)
			throws MessagingException, MalformedURLException {
		if (strURLAttachment != null && !"".equals(strURLAttachment)) {
			MimeBodyPart mimebodypart = new MimeBodyPart();
			mimebodypart.setDataHandler(new DataHandler(new URL(
					strURLAttachment)));
			mailBody.addBodyPart(mimebodypart);
		}
	}

	/**
	 * 设置邮件附件
	 * 
	 * @param strFileAttachment
	 *            文件的全路径
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public void setFileAttachment(String strFileAttachment)
			throws MessagingException, UnsupportedEncodingException {
		if (strFileAttachment != null && !"".equals(strFileAttachment)) {
			File path = new File(strFileAttachment);
			if (!path.exists() || path.isDirectory()) {
				return;
			}
			String strFileName = path.getName();
			MimeBodyPart mimebodypart = new MimeBodyPart();
			mimebodypart.setDataHandler(new DataHandler(new FileDataSource(
					strFileAttachment)));
			mimebodypart.setFileName(MimeUtility.encodeText(strFileName));
			mailBody.addBodyPart(mimebodypart);
		}
	}

	/**
	 * 邮件发送(一次发送多个地址, 优点速度快, 但是有非法邮件地址时将中断发送操作)
	 * 
	 * @throws MessagingException
	 */
	public void sendBatch() throws MessagingException {
		Transport transport = null;
		try {
			Properties properties = new Properties();
			properties.put("mail.smtp.host", this.mailSMTPHost);
			properties.put("mail.smtp.auth", "true");

			SmtpAuth sa = new SmtpAuth();
			sa.setUserinfo(this.mailUser, this.mailPassword);

			Session session = Session.getInstance(properties, sa);

			MimeMessage mimemessage = new MimeMessage(session);
			mimemessage.setFrom(new InternetAddress(this.mailFromAddress));
			if (mailTOAddress != null) {
				mimemessage.addRecipients(RecipientType.TO, this.mailTOAddress);
			}
			if (mailCCAddress != null) {
				mimemessage.addRecipients(RecipientType.CC, this.mailCCAddress);
			}
			if (mailBCCAddress != null) {
				mimemessage.addRecipients(RecipientType.BCC,
						this.mailBCCAddress);
			}
			mimemessage.setSubject(this.mailSubject);
			mimemessage.setContent(this.mailBody);
			mimemessage.setSentDate(new Date());
			transport = session.getTransport("smtp");
			transport.connect(this.mailSMTPHost, this.mailUser,
					this.mailPassword);
			Transport.send(mimemessage);

			LOG.debug("已向下列邮箱发送了邮件!");
			if (mailTOAddress != null) {
				for (int i = 0; i < mailTOAddress.length; i++) {
					LOG.debug(mailTOAddress[i].toString());
				}
			}
			if (mailCCAddress != null) {
				for (int i = 0; i < mailCCAddress.length; i++) {
					LOG.debug(mailCCAddress[i].toString());
				}
			}
			if (mailBCCAddress != null) {
				for (int i = 0; i < mailBCCAddress.length; i++) {
					LOG.debug(mailBCCAddress[i].toString());
				}
			}
		} finally {
			if (transport != null)
				transport.close();
		}

	}

	/**
	 * 用户认证 SmtpAuth
	 * 
	 * @author ltp Dec 3, 2009
	 */
	static class SmtpAuth extends Authenticator {

		private String user, password;

		public void setUserinfo(String getuser, String getpassword) {
			this.user = getuser;
			this.password = getpassword;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, password);
		}
	}


	public static void main(String[] args) {
		Email mail = new Email();
		mail.setSMTPHost("smtp.uway.cn", "liuwx@uway.cn", "xiangzi1qazx");
		mail.setFromAddress("liuwx@uway.cn");
		mail.setSubject("测试");
		try {
			// MessageFormat formatter = new MessageFormat("");

			// 从资源文件中获取相应的模版信息

			String text = "您好！<br>&nbsp;&nbsp;&nbsp;&nbsp;时间：2011-09-22 16:00:00；城市：南京；行政区：鼓楼；厂家：华为；BSC：JS_NJ_CBSC_1；BTS：67；CELL：新纪元大酒店_A[67]；CI：[CI]校园分析-CELL级-连续1小时“C6.4业务信道掉话率(%)>0.4“ .";
			// formatter.applyPattern(text);
			// String msgtext = formatter.format("");
			mail.setHtmlBody(text);
			/*
			 * try {
			 * mail.setFileAttachment("G:\\GD_CDL_GZ_1X_2011071810.csv.txt"); }
			 * catch (UnsupportedEncodingException e) { e.printStackTrace(); }
			 */
			mail.setAddress("liuwx@uway.cn;5@qq.com".split(";"), Email.TO);
			mail.sendBatch();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
}