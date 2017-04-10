package cn.uway.ucloude.rpc;

/**
 * 远程通信事件
 * @author uway
 *
 */
public class RpcEvent {
	private final RpcEventType type;
    private final String remoteAddr;
    private final Channel channel;

    public RpcEvent(RpcEventType type, String remoteAddr, Channel channel) {
        this.type = type;
        this.remoteAddr = remoteAddr;
        this.channel = channel;
    }

    public RpcEventType getType() {
        return type;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "RpcEvent [type=" + type + ", remoteAddr=" + remoteAddr + ", channel=" + channel + "]";
    }
}
