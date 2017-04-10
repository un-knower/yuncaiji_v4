package cn.uway.ucloude.rpc.codec;

import java.nio.ByteBuffer;

import cn.uway.ucloude.rpc.protocal.RpcCommand;

public interface Codec {
	 RpcCommand decode(final ByteBuffer byteBuffer) throws Exception;

	 ByteBuffer encode(final RpcCommand remotingCommand) throws Exception;
}
