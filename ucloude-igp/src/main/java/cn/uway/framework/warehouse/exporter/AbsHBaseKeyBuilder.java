package cn.uway.framework.warehouse.exporter;

public abstract class AbsHBaseKeyBuilder {
	
	/**
	 * 
	 * 创建HBASE RAWKEY
	 * @param tabIndex		表序号(0-128)
	 * @param rawFileKey1	原始文件ID 1，占:4Byte 默认城市BSCID(CITYID*10000+BSC/RNC ID)，占:4Byte
	 * @param rawFileKey2	原始文件ID 2，占:4Byte
	 * @param cdrId			记录ID，占:4Byte
	 * @param partitionNum	分区数量(0-128个)，占:1Byte
	 * @param indexKeys		索引主键，可以为0-n个，为空时为主表主键，每个主键在传入时要转换成byte[]；
	 * @return
	 */
	public abstract byte[] buildKey(byte tabIndex, int rawFileKey1, int rawFileKey2, int cdrId,
			byte partitionNum, byte[][] indexKeys);
	
}
