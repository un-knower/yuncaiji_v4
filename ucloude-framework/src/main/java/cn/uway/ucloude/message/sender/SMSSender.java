package cn.uway.ucloude.message.sender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.message.SGIPIntegerfaceMgr;
import cn.uway.ucloude.message.SMCCfgSys;
import cn.uway.ucloude.message.SMCData;
import cn.uway.ucloude.message.pack.AbsSmcPackage;
import cn.uway.ucloude.message.pack.SMSPackage;


public class SMSSender extends AbstractSender {

	private Logger LOG = LoggerFactory.getLogger(SMSSender.class);

	private String messageId = ""; // 消息ID

	private List<AbsSmcPackage> pkList = new ArrayList<AbsSmcPackage>();
	
	public SMSSender() {
	}

	@Override
	public int send(AbsSmcPackage data) {
		int result = 1;
		try {
			if (!(data instanceof SMSPackage))
				return -1;

			SMSPackage smsPack = (SMSPackage) data;
			return SGIPIntegerfaceMgr.getInstance().send(smsPack);
		} catch (Exception e) {
			LOG.error(data.getPackDesc() + "短信发送失败", e);
		}
		return result;
	}

	@Override
	public void close() {
		/*
		 * disconnectToGW(); closeSocket();
		 */
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public boolean sendAll(SMCData smcExpressData) {
		boolean bFlag = false;
		try {

			int count = 0;
			SMCCfgSys sys = SMCCfgSys.getInstance();
			LOG.debug(smcExpressData + ", NodeID : " + sys.getNodeId() + " IP: "
					+ sys.getServerIp() + "  Port:  " + sys.getServerPort()
					+ "  size : " + pkList.size());
			/*
			 * boolean b = connectToGW(sys.getSmsUserName(),
			 * sys.getSmsUserPwd());
			 */for (AbsSmcPackage pack : pkList) {
				/*
				 * if ( !b ) b = connectToGW(sys.getSmsUserName(),
				 * sys.getSmsUserPwd());
				 */

				int result = send(pack);
				LOG.debug(smcExpressData + " , result=" + result);
				// 对于sms来说，只是此次操作成功，并不是代表此次结果成功
				if (result == 0) {
					count++;
				} else if (result == -1) {
					boolean bf = reSendSent(pack);
					if (bf) {
						count++;
						// sendAfter(0, null);

						LOG.debug(smcExpressData + " : 短信重试发送成功.");
					} else {
						LOG.debug(smcExpressData + " : 短信重试发送失败.");
						// sendAfter(-1, "信息发送失败");
					}
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					LOG.error("buildMessage 出现异常:{}", e);
					break;
				}

			}
			close();// add 0801
			LOG.debug(smcExpressData + ", 已成功发送用户个数 : " + count);
			if (count>0) {
				LOG.debug(smcExpressData + ",发送成功，并 添加到历史表.");
				// BussinessMgr.updateExpressSentTimes(smcExpressData);
				sendAfter(0, null);
				bFlag = true;

			} else {
				LOG.debug(smcExpressData + ",信息发送失败.");
				sendAfter(-1, "信息发送失败");
				bFlag = false;
			}
		} catch (Exception e) {
			LOG.debug(smcExpressData + " 发送短信出现异常.", e);
		}
		return bFlag;
	}

	private void buildMessagePack(SMCData smcExpressData) {
		String toUsers = smcExpressData.getToUsers();
		Map<String, List<String>> users = SenderConstDef.getPhoneEmail(toUsers, SenderConstDef.PHONE);
		List<String> userPhoneList = users.get(SenderConstDef.PHONE);
		if (userPhoneList == null)
			return;

		// 网关是否支持群发，支持群发，就以群发方式发送，如果不支持，将用户号码拆分为多个号码发送
		SMCCfgSys sys = SMCCfgSys.getInstance();
		boolean isGroupSender = sys.isGroupSender();
		List<String> userPhones = new ArrayList<String>();

		int userCount = 0;
		StringBuilder userNumber = new StringBuilder();
		for (String cell : userPhoneList) {
			if (cell == null || (cell = cell.trim()).equals(""))
				continue;

			cell = cell.trim();
			if (!cell.startsWith("86"))
				cell = "86" + cell;

			if (!cell.startsWith("86")) {
				userNumber.append("86" + cell).append(",");
				userPhones.add("86" + cell);
			} else {
				userNumber.append(cell).append(",");
				userPhones.add(cell);
			}

			if (!isGroupSender) {
				userCount = 1;
			} else {
				userCount++;
			}
		}
		if (userNumber.length() > 0) {
			userNumber.deleteCharAt(userNumber.length() - 1);
		} else {
			LOG.debug(smcExpressData + ", 用户号码为空");
			return;
		}
		String messageContent = smcExpressData.getContent();
		messageContent = messageContent.trim();
		int messageLength = messageContent.length();

		String senderToUser = new String();
		if (!isGroupSender) {
			for (int i = 0; i < userPhones.size(); i++) {
				senderToUser = userPhones.get(i);
				LOG.debug(smcExpressData + ", 分解发送 ,  userNumber = "
						+ senderToUser);
				isExplor(smcExpressData, pkList, messageLength, messageContent,
						senderToUser, userCount);
			}
		} else {
			LOG.debug(smcExpressData + " , 群发号码, userNumber = "
					+ userNumber.toString());
			senderToUser = userNumber.toString();
			isExplor(smcExpressData, pkList, messageLength, messageContent, senderToUser,
					userCount);
		}
	}

	private void isExplor(SMCData smcExpressData, List<AbsSmcPackage> pkList,
			int messageLength, String messageContent, String senderToUser,
			int userCount) {
		AbsSmcPackage pack = null;
		SMCCfgSys sys = SMCCfgSys.getInstance();
		if (messageLength > SenderConstDef.ONEMESSAGELENGTH) {
			explorMessage(smcExpressData, pkList, sys.getSpNumber(),
					sys.getChargeNumber(), senderToUser, sys.getServiceType(),
					sys.getCorpId(), messageContent, userCount, sys);
		} else {
			pack = new SMSPackage.Builder(sys.getSpNumber(),
					sys.getChargeNumber(), senderToUser, sys.getServiceType(),
					sys.getCorpId(), messageContent, sys.getFeeType(),
					sys.getFeeValue(), sys.getMoRelateToMtFlag(),
					sys.getReportFlag(), sys.getAgentFlag(), sys.getPriority(),
					sys.getExpireTime(), sys.getScheduleTime(), sys.getTpPid(),
					sys.getTpUdhi(), sys.getMessagecoding())
					.setUserCount(userCount).setMessageLength(messageLength)
					.build(smcExpressData);
			pkList.add(pack);
		}
	}

	@Override
	public List<AbsSmcPackage> builderPackage(SMCData smcExpressData) {
		buildMessagePack(smcExpressData);

		return pkList;
	}

	public void sleep(long time) throws InterruptedException {
		Thread.sleep(time);
	}

}