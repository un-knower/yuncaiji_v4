package cn.uway.ucloude.rpc.serialize;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class AdaptiveSerializable implements RpcSerializable {

	private final static ILogger LOGGER = LoggerManager.getLogger(RpcSerializable.class);

    private static volatile String defaultSerializable;

    private static final Map<Integer, RpcSerializable>
            ID_SERIALIZABLE_MAP = new HashMap<Integer, RpcSerializable>();

    static {
        Set<String> names = ServiceFactory.getServiceProviders(RpcSerializable.class);
        for (String name : names) {
            if (!UCloudeConstants.ADAPTIVE.equalsIgnoreCase(name)) {
                RpcSerializable serializable = ServiceFactory.load(RpcSerializable.class, name);
                ID_SERIALIZABLE_MAP.put(serializable.getId(), serializable);
            }
        }
    }

    public static RpcSerializable getSerializableById(int id) {
        return ID_SERIALIZABLE_MAP.get(id);
    }

    public static void setDefaultSerializable(String defaultSerializable) {
        AdaptiveSerializable.defaultSerializable = defaultSerializable;
        LOGGER.info("Using defaultSerializable [{}]", defaultSerializable);
    }

    private RpcSerializable getRpcSerializable() {
        RpcSerializable rpcSerializable = null;

        String serializable = defaultSerializable; // copy reference
        if (serializable != null) {
        	rpcSerializable = ServiceFactory.load(RpcSerializable.class, serializable);
        } else {
        	rpcSerializable = ServiceFactory.loadDefault(RpcSerializable.class);
        }
        return rpcSerializable;
    }

    @Override
    public int getId() {
        return getRpcSerializable().getId();
    }

    @Override
    public byte[] serialize(Object obj) throws Exception {
        return getRpcSerializable().serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        return getRpcSerializable().deserialize(data, clazz);
    }

}
