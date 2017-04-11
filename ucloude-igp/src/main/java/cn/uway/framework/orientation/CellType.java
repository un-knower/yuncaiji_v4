package cn.uway.framework.orientation;

import java.util.HashMap;
import java.util.Map;

public enum CellType {
	// REPEATER:直放站，BEEHIVE:微蜂窝，INDOOR:室内分布，OUTDOOR:室外宏站
	REPEATER(1), BEEHIVE(2), INDOOR(3), OUTDOOR(4);

	private final int value;

	public int getValue() {
		return value;
	}

	CellType(int val) {
		this.value = val;
	}

	public static Map<Integer, CellType> map = new HashMap<Integer, CellType>();

	public static CellType getKey(int value) {
		if (map.size() <= 0) {
			for (CellType t : CellType.values()) {
				map.put(t.getValue(), t);
			}
		}
		return map.get(value);
	}
}
