package cn.uway.ucloude.rpc;

import java.net.SocketAddress;

/**
 * 通道接口
 * @author uway
 *
 */
public interface Channel {
	SocketAddress localAddress();

    SocketAddress remoteAddress();

    ChannelHandler writeAndFlush(Object msg);

    ChannelHandler close();

    boolean isConnected();

    boolean isOpen();

    boolean isClosed();
}
