package cn.uway.framework.orientation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.uway.framework.orientation.Type.CELL_INFO;
import cn.uway.framework.orientation.Type.CellInfoType;
import cn.uway.framework.orientation.Type.DiffuseInfo;
import cn.uway.framework.orientation.Type.GSM_CELL;
import cn.uway.framework.orientation.Type.LONG_LAT;
import cn.uway.framework.orientation.Type.ONEWAYDELAY_CELL;

public final class LocalOperation {

	public static boolean TwoCellLocalOperation(CellInfoType[] cells, int count, DistType dist, LONG_LAT ll) {

		CELL_INFO pcells[] = new CELL_INFO[Type.COUNT_CELL];
		for (int i = 0; i < count; i++) {
			pcells[i] = cells[i].cell_info;
		}

		double dis[] = new double[Type.COUNT_CELL];

		for (int i = 0; i < count; i++) {
			dis[i] = dist.CalcDistance(cells[i]);

		}
		return Local.TwoCell(pcells, dis, count, ll);

	}

	public static boolean DistanceWeightLocalOperation(CellInfoType[] cells, int count, DistType dist, LONG_LAT ll) {
		CELL_INFO pcells[] = new CELL_INFO[Type.COUNT_CELL];
		for (int i = 0; i < count; i++) {
			pcells[i] = cells[i].cell_info;
		}

		double dis[] = new double[Type.COUNT_CELL];

		for (int i = 0; i < count; i++) {
			dis[i] = dist.CalcDistance(cells[i]);
		}

		double weight[] = new double[Type.COUNT_CELL];
		Local.DistanceWeight(dis, weight, count);

		return Local.IncorporateWeight(pcells, weight, count, ll);
	}

	public static int CellsDiffCount(CellInfoType[] p1, CellInfoType[] p2, int count) {
		return Local.diffCount(p1, p2, count);

	}

	public static boolean RepeaterLocalOperation(CellInfoType[] cells, int count, DistType dist, LONG_LAT ll) {
		CELL_INFO pcells[] = new CELL_INFO[Type.COUNT_CELL];
		double dis[] = new double[Type.COUNT_CELL];
		for (int i = 0; i < count; i++) {
			pcells[i] = cells[i].cell_info;
			dis[i] = dist.MapDistance(cells[i]); // DisTanceByOneWayDelay(type,
		}
		return Local.RepeaterOrien(pcells, dis, count, ll);
	}

	public static boolean OnePoint1CellLocalOperation(CellInfoType cell, DistType dist, LONG_LAT ll) {
		double d = dist.CalcDistance(cell);
		Local.OnePoint1Cell(cell.cell_info, d, ll);
		return true;
	}

	public static boolean DistanceLocalOperation(CellInfoType[] cells, int count, DistType dist, LONG_LAT ll) {

		CellInfoType unrepeat_cells[] = new CellInfoType[Type.COUNT_CELL];
		int n = CellsDiffCount(cells, unrepeat_cells, count);
		if (n <= 0) {
			return false;
		}

		boolean rtn = false;
		// ------------------------微蜂窝----------------------------------------------------------------------
		for (int i = 0; i < n; i++) {
			if (Type.CELL_TYPE.BEEHIVE == unrepeat_cells[i].cell_info.CellType) {
				rtn = Local.RandOrien(unrepeat_cells[i].cell_info, ll, unrepeat_cells[i].cell_info.Radius);
				return rtn;
			}
		}
		// -------------------直放站---------------------------------------------------------------------------

		rtn = RepeaterLocalOperation(unrepeat_cells, n, dist, ll);
		if (rtn) {
			return rtn;
		}
		// ----------非直放站，非微蜂窝-------------------------------------------------

		if (1 == n) {
			rtn = OnePoint1CellLocalOperation(unrepeat_cells[0], dist, ll);
		} else if (2 == n) {
			rtn = TwoCellLocalOperation(unrepeat_cells, n, dist, ll);
		} else {
			rtn = DistanceWeightLocalOperation(unrepeat_cells, n, dist, ll);
		}

		if (Utility.Distance(unrepeat_cells[0].cell_info.LatLong, ll) > 3200) {
			rtn = Local.RandOrien(unrepeat_cells[0].cell_info, ll, unrepeat_cells[0].cell_info.Radius);
		}

		return rtn;

	}

