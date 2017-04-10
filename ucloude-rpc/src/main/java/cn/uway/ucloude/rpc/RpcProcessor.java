package cn.uway.ucloude.rpc;

import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;

/**
 * 接收请求处理器，服务器与客户端通用
 * @author uway
 *
 */
public interface RpcProcessor {
    public RpcCommand processRequest(Channel channel, RpcCommand request)
            throws RpcCommandException;
}
