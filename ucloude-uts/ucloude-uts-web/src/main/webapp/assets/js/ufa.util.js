/**
 * js工具
 */
var ufaUtil = {
	/**
	 * 判断是否为整数
	 * @param obj 传入参数必须为number
	 * @returns false:非整数
	 */
	isInteger : function(obj) {
		try {
			var number = parseFloat(obj);
			return number % 1 === 0
		} catch (e) {
		}
		return false;
	},

	/**
	 * 获取URL参数
	 * @param name
	 * @returns 有：返回参数值 无：返回null
	 */
	getQueryString : function(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
		var r = window.location.search.substr(1).match(reg);
		if (r != null)
			return unescape(r[2]);
		return null;
	}
}