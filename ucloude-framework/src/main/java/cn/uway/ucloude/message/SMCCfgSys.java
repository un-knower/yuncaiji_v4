package cn.uway.ucloude.message;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import cn.uway.ucloude.utils.NumberUtil;

/**
 * 系统配置表
 * 
 * @author litp Sep 29, 2010
 * @since 1.0
 */
public class SMCCfgSys {

	private String smtpHost;// 邮件服务器

	private String mailUser; // 邮件账号

	private String mailPwd; // 邮件账号密码

	private int nodeId;// 节点编号

	private int feeType;// 计费类型

	private int agentFlag;// 代收标志

	private int moRelateToMtFlag;// 引起MT消息的原因

	private int priority;// 优先级

	private int reportFlag;// 状态报告标记

	private int tpPid;// GSM协议类型

	private int tpUdhi;// GSM协议类型

	private int messagecoding;// 短消息的编码格式

	private int messageType;// 信息类型

	private String spNumber;// SP的接入号

	private String chargeNumber;// 付费号码

	private String corpId;// 企业代码

	private String serviceType;// 业务代码

	private String feeValue;// 该条短消息的收费值

	private String expireTime;// 短消息寿命的终止时间

	private String scheduleTime;// 短消息定时发送的时间

	private String serverIp;// 短消息服务器IP

	private int serverPort;// 短消息服务器端口

	private String smsUserName;// 短信用户名

	private String smsUserPwd;// 短信密码

	/**
	 * ini配置属性
	 */
	private volatile Properties props;

	/**
	 * 是否支持群发
	 */
	private boolean groupSender;

	/**
	 * 单条信息长度
	 */
	private int messageLength;
	
	/**
	 * 配置文件
	 */
	private String iniFile;
	

	public SMCCfgSys() {
	}
	
	private static class SMCCfgSysInstance {
		private static SMCCfgSys instance = new SMCCfgSys();
	}
	
	public static void init(SMCCfgSys instance, String iniFile) {
		instance.init(iniFile);
	}

	public synchronized void init(String iniFile) {
		this.iniFile = iniFile;
		if (this.props != null)
			return;
		
		props = new Properties();
		FileInputStream propfile = null;
		try {
			propfile = new FileInputStream(iniFile);
			props.load(propfile);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
			return;
		} finally {
			if (propfile != null) {
				try {
					propfile.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		setSmtpHost(getProperty("smtp_host", ""));
		setMailUser(getProperty("mail_user", ""));
		setMailPwd(getProperty("mail_pwd", ""));

		setNodeId(getPropertyInt("nodeid"));
		setFeeType(getPropertyInt("feetype"));
		setAgentFlag(getPropertyInt("agentflag"));
		setMoRelateToMtFlag(getPropertyInt("morelatetomtflag"));
		setPriority(getPropertyInt("priority"));
		setReportFlag(getPropertyInt("reportflag"));
		setTpPid(getPropertyInt("tp_pid"));
		setTpUdhi(getPropertyInt("tp_udhi"));
		setMessagecoding(getPropertyInt("messagecoding"));
		setMessageType(getPropertyInt("messagetype"));
		setSpNumber(getProperty("spnumber", ""));
		setChargeNumber(getProperty("chargenumber", ""));
		setCorpId(getProperty("corpid", ""));
		setServiceType(getProperty("servicetype", ""));
		setFeeValue(getProperty("feevalue", ""));
		setExpireTime(getProperty("expireTime", ""));
		setScheduleTime(getProperty("scheduletime", ""));
		setServerIp(getProperty("serveip", ""));
		setServerPort(getPropertyInt("serveport"));
		setSmsUserName(getProperty("sms_username", ""));
		setSmsUserPwd(getProperty("sms_userpwd", ""));
		
		setGroupSender("1".equals(getProperty("groupSender", "")));
		setMessageLength(getPropertyInt("messageLength", 140));
	}

	/**
	 * 获取SMCCfgSys的单实例
	 * 
	 * @return
	 */
	public static SMCCfgSys getInstance() {
		SMCCfgSys inst = SMCCfgSysInstance.instance;
		return inst;
	}

	/**
	 * 获取配置属性值
	 * 
	 * @param properyName	属性名
	 * @param defStr		默认字符
	 *            
	 * @return
	 */
	public String getProperty(String properyName, String defStr) {
		String value = props.getProperty(properyName);
		if (value != null)
			return value.trim();

		return defStr;
	}
	
	public String getProperty(String properyName) {
		String value = props.getProperty(properyName, null);
		if (value != null)
			return value.trim();

		return null;
	}

	public int getPropertyInt(String properyName) {
		return getPropertyInt(properyName, 0);
	}

	public int getPropertyInt(String properyName, int defValue) {
		String value = props.getProperty(properyName);
		if (value != null) {
			value = value.trim();
			if (NumberUtil.isDigits(value)) {
				return NumberUtil.parseInteger(value);
			}
		}

		return defValue;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getMailUser() {
		return mailUser;
	}

	public void setMailUser(String mailUser) {
		this.mailUser = mailUser;
	}

	public String getMailPwd() {
		return mailPwd;
	}

	public void setMailPwd(String mailPwd) {
		this.mailPwd = mailPwd;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getFeeType() {
		return feeType;
	}

	public void setFeeType(int feeType) {
		this.feeType = feeType;
	}

	public int getAgentFlag() {
		return agentFlag;
	}

	public void setAgentFlag(int agentFlag) {
		this.agentFlag = agentFlag;
	}

	public int getMoRelateToMtFlag() {
		return moRelateToMtFlag;
	}

	public void setMoRelateToMtFlag(int moRelateToMtFlag) {
		this.moRelateToMtFlag = moRelateToMtFlag;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getReportFlag() {
		return reportFlag;
	}

	public void setReportFlag(int reportFlag) {
		this.reportFlag = reportFlag;
	}

	public int getTpPid() {
		return tpPid;
	}

	public void setTpPid(int tpPid) {
		this.tpPid = tpPid;
	}

	public int getTpUdhi() {
		return tpUdhi;
	}

	public void setTpUdhi(int tpUdhi) {
		this.tpUdhi = tpUdhi;
	}

	public int getMessagecoding() {
		return messagecoding;
	}

	public void setMessagecoding(int messagecoding) {
		this.messagecoding = messagecoding;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getSpNumber() {
		return spNumber;
	}

	public void setSpNumber(String spNumber) {
		this.spNumber = spNumber;
	}

	public String getChargeNumber() {
		return chargeNumber;
	}

	public void setChargeNumber(String chargeNumber) {
		this.chargeNumber = chargeNumber;
	}

	public String getCorpId() {
		return corpId;
	}

	public void setCorpId(String corpId) {
		this.corpId = corpId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getFeeValue() {
		return feeValue;
	}

	public void setFeeValue(String feeValue) {
		this.feeValue = feeValue;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getSmsUserName() {
		return smsUserName;
	}

	public void setSmsUserName(String smsUserName) {
		this.smsUserName = smsUserName;
	}

	public String getSmsUserPwd() {
		return smsUserPwd;
	}

	public void setSmsUserPwd(String smsUserPwd) {
		this.smsUserPwd = smsUserPwd;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isGroupSender() {
		return groupSender;
	}

	public void setGroupSender(boolean groupSender) {
		this.groupSender = groupSender;
	}

	public int getMessageLength() {
		return messageLength;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}
	
	public String getIniFile() {
		return iniFile;
	}

	public static void main(String[] args) {
		SMCCfgSys sys = SMCCfgSys.getInstance();
		if (sys == null) {
			System.out.println("faild");
		}
	}
}