	public static boolean LevelWeightLocalOperation(CellInfoType[] cells, int count, DistType dist, LONG_LAT ll) {

		double rscps[] = new double[Type.COUNT_CELL];

		for (int i = 0; i < count; i++) {
			rscps[i] = cells[i].rscp; // /GsmCalcDistance(cells[i], diff);
		}

		double weight[] = new double[Type.COUNT_CELL];
		// DistanceWeight(dis, weight, count);
		Local.ElectricalLevelWeight(rscps, weight, count, dist.m_diff.d1.K);

		CELL_INFO pcells[] = new CELL_INFO[Type.COUNT_CELL];
		for (int i = 0; i < count; i++) {
			pcells[i] = cells[i].cell_info;
		}
		return Local.IncorporateWeight(pcells, weight, count, ll);
	}

	public static boolean ElectricLevelLocalOperation(CellInfoType[] cells, int count, DistType dist, LONG_LAT ll) {
		if (cells == null) {
			throw new IllegalArgumentException("cells is null.");
		}

		// CellInfoType unrepeat_cells[] = new CellInfoType[Type.COUNT_CELL];
		// int n = CellsDiffCount(cells, unrepeat_cells, count);
		// if (n <= 0) {
		// return false;
		// }
		boolean rtn = false;
		// ------------------------微蜂窝----------------------------------------------------------------------
		for (int i = 0; i < cells.length; i++) {
			if (Type.CELL_TYPE.BEEHIVE == cells[i].cell_info.CellType) {
				rtn = Local.RandOrien(cells[i].cell_info, ll, cells[i].cell_info.Radius);
				return rtn;
			}
		}
		// -------------------直放站---------------------------------------------------------------------------
		rtn = RepeaterLocalOperation(cells, count, dist, ll);
		if (rtn) {
			return rtn;
		}
		// ----------非直放站，非微蜂窝-------------------------------------------------
		// 将数组转为集合
		LinkedList<CellInfoEntity> cellInfoList = new LinkedList<CellInfoEntity>();
		// 构建定位算法需要参数。
		for (int i = 0; i < cells.length; i++) {
			CellInfoType cell = cells[i];
			if (cell == null)
				continue;
			LonLatInfo lonLatInfo = new LonLatInfo(cell.cell_info.LatLong.LON, cell.cell_info.LatLong.LAT);
			CellInfoEntity cellInfo = new CellInfoEntity(lonLatInfo, cell.rscp, cell.cell_info.Angle, cell.cell_info.AngleRang, cell.cell_info.Radius);
			cellInfoList.add(cellInfo);
		}
		int btsCnt = LocalOperation.countBtsNumber(cellInfoList);
		if (btsCnt < 3) {
			LonLatInfo lonLatInfo = oneBtsLocalOperation(cellInfoList);
			ll.LAT = lonLatInfo.lat;
			ll.LON = lonLatInfo.lon;
			rtn = true;
		} else {
			rtn = LevelWeightLocalOperation(cells, count, dist, ll);
			if (Utility.Distance(cells[0].cell_info.LatLong, ll) > 3200) {
				rtn = Local.RandOrien(cells[0].cell_info, ll, cells[0].cell_info.Radius);
			}
		}
		return rtn;
	}

	public static double CalcDistanceByOneWayDelay(Type.DEV_TYPE type, ONEWAYDELAY_CELL info, DiffuseInfo diff) {
		double dis = Distance.DistanceByOneWayDelay(type, info.one_way_delay, diff);
		if (info.cell_info.Radius > 0 && dis > info.cell_info.Radius) {
			return info.cell_info.Radius;
		}
		return dis;

	}

	public static double GsmCalcDistance(GSM_CELL cell, DiffuseInfo diff) {
		double dis_ta = 0;
		double dis_rscp = 0;
		double dis_pathloss = 0;
		double tmp = 0;
		if (cell.TA >= 0) {
			dis_ta = Distance.DistanceByGsmTA(cell.TA, diff);
			if (cell.rscp < 0) {
				dis_rscp = Distance.DistanceByRscp(cell.rscp, cell.cell_info.Radius);
				tmp = Math.abs(dis_rscp - dis_ta);
				if (tmp > 512) {
					double i = dis_rscp - dis_ta;
					if (i > 0) {
						return dis_rscp - Utility.Rand() * tmp;
					} else {
						return dis_rscp + Utility.Rand() * tmp;
					}

				} else {
					return (dis_rscp > cell.cell_info.Radius) ? cell.cell_info.Radius : dis_rscp;
				}
			} else if (cell.pathloss > 0) {
				dis_pathloss = Distance.DistanceByGsmPathloss(cell.pathloss, diff.d1);
				tmp = Math.abs(dis_pathloss - dis_ta);
				if (tmp > 512) {
					double i = dis_pathloss - dis_ta;
					if (i > 0) {
						return dis_pathloss - Utility.Rand() * tmp;
					} else {
						return dis_pathloss + Utility.Rand() * tmp;
					}

				} else {
					return (dis_pathloss > cell.cell_info.Radius) ? cell.cell_info.Radius : dis_pathloss;
				}

			} else {
				return (dis_ta > cell.cell_info.Radius) ? cell.cell_info.Radius : dis_ta;
			}
		} else {
			if (cell.rscp < 0) {
				dis_rscp = Distance.DistanceByRscp(cell.rscp, cell.cell_info.Radius);
				return (dis_rscp > cell.cell_info.Radius) ? cell.cell_info.Radius : dis_rscp;
			} else if (cell.pathloss > 0) {
				dis_pathloss = Distance.DistanceByGsmPathloss(cell.pathloss, diff.d1);
				return (dis_pathloss > cell.cell_info.Radius) ? cell.cell_info.Radius : dis_pathloss;
			} else {
				assert (null == null);
				return 0;
			}
		}
	}

