package cn.uway.ucloude.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.message.pack.AbsSmcPackage;
import cn.uway.ucloude.message.pack.SMSPackage;

import spApi.Bind;
import spApi.BindResp;
import spApi.SGIP_Command;
import spApi.Submit;
import spApi.SubmitResp;
import spApi.Unbind;
import spApi.UnbindResp;

/**
 * 
 */
public class SGIPIntegerfaceMgr {

	private Logger LOG = LoggerFactory.getLogger(SGIPIntegerfaceMgr.class);

	private int nodeId; // 序列号：SP节点编号（3）+长途区号（0755）+企业代码（31090），默认为3075531090L

	private Socket socket = null;

	private OutputStream outputStream = null;

	private InputStream inputStream = null;

	private SGIP_Command sgip = null;

	private SGIP_Command tmp = null;

	private Bind bind = null;

	private BindResp bindResp1 = null;

	private Submit submit = null;

	private SubmitResp submitResp = null;

	private Unbind unbind = null;

	private UnbindResp unbindResp = null;

	private int seq1 = 0;

	private int seq2 = 0;

	private int seq3 = 0;

	private String messageId = ""; // 消息ID

	/** 是否和网关有连接 */
	private boolean connectedFlag = false; // 是否连接

	/** 是否登录上网关 */
	public boolean connToGWFlag = false; // 是否连接到网关去了

	private SMCCfgSys sys = SMCCfgSys.getInstance();

	public synchronized static SGIPIntegerfaceMgr getInstance() {
		return IngerfaceMgrContain.instance;
	}

	private static class IngerfaceMgrContain {

		private static SGIPIntegerfaceMgr instance = new SGIPIntegerfaceMgr();
	}

	private SGIPIntegerfaceMgr() {
		init();
	}

	private void init() {
		//TODO
		init(sys.getNodeId(), sys.getServerIp(), sys.getServerPort());
		connectToGW(sys.getSmsUserName(), sys.getSmsUserPwd());

	}

	/**
	 * @param nodeId
	 *            序列号：SP节点编号（3）+长途区号（0755）+企业代码（31090），默认为3075531090L
	 * @param serverIp
	 *            网关IP
	 * @param serverPort
	 *            网关端口
	 */
	public void init(long nodeId, String serverIp, int serverPort) {
		this.nodeId = (int) nodeId;
		try {
			socket = new Socket(serverIp, serverPort);
			outputStream = new DataOutputStream(socket.getOutputStream());
			inputStream = new DataInputStream(socket.getInputStream());
			sgip = new SGIP_Command();
		} catch (Exception e) {
			LOG.error("Socket Exception:", e);
			connToGWFlag = false;
		}
	}

	/**
	 * 登陆SMG短信网关
	 * 
	 * @param username
	 *            登录名
	 * @param userpwd
	 *            登录密码
	 */
	private void loginGW(String username, String userpwd) {
		try {
			if (username != null && userpwd != null) {
				bind = new Bind(nodeId, 1, username, userpwd);
				bind.write(outputStream);
				connectedFlag = true;
				LOG.debug(" logingw  bind     :  " + "outputStream "
						+ outputStream + " bind  " + bind.GetFlag() + ""
						+ bind.getCommandID() + " connectedFlag: "
						+ connectedFlag);

			}
		} catch (Exception e) {
			LOG.error("login  Exception:" + e.getMessage());
		}
	}

	/**
	 * 断开与服务器的连接
	 */
	private void exitGW() {
		try {
			unbind = new Unbind(nodeId);
			unbind.write(outputStream);
		} catch (Exception e) {
			LOG.error("断开网关异常： " + e.getMessage());
		}
	}

	/**
	 * 读取状态报告，消息(登录，退出，短消息)发送完后，立即读取其返回状态报告，该报告是同步返回的 如果respId值被置为0则发送成功，其他值则失败
	 */
	private synchronized int readPack() {
		int result = 1;
		try {
			LOG.debug("readPack  begin  ,inputStream :  " + inputStream
					+ " SGIP_Command : " + sgip + " inputStream  size : "
					+ inputStream.available());
			tmp = sgip.read(inputStream);

		} catch (Exception exe) {
			LOG.error("Sender异常，Socket关闭,连接网关或接收应答包异常 " + exe.getMessage());
			// closeSocket();// 此方法内部已设connectedFlag=false;
			connectedFlag = false;
			LOG.error("开始进行重新连接短信网关");

			closeSocket();
			init();
			LOG.error("重新连接短信网关结束");
		}

		try {
			int commandID = tmp.getCommandID();
			LOG.debug("读取commandid=" + commandID);
			if (commandID == 2) {
				connectedFlag = false; // 如为2，则没连上网关服务器，连接状态标志改为FALSE
			} else if (commandID == SGIP_Command.ID_SGIP_SUBMIT_RESP)// 为SUBMIT包的应答包
			{
				submitResp = (SubmitResp) tmp;
				submitResp.readbody();
				result = submitResp.getResult();
				seq1 = submitResp.getSeqno_1();
				seq2 = submitResp.getSeqno_2();
				seq3 = submitResp.getSeqno_3();
				messageId = Integer.toHexString(seq1)
						+ Integer.toHexString(seq2) + Integer.toHexString(seq3); // 组合成消息ID
				LOG.debug("应答包 ,状态=" + result + "RecordID = " + messageId);
			} else if (commandID == SGIP_Command.ID_SGIP_BIND_RESP)// 为BIND的应答包
			{
				bindResp1 = (BindResp) tmp;
				bindResp1.readbody();
				result = bindResp1.GetResult();
				LOG.debug("Bind包, Bind_Id=" + result);
			} else if (commandID == SGIP_Command.ID_SGIP_UNBIND_RESP)// 为UNBIND的应答包
			{
				unbindResp = (UnbindResp) tmp;
				result = unbindResp.GetFlag();
				LOG.debug("unBind,Socket关闭" + result);

			}
		} catch (Exception e) {
			LOG.error("Read_Pack2连接网关或接收应答包异常:" + e.getMessage());
			connectedFlag = false;
		}
		return result;
	}

