package cn.uway.framework.orientation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.framework.context.AppContext;

/**
 * 栅格grid_id,grid_m,grid_n计算
 * 
 * @author tianjing @ 2014-3-5
 */
public class GridOrientation {

	/**
	 * 赤道半径
	 */
	public static final double EQUATOR_RADIUS = 6378137.00;

	/**
	 * 极半径
	 */
	public static final double POLE_RADIUS = 6356725;

	/**
	 * 圆周率
	 */
	public static final double PI = Math.PI;

	/**
	 * 角度和弧度转换系数
	 */
	public static final double RADIAN = PI / 180;

	/**
	 * 定义栅格格子为100*100大小
	 */
	public static int GRID_LEN = 100;

	/**
	 * 定义栅格格子为800*800大小
	 */
	public static int GRID_LEN_400 = 400;

	/**
	 * 定义栅格格子为800*800大小
	 */
	public static int GRID_LEN_800 = 800;
	
	/**
	 * grid经度位数(重保项目9位)
	 */
	public static int GRID_ID_LONGITUDE_LENGTH = 7;
	
	/**
	 * grid纬度位数(重保项目8位)
	 */
	public static int GRID_ID_LATITUDE_LENGTH = 6;
	
	/**
	 * 掩码
	 */
	public static long GRID_ID_MASK = (long)Math.pow(10, Math.max(GRID_ID_LONGITUDE_LENGTH, GRID_ID_LATITUDE_LENGTH)-1);
	

	private static final Logger LOGGER = LoggerFactory.getLogger(GridOrientation.class);

	static {
		String gridSize = AppContext.getBean("gridSize", String.class);
		if (gridSize != null) {
			String[] sizes = gridSize.split("\\,");
			if (sizes != null && sizes.length >= 3) {
				GRID_LEN = Integer.parseInt(sizes[0]);
				GRID_LEN_400 = Integer.parseInt(sizes[1]);
				GRID_LEN_800 = Integer.parseInt(sizes[2]);
			}
			
			if (sizes != null && sizes.length >= 5) {
				GRID_ID_LONGITUDE_LENGTH = Integer.parseInt(sizes[3]);
				GRID_ID_LATITUDE_LENGTH = Integer.parseInt(sizes[4]);
				GRID_ID_MASK = (long)Math.pow(10, Math.max(GRID_ID_LONGITUDE_LENGTH, GRID_ID_LATITUDE_LENGTH)-1);
			}
		}
		
		LOGGER.debug("栅格尺寸定义：{},{},{} 栅格id长度为经度{}位，纬度{}位", new Object[]{GRID_LEN, GRID_LEN_400, GRID_LEN_800, GRID_ID_LONGITUDE_LENGTH, GRID_ID_LATITUDE_LENGTH});
	}
	
	
	/**
	 * 
	 * @param longitude1
	 *            城市记录的经度
	 * @param longitude
	 *            ms定位的经度
	 * @param latitude
	 *            ms定位的纬度
	 * @param latitude1
	 *            城市记录的纬度
	 * @return String[] 长度3，分别:gridInfos[0]=GRID_M, gridInfos[1]=GRID_N, gridInfos[2]=GRID_ID
	 */
	public static String[] orientationGridInfo(double longitude, double latitude, double longLt, double longRt,
			double latLt, double latRt, int cityMaxGridM, int cityMaxGridN) {
		String[] gridInfos = new String[3];
		// 根据城市记录的最小经度和MS定位的经度，计算MS属于哪个栅格格子。
		int gridM = distanceLongitude(longLt, longitude, latitude, GRID_LEN);
		if (longitude == longRt) {
			gridM = gridM - 1;
		}else if(gridM > cityMaxGridM){
			gridM = cityMaxGridM;
		}
		// 根据城市记录的最小纬度和MS定位的纬度，计算MS属于哪个栅格格子。
		int gridN = distanceLatitude(latLt, latitude, GRID_LEN);
		if(latitude == latRt){
			gridN = gridN - 1;
		}else if(gridN > cityMaxGridN){
			gridN = cityMaxGridN;
		}
		// 根据MS定位的经纬度生成栅格ID。生成原则： longitude = 经度取7位，不足7位补0。latitude = 纬度取6位，不足6位补0。GRID_ID = longitude + latitude
		gridInfos[2] = getGridId(gridM, gridN, longLt, longRt, latLt, latRt,latitude);
		gridInfos[0] = String.valueOf(gridM);
		gridInfos[1] = String.valueOf(gridN);
		return gridInfos;
	}

