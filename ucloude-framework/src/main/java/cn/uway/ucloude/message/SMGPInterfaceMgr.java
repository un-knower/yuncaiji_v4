package cn.uway.ucloude.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SmgwClient.DeliverResp;
import SmgwClient.ErrorCode;
import SmgwClient.GlobalVar;
import SmgwClient.SmgpMsgID;
import SmgwClient.UserInterface;

/**
 * SMGP电信短信协议消息管理器
 * 
 * @author liuwx 2011-8-8
 */
public class SMGPInterfaceMgr {

	private static Logger LOG = LoggerFactory.getLogger(SMGPInterfaceMgr.class);

	private UserInterface ui = null;

	private ErrorCode err = null;

	private  boolean connectFlag =false;
	
	public synchronized static SMGPInterfaceMgr getInstance() {
		return IngerfaceMgrContain.instance;
	}

	private static class IngerfaceMgrContain {

		private static SMGPInterfaceMgr instance = new SMGPInterfaceMgr();
	}

	private SMGPInterfaceMgr() {
		init();
	}

	/**
	 * 初始化
	 */
	private boolean init() {
		ui = new UserInterface();
		err = new ErrorCode();
		return initSMGPApi();
	}

	/**
	 * 初始化SMGP协议配置参数
	 * 
	 * @return
	 */
	private boolean initSMGPApi() {
		boolean b = true;
		String iniFile = SMCCfgSys.getInstance().getIniFile();
		//int ret = ui.InitSMGPAPI(err);
		int ret = GlobalVar.InitSMGPAPI(err, iniFile);
		LOG.debug(err.GetErrorCodeString());
		if (ret != 0) {
			b = false;
		}
		connectFlag=b; 
		LOG.debug("初始化SMGP协议配置参数:" + (b ? "成功" : "失败"));
		return b;
	}
	
