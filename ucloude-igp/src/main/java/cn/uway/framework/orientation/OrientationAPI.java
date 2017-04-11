package cn.uway.framework.orientation;

import cn.uway.framework.orientation.Type.CELL_INFO;
import cn.uway.framework.orientation.Type.CELL_TYPE;
import cn.uway.framework.orientation.Type.COVERAGE_AREA;
import cn.uway.framework.orientation.Type.DEV_TYPE;
import cn.uway.framework.orientation.Type.DiffuseInfo;
import cn.uway.framework.orientation.Type.LONG_LAT;
import cn.uway.framework.orientation.Type.ONEWAYDELAY_CELL;
import cn.uway.framework.orientation.Type.WcdmaMr_Rscp_CELL;
import cn.uway.util.Util;

public final class OrientationAPI {

	private static DiffuseInfo deffuseinfo = DiffuseInfo.DEFAULT_VAL;
	
	static {
		//加载c++定位算法动态库
		if(Util.is32Digit()){
			System.loadLibrary("java_orientation_x86");
		}else{
			System.loadLibrary("java_orientation_x64");
		}
	}
	
	public static void orientation(DEV_TYPE dt, ONEWAYDELAY_CELL[] cells, int count, DiffuseInfo diff, LONG_LAT ll) throws Exception {
		boolean b = LocalOperation.DistanceLocalOperation(cells, count, new OneWayDelayDist(dt, diff), ll);
		if (!b)
			throw new Exception("定位调用返回false.");
	}

	public static void WcdmaMrLevelWeighOrien(WcdmaMr_Rscp_CELL[] cells, int count, DiffuseInfo diff, LONG_LAT ll) throws Exception {
		boolean b = LocalOperation.ElectricLevelLocalOperation(cells, count, new WcdmaMrRscpDist(null, diff), ll);
		if (!b)
			throw new Exception("定位调用返回false.");
	}

	public static void LteTaOrientation(long CONNECTION_TA, double azimuth, double longitude, double latitude, LONG_LAT outll) {
		LocalOperation.lteTaLocationOperate(CONNECTION_TA, azimuth, longitude, latitude, outll);
	}
	
	/**
	 * c++定位算法 c网
	 * @param cells
	 * @param dist
	 * @param ll
	 * @return
	 */
	public static native boolean doLocation_C(ONEWAYDELAY_CELL[] cells, DistType dist, LONG_LAT ll);
	
	/**
	 * c++定位算法 LTE网 
	 * @param cells
	 * @param dist
	 * @param ll
	 * @return
	 */
	public static native boolean doLocation_LTE(ONEWAYDELAY_CELL[] cells, DistType dist, LONG_LAT ll);

	/**
	 * 将RSCP转换为onewaydelay
	 * 
	 * @param rscp
	 * @param radius
	 * @return onewaydelay
	 */
	public static float rscpToDelay(double rscp, float radius) {
		double distance = Distance.DistanceByRscp(rscp, radius);
		return (float) ((deffuseinfo.amend + distance) / 244 * 8 - 1);
	}

	public static CELL_TYPE convertCellType(int neTableSiteType) {
		switch (neTableSiteType) {
//			case 1 :// 微蜂窝
//				return CELL_TYPE.BEEHIVE;
			case 2 :// 室内分布
				return CELL_TYPE.INDOOR;
			default :
				return CELL_TYPE.OUTDOOR;
		}
	}
	
	public static CELL_TYPE convertCellTypeLTE(String neTableSiteType) {
		if("室内".equalsIgnoreCase(neTableSiteType))
			return CELL_TYPE.INDOOR;
		else
			return CELL_TYPE.OUTDOOR;
	}
	
	//lte定位使用
	public static COVERAGE_AREA convertCoverageType(String coverage) {
		if ("郊区".equals(coverage))
			return COVERAGE_AREA.SUBURBS;
		else
			return COVERAGE_AREA.DOWNTOWN;  //城区
	}

	public static void main(String[] args) throws Exception {
		LONG_LAT ll = new LONG_LAT();
		WcdmaMr_Rscp_CELL cell1 = new WcdmaMr_Rscp_CELL();
		cell1.cell_info = new CELL_INFO();
		cell1.cell_info.CellType = CELL_TYPE.OUTDOOR;
		cell1.cell_info.LatLong = new LONG_LAT(11, 11, 11);
		cell1.rscp = 11;
		OrientationAPI.WcdmaMrLevelWeighOrien(new WcdmaMr_Rscp_CELL[]{cell1}, 1, DiffuseInfo.DEFAULT_VAL, ll);
		System.out.println(ll);
	}
}
