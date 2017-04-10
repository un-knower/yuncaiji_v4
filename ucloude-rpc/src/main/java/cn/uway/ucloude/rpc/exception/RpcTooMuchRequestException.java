package cn.uway.ucloude.rpc.exception;

/**
 * 异步调用或者Oneway调用，堆积的请求超过信号量最大值
 */
public class RpcTooMuchRequestException extends RpcException {
	   private static final long serialVersionUID = 4326919581254519654L;

	    public RpcTooMuchRequestException(String message) {
	        super(message);
	    }
}
