package cn.uway.ucloude.rpc.exception;

/**
 * Client连接Server失败，抛出此异常
 */
public class RpcConnectionException extends RpcException {
	private static final long serialVersionUID = -5565366231695911316L;

    public RpcConnectionException(String addr) {
        this(addr, null);
    }

    public RpcConnectionException(String addr, Throwable cause) {
        super("connect to <" + addr + "> failed", cause);
    }
}
