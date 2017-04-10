package cn.uway.ucloude.alarm;

public interface AlarmNotifier<T extends AlarmMessage> {
	 /**
     * 告警发送通知
     */
    void notice(T message);
}
