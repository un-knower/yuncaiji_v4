package cn.uway.ucloude.rpc;

/**
 * 通道处理器接口
 * @author uway
 *
 */
public interface ChannelHandler {

    ChannelHandler addListener(ChannelHandlerListener listener);

}
