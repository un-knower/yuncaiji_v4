package cn.uway.ucloude.alarm;

/**
 * 要保证同一条消息不会被重复发送多次
 * @author uway
 *
 * @param <T>
 */
public abstract class AbstractAlarmNotifier<T extends AlarmMessage> implements AlarmNotifier<T> {
	@Override
	public final void notice(T message) {
		// TODO

		doNotice(message);
	}

	protected abstract void doNotice(T message);
}