	/**
	 * 根据城市记录的最小经度和MS定位的经度，计算MS属于哪个栅格格子。
	 * 
	 * @param longitude1
	 *            城市记录的经度
	 * @param longitude2
	 *            ms定位的经度
	 * @param latitude
	 *            ms定位的纬度
	 * @return
	 */
	public final static int distanceLongitude(double longitude1, double longitude2, double latitude, int gridLen) {
		// 求得两个经度之间的差的绝对值<br>
		double dValue = Math.abs(longitude1 - longitude2);
		// 弧长的计算公式 :弧长等于弧所对的圆心角乘以圆周率乘以半径长再除以180
		// 因为地球从赤道上下弧半径越来越小，需要乘以角度的余弦
		int gridM = (int) ((dValue * PI * EQUATOR_RADIUS / 180 * Math.cos(latitude * RADIAN)) / gridLen);
		if (gridM < 0)
			gridM = 0;
		return gridM;
	}

	

	/**
	 * 根据MS定位的经纬度和城市经纬度算出栅格最小经纬度，取此经纬度生成栅格ID。生成原则： longitude = 经度取7位，不足7位补0。latitude = 纬度取6位，不足6位补0。GRID_ID = longitude + latitude
	 * 
	 * @param gridM 栅格横坐标
	 * @param gridN 栅格竖坐标
	 * @param longLt 城市左上经度
	 * @param longRt 城市右下经度
	 * @param latLt 城市左上纬度
	 * @param latRt 城市右下纬度
	 * @return 栅格ID
	 */
	public final static String getGridId(final int gridM, final int gridN, double longLt, double longRt, double latLt, double latRt,double latitude) {
		// 先计算纬度，再计算经度 不同纬度，相同精度差的距离不一致
		// 如果左上角纬度大于右下角纬度 则经度递减，否则递增
		double startLat, startLong;
		if (latLt > latRt) {
			startLat = subLatitude(latLt, gridN * GRID_LEN);
		} else {
			startLat = addLatitude(latLt, gridN * GRID_LEN);
		}
		
		// 如果左上角经度大于右下角经度 则经度递减，否则递增
		if (longLt > longRt) {
			startLong = subLongitude(longLt, startLat/*latitude*/, gridM * GRID_LEN);
		} else {
			startLong = addLongitude(longLt, startLat/*latitude*/, gridM * GRID_LEN);
		}
		String longtitudeStr = String.valueOf((long) (startLong * GRID_ID_MASK)).substring(0, GRID_ID_LONGITUDE_LENGTH);
		String latitudeStr = String.valueOf((long) (startLat * GRID_ID_MASK)).substring(0, GRID_ID_LATITUDE_LENGTH);
		
		StringBuilder strBuilder = new StringBuilder();
		return strBuilder.append(longtitudeStr).append(latitudeStr).toString();
	}

	
	public static void main(String [] args){
		//119.02668,31.65163
		//118.79084321665437,32.04649660652364
		String gridId = getGridId(657,475,31.65163,118.1019444,119.2153472,32.6664583,31.2329167);
		System.out.println(gridId);
		
		int gridM = distanceLongitude(118.77965,118.1019444,32.05269,100);
		System.out.println(gridM);
	}
	