	/**
	 * @param nMsgType
	 *            消息类型
	 * @param nNeedReport
	 *            是否需要状态报
	 * @param nMsgLevel
	 *            消息发送优先级别
	 * @param nServiceID
	 *            业务代码
	 * @param nMsgFormat
	 *            消息格式
	 * @param sFeeType
	 *            计费类型
	 * @param sFeeCode
	 *            计费代码
	 * @param sFixedFee
	 *            固定费用
	 * @param sValidTime
	 *            有效期
	 * @param sAtTime
	 *            定时发送时间
	 * @param sChargeTermID
	 *            计费用户号码
	 * @param sDestTermID
	 *            目的用户号码
	 * @param sReplyPath
	 *            源用户号码 //
	 * @param nMsgLen
	 *            短消息长度，如果该值为0，表示短消息内容在以sMsgContent所表示的文件中 byte[]
	 * @param sMsgContent
	 *            短消息内容 byte[]
	 * @param sReserve
	 *            保留字段 输入/输出参数：SmgpMsgID sMsgID, 短消息ID。
	 * @param sMsgID
	 *            SmgpMsgID是用来保存短消息ID的类。函数调用结束后
	 *            ，通过该类的GetCount方法可得到产生了多少短消息ID，然后通过该类的GetMsgId(int
	 *            i)方法可得到MSGID。
	 * @param nErrorCode
	 *            如SMGPSendSingle
	 * @param nTLVMask
	 *            可选参数掩码
	 * @param nTP_pid
	 *            GSM协议类型
	 * @param nTP_udhi
	 *            GSM协议类型
	 * @param sLinkID
	 *            SPMS分配的关联上下行消息的唯一标识
	 * @param nChargeUserType
	 *            计费用户类型
	 * @param nChargeTermType
	 *            计费用户的号码类型
	 * @param sChargeTermPseudo
	 *            计费用户的伪码
	 * @param nDestTermType
	 *            短消息接收方的号码类型
	 * @param sDestTermPseudo
	 *            短消息接收方的伪码
	 * @param nPkTotal
	 *            相同Msg_Id的消息总条数
	 * @param nPkNumber
	 *            相同Msg_Id的消息序号，从1开始
	 * @param nSubmitMsgType
	 *            消息类型
	 * @param nSPDealResult
	 *            对原请求的处理结果通知
	 * @param sMsgSrc
	 *            信息内容的来源
	 * @param sMServiceID
	 *            业务代码
	 * @return 0：成功；1：失败。
	 */
	public int SMGPSendSingle(String sDestTermID, String msgContent) {
		int ret  =1;
		try {
			SmgpMsgID msgid = new SmgpMsgID();
			
			int nMsgType = 0;

			int nNeedReport = 1;

			int nMsgLevel = 1;

			String nServiceID = "test";

			int nMsgFormat = 15;

			String sFeeType = "01";

			String sFeeCode = "000010";

			String sFixedFee = "000100";

			String sValidTime = "";

			String sAtTime = "";

			String sChargeTermID = SMCCfgSys.getInstance().getProperty("smgp_chargeTermId");

			String sReplyPath = SMCCfgSys.getInstance().getProperty("smgp_spNumber");;

			byte[] sMsgContent = msgContent.getBytes("gbk");;

			int nMsgLen = sMsgContent.length;

			byte[] sReserve = "reserve".getBytes();

			int nTLVMask = 16777215;

			int nTP_pid = 0;

			int nTP_udhi = 1;

			byte[] sLinkID = "89021135".getBytes();//

			int nChargeUserType = 1;

			int nChargeTermType = 1;

			byte[] sChargeTermPseudo = "fbgahdfjkgdahs".getBytes();//

			int nDestTermType = 1;

			byte[] sDestTermPseudo = "dsfdfas".getBytes(); //

			int nPkTotal = 1;

			int nPkNumber = 1;

			int nSubmitMsgType = 1;

			int nSPDealResult = 1;

			byte[] sMsgSrc = "mylove".getBytes();//

			byte[] sMServiceID = "test".getBytes();//
			
			ret = ui.SMGPSendSingle(nMsgType, nNeedReport, nMsgLevel,
					nServiceID, nMsgFormat, sFeeType, sFeeCode, sFixedFee,
					sValidTime, sAtTime, sChargeTermID, sDestTermID, sReplyPath,
					nMsgLen, sMsgContent, sReserve, msgid, err, nTLVMask, nTP_pid,
					nTP_udhi, sLinkID, nChargeUserType, nChargeTermType,
					sChargeTermPseudo, nDestTermType, sDestTermPseudo, nPkTotal,
					nPkNumber, nSubmitMsgType, nSPDealResult, sMsgSrc, sMServiceID);
			
			String info = sChargeTermID + " sDestTermID: " + sDestTermID
					+ " sReplyPath: " + sReplyPath;
			
			String  code =" 短信网管返回状态码： "+ err.GetErrorCodeValue()+" 短信网关返回状态： "+ err.GetErrorCodeString() ; 
			LOG.debug(info + ": "+ code) ; 
			if (ret == 0) {
				for (int i = 0; i < msgid.GetCount(); i++) {
					LOG.debug("短消息ID为：" + rhex(msgid.GetMsgID(i)));
				}
			}
		} catch (UnsupportedEncodingException e) {
			LOG.error("出现异常，"+e);
		}
		try {
			Thread.sleep(50);
		} catch (Exception ex) {

		}
		return ret;
	}
	

