package cn.uway.ucloude.rpc.netty;

import cn.uway.ucloude.configuration.BasicConfiguration;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AbstractRpcClient;
import cn.uway.ucloude.rpc.ChannelEventListener;
import cn.uway.ucloude.rpc.RpcEvent;
import cn.uway.ucloude.rpc.RpcEventType;
import cn.uway.ucloude.rpc.common.RpcHelper;
import cn.uway.ucloude.rpc.configuration.ClientConfiguration;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.rpc.Channel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.SocketAddress;



public class NettyRpcClient extends AbstractRpcClient {
    private static final ILogger LOGGER = LoggerManager.getLogger(NettyRpcClient.class);

    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroup;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private BasicConfiguration configuration;

    public NettyRpcClient(BasicConfiguration configuration, final ClientConfiguration clientConfig) {
        this(clientConfig, null);
        this.configuration = configuration;
    }

    public NettyRpcClient(final ClientConfiguration clientConfig,
                               final ChannelEventListener channelEventListener) {
        super(clientConfig, channelEventListener);

        this.eventLoopGroup = new NioEventLoopGroup(clientConfig.getClientSelectorThreads(), new NamedThreadFactory("NettyClientSelectorThread_", true));
    }

    @Override
    protected void clientStart() throws RpcException {

        NettyLogger.setNettyLoggerFactory();

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                clientConfig.getClientWorkerThreads(),
                new NamedThreadFactory("NettyClientWorkerThread_")
        );

        final NettyCodecFactory nettyCodecFactory = new NettyCodecFactory(configuration, getCodec());

        this.bootstrap.group(this.eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        defaultEventExecutorGroup,
                        nettyCodecFactory.getEncoder(),
                        nettyCodecFactory.getDecoder(),
                        new IdleStateHandler(clientConfig.getReaderIdleTimeSeconds(), clientConfig.getWriterIdleTimeSeconds(), clientConfig.getClientChannelMaxIdleTimeSeconds()),//
                        new NettyConnectManageHandler(),
                        new NettyClientHandler());
            }
        });

    }

    @Override
    protected void clientShutdown() {

        this.eventLoopGroup.shutdownGracefully();

        if (this.defaultEventExecutorGroup != null) {
            this.defaultEventExecutorGroup.shutdownGracefully();
        }
    }

    @Override
    protected cn.uway.ucloude.rpc.ChannelFuture connect(SocketAddress socketAddress) {
        ChannelFuture channelFuture = this.bootstrap.connect(socketAddress);
        return new cn.uway.ucloude.rpc.netty.NettyChannelFuture(channelFuture);
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<RpcCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcCommand msg) throws Exception {
            processMessageReceived(new NettyChannel(ctx), msg);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                            SocketAddress localAddress, ChannelPromise promise) throws Exception {
            final String local = localAddress == null ? "UNKNOW" : localAddress.toString();
            final String remote = remoteAddress == null ? "UNKNOW" : remoteAddress.toString();
            LOGGER.info("CLIENT : CONNECT  {} => {}", local, remote);
            super.connect(ctx, remoteAddress, localAddress, promise);

            if (channelEventListener != null) {
                assert remoteAddress != null;
                putRpcEvent(new RpcEvent(RpcEventType.CONNECT, remoteAddress
                        .toString(), new NettyChannel(ctx)));
            }
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

            Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("CLIENT : DISCONNECT {}", remoteAddress);
            closeChannel(channel);
            super.disconnect(ctx, promise);

            if (channelEventListener != null) {
                putRpcEvent(new RpcEvent(RpcEventType.CLOSE, remoteAddress, channel));
            }
        }


        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("CLIENT : CLOSE {}", remoteAddress);
            closeChannel(channel);
            super.close(ctx, promise);

            if (channelEventListener != null) {
                putRpcEvent(new RpcEvent(RpcEventType.CLOSE, remoteAddress, channel));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

            Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);
            LOGGER.warn("CLIENT : exceptionCaught {}", remoteAddress);
            LOGGER.warn("CLIENT : exceptionCaught exception.", cause);
            closeChannel(channel);
            if (channelEventListener != null) {
                putRpcEvent(new RpcEvent(RpcEventType.EXCEPTION, remoteAddress, channel));
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;

                Channel channel = new NettyChannel(ctx);

                final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);

                if (event.state().equals(io.netty.handler.timeout.IdleState.ALL_IDLE)) {
                    LOGGER.warn("CLIENT : IDLE [{}]", remoteAddress);
                    closeChannel(channel);
                }

                if (channelEventListener != null) {
                    RpcEventType remotingEventType = RpcEventType.valueOf(event.state().name());
                    putRpcEvent(new RpcEvent(remotingEventType,
                            remoteAddress, channel));
                }
            }

            ctx.fireUserEventTriggered(evt);
        }
    }


}
