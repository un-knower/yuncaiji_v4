package cn.uway.framework.warehouse.exporter;

import org.apache.hadoop.hbase.util.Bytes;


/**
 *	普通HBASE key生成器
 *  
 * {索引主键、...} 
 * @ 2016-06-15
 */
public class HBaseKeyImsiBuilder extends AbsHBaseKeyBuilder {

	@Override
	public byte[] buildKey(byte tabIndex, int rawFileKey1, int rawFileKey2,
			int cdrId, byte partitionNum, byte[][] indexKeys) {
		int keyLength = 0;
		if (indexKeys != null) {
			for (byte[] keyBytes: indexKeys) {
				keyLength += keyBytes.length;
			}
		}
		byte[] key = new byte[keyLength];
		int offset=0;
		
		//索引主键
		if (indexKeys != null) {
			for (byte[] keyBytes: indexKeys) {
				offset = Bytes.putBytes(key, offset, keyBytes, 0, keyBytes.length);
			}
		}
		//Bytes.putInt(key, offset, cdrId);
		return key;
	}

}
