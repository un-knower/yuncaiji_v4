package cn.uway.framework.task;

import java.io.Serializable;

/**
 * 任务附件信息<br>
 * 用户从不同的任务表中加载任务附加信息,如网元信息等<br>
 * 
 * @author chenrongqiang @ 2014-3-26
 */
public class ExtraInfo implements Serializable {

	/**
	 * UUID
	 */
	private static final long serialVersionUID = 7116884368790255675L;

	/**
	 * 城市信息
	 */
	private int cityId;

	/**
	 * 城市英文名缩写
	 */
	private String enName;

	/**
	 * OMC信息
	 */
	private int omcId;

	/**
	 * 对应的BSCID
	 */
	private int bscId;

	/**
	 * 厂家信息<br>
	 */
	private String vendor;

	/**
	 * 网络类型 1：CDMA, 2：GSM, 3：WCDMA, 4：LTE-FDD, 5：LTE-TDD
	 */
	private int netType;

	public ExtraInfo(int cityId, int omcId, int bscId, int netType) {
		super();
		this.cityId = cityId;
		this.omcId = omcId;
		this.bscId = bscId;
		this.netType = netType;
	}

	/**
	 * 构造方法
	 * 
	 * @param cityId
	 *            城市ID
	 * @param omcId
	 *            OMCID
	 * @param bscId
	 * @param vendor
	 */
	public ExtraInfo(int cityId, int omcId, int bscId, String vendor) {
		super();
		this.cityId = cityId;
		this.omcId = omcId;
		this.bscId = bscId;
		this.vendor = vendor;
	}

	/**
	 * @return the cityId
	 */
	public int getCityId() {
		return cityId;
	}

	/**
	 * @param cityId
	 *            the cityId to set
	 */
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	/**
	 * @return enName
	 */
	public String getEnName() {
		return enName;
	}

	/**
	 * @param enName
	 */
	public void setEnName(String enName) {
		this.enName = enName;
	}

	/**
	 * @return the omcId
	 */
	public int getOmcId() {
		return omcId;
	}

	/**
	 * @param omcId
	 *            the omcId to set
	 */
	public void setOmcId(int omcId) {
		this.omcId = omcId;
	}

	/**
	 * @return the bscId
	 */
	public int getBscId() {
		return bscId;
	}

	/**
	 * @param bscId
	 *            the bscId to set
	 */
	public void setBscId(int bscId) {
		this.bscId = bscId;
	}

	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}

	/**
	 * @param vendor
	 *            the vendor to set
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	/**
	 * @return netType
	 */
	public int getNetType() {
		return netType;
	}

	/**
	 * @param netType
	 */
	public void setNetType(int netType) {
		this.netType = netType;
	}

	@Override
	public String toString() {
		return "ExtraInfo [cityId=" + cityId + ", omcId=" + omcId + ", bscId=" + bscId + ", vendor=" + vendor + ", netType=" + netType + "]";
	}
}
