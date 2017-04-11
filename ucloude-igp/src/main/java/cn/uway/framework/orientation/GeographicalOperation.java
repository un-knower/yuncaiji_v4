package cn.uway.framework.orientation;

import java.util.Random;

public class GeographicalOperation {

	/**
	 * 角度转弧度
	 * 
	 * @param d 角度
	 * @return 弧度值
	 */
	public static final double convertToRadian(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 弧度转角度
	 * 
	 * @param d 弧度
	 * @return 角度值
	 */
	public static final double convertToAngle(double d) {
		return d * 180.0 / Math.PI;
	}
	
	/**
	 * 计算圆心角所对应的弧长
	 * @param centralAngle 圆心角
	 * @param radius 圆半径，单位：千米
	 * @return 两点之间的弧长，单位：千米
	 */
	public static final double arcLenOperationByCentralAngle(double centralAngle, double radius) {
		// l = n（圆心角）x π（圆周率）x r（半径）/180
		return centralAngle * Math.PI * radius / 180.0;
	}
	
	/**
	 * 计算弧长所对应的圆心角
	 * @param arcLen 弧长, 单位：千米
	 * @param radius 圆半径, 单位：千米
	 * @return 弧长所对应的圆心角
	 */
	public static final double centralAngleOperationByArcLen(double arcLen, double radius) {
		// n（圆心角） = l（弧长）x 180 / π（圆周率）/r（半径）
		return arcLen * 180.0 / Math.PI / radius;
	}
	
	/**
	 * 计算两纬度间的弧长
	 * 
	 * @param lat1 纬度1
	 * @param lat2 纬度2
	 * @return 两点纬度之间的弧长, 单位：千米
	 */
	public static final double getArcLenByLatitude(double lat1, double lat2) {
		double n = Math.abs(lat1 - lat2);
		return arcLenOperationByCentralAngle(n, GeographicalFinal.POLE_RADIUS);
	}

	/**
	 * 计算两经度间的弧长
	 * 
	 * @param lon1 纬度1
	 * @param lon2 纬度2
	 * @return 两点经度之间的弧长, 单位：千米
	 */
	public static final double getArcLenByLongtitude(double lon1, double lon2) {
		double n = Math.abs(lon1 - lon2);
		return arcLenOperationByCentralAngle(n, GeographicalFinal.EQUATOR_RADIUS);
	}
	
	/**
	 * 已知弧长和弧上一点，求弧上点上方的另外一点
	 * @param point
	 * @param arcLen 弧长, 单位：千米
	 * @param radius 圆半径, 单位：千米
	 * @return 弧上点上方的另外一点
	 */
	public final static double getOnArcUpPoint(double point, double arcLen, double radius) {
		return point + centralAngleOperationByArcLen(arcLen, radius);
	}

	/**
	 * 已知弧长和弧上一点，求弧上点下方的另外一点
	 * @param point
	 * @param arcLen 弧长, 单位：千米
	 * @param radius 圆半径, 单位：千米
	 * @return 弧上点下方的另外一点
	 */
	public final static double getOnArcDownPoint(double point, double arcLen, double radius) {
		return point - centralAngleOperationByArcLen(arcLen, radius);
	}

	/**
	 * 两点经纬度求距离
	 * 
	 * @param lon 经度
	 * @param lat 纬度
	 * @param lon1 经度1
	 * @param lat1 纬度1
	 * @return 两点距离，单位:千米
	 */
	public static final double distanceOperation(double lon, double lat, double lon1, double lat1) {
		double latRadian = convertToRadian(lat); // 纬度转换弧度
		double lat1Radian = convertToRadian(lat1); // 纬度转换弧度
		double a = latRadian - lat1Radian; // 两点纬度弧度差
		double b = convertToRadian(lon - lon1);
		// google map 算法
		double s = 2.0 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2.0), 2.0) + Math.cos(latRadian) * Math.cos(lat1Radian) * Math.pow(Math.sin(b / 2.0), 2.0)));
		return s * GeographicalFinal.EQUATOR_RADIUS;
	}
	
	public static final double RSCP_MAX = -47.0; //最大电频强度

	public static final double RSCP_MIN = -116.0; //最小电频强度
	/**
	 * 根据电频强度、天线方位角计算出实际小区信号覆盖距离。
	 * 
	 * @param rscp 电频强度
	 * @param radius 天线方位角
	 * @return 实际小区信号覆盖距离
	 */
	public static double operationRealityRadiusByRscp(double rscp, double radius) {
		if (rscp < RSCP_MIN)
			rscp = RSCP_MIN;
		else if(rscp > RSCP_MAX){
			rscp = RSCP_MAX;
		}
		return radius * (Math.abs(rscp) + RSCP_MAX + nextFloat()) / (RSCP_MAX - RSCP_MIN);
	}
	
	/**
	 * 角度进行象限修订
	 */
	public static double quadrantAmendments(double angle) {
		if (0 < angle && angle < 90) {
			angle = 90 - angle;
		} else {
			angle = 450 - angle;
		}
		return angle;
	}
	
	/**
	 * 产生高斯分布随机数
	 * @return
	 */
	public static double nextGaussian(){
		return new Random().nextGaussian();
	}
	
	/**
	 * 产生一个-0.5 ~ 0.5范围内的随机数
	 * @return
	 */
	public static double nextFloat(){
		return new Random().nextFloat() - 0.5;
	}

	public static void main(String[] args) {
		System.out.println(Math.cos(89.6));
	}
}
