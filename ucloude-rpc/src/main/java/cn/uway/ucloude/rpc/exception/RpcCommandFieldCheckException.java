package cn.uway.ucloude.rpc.exception;

import java.io.Serializable;

/**
 * 命令解析自定义字段时，校验字段有效性抛出异常
 */
public class RpcCommandFieldCheckException extends Exception implements Serializable {
	private static final long serialVersionUID = -3040346783583325400L;

	public RpcCommandFieldCheckException(String message) {
        super(message);
    }

    public RpcCommandFieldCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