	/**
	 * 统计基站数量,分组小区
	 * 
	 * @param cellInfoList
	 * @return 基站个数
	 */
	public static final int countBtsNumber(LinkedList<CellInfoEntity> cellInfoList) {
		List<CellInfoEntity> list = new ArrayList<CellInfoEntity>();
		list.add(cellInfoList.get(0));
		int size = cellInfoList.size();
		make : for (int i = 1; i < size; i++) {
			CellInfoEntity cellInfo = cellInfoList.get(i);
			int jSize = list.size();
			for (int j = 0; j < jSize; j++) {
				CellInfoEntity cellInfoOther = list.get(j);
				if (Math.abs(cellInfo.lonLatInfo.lat - cellInfoOther.lonLatInfo.lat) <= LonLatInfo.DIFFERENCE
						&& Math.abs(cellInfo.lonLatInfo.lon - cellInfoOther.lonLatInfo.lon) <= LonLatInfo.DIFFERENCE) {
					continue make;
				}
			}
			list.add(cellInfo);
		}
		return list.size();
	}

	/**
	 * 小区定位算法
	 * 
	 * @return
	 */
	public static LonLatInfo oneBtsLocalOperation(List<CellInfoEntity> cellInfos) {
		// 一个小区采用在小区天线覆盖方向上、最远覆盖距离内按照rscp强度定位。
		if (cellInfos.size() == 1) {
			return LocalOperation.localOperation(cellInfos.get(0));
		}
		// 基站下每个定位点离小区点的距离，取所有距离的一个共同权重值
		double weightNumber = 0.0;
		LonLatInfo[] lonlatInfos = new LonLatInfo[cellInfos.size()];
		for (int i = 0; i < cellInfos.size(); i++) {
			// 获得了每个小区覆盖范围内的高斯分布随机点经纬度。
			lonlatInfos[i] = LocalOperation.localOperation(cellInfos.get(i));
			weightNumber += lonlatInfos[i].distance;
		}
		weightNumber = 1.0 / weightNumber;
		double weightDistance = 0.0;
		LonLatInfo lonLatInfo = new LonLatInfo();
		// 根据每个定位点离小区点的距离，取得所有小区的一个共同权重值。
		for (int i = 0; i < lonlatInfos.length; i++) {
			weightDistance = lonlatInfos[i].distance * weightNumber;
			lonLatInfo.lon += lonlatInfos[i].lon * weightDistance;
			lonLatInfo.lat += lonlatInfos[i].lat * weightDistance;
		}
		return lonLatInfo;
	}

	/**
	 * 采用在小区天线覆盖方向上、最远覆盖距离内按照rscp强度定位。
	 * 
	 * @param cellInfo
	 * @return
	 */
	public static LonLatInfo localOperation(CellInfoEntity cellInfo) {
		// 采用高斯分布在信号覆盖范围角内随机产生角
		double gaussianAngle = (GeographicalOperation.nextGaussian() * cellInfo.angleRang + cellInfo.angle) % 360;
		// 象限修订
		gaussianAngle = GeographicalOperation.quadrantAmendments(gaussianAngle);
		// 实际小区信号覆盖距离
		double realityRadius = GeographicalOperation.operationRealityRadiusByRscp(cellInfo.rscp, cellInfo.radius);
		// 经度弧长距离。1.角度转弧度。2.结果距离米为单位。
		double xd = realityRadius * Math.cos(GeographicalOperation.convertToRadian(gaussianAngle));
		// 纬度弧长距离。1.角度转弧度。2.结果距离米为单位。
		double yd = realityRadius * Math.sin(GeographicalOperation.convertToRadian(gaussianAngle));
		LonLatInfo lonlatInfo = new LonLatInfo();
		// 通过弧长计算公式，算出随机点经度
		lonlatInfo.lon = GeographicalOperation.getOnArcUpPoint(cellInfo.lonLatInfo.lon, xd / 1000, GeographicalFinal.EQUATOR_RADIUS);
		// 通过弧长计算公式，算出随机点纬度
		lonlatInfo.lat = GeographicalOperation.getOnArcUpPoint(cellInfo.lonLatInfo.lat, yd / 1000, GeographicalFinal.POLE_RADIUS);
		// 计算高斯分布随机点经纬度与小区经纬度之间的距离。
		lonlatInfo.distance = 1.0 / GeographicalOperation.distanceOperation(cellInfo.lonLatInfo.lon, cellInfo.lonLatInfo.lat, lonlatInfo.lon,
				lonlatInfo.lat);
		return lonlatInfo;
	}