	/**
	 * @param nMsgType
	 *            消息类型
	 * @param nNeedReport
	 *            是否需要状态报
	 * @param nMsgLevel
	 *            消息发送优先级别
	 * @param nServiceID
	 *            业务代码
	 * @param nMsgFormat
	 *            消息格式
	 * @param sFeeType
	 *            计费类型
	 * @param sFeeCode
	 *            计费代码
	 * @param sFixedFee
	 *            固定费用
	 * @param sValidTime
	 *            有效期
	 * @param sAtTime
	 *            定时发送时间
	 * @param sChargeTermID
	 *            计费用户号码
	 * @param sDestTermID
	 *            目的用户号码
	 * @param sReplyPath
	 *            源用户号码 //
	 * @param nMsgLen
	 *            短消息长度，如果该值为0，表示短消息内容在以sMsgContent所表示的文件中 byte[]
	 * @param sMsgContent
	 *            短消息内容 byte[]
	 * @param sReserve
	 *            保留字段 输入/输出参数：SmgpMsgID sMsgID, 短消息ID。
	 * @param sMsgID
	 *            SmgpMsgID是用来保存短消息ID的类。函数调用结束后
	 *            ，通过该类的GetCount方法可得到产生了多少短消息ID，然后通过该类的GetMsgId(int
	 *            i)方法可得到MSGID。
	 * @param nErrorCode
	 *            如SMGPSendSingle
	 * @param nTLVMask
	 *            可选参数掩码
	 * @param nTP_pid
	 *            GSM协议类型
	 * @param nTP_udhi
	 *            GSM协议类型
	 * @param sLinkID
	 *            SPMS分配的关联上下行消息的唯一标识
	 * @param nChargeUserType
	 *            计费用户类型
	 * @param nChargeTermType
	 *            计费用户的号码类型
	 * @param sChargeTermPseudo
	 *            计费用户的伪码
	 * @param nDestTermType
	 *            短消息接收方的号码类型
	 * @param sDestTermPseudo
	 *            短消息接收方的伪码
	 * @param nPkTotal
	 *            相同Msg_Id的消息总条数
	 * @param nPkNumber
	 *            相同Msg_Id的消息序号，从1开始
	 * @param nSubmitMsgType
	 *            消息类型
	 * @param nSPDealResult
	 *            对原请求的处理结果通知
	 * @param sMsgSrc
	 *            信息内容的来源
	 * @param sMServiceID
	 *            业务代码
	 * @return 0：成功；1：失败。
	 */
	public int SMGPSendSingle2(int nMsgType, int nNeedReport, int nMsgLevel,
			String nServiceID, int nMsgFormat, String sFeeType,
			String sFeeCode, String sFixedFee, String sValidTime,
			String sAtTime, String sChargeTermID, String sDestTermID,
			String sReplyPath, int nMsgLen, byte[] sMsgContent,
			byte[] sReserve, SmgpMsgID sMsgID, ErrorCode nErrorCode,
			int nTLVMask, int nTP_pid, int nTP_udhi, byte[] sLinkID,
			int nChargeUserType, int nChargeTermType, byte[] sChargeTermPseudo,
			int nDestTermType, byte[] sDestTermPseudo, int nPkTotal,
			int nPkNumber, int nSubmitMsgType, int nSPDealResult,
			byte[] sMsgSrc, byte[] sMServiceID) {
		SmgpMsgID msgid = new SmgpMsgID();
		int ret = ui.SMGPSendSingle(nMsgType, nNeedReport, nMsgLevel,
				nServiceID, nMsgFormat, sFeeType, sFeeCode, sFixedFee,
				sValidTime, sAtTime, sChargeTermID, sDestTermID, sReplyPath,
				nMsgLen, sMsgContent, sReserve, msgid, err, nTLVMask, nTP_pid,
				nTP_udhi, sLinkID, nChargeUserType, nChargeTermType,
				sChargeTermPseudo, nDestTermType, sDestTermPseudo, nPkTotal,
				nPkNumber, nSubmitMsgType, nSPDealResult, sMsgSrc, sMServiceID);
		if (ret == 0) {
			for (int i = 0; i < msgid.GetCount(); i++) {
				LOG.debug("短消息ID为：" + rhex(msgid.GetMsgID(i)));
			}
		}
		try {
			Thread.sleep(50);
		} catch (Exception ex) {

		}
		return ret;
	}

