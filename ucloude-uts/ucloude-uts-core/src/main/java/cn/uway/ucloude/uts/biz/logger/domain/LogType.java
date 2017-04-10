package cn.uway.ucloude.uts.biz.logger.domain;

public enum LogType {

	RECEIVE(0, "接受任务"), // 接受任务
	SENT(1, "发送任务"), // 任务发送 开始执行
	FINISHED(2, "完成任务"), // 任务执行完成
	RESEND(3, "重新发送任务结果"), // TaskTracker 重新发送的任务执行结果
	FIXED_DEAD(4, "修复死任务"), // 修复死掉的任务
	BIZ(5, "业务日志"), // 业务日志
	UPDATE(6, "更新"), // 更新
	DEL(7, "删除"), // 删除
	SUSPEND(8, "暂停"), // 暂停
	RESUME(9, "恢复") // 恢复
	;
	private final int value;

	public int getValue() {
		return value;
	}

	public String getText() {
		return text;
	}

	private final String text;

	LogType(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public static LogType getLogType(int value) {
		LogType[] logTypes = LogType.values();
		for (LogType logType : logTypes) {
			if (logType.getValue() == value)
				return logType;
		}
		return null;
	}
}
