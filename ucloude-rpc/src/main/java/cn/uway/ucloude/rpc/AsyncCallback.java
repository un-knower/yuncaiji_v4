package cn.uway.ucloude.rpc;

/**
 * 异步调用应答回调接口
 */
public interface AsyncCallback {
	public void onComplete(final ResponseFuture responseFuture);
}
