package cn.uway.ucloude.rpc.netty;

import java.nio.ByteBuffer;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.configuration.BasicConfiguration;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.codec.Codec;
import cn.uway.ucloude.rpc.common.RpcHelper;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyCodecFactory {
	private static final ILogger LOGGER = LoggerManager.getLogger(NettyCodecFactory.class);

    private Codec codec;
    private BasicConfiguration configuration;

    public NettyCodecFactory(BasicConfiguration configuration, Codec codec) {
        this.configuration = configuration;
        this.codec = codec;
    }

    @ChannelHandler.Sharable
    public class NettyEncoder extends MessageToByteEncoder<RpcCommand> {
        @Override
        public void encode(ChannelHandlerContext ctx, RpcCommand remotingCommand, ByteBuf out)
                throws Exception {

            if (remotingCommand == null) {
                LOGGER.error("Message is null");
                return;
            }

            try {
                ByteBuffer byteBuffer = codec.encode(remotingCommand);
                out.writeBytes(byteBuffer);
            } catch (Exception e) {
                Channel channel = new NettyChannel(ctx);
                LOGGER.error("encode exception, addr={}, remotingCommand={}", RpcHelper.parseChannelRemoteAddr(channel), remotingCommand.toString(), e);
                RpcHelper.closeChannel(channel);
            }
        }
    }

    public class NettyDecoder extends LengthFieldBasedFrameDecoder {

//        private static final int FRAME_MAX_LENGTH = Constants.DEFAULT_BUFFER_SIZE;

        public NettyDecoder() {
            super(configuration.getParameter("netty.frame.length.max", UCloudeConstants.DEFAULT_BUFFER_SIZE), 0, 4, 0, 4);
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            try {
                ByteBuf frame = (ByteBuf) super.decode(ctx, in);
                if (frame == null) {
                    return null;
                }

                byte[] tmpBuf = new byte[frame.capacity()];
                frame.getBytes(0, tmpBuf);
                frame.release();

                ByteBuffer byteBuffer = ByteBuffer.wrap(tmpBuf);
                return codec.decode(byteBuffer);
            } catch (Exception e) {
                Channel channel = new NettyChannel(ctx);
                LOGGER.error("decode exception, {}", RpcHelper.parseChannelRemoteAddr(channel), e);
                // 这里关闭后， 会在pipeline中产生事件，通过具体的close事件来清理数据结构
                RpcHelper.closeChannel(channel);
            }

            return null;
        }
    }

    public ChannelHandler getEncoder() {
        return new NettyEncoder();
    }

    public ChannelHandler getDecoder() {
        return new NettyDecoder();
    }
}
