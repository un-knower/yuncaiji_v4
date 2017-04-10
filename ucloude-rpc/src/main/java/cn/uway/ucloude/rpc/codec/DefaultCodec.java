package cn.uway.ucloude.rpc.codec;

import java.nio.ByteBuffer;

import cn.uway.ucloude.rpc.RpcCommandBody;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.serialize.RpcSerializable;

public class DefaultCodec extends AbstractCodec {

	@Override
	public RpcCommand decode(ByteBuffer byteBuffer) throws Exception {
		// TODO Auto-generated method stub
		int length = byteBuffer.limit();
        int serializableId = byteBuffer.getInt();

        RpcSerializable serializable =
                getRpcSerializable(serializableId);

        int headerLength = byteBuffer.getInt();
        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        RpcCommand cmd = serializable.deserialize(headerData, RpcCommand.class);

        int remaining = length - 4 - 4 - headerLength;

        if (remaining > 0) {

            int bodyLength = byteBuffer.getInt();
            int bodyClassLength = remaining - 4 - bodyLength;

            if (bodyLength > 0) {

                byte[] bodyData = new byte[bodyLength];
                byteBuffer.get(bodyData);

                byte[] bodyClassData = new byte[bodyClassLength];
                byteBuffer.get(bodyClassData);

                cmd.setBody((RpcCommandBody) serializable.deserialize(bodyData, Class.forName(new String(bodyClassData))));
            }
        }
        return cmd;
	}

	@Override
	public ByteBuffer encode(RpcCommand RpcCommand) throws Exception {
		// TODO Auto-generated method stub
		RpcSerializable serializable =
                getRpcSerializable(RpcCommand.getSid());

        // header length size
        int length = 4;

        // serializable id (int)
        length += 4;

        //  header data length
        byte[] headerData = serializable.serialize(RpcCommand);
        length += headerData.length;

        byte[] bodyData = null;
        byte[] bodyClass = null;

        RpcCommandBody body = RpcCommand.getBody();

        if (body != null) {
            // body data
            bodyData = serializable.serialize(body);
            length += bodyData.length;

            bodyClass = body.getClass().getName().getBytes();
            length += bodyClass.length;

            length += 4;
        }

        ByteBuffer result = ByteBuffer.allocate(4 + length);

        // length
        result.putInt(length);

        // serializable Id
        result.putInt(serializable.getId());

        // header length
        result.putInt(headerData.length);

        // header data
        result.put(headerData);

        if (bodyData != null) {
            //  body length
            result.putInt(bodyData.length);
            //  body data
            result.put(bodyData);
            // body class
            result.put(bodyClass);
        }

        result.flip();

        return result;
	}

}
