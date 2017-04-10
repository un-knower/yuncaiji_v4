package cn.uway.ucloude.rpc.netty;

import cn.uway.ucloude.rpc.ChannelHandler;
import cn.uway.ucloude.rpc.ChannelHandlerListener;
import cn.uway.ucloude.rpc.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NettyChannelHandler implements ChannelHandler {
	 private ChannelFuture channelFuture;

	    public NettyChannelHandler(ChannelFuture channelFuture) {
	        this.channelFuture = channelFuture;
	    }

	    @Override
	    public ChannelHandler addListener(final ChannelHandlerListener listener) {

	        channelFuture.addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(final ChannelFuture future) throws Exception {
	                listener.operationComplete(new Future() {
	                    @Override
	                    public boolean isSuccess() {
	                        return future.isSuccess();
	                    }

	                    @Override
	                    public Throwable cause() {
	                        return future.cause();
	                    }
	                });
	            }
	        });

	        return this;
	    }

}
