package cn.uway.ucloude.uts.web.access.domain;


/**
 * 锁定类型
 * @author Uway-M3
 *
 */
public enum LockType {
	NONE(0, "不锁定"), LOCKIP(1, "锁定IP"), LOCKUSERNO(2, "锁定用户名");

	private final int value;

	public int getValue() {
		return value;
	}

	public String getText() {
		return text;
	}

	private final String text;

	LockType(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public static LockType getLockType(int value) {
		LockType[] lockTypes = LockType.values();
		for (LockType item : lockTypes) {
			if (item.getValue() == value)
				return item;
		}
		return null;
	}
}