	/**
	 * LTE 定位算法调用接口 <br/>
	 * LTE无线话单有LTE基站小区、CONNECTION_TA（连接时间提前量），用这两个内容做粗定位。 <br/>
	 * （1）CONNECTION_TA的单位是1/16个TA，1个TA=78.12米，因此CONNECTION_TA字段1个单位=4.89米 <br/>
	 * （2）以LTE小区经纬度为原点，在小区方位角方向正负60度（总共120度）范围内，以CONNECTION_TA表征的距离为半径画圆弧，在圆弧上随机用一个点作为话单的经纬度 <br/>
	 * 
	 * @param CONNECTION_TA
	 * @param azimuth
	 * @param inll
	 * @param outll
	 */
	public static void lteTaLocationOperate(long CONNECTION_TA, Double azimuth, Double longitude, Double latitude, LONG_LAT outll) {
		// 连接时间提前量
		if (CONNECTION_TA >= 2048)
			return;

		if (azimuth == null || longitude == null || latitude == null)
			return;
		if (longitude <= 0 || latitude <= 0)
			return;

		// 小区方位角方向正负60度（总共120度）范围内随机,取信号到达角度（Angle Of Arrival）
		double randomNum = ((int) (Math.random() * 1000)) % 120;
		// 方向角
		double angle = azimuth + (randomNum - 60);

		if (angle >= 360) {
			angle -= 360;
		} else if (angle < 0) {
			angle += 360;
		}

		// 半径
		double radius = lteTaDistance(CONNECTION_TA);
		// 定位出经纬度
		lteTaLocationPoint(angle, radius, longitude, latitude, outll);
	}

	/**
	 * CONNECTION_TA的单位是1/16个TA，1个TA=78.12米，因此CONNECTION_TA字段1个单位=4.89米
	 * 
	 * @param CONNECTION_TA
	 * @return
	 */
	public static double lteTaDistance(long CONNECTION_TA) {
		return CONNECTION_TA * 4.89;
	}

	/**
	 * 算出经纬度
	 * 
	 * @param angle
	 * @param radius
	 * @param inll
	 * @param outll
	 */
	public static void lteTaLocationPoint(double angle, double radius, double longitude, double latitude, LONG_LAT outll) {
		double radian = (360 - angle) * Math.PI / 180;
		double x = Math.cos(radian) * radius;
		double y = Math.sin(radian) * radius;
		outll.LON = longitude - y / distancePerLongitude(longitude, latitude);
		outll.LAT = latitude - x / distancePerLatitude(longitude, latitude);
	}

	/**
	 * 求每1个经度的距离
	 * 
	 * @param lon
	 * @param lat
	 * @return 每1个经度的距离，单位:米
	 */
	public static double distancePerLongitude(double lon, double lat) {
		return GeographicalOperation.distanceOperation(lon - 1, lat, lon + 1, lat) / 2 * 1000;
	}

	/**
	 * 求每1个纬度的距离
	 * 
	 * @param lon
	 * @param lat
	 * @return 每1个纬度的距离，单位:米
	 */
	public static double distancePerLatitude(double lon, double lat) {
		return GeographicalOperation.distanceOperation(lon, lat - 1, lon, lat + 1) / 2 * 1000;
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		LONG_LAT outll = new LONG_LAT();
		long CONNECTION_TA = 368;
		double azimuth = 240;
		double longitude = 118.88442;
		double latitude = 31.98624;
		LocalOperation.lteTaLocationOperate(CONNECTION_TA, azimuth, longitude, latitude, outll);
		System.out.println("小区经纬度，LON:" + longitude + " LAT:" + latitude);
		System.out.println("定位结果，LON:" + outll.LON + " LAT:" + outll.LAT);
		System.out.println("定位所花时间:" + (System.currentTimeMillis() - time));
	}
}
