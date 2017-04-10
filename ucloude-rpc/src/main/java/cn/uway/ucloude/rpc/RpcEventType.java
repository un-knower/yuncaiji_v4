package cn.uway.ucloude.rpc;

/**
 * 事件类型
 * @author uway
 *
 */
public enum RpcEventType {
	CONNECT,
    CLOSE,
    READER_IDLE,
    WRITER_IDLE,
    ALL_IDLE,
    EXCEPTION
}
