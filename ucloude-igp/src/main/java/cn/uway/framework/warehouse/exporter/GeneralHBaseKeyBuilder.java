package cn.uway.framework.warehouse.exporter;

import org.apache.hadoop.hbase.util.Bytes;


/**
 *	普通HBASE RAW KEY生成器
 *	分区序号(1Byte) + {索引主键、...} + CITYBSC(4Byte) + RAWFILE(4Byte) + CDR_ID(4Byte) 
 * @ 2014-6-10
 */
public class GeneralHBaseKeyBuilder extends AbsHBaseKeyBuilder {
	//主表键字节长度
	private static final int MAIN_TABLE_KEY_BYTES_LEN = (1+4+4+4);


	@Override
	public byte[] buildKey(byte tabIndex, int rawFileKey1, int rawFileKey2,
			int cdrId, byte partitionNum, byte[][] indexKeys) {
		int keyLength = MAIN_TABLE_KEY_BYTES_LEN;
		if (indexKeys != null) {
			for (byte[] keyBytes: indexKeys) {
				keyLength += keyBytes.length;
			}
		}
		byte[] key = new byte[keyLength];
		
		//1 BYTE 分区序号
		int offset = Bytes.putByte(key, 0, (byte)(cdrId % partitionNum));
		//1 BYTE 表序号 (去掉)
		//offset = Bytes.putByte(key, offset, tabIndex);
		
		//索引主键
		if (indexKeys != null) {
			for (byte[] keyBytes: indexKeys) {
				offset = Bytes.putBytes(key, offset, keyBytes, 0, keyBytes.length);
			}
		}
		
		// 4 BYTE RAW FILE KEY1
		offset = Bytes.putInt(key, offset, rawFileKey1);
		// 4 BYTE RAW FILE KEY2
		offset = Bytes.putInt(key, offset, rawFileKey2);
		// 4 BYTE CDR ID
		Bytes.putInt(key, offset, cdrId);
		return key;
	}

}
