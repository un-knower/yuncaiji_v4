package cn.uway.framework.orientation;

public class CellInfoEntity {

	/**
	 * 小区的类型
	 */
	public CellType cellType;

	/**
	 * 小区的经纬度
	 */
	public LonLatInfo lonLatInfo = new LonLatInfo();
	
	/**
	 * 电频强度
	 */
	public double rscp;

	/**
	 * 高度
	 */
	public double height;

	/**
	 * 天线方位角
	 */
	public double angle;

	/**
	 * 信号覆盖范围角
	 */
	public double angleRang;

	/**
	 * 小区信号最远覆盖距离
	 */
	public double radius;

	public CellInfoEntity() {
		super();
	}
	
	public CellInfoEntity(LonLatInfo lonLatInfo, double rscp, double angle, double angleRang, double radius) {
		super();
		this.lonLatInfo = lonLatInfo;
		this.rscp = rscp;
		this.angle = angle;
		this.angleRang = angleRang;
		this.radius = radius;
	}
}
