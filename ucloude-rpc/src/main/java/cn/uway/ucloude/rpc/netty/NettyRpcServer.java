package cn.uway.ucloude.rpc.netty;

import java.net.InetSocketAddress;

import cn.uway.ucloude.configuration.BasicConfiguration;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.rpc.AbstractRpcServer;
import cn.uway.ucloude.rpc.ChannelEventListener;
import cn.uway.ucloude.rpc.RpcEvent;
import cn.uway.ucloude.rpc.RpcEventType;
import cn.uway.ucloude.rpc.common.RpcHelper;
import cn.uway.ucloude.rpc.configuration.ServerConfiguration;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.thread.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class NettyRpcServer extends AbstractRpcServer {

	public static final ILogger LOGGER = AbstractRpcServer.LOGGER;

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossSelectorGroup;
    private final EventLoopGroup workerSelectorGroup;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private BasicConfiguration configuration;

    public NettyRpcServer(BasicConfiguration configuration, ServerConfiguration serverConfig) {
        this(serverConfig, null);
        this.configuration = configuration;
    }

    public NettyRpcServer(ServerConfiguration RpcServerConfig, final ChannelEventListener channelEventListener) {
        super(RpcServerConfig, channelEventListener);
        this.serverBootstrap = new ServerBootstrap();
        this.bossSelectorGroup = new NioEventLoopGroup(1, new NamedThreadFactory("NettyBossSelectorThread_"));
        this.workerSelectorGroup = new NioEventLoopGroup(RpcServerConfig.getServerSelectorThreads(), new NamedThreadFactory("NettyServerSelectorThread_", true));
    }

    @Override
    protected void serverStart() throws RpcException {

        NettyLogger.setNettyLoggerFactory();

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
        		serverConfig.getServerWorkerThreads(),
                new NamedThreadFactory("NettyServerWorkerThread_")
        );

        final NettyCodecFactory nettyCodecFactory = new NettyCodecFactory(configuration, getCodec());

        this.serverBootstrap.group(this.bossSelectorGroup, this.workerSelectorGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 65536)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(this.serverConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                nettyCodecFactory.getEncoder(),
                                nettyCodecFactory.getDecoder(),
                                new IdleStateHandler(serverConfig.getReaderIdleTimeSeconds(),
                                		serverConfig.getWriterIdleTimeSeconds(), serverConfig.getServerChannelMaxIdleTimeSeconds()),//
                                new NettyConnectManageHandler(), //
                                new NettyServerHandler());
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            throw new RpcException("Start Netty server bootstrap error", e);
        }
    }

    @Override
    protected void serverShutdown() throws RpcException{

        this.bossSelectorGroup.shutdownGracefully();
        this.workerSelectorGroup.shutdownGracefully();

        if (this.defaultEventExecutorGroup != null) {
            this.defaultEventExecutorGroup.shutdownGracefully();
        }
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RpcCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcCommand msg) throws Exception {
            processMessageReceived(new NettyChannel(ctx), msg);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(new NettyChannel(ctx));
            LOGGER.info("SERVER : channelRegistered {}", remoteAddress);
            super.channelRegistered(ctx);
        }


        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(new NettyChannel(ctx));
            LOGGER.info("SERVER : channelUnregistered, the channel[{}]", remoteAddress);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            cn.uway.ucloude.rpc.Channel channel = new NettyChannel(ctx);
            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: channelActive, the channel[{}]", remoteAddress);
            super.channelActive(ctx);

            if (channelEventListener != null) {
                putRpcEvent(new RpcEvent(RpcEventType.CONNECT, remoteAddress, channel));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            cn.uway.ucloude.rpc.Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: channelInactive, the channel[{}]", remoteAddress);
            super.channelInactive(ctx);

            if (channelEventListener != null) {
                putRpcEvent(new RpcEvent(RpcEventType.CLOSE, remoteAddress, channel));
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;

                cn.uway.ucloude.rpc.Channel channel = new NettyChannel(ctx);

                final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);

                if (event.state().equals(IdleState.ALL_IDLE)) {
                    LOGGER.warn("SERVER: IDLE [{}]", remoteAddress);
                    RpcHelper.closeChannel(channel);
                }

                if (channelEventListener != null) {
                    RpcEventType rpcEventType = RpcEventType.valueOf(event.state().name());
                    putRpcEvent(new RpcEvent(rpcEventType,
                            remoteAddress, channel));
                }
            }

            ctx.fireUserEventTriggered(evt);
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

            cn.uway.ucloude.rpc.Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RpcHelper.parseChannelRemoteAddr(channel);
            LOGGER.warn("SERVER: exceptionCaught {}", remoteAddress, cause);

            if (channelEventListener != null) {
                putRpcEvent(new RpcEvent(RpcEventType.EXCEPTION, remoteAddress, channel));
            }

            RpcHelper.closeChannel(channel);
        }
    }

}
