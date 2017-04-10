package cn.uway.ucloude.ec;

/**
 * 事件观察者接口
 * @author uway
 *
 */
public interface IObserver {
	/**
	 * 通知
	 * @param eventInfo
	 */
	public void onObserved(EventInfo eventInfo);
}
