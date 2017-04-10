package cn.uway.ucloude.message.sender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.message.SMCCfgSys;
import cn.uway.ucloude.message.SMCData;
import cn.uway.ucloude.message.SMGPInterfaceMgr;
import cn.uway.ucloude.message.pack.AbsSmcPackage;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.DateUtil.TimePattern;
import cn.uway.ucloude.utils.StringUtil;

public class SMGPSender extends AbstractSender {

	private static Logger LOG = LoggerFactory.getLogger(SMGPSender.class);

	@Override
	public int send(AbsSmcPackage data) {
		return 0;
	}

	@Override
	public String getMessageId() {
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean sendAll(SMCData smcExpressData) {
		boolean bFlag = false;
		String pNum = buildSmgpSmsPhone(smcExpressData);
		String recvUsers = this.getSmgpPhonePath(smcExpressData, pNum);
		List<String> contentPathList = this.getSmgpContentPath(smcExpressData);
		
		SMCCfgSys smcCfgSys = SMCCfgSys.getInstance();
		int count = 0;
		for (String contentPath : contentPathList) {
			int result = 1;
			try {
				StringBuilder sb = new StringBuilder();

				sb.append("msgType:")
						.append(smcCfgSys.getPropertyInt("smgp_msgType"))
						.append(" ");
				sb.append("needReport:")
						.append(smcCfgSys.getPropertyInt("smgp_needReport"))
						.append(" ");
				sb.append("msgLevel:")
						.append(smcCfgSys.getPropertyInt("smgp_msgLevel"))
						.append(" ");
				sb.append("serviceId:")
						.append(smcCfgSys.getProperty("smgp_serviceId"))
						.append(" ");
				sb.append("msgFormat:")
						.append(smcCfgSys.getPropertyInt("smgp_msgFormat"))
						.append(" ");
				sb.append("feeType:")
						.append(smcCfgSys.getProperty("smgp_feeType"))
						.append(" ");
				sb.append("feecode:")
						.append(smcCfgSys.getProperty("smgp_feecode"))
						.append(" ");
				sb.append("fixedFee:")
						.append(smcCfgSys.getProperty("smgp_fixedFee"))
						.append(" ");
				sb.append("chargeTermId:")
						.append(smcCfgSys.getProperty("smgp_chargeTermId"))
						.append(" ");
				sb.append("spNumber:")
						.append(smcCfgSys.getProperty("smgp_spNumber"))
						.append(" ");

				LOG.debug(sb.toString());
				if (cn.uway.ucloude.utils.StringUtil.isNotEmpty(pNum)) {
					result = SMGPInterfaceMgr.getInstance().sendBatch(
							smcCfgSys.getPropertyInt("smgp_msgType"),
							smcCfgSys.getPropertyInt("smgp_needReport"),
							smcCfgSys.getPropertyInt("smgp_msgLevel"),
							smcCfgSys.getProperty("smgp_serviceId"),
							smcCfgSys.getPropertyInt("smgp_msgFormat"),
							smcCfgSys.getProperty("smgp_feeType"),
							smcCfgSys.getProperty("smgp_feecode"),
							smcCfgSys.getProperty("smgp_fixedFee"), "", "",
							smcCfgSys.getProperty("smgp_chargeTermId"),
							recvUsers, smcCfgSys.getProperty("smgp_spNumber"),
							0, contentPath.getBytes(), "reserve".getBytes());

				}
				
				if (result == 0) {
					LOG.debug(smcExpressData + ",发送成功，并 添加到快递历史表.内容路径： "
							+ contentPath);
					count++;
					sendAfter(0, null);
					bFlag = true;
					deleteFile(contentPath);
				} else {
					bFlag = false;
					LOG.debug(smcExpressData + ",信息发送失败.内容路径： " + contentPath);
					sendAfter(-1, "信息发送失败");
				}

			} catch (Exception e) {
				LOG.error(smcExpressData + ",信息发送失败.", e);
			}

		}
		
		if (count == contentPathList.size())
			deleteFile(recvUsers);
		return bFlag;
	}

	private void deleteFile(String file) {
		File f = new File(file);
		if (f.exists()) {
			f.delete();
		}

	}

	@Override
	public List<AbsSmcPackage> builderPackage(SMCData smcExpressData) {
		return null;
	}

	private String buildSmgpSmsPhone(SMCData smcData) {
		String toUsers = smcData.getToUsers();
		Map<String, List<String>> users = SenderConstDef.getPhoneEmail(toUsers, SenderConstDef.PHONE);
		List<String> userPhoneList = users.get(SenderConstDef.PHONE);
		if (userPhoneList == null)
			return null;

		SMCCfgSys sys = SMCCfgSys.getInstance();
		// 网关是否支持群发，支持群发，就以群发方式发送，如果不支持，将用户号码拆分为多个号码发送
		boolean isGroupSender = sys.isGroupSender();
		List<String> userPhones = new ArrayList<String>();

		int userCount = 0;
		StringBuilder userNumber = new StringBuilder();
		for (String cell : userPhoneList) {
			if (cell == null || (cell = cell.trim()).equals(""))
				continue;
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
			LOG.debug(smcData + ", 发送号码个数为" + userCount);
			userNumber.deleteCharAt(userNumber.length() - 1);
		} else {
			LOG.debug(smcData + ", 用户号码为空");
			return null;
		}
		if (StringUtil.isEmpty(smcData.getContent()))
			return null;

		return userNumber.toString();
	}

	/**
	 * @param smcExpressData
	 * @param userNumber
	 *            用户号码
	 * @return
	 */
	public String getSmgpPhonePath(SMCData smcExpressData, String userNumber) {
		String tempPath = getSmgpTempPath()
				+ File.separator
				+ DateUtil.formatNonException(new Date(),
						TimePattern.yyyyMMddHHmmssSSS);
		String smgpPath = tempPath + "_P_" + smcExpressData.getId()
				+ SenderConstDef.FILE_SUFFIX;
		FileWriter fw = null;
		try {
			File f = new File(smgpPath);
			if (!f.exists())
				f.createNewFile();
			fw = new FileWriter(smgpPath);

			String[] ns = userNumber.split(",");
			for (String s : ns) {
				if (s.startsWith("86"))
					s = s.substring(2);
				fw.write(s);
				fw.write("\n");
			}
			fw.flush();

		} catch (IOException e) {
			LOG.error(smcExpressData + ": 获取SMGP短信电话号码文件路径失败,原因:{}", e);
		} finally {
			try {
				if (fw != null) {
					fw.flush();
					fw.close();
				}
			} catch (IOException e) {
			}

		}
		return smgpPath;

	}

	public List<String> getSmgpContentPath(SMCData smcExpressData) {
		String tempPath = getSmgpTempPath()
				+ File.separator
				+ DateUtil.formatNonException(new Date(),
						TimePattern.yyyyMMddHHmmssSSS);
		List<String> contentPathList = new ArrayList<String>();

		String content = new String(smcExpressData.getContent());

		int contentLen = content.length();
		
		SMCCfgSys sys = SMCCfgSys.getInstance();
		int oneSmsLen = sys.getMessageLength();
		int count = (int) Math.ceil((float) contentLen / (float) oneSmsLen);

		for (int i = 0; i < count; i++) {

			String smgpContentPath = tempPath + "_C_" + smcExpressData.getId()
					+ "_" + (i + 1) + SenderConstDef.FILE_SUFFIX;
			String contentTmp = content.substring(i * oneSmsLen, oneSmsLen
					* (i + 1) > contentLen
					? contentLen
					: (oneSmsLen * (i + 1) > contentLen
							? contentLen
							: oneSmsLen * (i + 1)));
			LOG.error(smcExpressData + " count: " + count + ": " + contentTmp);
			FileWriter fwContent = null;
			try {
				File f = new File(smgpContentPath);
				if (!f.exists())
					f.createNewFile();
				fwContent = new FileWriter(smgpContentPath);
				fwContent.write(contentTmp);

				contentPathList.add(smgpContentPath);

			} catch (IOException e) {
				LOG.error(smcExpressData + ": 获取SMGP短信内容文件路径失败,原因:{}", e);
			} finally {
				try {
					if (fwContent != null) {
						fwContent.flush();
						fwContent.close();
					}
				} catch (IOException e) {

				}
			}
		}

		return contentPathList;
	}

	public static String getSmgpTempPath() {
		//String path = System.getProperty("user.home");
		String path = "." + File.separator + "temp";
		String subPath = "smgp";
		File smsFile = new File(path + File.separator + subPath);
		if (!smsFile.exists() && !smsFile.isDirectory()) {
			smsFile.mkdirs();
		}
		return smsFile.getAbsolutePath();
	}
}
