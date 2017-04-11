package cn.uway.framework.orientation;

import java.util.List;

import cn.uway.framework.orientation.ReseauCfgInfo.LonLatInfo;

public class ReseauOrientationAPI {

	/**
	 * 给定一点经纬度，根据经纬度定位属于哪个网格
	 * 
	 * @param lon 给定点经度
	 * @param lat 给定点纬度
	 * @param reseauCfgInfoLst 网格列表
	 * @return 给定点属于当前网格的网格信息，不属于将返回null
	 */
	public final static ReseauCfgInfo locationLonLatOfReseau(Double lon, Double lat, List<ReseauCfgInfo> reseauCfgInfoLst) {
		if (lon == null || lat == null)
			return null;
		if (reseauCfgInfoLst == null || reseauCfgInfoLst.size() < 1)
			return null;
		/**
		 * 定位流程： 1.算出每个网格多边形的左上和右下顶点。<br>
		 * 2.初步过滤给定点经纬度不属于当前网格多边形，依赖左上和右下顶点过滤。<br>
		 * 3.通过直线方程式计算与网格多边形面的交点个数决定给定点是否落在网格多边形面上。<br>
		 * 直线方程式定律:线与网格多边形边的交点为基数个，则给定点落在网格多边形面上。
		 */
		for (ReseauCfgInfo reseauCfgInfo : reseauCfgInfoLst) {
			// 计算网格多边形的左上和右下顶点。
			boolean calcResult = ReseauOrientationAPI.calcUplAndLowrLonLat(reseauCfgInfo);
			if (!calcResult) {
				// 拐点经纬度为空，当前网格多边形无效。
				continue;
			}
			/**
			 * 网格多边形左上和右下顶点连成一个矩形，判断给定点是否在当前矩形范围内(由于是取网格多边形左上和右下顶点，所以矩形包含多边形)。<br>
			 * 即：给定点经度大于左上顶点经度，给定点纬度小于左上顶点纬度。给定点经度小于右下顶点经度，给定点纬度大于右下顶点纬度
			 */
			if ((lon >= reseauCfgInfo.lonUpl && lat <= reseauCfgInfo.latUpl) && (lon <= reseauCfgInfo.lonLowr && lat >= reseauCfgInfo.latLowr)) {
				// 横向左右延伸,计算点与多边形边的交点。直线方程式:(((x2-x1)*(y-y1))/(y2-y1))+x1 条件:(x1≠x2，y1≠y2)
				if (xAxisIntersectPointCalc(reseauCfgInfo, lon, lat)) {
					return reseauCfgInfo;
				}
				// 竖向上下延伸,计算点与多边形边的交点。直线方程式:(((y2-y1)*(x-x1))/(x2-x1))+y1 条件:(x1≠x2，y1≠y2)
				if (yAxisIntersectPointCalc(reseauCfgInfo, lon, lat)) {
					return reseauCfgInfo;
				}
			}
		}
		return null;
	}

	/**
	 * 计算网格多边形的左上和右下顶点。
	 * 
	 * @param reseauCfgInfo 网格多边形信息
	 * @return 拐点经纬度为空，当前网格多边形无效。
	 */
	public final static boolean calcUplAndLowrLonLat(ReseauCfgInfo reseauCfgInfo) {
		if (reseauCfgInfo.reseauPoints == null) {
			// 拐点为空，当前网格多边形无效。
			return false;
		}
		for (LonLatInfo lonlatInfo : reseauCfgInfo.reseauPoints) {
			if (lonlatInfo.lon == null || lonlatInfo.lat == null) {
				// 拐点经纬度为空，当前网格多边形无效。
				return false;
			}
			// 网格多边形左上顶点经纬度。经度最小，纬度最大
			if (reseauCfgInfo.lonUpl == null || (lonlatInfo.lon < reseauCfgInfo.lonUpl)) {
				reseauCfgInfo.lonUpl = lonlatInfo.lon;
			} else if (reseauCfgInfo.latUpl == null || (lonlatInfo.lat > reseauCfgInfo.latUpl)) {
				reseauCfgInfo.latUpl = lonlatInfo.lat;
			}
			// 网格多边形右下顶点经纬度。经度最大，纬度最小
			if (reseauCfgInfo.lonLowr == null || (lonlatInfo.lon > reseauCfgInfo.lonLowr)) {
				reseauCfgInfo.lonLowr = lonlatInfo.lon;
			} else if (reseauCfgInfo.latLowr == null || (lonlatInfo.lat < reseauCfgInfo.latLowr)) {
				reseauCfgInfo.latLowr = lonlatInfo.lat;
			}
		}
		return true;
	}

