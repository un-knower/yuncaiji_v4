package cn.uway.ucloude.rpc.netty;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.ChannelFuture;

public class NettyChannelFuture implements ChannelFuture {

	private io.netty.channel.ChannelFuture channelFuture;

    public NettyChannelFuture(io.netty.channel.ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    @Override
    public boolean isConnected() {
        return channelFuture.channel() != null && channelFuture.channel().isActive();
    }

    @Override
    public Channel getChannel() {
        return new NettyChannel(channelFuture.channel());
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return channelFuture.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public boolean isDone() {
        return channelFuture.isDone();
    }

    @Override
    public Throwable cause() {
        return channelFuture.cause();
    }

}
