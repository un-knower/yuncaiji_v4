package cn.uway.ucloude.message.sender;

import java.util.List;

import cn.uway.ucloude.message.SMCData;
import cn.uway.ucloude.message.pack.AbsSmcPackage;

public interface ISender {

	public boolean sendAll(SMCData smcExpressData);

	/**
	 * 消息发送方法
	 * 
	 * @param smsPack
	 *            :封装消息的数据包
	 * @return int:0表示成功，其它都表示失败
	 */
	public int send(AbsSmcPackage data);

	/** 返回此消息的消息编号 */
	public String getMessageId();

	// 关闭连接，释放资源
	public void close();

	/** 构建消息包 **/
	public List<AbsSmcPackage> builderPackage(SMCData smcExpressData);
}