	/**
	 * 横向左右延伸,直线方程式计算给定点与网格多边形面的交点。<br>
	 * 考虑条件:<br>
	 * 1.网格多边形两个拐点的经纬度应该不相等。<br>
	 * 2.给定点经纬度与网格多边形两拐点的经纬度,其中一个点相等默认给定点落在多边形范围内。<br>
	 * 3.给定点纬度必须在网格多边形两拐点的纬度范围内才可能有交点。<br>
	 * 4.给定点经度或纬度与网格多边形拐点的经度或纬度相等的情况下需要考虑到，给定点向左或右延伸的交点统计结果值无效。例如：给定点向右延伸那么给定点向右延伸与网格多边形交点数统计结果值无效<br>
	 * 5.给定点在网格多边形两拐点连线上。
	 * 
	 * @param reseauCfgInfo 网格多边形信息
	 * @param lon 给定点经度
	 * @param lat 给定点纬度
	 * @return 给定点是否落在网格多边形面上。true：落在网格多边形面上。
	 */
	public final static boolean xAxisIntersectPointCalc(ReseauCfgInfo reseauCfgInfo, double lon, double lat) {
		double x; // 交点经度坐标
		int rightIntersectPointCnt = 0;
		int leftIntersectPointCnt = 0;
		double lon1, lat1, lon2, lat2;
		List<ReseauCfgInfo.LonLatInfo> reseauPoints = reseauCfgInfo.reseauPoints;
		int size = reseauPoints.size();
		for (int i = 0; i < size; i++) {
			if (i + 2 > size)
				break;
			lon1 = reseauPoints.get(i).lon;
			lat1 = reseauPoints.get(i).lat;
			lon2 = reseauPoints.get(i + 1).lon;
			lat2 = reseauPoints.get(i + 1).lat;
			// 多边形两个拐点的经纬度应该不相等
			if (lon1 == lon2 && lat1 == lat2) {
				continue;
			}
			// 给定点经纬度与网格多边形两拐点的经纬度其中一点相等，默认给定点落在多边形范围内
			if ((lon == lon1 && lat == lat1) || (lon == lon2 && lat == lat2)) {
				return true;
			}
			if (lat1 > lat2) {
				// 满足给定点纬度在多边形两个拐点的纬度范围内
				if (lat > lat1 || lat < lat2) {
					// 给定点纬度不在多边形两个拐点的纬度范围内则直接不参与相交求交点计算。
					continue;
				}
			} else {
				if (lat < lat1 || lat > lat2) {
					// 给定点纬度不在多边形两个拐点的纬度范围内则直接不参与相交求交点计算。
					continue;
				}
			}
			// 确认给定点纬度与多边形两个拐点的纬度有相等的情况
			if (lat == lat1 || lat == lat2) {
				// 确认给定点经度是否在多边形两个拐点的经度右边
				if (lon > lon1 && lon > lon2) {
					// 标识给定点向左延伸计算结果值无效。
					leftIntersectPointCnt = -1;
					continue;
				} else if (lon < lon1 && lon < lon2) {
					// 标识给定点向右延伸计算结果值无效。
					rightIntersectPointCnt = -1;
					continue;
				} else {
					// 给定点经度在多边形两个拐点的经度之间时,无法判断给定点向右还是向左延伸。所以需要计算出交点的经度，通过交点经度与给定点经度比较才能知道结果。另外交点经度必定和多边形两个拐点其中一点经度相等。
				}
			}
			// 如果向左向右延伸计算交点都有标识结果值无效,则退出横向延伸计算。以竖向上下延伸计算结果为准。
			if (leftIntersectPointCnt == -1 && rightIntersectPointCnt == -1) {
				return false;
			}
			// 公式:(((x2-x1)*(y-y1))/(y2-y1))+x1 条件:(x1≠x2，y1≠y2)
			x = (((lon2 - lon1) * (lat - lat1)) / (lat2 - lat1)) + lon1;
			// 确认交点经度是否与多边形两个拐点的经度有相等的情况
			if ((lat == lat1 && x == lon1) || (lat == lat2 && x == lon2)) {
				if (x > lon) {
					// 标识给定点向右延伸计算结果值无效。
					rightIntersectPointCnt = -1;
					continue;
				} else {
					// 标识给定点向左延伸计算结果值无效。
					leftIntersectPointCnt = -1;
					continue;
				}
			}
			if (x > lon && rightIntersectPointCnt != -1)
				rightIntersectPointCnt++;
			else if (x < lon && leftIntersectPointCnt != -1)
				leftIntersectPointCnt++;
			else if (x == lon) {
				// 给定点经纬度落在多边形两拐点连线上，即给定点和交点是一个点。则默认给定点落在多边形范围内
				return true;
			}
		}
		// 确认给定点与多边形交点的个数
		if (rightIntersectPointCnt == 0 && leftIntersectPointCnt == 0) {
			return false;
		}
		boolean result = false;
		if (leftIntersectPointCnt != 0 && leftIntersectPointCnt != -1) {
			result = leftIntersectPointCnt % 2 > 0;
			if (!result) {
				return result;
			}
		}
		if (rightIntersectPointCnt != 0 && rightIntersectPointCnt != -1) {
			result = rightIntersectPointCnt % 2 > 0;
		}
		return result;
	}

