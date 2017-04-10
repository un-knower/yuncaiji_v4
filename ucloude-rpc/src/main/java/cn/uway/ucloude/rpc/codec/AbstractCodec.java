package cn.uway.ucloude.rpc.codec;

import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.rpc.serialize.AdaptiveSerializable;
import cn.uway.ucloude.rpc.serialize.RpcSerializable;

public abstract class AbstractCodec implements Codec {

	  protected RpcSerializable getRpcSerializable(int serializableTypeId) {

		  RpcSerializable serializable = null;
	        if (serializableTypeId > 0) {
	            serializable = AdaptiveSerializable.getSerializableById(serializableTypeId);
	            if (serializable == null) {
	                throw new IllegalArgumentException("Can not support Rpc Serializable that serializableTypeId=" + serializableTypeId);
	            }
	        } else {
	            serializable = ServiceFactory.load(RpcSerializable.class, "adaptive");
	        }
	        return serializable;
	    }

}