	/**
	 * 栅格最小经纬度，取此经纬度生成栅格ID。生成原则： longitude = 经度取7位，不足7位补0。latitude = 纬度取6位，不足6位补0。GRID_ID = longitude + latitude
	 * 
	 * @param latitude
	 * @param longtitude
	 * @return 栅格ID
	 */
	public final static String getGridId(double startLat, double startLong) {
		String longtitudeStr = String.valueOf((long) (startLong * GRID_ID_MASK)).substring(0, GRID_ID_LONGITUDE_LENGTH);
		String latitudeStr = String.valueOf((long) (startLat * GRID_ID_MASK)).substring(0, GRID_ID_LATITUDE_LENGTH);
		StringBuilder strBuilder = new StringBuilder();
		return strBuilder.append(longtitudeStr).append(latitudeStr).toString();
	}

	/**
	 * 指定纬度，在经度上增加一定距离<br>
	 * 
	 * @param longtitude
	 * @param latitude
	 * @param distance
	 * @return 增加一定距离后的经度
	 */
	public final static double addLongitude(double longtitude, double latitude, int distance) {
		return distance * 180 / Math.cos(latitude * RADIAN) / PI / EQUATOR_RADIUS + longtitude;
	}
	
	/**
	 * 指定纬度，在经度上减去一定距离<br>
	 * 
	 * @param longtitude
	 * @param latitude
	 * @param distance
	 * @return 减去一定距离后的经度
	 */
	public final static double subLongitude(double longtitude, double latitude, int distance) {
		return longtitude - distance * 180 / Math.cos(latitude * RADIAN) / PI / EQUATOR_RADIUS;
	}

	/**
	 * 指定经度，在纬度上增加一定距离<br>
	 * 
	 * @param latitude
	 * @param distance
	 * @return 增加一定距离后的纬度
	 */
	public final static double addLatitude(double latitude, int distance) {
		return distance * 180 / PI / POLE_RADIUS + latitude;
	}

	/**
	 * 指定经度，在纬度上减去一定距离<br>
	 * 
	 * @param latitude
	 * @param distance
	 * @return 减去一定距离后的纬度
	 */
	public final static double subLatitude(double latitude, int distance) {
		return latitude - distance * 180 / PI / POLE_RADIUS;
	}
	
	/**
	 * 根据城市记录的最小纬度和MS定位的纬度，计算MS属于哪个栅格格子。
	 * 
	 * @param latitude1
	 *            城市记录的纬度
	 * @param latitude2
	 *            ms定位的纬度
	 * @return
	 */
	public final static int distanceLatitude(double latitude1, double latitude2, int gridLen) {
		// 求得两个经度之间的差的绝对值<br>
		double dValue = Math.abs(latitude1 - latitude2);
		// 弧长的计算公式 :弧长等于弧所对的圆心角乘以圆周率乘以半径长再除以180
		// 因为地球从赤道上下弧半径越来越小，需要乘以角度的余弦
		int gridN = (int) ((dValue * PI * POLE_RADIUS / 180) / gridLen);
		if (gridN < 0)
			gridN = 0;
		return gridN;
	}
	
	/*
	 * 经纬度间的距离计算
	 */
	public final static double CalcDistance(double lon1, double lat1, double lon2, double lat2)
	{
		double Rc = EQUATOR_RADIUS; 	//6378137.00 ; // 赤道半径
		double Rj = POLE_RADIUS ;		//6356725; // 极半径

		double radLo1 = lon1 * PI/180;
		double radLa1 = lat1 * PI/180;
		double Ec1 = Rj + (Rc - Rj) * (90.-lat1) / 90.;
		double Ed1 = Ec1 * Math.cos(radLa1);

		double radLo2 = lon2 * PI/180;
		double radLa2 = lat2 * PI/180;


		double dx = (radLo2 - radLo1) * Ed1;
		double dy = (radLa2 - radLa1) * Ec1;
		double dDeta = Math.sqrt(dx * dx + dy * dy);

		return dDeta;
	}
}
