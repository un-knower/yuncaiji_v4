package cn.uway.ucloude.rpc;

/**
 * 特征接口
 * @author uway
 *
 */
public interface Future {
	 boolean isSuccess();

	 Throwable cause();
}