	/**
	 * @param nMsgType
	 *            消息类型
	 * @param nNeedReport
	 *            是否需要状态报告
	 * @param nMsgLevel
	 *            消息发送优先级别
	 * @param nServiceID
	 *            业务代码
	 * @param nMsgFormat
	 *            消息格式
	 * @param sFeeType
	 *            计费类型
	 * @param sFeeCode
	 *            计费代码
	 * @param sFixedFee
	 *            固定费用
	 * @param sValidTime
	 *            有效期
	 * @param sAtTime
	 *            定时发送时间
	 * @param sChargeTermID
	 *            计费用户号码
	 * @param sDestTermIDFile
	 *            目的用户号码
	 * @param sReplyPath
	 *            源用户号码
	 * @param nMsgLen
	 *            短消息长度，如果该值为0，表示短消息内容在以sMsgContent所表示的文件中
	 * @param sMsgContent
	 *            sMsgContent, 短消息内容
	 * @param sReserve
	 *            保留字段 输入/输出参数：SmgpMsgID sMsgID, 短消息ID。SmgpMsgID是用来保存短消息ID的类
	 *            。函数调用结束后，通过该类的GetCount方法可得到产生了多少短消息ID，然后通过该类的GetMsgId(int
	 *            i)方法可得到MSGID。
	 * @param sMsgID
	 *            如SMGPSendSingle。
	 * @param nErrorCode
	 *            可选参数包的长度。
	 * @return 0 1 1 test 15 01 000010 000100 11889021063 num.txt 05100100001 0
	 *         msg.txt "reserve"
	 */
	public synchronized int sendBatch(int nMsgType, int nNeedReport,
			int nMsgLevel, String nServiceID, int nMsgFormat, String sFeeType,
			String sFeeCode, String sFixedFee, String sValidTime,
			String sAtTime, String sChargeTermID, String sDestTermIDFile,
			String sReplyPath, int nMsgLen, byte[] sMsgContent, byte[] sReserve) {
		if (!connectFlag) {
			LOG.debug("SMGP未初始化或初始化失败.") ; 
			return -1;
		}
		
		SmgpMsgID msgid = new SmgpMsgID();
		ErrorCode err = new ErrorCode();
		String info = sChargeTermID + " sDestTermIDFile: " + sDestTermIDFile
				+ " sReplyPath: " + sReplyPath;
		int ret = -1;
		try {
			ret = ui.SMGPSendBatch(nMsgType, nNeedReport, nMsgLevel,
					nServiceID, nMsgFormat, sFeeType, sFeeCode, sFixedFee,
					sValidTime, sAtTime, sChargeTermID, sDestTermIDFile,
					sReplyPath, nMsgLen, sMsgContent, sReserve, msgid, err);
			
			String  code =" 短信网管返回状态码： "+ err.GetErrorCodeValue()+" 短信网关返回状态： "+ err.GetErrorCodeString() ; 
			
			System.out.println(code);
			if (ret == 0) {
				for (int i = 0; i < msgid.GetCount(); i++) {
					LOG.debug("短消息ID为：" + rhex(msgid.GetMsgID(i)));
				}
				LOG.debug(info + ": 成功. "+ code) ; 
			} else {
				LOG.debug(info + ":失败."+ code) ; 
			}
		} catch (Exception e) {
			LOG.error(info + ":失败."+ " 短信网管返回状态码： "+ err.GetErrorCodeValue()+" 短信网关返回状态： "+ err.GetErrorCodeString() , e);
		}
		return ret;

	}

	/**
	 * 下行，接收从网关传递过来的消息
	 * 
	 * @param timeOut
	 *            延迟时间
	 * @param deliverresp
	 *            下行响应
	 * @param err
	 *            错误码
	 * @return
	 */
	public int smgpDeliver(int timeOut, DeliverResp deliverresp, ErrorCode err) {
		return ui.SMGPDeliver(timeOut, deliverresp, err);
	}

	/*
	 * 16进制输出byte[] 例如 byte[0] = 1 byte[1] = 2 ----> 0x3132
	 */

	private static String rhex(byte[] in) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(in));
		String str = "0x";
		try {
			for (int j = 0; j < in.length; j++) {
				String tmp = Integer.toHexString(data.readUnsignedByte());
				if (tmp.length() == 1) {
					tmp = "0" + tmp;
				}
				str = str + tmp;
			}
		} catch (Exception ex) {
			LOG.error("十六进制转换失败.", ex);
		}
		return str;
	}
}
