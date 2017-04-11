package cn.uway.framework.console.command;

class CMDUtil {

	static String costConvert(long mills) {
		String cost = "";
		// 小于一分钟之内使用秒为单位
		if (mills < (1000 * 60))
			cost = Math.round(mills / 1000) + " Sec.";
		// 大于一分钟的使用分钟作为单位
		else {
			cost = Math.round(mills / (1000 * 60)) + " Min.";
		}
		return cost;
	}
}
