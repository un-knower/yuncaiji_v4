package cn.uway.ucloude.message.pack;

import cn.uway.ucloude.message.SMCData;

/**
 * 短信消息的消息体
 * 
 * @author litp Sep 29, 2010
 * @since 1.0
 */
public class SMSPackage extends AbsSmcPackage {

	// SP号,等于传入参数中的//10655960970
	private String spNumber = "";

	// （重要参数）付费号码，手机号码前加“86”国别标志；当且仅当群发且对用户收费时为空；如果为空，则该条短消息产生的费用由UserNumber代表的用户支付；如果为全零字符串“000000000000000000000”，表示该条短消息产生的费用由SP支付，默认为空
	private String chargeNumber = "";

	private int userCount = 1; // 接收短信息的手机数量，默认为1

	private String userNumber = ""; // （重要参数）接收该短消息的手机号码，该字段重复UserCount指定的次数，手机号码前加“86”国别标志

	private String corpId = ""; // 企业代码，默认为31090(0)

	private String serviceType = ""; // （重要参数）业务代码，默认为TEST，表示测试信息或不明原因的信息//主要用于包月计费

	private int feeType = 2; // （重要参数）计费类型，0表示只收信道费，1表示免费，2表示按条计费，3表示按包月计费，默认为2

	private String feeValue = "0"; // （重要参数）该条短消息的收费值，单位为分，取值范围0-99999；对于包月制收费的用户，该值为月租费的值，默认为0

	private String givenValue = "0"; // 赠送用户的话费，单位为分，取值范围0-99999，特指由SP向用户发送广告时的赠送话费，默认为0

	private int agentFlag = 0; // 代收标志，0表示应收，1表示实收，默认为0

	private int moRelateToMTFlag = 2; // 引起MT消息的原因，0表示MO点播引起的第一条MT消息，1表示MO点播引起的非第一条MT消息，2表示非MO点播引起的MT消息，3表示系统反馈引起的MT消息，默认为2

	private int priority = 5; // 优先级，取值0-9，默认为5

	private String expireTime = ""; // 短消息寿命的终止时间，默认为空

	private String scheduleTime = ""; // 短消息定时发送的时间，如果为空，表示立即发送该短消息，默认为空

	private int reportFlag = 1; // （重要参数）状态报告标记，0表示该条消息只有最后出错时要返回状态报告，1表示该条消息无论最后是否成功都要返回状态报告，2表示该条消息不需要返回状态报告，3表示该条消息仅携带包月计费信息，不下发给用户并需要返回状态报告，默认为1

	private int tpPid = 0; // GSM协议类型，默认为1

	private int tpUdhi = 0; // GSM协议类型，默认为1

	private int messageCoding = 15; // （重要参数）短消息的编码格式，0表示纯ASCII字符串，3表示写卡操作，4表示二进制编码，8表示UCS2编码，15表示GBK编码，默认为15

	private int messageType = 0; // 信息类型，0表示短消息，默认为0

	private int messageLength = 140; // 消息长度，默认140

	private String messageContent = ""; // （重要参数）短消息内容

	private SMSPackage(Builder builder, SMCData smcData) {
		super(smcData);
		this.spNumber = builder.spNumber;
		this.chargeNumber = builder.chargeNumber;
		this.userCount = builder.userCount;
		this.userNumber = builder.userNumber;
		this.corpId = builder.corpId;
		this.serviceType = builder.serviceType;
		this.feeType = builder.feeType;
		this.feeValue = builder.feeValue;
		this.givenValue = builder.givenValue;
		this.agentFlag = builder.agentFlag;
		this.moRelateToMTFlag = builder.moRelateToMTFlag;
		this.priority = builder.priority;
		this.expireTime = builder.expireTime;
		this.scheduleTime = builder.scheduleTime;
		this.reportFlag = builder.reportFlag;
		this.tpPid = builder.tpPid;
		this.tpUdhi = builder.tpUdhi;
		this.messageCoding = builder.messageCoding;
		this.messageType = builder.messageType;
		this.messageLength = builder.messageLength;
		this.messageContent = builder.messageContent;
	}

	public static class Builder {

		// required fields
		private String spNumber = "";// SP号,等于传入参数中的//10655960970

		private String chargeNumber = ""; // （重要参数）付费号码，手机号码前加“86”国别标志；当且仅当群发且对用户收费时为空；如果为空，则该条短消息产生的费用由UserNumber代表的用户支付；如果为全零字符串“000000000000000000000”，表示该条短消息产生的费用由SP支付，默认为空

		private String userNumber = ""; // （重要参数）接收该短消息的手机号码，该字段重复UserCount指定的次数，手机号码前加“86”国别标志

		private String serviceType = ""; // （重要参数）业务代码，默认为TEST，表示测试信息或不明原因的信息//主要用于包月计费

		private String corpId = ""; // 企业代码

		private String messageContent = ""; // （重要参数）短消息内容

		// optional fields
		private int feeType = 2; // （重要参数）计费类型，0表示只收信道费，1表示免费，2表示按条计费，3表示按包月计费，默认为2

		private String feeValue = "0"; // （重要参数）该条短消息的收费值，单位为分，取值范围0-99999；对于包月制收费的用户，该值为月租费的值，默认为0

		private int moRelateToMTFlag = 2; // 引起MT消息的原因，0表示MO点播引起的第一条MT消息，1表示MO点播引起的非第一条MT消息，2表示非MO点播引起的MT消息，3表示系统反馈引起的MT消息，默认为2

