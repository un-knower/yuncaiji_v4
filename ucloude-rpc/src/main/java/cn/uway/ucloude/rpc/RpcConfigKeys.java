package cn.uway.ucloude.rpc;

public interface RpcConfigKeys {
    /**
     * JobClient,JobTracker,TaskTracker端: 远程通讯序列化方式, 可选值 fastjson
     */
	String RPC_SERIALIZABLE_DFT = "ucloude.remoting.serializable.default";
}
