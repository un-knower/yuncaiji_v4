package cn.uway.ucloude.message;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息体
 */
public class SMCData implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	private int srcid;// 消息类型

	private int levelid;// 消息级别

	private String toUsers;

	private int sendWay; // 发送方式

	private Date occurTime;// 消息产生时间

	private String content;// 消息具体内容

	private String sendTime; // 发送时间

	private String sendTimeExclude;

	private int sentOkTimes = 0;// 消息已经发送的次数(开始为0)//add

	private String subject;// 主题，针对邮件

	private int isReceiveGw = 0;// 是否是从网关接收

	private String spNumber;

	private String attachmentfile;// 针对邮件附件

	private int type = 2;

	private String username;

	private String password;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getToUsers() {
		return toUsers;
	}

	public void setToUsers(String toUsers) {
		this.toUsers = toUsers;
	}

	public int getSendWay() {
		return sendWay;
	}

	public void setSendWay(int sendWay) {
		this.sendWay = sendWay;
	}

	public Date getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(Date occurTime) {
		this.occurTime = occurTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getSendTimeExclude() {
		return sendTimeExclude;
	}

	public void setSendTimeExclude(String sendTimeExclude) {
		this.sendTimeExclude = sendTimeExclude;
	}

	public int getSentOkTimes() {
		return sentOkTimes;
	}

	public void setSentOkTimes(int sentOkTimes) {
		this.sentOkTimes = sentOkTimes;
	}

	public int getSrcid() {
		return srcid;
	}

	public void setSrcid(int srcid) {
		this.srcid = srcid;
	}

	public int getLevelid() {
		return levelid;
	}

	public void setLevelid(int levelid) {
		this.levelid = levelid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getIsReceiveGw() {
		return isReceiveGw;
	}

	public void setIsReceiveGw(int isReceiveGw) {
		this.isReceiveGw = isReceiveGw;
	}

	public String getSpNumber() {
		return spNumber;
	}

	public void setSpNumber(String spNumber) {
		this.spNumber = spNumber;
	}

	public String getAttachmentfile() {
		return attachmentfile;
	}

	public void setAttachmentfile(String attachmentfile) {
		this.attachmentfile = attachmentfile;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "SMCExpressData [id=" + id + ", srcid=" + srcid + ", levelid="
				+ levelid + "]";
	}
}
