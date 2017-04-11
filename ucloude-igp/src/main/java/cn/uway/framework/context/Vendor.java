package cn.uway.framework.context;

/**
 * 厂家定义实体<br>
 * 
 * @author chenrongqiang @ 2014-4-1
 */
public class Vendor{

	/**
	 * 厂家定义 爱立信
	 */
	public static final String VENDOR_ERICSSON = "ZY0801";

	/**
	 * 厂家定义 中兴
	 */
	public static final String VENDOR_ZTE = "ZY0804";

	/**
	 * 厂家定义 华为
	 */
	public static final String VENDOR_HW = "ZY0808";

	/**
	 * 厂家定义 阿朗
	 */
	public static final String VENDOR_ALC = "ZY0810";
	
	/**
	 * 厂家定义 上海贝尔
	 */
	public static final String VENDOR_BELL = "ZY0806";

	/**
	 * 厂家定义 诺基亚
	 */
	public static final String VENDOR_NOKIA = "ZY0807";
	
	/**
	 * 厂家定义 大唐
	 */
	public static final String VENDOR_DT = "ZY0802";

	/**
	 * 厂家定义 普天
	 */
	public static final String VENDOR_PT = "ZY0805";
	
	/**
	 * 厂家定义 未知厂家
	 */
	public static final String VENDOR_NULL = "ZY0000";

	/**
	 * 厂家编号获得厂家名称
	 * @param vendor
	 * @return
	 */
	public static String getVendorName(String vendor) {
		if (null == vendor || "".equals(vendor))
			return null;
		vendor = vendor.toUpperCase();
		if (VENDOR_HW.equals(vendor))
			return "华为";
		else if (VENDOR_ZTE.equals(vendor)) {
			return "中兴";
		} else if (VENDOR_ERICSSON.equals(vendor)) {
			return "爱立信";
		} else if (VENDOR_ALC.equals(vendor)) {
			return "阿朗";
		} else if (VENDOR_NOKIA.equals(vendor)) {
			return "诺基亚";
		} else {
			return null;
		}
	}
	
	/**
	 * 厂家编号获得厂家名称
	 * @param vendor
	 * @return
	 */
	public static String getEnVendorName(String vendor) {
		if (null == vendor || "".equals(vendor))
			return null;
		vendor = vendor.toUpperCase();
		if (VENDOR_HW.equals(vendor))
			return "HUAWEI";
		else if (VENDOR_ZTE.equals(vendor)) {
			return "ZTE";
		} else if (VENDOR_ERICSSON.equals(vendor)) {
			return "ERICSSON";
		} else if (VENDOR_ALC.equals(vendor)) {
			return "LUCENT";
		} else if (VENDOR_BELL.equals(vendor)) {
			return "BELL";
		} else if (VENDOR_NOKIA.equals(vendor)) {
			return "NOKIA";
		} else {
			return null;
		}
	}
}
