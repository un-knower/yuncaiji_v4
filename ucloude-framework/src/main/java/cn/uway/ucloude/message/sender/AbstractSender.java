package cn.uway.ucloude.message.sender;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.message.SMCCfgSys;
import cn.uway.ucloude.message.SMCData;
import cn.uway.ucloude.message.pack.AbsSmcPackage;
import cn.uway.ucloude.message.pack.SMSPackage;
import cn.uway.ucloude.utils.StringUtil;

public abstract class AbstractSender implements ISender {
	/**
	 * 重试次数
	 */
	private final int nTryTimes = 3;
	
	private static Logger LOG = LoggerFactory.getLogger(AbstractSender.class);
	
	public boolean reSendSent(AbsSmcPackage pack) {
		for (int i = 0; i < nTryTimes; i++) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				break;
			}

			int result = send(pack);
			if (result == 0) {
				LOG.debug("{}第{}次重试成功.", pack.getPackDesc(), i + 1);
				return true;
			} else {
				LOG.debug("{}第{}次重试失败.", pack.getPackDesc(), i + 1);
			}
		}

		return false;
	}
	
	public void sendAfter(int result, String cause) {
		
	}
	
	/**
	 * 字符长度限制140 *5=720个字符，短信最大长度不能超过720个字符，SMC会自动拆分成5个短信来发
	 * 
	 * @param content
	 * @return
	 */
	public static List<AbsSmcPackage> explorMessage(SMCData smcExpressData, List<AbsSmcPackage> pkList,
			String spNumber, String chargeNumber, String userNumber,
			String serviceType, String corpId, String messageContent,
			int userCount, SMCCfgSys sys) {
		if (StringUtil.isEmpty(messageContent))
			return null;
		AbsSmcPackage pack = null;
		int len = messageContent.length();
		int mod = len % SenderConstDef.ONEMESSAGELENGTH;
		int count = len / SenderConstDef.ONEMESSAGELENGTH;
		if (mod != 0) {
			count = len / SenderConstDef.ONEMESSAGELENGTH + 1;
		}

		int bindex = 0;
		for (int i = 0; i < count; i++) {// 200
			String c = messageContent.substring(bindex, (i + 1) >= count
					? (SenderConstDef.ONEMESSAGELENGTH * i + (mod == 0
							? SenderConstDef.ONEMESSAGELENGTH
							: mod)) : SenderConstDef.ONEMESSAGELENGTH * (i + 1));
			bindex = SenderConstDef.ONEMESSAGELENGTH * (i + 1);

			pack = new SMSPackage.Builder(sys.getSpNumber(),
					sys.getChargeNumber(), userNumber.toString(),
					sys.getServiceType(), sys.getCorpId(), c, sys.getFeeType(),
					sys.getFeeValue(), sys.getMoRelateToMtFlag(),
					sys.getReportFlag(), sys.getAgentFlag(), sys.getPriority(),
					sys.getExpireTime(), sys.getScheduleTime(), sys.getTpPid(),
					sys.getTpUdhi(), sys.getMessagecoding())
					.setUserCount(userCount).setMessageLength(c.length())
					.build(smcExpressData);
			pkList.add(pack);
		}
		return pkList;
	}
}