	public synchronized int send(AbsSmcPackage data) {
		int result = 1;
		try {
			if (!(data instanceof SMSPackage))
				return -1;

			if (!connToGWFlag) {
				LOG.debug(" 关闭连接，并连接连接短信网关 ");
				closeSocket();
				init();

			}
			if (!connectedFlag) {
				connectToGW(sys.getSmsUserName(), sys.getSmsUserPwd());

				LOG.debug(" 重新连接连接短信网关 ");
			}

			SMSPackage smsPack = (SMSPackage) data;
			try {

				LOG.debug("发送内容: " + smsPack.getMessageContent());
				// 将消息打包
				submit = new Submit(nodeId, smsPack.getSpNumber(),
						smsPack.getChargeNumber(), smsPack.getUserCount(),
						smsPack.getUserNumber(), smsPack.getCorpId(),
						smsPack.getServiceType(), smsPack.getFeeType(),
						smsPack.getFeeValue(), smsPack.getGivenValue(),
						smsPack.getAgentFlag(), smsPack.getMoRelateToMTFlag(),
						smsPack.getPriority(), smsPack.getExpireTime(),
						smsPack.getScheduleTime(), smsPack.getReportFlag(),
						smsPack.getTpPid(), smsPack.getTpUdhi(),
						smsPack.getMessageCoding(), smsPack.getMessageType(),
						smsPack.getMessageLength(), smsPack.getMessageContent());
				// 将各个字段组合成二进制流的形式
				int subsuc = submit.write(outputStream); // 相当于把消息发送出去
				
				LOG.debug("submit write  end =================== subsuc = "
						+ subsuc);
				if (subsuc == -1) {
					connectedFlag = false; // 如果为-1，则没连上网关服务器，连接状态标志改为FALSE
				}
			} catch (Exception e)// 如异常，则没连上网关服务器，连接状态标志改为FALSE connectsend =
									// false;
			{
				connectedFlag = false; // 如异常，则没连上网关服务器，连接状态标志改为FALSE
				LOG.error("发下行包异常:" + e.getMessage());
			}
			// 发送完后，立即读取其返回状态报告，该报告是同步返回的
			result = readPack();

			LOG.debug("send method readPack  end =================== result = "
					+ result);

			close();

		} catch (Exception e) {
			LOG.error("短信发送失败", e);
		}
		return result;
	}

	/**
	 * 关闭Socket
	 */
	public void closeSocket() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
			if (socket != null) {
				socket.close();
			}
			connectedFlag = false;
			LOG.debug("closeSocket():Socket关闭");
		} catch (Exception e) {
			LOG.error("closeSocket():Socket关闭异常：" + e.toString());
		}
	}

	/**
	 * 连接网关，先发送BIND包，如返回值为0，则连接网关成功，否则失败
	 * 
	 * @param username
	 *            用户名
	 * @param userpwd
	 *            用户密码
	 * @return boolean :true 连接成功，false 连接失败
	 */
	public boolean connectToGW(String username, String userpwd) {
		boolean flag = false;
		try {
			LOG.debug("开始登陆短信网关.");
			loginGW(username, userpwd);
			LOG.debug("登陆短信网关结束，并开始读取登陆状态包信息状态.");
			int result = readPack();
			LOG.debug("读取登陆状态包信息状态结束,状态={}.", result);
			if (result == 0) {
				flag = connToGWFlag = true;
				connectedFlag = true;
			} else {
				flag = connToGWFlag = false;
				connectedFlag = false;
			}
		} catch (Exception e) {
			LOG.error("SendAgent Bind 异常" + e.getMessage());
			flag = connToGWFlag = false;
			connectedFlag = false;
		}

		LOG.debug("连接网关状态: " + (flag ? "成功" : "失败"));

		return flag;
	}

	/**
	 * 从网关断开连接，先发送UNBIND包，如返回值为0，则断开网关成功，否则失败
	 * 
	 * @return
	 */
	public boolean disconnectToGW() {
		int result = 1;
		try {
			if (socket.isConnected() && !socket.isClosed()) {
				LOG.debug("开始退出短信网关(unbing)");
				exitGW();
				LOG.debug("退出短信网关(unbing),并开始读包");
				result = readPack();
				LOG.debug("退出短信网关(unbing),读包结束,状态={}", result);

				connToGWFlag = false; // added by zj
			} else {
				LOG.debug("disconnectToGW 连接已经关闭. ");
			}
		} catch (Exception e) {
			LOG.error("断开网关异常:" + e.getMessage());
		}
		return result == 0;
	}

	public void close() {
		disconnectToGW();
		closeSocket();

	}

	public boolean isConnectedFlag() {
		return connectedFlag;
	}

	public boolean isConnToGWFlag() {
		return connToGWFlag;
	}

	public void sleep(long time) throws InterruptedException {
		Thread.sleep(time);
	}
}