	/**
	 * 竖向上下延伸,直线方程式计算给定点与网格多边形面的交点。<br>
	 * 考虑条件:<br>
	 * 1.网格多边形两个拐点的经纬度应该不相等。<br>
	 * 2.给定点经纬度与网格多边形两拐点的经纬度,其中一个点相等默认给定点落在多边形范围内。<br>
	 * 3.给定点经度必须在网格多边形两拐点的经度度范围内才可能有交点。<br>
	 * 4.给定点经度或纬度与网格多边形拐点的经度或纬度相等的情况下需要考虑到，给定点向左或右延伸的交点统计结果值无效。例如：给定点向右延伸那么给定点向右延伸与网格多边形交点数统计结果值无效<br>
	 * 5.给定点在网格多边形两拐点连线上。
	 * 
	 * @param reseauCfgInfo 网格多边形信息
	 * @param lon 给定点经度
	 * @param lat 给定点纬度
	 * @return 给定点是否落在网格多边形面上。true：落在网格多边形面上。
	 */
	public final static boolean yAxisIntersectPointCalc(ReseauCfgInfo reseauCfgInfo, double lon, double lat) {
		double y; // 交点纬度坐标
		int downIntersectPointCnt = 0;
		int upIntersectPointCnt = 0;
		double lon1, lat1, lon2, lat2;
		List<ReseauCfgInfo.LonLatInfo> reseauPoints = reseauCfgInfo.reseauPoints;
		int size = reseauPoints.size();
		for (int i = 0; i < size; i++) {
			if (i + 2 > size)
				break;
			lon1 = reseauPoints.get(i).lon;
			lat1 = reseauPoints.get(i).lat;
			lon2 = reseauPoints.get(i + 1).lon;
			lat2 = reseauPoints.get(i + 1).lat;
			// 多边形两个拐点的经纬度应该不相等
			if (lon1 == lon2 && lat1 == lat2) {
				continue;
			}
			// 给定点经纬度与网格多边形两拐点的经纬度其中一点相等，默认给定点落在多边形范围内
			if ((lon == lon1 && lat == lat1) || (lon == lon2 && lat == lat2)) {
				return true;
			}
			if (lon1 > lon2) {
				// 满足给定点经度在多边形两个拐点的经度范围内
				if (lon > lon1 || lon < lon2) {
					// 给定点经度不在多边形两个拐点的经度范围内则直接不参与相交求交点计算。
					continue;
				}
			} else {
				if (lon < lon1 || lon > lon2) {
					// 给定点经度不在多边形两个拐点的经度范围内则直接不参与相交求交点计算。
					continue;
				}
			}
			// 确认给定点经度与多边形两个拐点的经度有相等的情况
			if (lon == lon1 || lon == lon2) {
				// 确认给定点纬度是否在多边形两个拐点的纬度下边
				if (lat > lat1 && lat > lat2) {
					// 标识给定点向下延伸计算结果值无效。
					downIntersectPointCnt = -1;
					continue;
				} else if (lat < lat1 && lat < lat2) {
					// 标识给定点向上延伸计算结果值无效。
					upIntersectPointCnt = -1;
					continue;
				} else {
					// 给定点纬度在多边形两个拐点的纬度之间时,无法判断给定点向上还是向下延伸。所以需要计算出交点的纬度，通过交点纬度与给定点纬度比较才能知道结果。另外交点纬度必定和多边形两个拐点其中一点纬度相等。
				}
			}
			// 如果向上向下延伸计算交点都有标识结果值无效,则退出竖向延伸计算。结果以横向延伸计算为准。
			if (upIntersectPointCnt == -1 && downIntersectPointCnt == -1) {
				return false;
			}
			// 公式:(((y2-y1)*(x-x1))/(x2-x1))+y1 条件:(x1≠x2，y1≠y2)
			y = (((lat2 - lat1) * (lon - lon1)) / (lon2 - lon1)) + lat1;
			// 确认交点纬度是否与多边形两个拐点的纬度有相等的情况
			if ((lon == lon1 && y == lat1) || (lon == lon2 && y == lat2)) {
				if (y > lat) {
					// 标识给定点向上延伸计算结果值无效。
					upIntersectPointCnt = -1;
					continue;
				} else {
					// 标识给定点向下延伸计算结果值无效。
					downIntersectPointCnt = -1;
					continue;
				}
			}
			if (y > lat && upIntersectPointCnt != -1)
				upIntersectPointCnt++;
			else if (y < lat && downIntersectPointCnt != -1)
				downIntersectPointCnt++;
			else if (y == lat) {
				// 给定点经纬度落在多边形两拐点连线上，即给定点和交点是一个点。则默认给定点落在多边形范围内
				return true;
			}
		}
		// 确认给定点与多边形交点的个数
		if (downIntersectPointCnt == 0 && upIntersectPointCnt == 0) {
			return false;
		}
		boolean result = false;
		if (upIntersectPointCnt != 0 && upIntersectPointCnt != -1) {
			result = upIntersectPointCnt % 2 > 0;
			if (!result) {
				return result;
			}
		}
		if (downIntersectPointCnt != 0 && downIntersectPointCnt != -1) {
			result = downIntersectPointCnt % 2 > 0;
		}
		return result;
	}
}
