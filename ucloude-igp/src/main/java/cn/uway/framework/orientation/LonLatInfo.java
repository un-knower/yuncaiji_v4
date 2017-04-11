package cn.uway.framework.orientation;

public class LonLatInfo {

	/**
	 * 默认：基站与小区间的经纬度差值。
	 */
	public static final double DIFFERENCE = 0.00005;

	/**
	 * 经度
	 */
	public double lon;

	/**
	 * 纬度
	 */
	public double lat;

	/**
	 * 定位经纬度与小区经纬度之间距离的导数。
	 */
	public double distance;
	
	public LonLatInfo() {
		super();
	}

	public LonLatInfo(double lon, double lat) {
		super();
		this.lon = lon;
		this.lat = lat;
	}
	
}
