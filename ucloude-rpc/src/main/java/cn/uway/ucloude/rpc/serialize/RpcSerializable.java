package cn.uway.ucloude.rpc.serialize;

import cn.uway.ucloude.rpc.RpcConfigKeys;
import cn.uway.ucloude.container.SPI;

@SPI(key = RpcConfigKeys.RPC_SERIALIZABLE_DFT, dftValue = "fastjson")
public interface RpcSerializable {
	int getId();

    byte[] serialize(final Object obj) throws Exception;

    <T> T deserialize(final byte[] data, Class<T> clazz) throws Exception;
}
