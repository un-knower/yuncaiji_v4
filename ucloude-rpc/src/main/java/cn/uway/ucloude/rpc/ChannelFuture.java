package cn.uway.ucloude.rpc;

/**
 * 通道特征
 * @author uway
 *
 */
public interface ChannelFuture {
	boolean isConnected();

    Channel getChannel();

    boolean awaitUninterruptibly(long timeoutMillis);

    boolean isDone();

    Throwable cause();
}
