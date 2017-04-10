package cn.uway.ucloude.message.pack;

import cn.uway.ucloude.message.SMCData;

/**
 * 用于包装Email数据的类
 * 
 * @author litp Sep 30, 2010
 * @since 1.0
 */
public class EmailPackage extends AbsSmcPackage {

	public EmailPackage(SMCData smcData) {
		super(smcData);
	}

	private String[] mailTO;

	private String mailSMTPHost;

	private String mailAccount;

	private String mailPassword;

	private String subject;// 邮件标题

	private String content;

	private String strFileAttachment;// 邮件附件,目前支持发送单个附件，不支持一次发送多个附件

	private String title;// 邮件标题

	public String[] getMailTO() {
		return mailTO;
	}

	public void setMailTO(String[] mailTO) {
		this.mailTO = mailTO;
	}

	public String getMailSMTPHost() {
		return mailSMTPHost;
	}

	public void setMailSMTPHost(String mailSMTPHost) {
		this.mailSMTPHost = mailSMTPHost;
	}

	public String getMailAccount() {
		return mailAccount;
	}

	public void setMailAccount(String mailAccount) {
		this.mailAccount = mailAccount;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStrFileAttachment() {
		return strFileAttachment;
	}

	public void setStrFileAttachment(String strFileAttachment) {
		this.strFileAttachment = strFileAttachment;
	}

	public String getPackDesc() {
		return super.getPackDesc();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
