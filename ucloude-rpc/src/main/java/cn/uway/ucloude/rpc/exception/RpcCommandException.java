package cn.uway.ucloude.rpc.exception;

public class RpcCommandException extends RpcException {
	 private static final long serialVersionUID = -6061365915274953096L;

    public RpcCommandException(String message) {
        super(message, null);
    }

    public RpcCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
