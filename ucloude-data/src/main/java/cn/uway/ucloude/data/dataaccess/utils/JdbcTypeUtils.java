package cn.uway.ucloude.data.dataaccess.utils;

import java.util.Date;

public class JdbcTypeUtils {
	public static Long toTimestamp(Date date) {

		if (date == null) {
			return null;
		}
		return date.getTime();
	}

	public static Date toDate(Long timestamp) {
		if (timestamp == null) {
			return null;
		}
		return new Date(timestamp);
	}
}
