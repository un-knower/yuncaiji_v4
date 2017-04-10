package cn.uway.ucloude.rpc.exception;

/**
 * RPC调用中，客户端发送请求失败，抛出此异常
 */
public class RpcSendRequestException extends RpcException {
	private static final long serialVersionUID = 5391285827332471674L;

    public RpcSendRequestException(String addr) {
        this(addr, null);
    }

    public RpcSendRequestException(String addr, Throwable cause) {
        super("send request to <" + addr + "> failed", cause);
    }
}
