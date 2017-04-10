package cn.uway.ucloude.rpc;

import java.util.EventListener;

/**
 * 通道监听器
 * @author uway
 *
 */
public interface ChannelHandlerListener extends EventListener {

    void operationComplete(Future future) throws Exception;
}