		private int reportFlag = 1; // （重要参数）状态报告标记，0表示该条消息只有最后出错时要返回状态报告，1表示该条消息无论最后是否成功都要返回状态报告，2表示该条消息不需要返回状态报告，3表示该条消息仅携带包月计费信息，不下发给用户并需要返回状态报告，默认为1

		private int userCount = 1; // 接收短信息的手机数量，默认为1

		private String givenValue = "0"; // 赠送用户的话费，单位为分，取值范围0-99999，特指由SP向用户发送广告时的赠送话费，默认为0

		private int agentFlag = 0; // 代收标志，0表示应收，1表示实收，默认为0

		private int priority = 5; // 优先级，取值0-9，默认为5

		private String expireTime = ""; // 短消息寿命的终止时间，默认为空

		private String scheduleTime = ""; // 短消息定时发送的时间，如果为空，表示立即发送该短消息，默认为空

		private int tpPid = 0; // GSM协议类型，默认为1

		private int tpUdhi = 0; // GSM协议类型，默认为1

		private int messageCoding = 15; // （重要参数）短消息的编码格式，0表示纯ASCII字符串，3表示写卡操作，4表示二进制编码，8表示UCS2编码，15表示GBK编码，默认为15

		private int messageType = 0; // 信息类型，0表示短消息，默认为0

		private int messageLength = 140; // 消息长度，默认140

		/** 必须参数 */
		public Builder(String spNumber, String chargeNumber, String userNumber,
				String serviceType, String corpId, String messageContent,
				int feeType, String feeValue, int moRelateToMTFlag,
				int reportFlag, int agentFlag, int priority, String expireTime,
				String scheduleTime, int tpPid, int tpUdhi, int messageCoding) {
			super();
			this.spNumber = spNumber;
			this.chargeNumber = chargeNumber;
			this.userNumber = userNumber;
			this.serviceType = serviceType;

			this.corpId = corpId;
			this.messageContent = messageContent;

			this.feeType = feeType;
			this.feeValue = feeValue;
			this.moRelateToMTFlag = moRelateToMTFlag;
			this.reportFlag = reportFlag;
			// this.givenValue=givenValue;
			this.agentFlag = agentFlag;
			this.priority = priority;
			this.expireTime = expireTime;
			this.scheduleTime = scheduleTime;
			this.tpPid = tpPid;
			this.tpUdhi = tpUdhi;
			this.messageCoding = messageCoding;

		}

		/** 必须参数 */
		public Builder(String spNumber, String chargeNumber, String userNumber,
				String serviceType, String corpId, String messageContent) {
			super();
			this.spNumber = spNumber;
			this.chargeNumber = chargeNumber;
			this.userNumber = userNumber;
			this.serviceType = serviceType;

			this.corpId = corpId;
			this.messageContent = messageContent;
		}

		public Builder setUserCount(int userCount) {
			this.userCount = userCount;
			return this;
		}

		public Builder setFeeType(int feeType) {
			this.feeType = feeType;
			return this;
		}

		public Builder setFeeValue(String feeValue) {
			this.feeValue = feeValue;
			return this;
		}

		public Builder setMoRelateToMTFlag(int moRelateToMTFlag) {
			this.moRelateToMTFlag = moRelateToMTFlag;
			return this;
		}

		public Builder setReportFlag(int reportFlag) {
			this.reportFlag = reportFlag;
			return this;
		}

		public Builder setGivenValue(String givenValue) {
			this.givenValue = givenValue;
			return this;
		}

		public Builder setAgentFlag(int agentFlag) {
			this.agentFlag = agentFlag;
			return this;
		}

		public Builder setPriority(int priority) {
			this.priority = priority;
			return this;
		}

		public Builder setExpireTime(String expireTime) {
			this.expireTime = expireTime;
			return this;
		}

		public Builder setScheduleTime(String scheduleTime) {
			this.scheduleTime = scheduleTime;
			return this;
		}

		public Builder setTpPid(int tpPid) {
			this.tpPid = tpPid;
			return this;
		}

		public Builder setTpUdhi(int tpUdhi) {
			this.tpUdhi = tpUdhi;
			return this;
		}

		public Builder setMessageCoding(int messageCoding) {
			this.messageCoding = messageCoding;
			return this;
		}

		public Builder setMessageType(int messageType) {
			this.messageType = messageType;
			return this;
		}

		public Builder setMessageLength(int messageLength) {
			this.messageLength = messageLength;
			return this;
		}

		public SMSPackage build(SMCData smcData) {
			return new SMSPackage(this, smcData);
		}
	}

	public String getSpNumber() {
		return spNumber;
	}

	public String getChargeNumber() {
		return chargeNumber;
	}

	public int getUserCount() {
		return userCount;
	}

	public String getUserNumber() {
		return userNumber;
	}

	public String getCorpId() {
		return corpId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public int getFeeType() {
		return feeType;
	}

	public String getFeeValue() {
		return feeValue;
	}

	public String getGivenValue() {
		return givenValue;
	}

	public int getAgentFlag() {
		return agentFlag;
	}

	public int getMoRelateToMTFlag() {
		return moRelateToMTFlag;
	}

	public int getPriority() {
		return priority;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public int getReportFlag() {
		return reportFlag;
	}

	public int getTpPid() {
		return tpPid;
	}

	public int getTpUdhi() {
		return tpUdhi;
	}

	public int getMessageCoding() {
		return messageCoding;
	}

	public int getMessageType() {
		return messageType;
	}

	public int getMessageLength() {
		return messageLength;
	}

	public String getMessageContent() {
		return messageContent;
	}
}
