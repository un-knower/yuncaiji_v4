package cn.uway.framework.orientation;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 网格信息表
 * 
 * @author tianjing @ 2014-4-28
 */
public class ReseauCfgInfo {

	private static final ILogger LOGGER = LoggerManager.getLogger(ReseauCfgInfo.class);

	/**
	 * 网格Id
	 */
	public long reseauId;

	/**
	 * 网格编号
	 */
	public String reseauNo;

	/**
	 * 城市Id
	 */
	public Short cityId;

	/**
	 * 网格左上顶点经度
	 */
	public Double lonUpl;

	/**
	 * 网格左上顶点纬度
	 */
	public Double latUpl;

	/**
	 * 网格右下顶点经度
	 */
	public Double lonLowr;

	/**
	 * 网格右下顶点纬度
	 */
	public Double latLowr;

	/**
	 * 网格拐点的经纬度。
	 */
	public List<ReseauCfgInfo.LonLatInfo> reseauPoints = new ArrayList<ReseauCfgInfo.LonLatInfo>();

	public class LonLatInfo {

		public Double lon;

		public Double lat;
	}

	/**
	 * 网格名称
	 */
	public String reseauName;

	/**
	 * 所属营业部名称
	 */
	public String bhallName;

	/**
	 * 网格划分内地域类型
	 */
	public String reseauType;

	/**
	 * 位置区类型
	 */
	public String positionType;

	/**
	 * 拆分网格拐点信息。 拐点信息格式：[[[longitude, latitude], [longitude, latitude], [longitude, latitude], [longitude, latitude]]]
	 * 
	 * @param srcStr
	 */
	// public final void splitPoints(String srcStr) throws Exception {
	// int idx1 = "  ".length();
	// int idx2 = "]  [".length();
	// int begin = "[[[".length();
	// String lonLatStr;
	// try {
	// int index = srcStr.indexOf("]", begin);
	// if (index <= begin) {
	// throw new IllegalArgumentException("拐点信息格式错误。标准拐点格式：[[[longitude  latitude]  [longitude, latitude]  ……]]");
	// }
	// while (index != -1) {
	// lonLatStr = srcStr.substring(begin, index);
	// int idx = lonLatStr.indexOf("  ");
	// LonLatInfo lonlatInfo = new LonLatInfo();
	// lonlatInfo.lon = Double.valueOf(lonLatStr.substring(0, idx));
	// lonlatInfo.lat = Double.valueOf(lonLatStr.substring(idx + idx1, lonLatStr.length()));
	// this.reseauPoints.add(lonlatInfo);
	// begin = index + idx2;
	// index = srcStr.indexOf("]", begin);
	// }
	// } catch (Exception e) {
	// this.reseauPoints.clear();
	// LOGGER.error("解析RESEAU_POINT字段失败!", e);
	// }
	// }

	/**
	 * 拆分网格拐点信息。 拐点信息格式：[[[longitude, latitude], [longitude, latitude], [longitude, latitude], [longitude, latitude]]]
	 * 
	 * @param srcStr
	 */
	public final void splitPoints(String srcStr) throws Exception {
		if (null==srcStr || "".equals(srcStr.trim()))
			return;
		int begin = srcStr.indexOf("[[");
		int end = srcStr.indexOf("]]");
		String split_comma = "],";
		String split_space = "] ";
		boolean split_space_flag = false;
		try {
			if (end <= begin) {
				throw new IllegalArgumentException("拐点信息格式错误。标准拐点格式：[[[longitude,  latitude] , [longitude, latitude]  ……]]");
			}
			// [[]]这种情况的判断
			if (begin + 4 >= end) {
				LOGGER.warn("ne_reseau_c表中的拐点为空,例如[[]]");
				return;
			}
			srcStr = srcStr.replace("[[", "").replace("]]", "").replace("]  ,", split_comma).replace("] ,", split_comma);
			String[] srcArr = srcStr.split(split_comma);
			// 如果是用空格进行分割的
			if (srcArr == null || (srcArr.length == 1 && (srcStr.contains(split_space) || !srcStr.contains(",")))) {
				srcStr = srcStr.replace("[[", "").replace("]]", "").replace("]  ", split_space).replace("] ", split_space);
				srcArr = srcStr.split(split_space);
				split_space_flag = true;
			}

			// split_comma 逗号进行分割
			if (srcArr != null && srcArr.length > 0 && !split_space_flag) {
				for (String src : srcArr) {
					src = src.replace("[", "").replace("]", "").replace(" ", "").replace(" ", "");
					String[] srcInfo = src.split(",");
					LonLatInfo lonlatInfo = new LonLatInfo();
					lonlatInfo.lon = Double.valueOf(srcInfo[0]);
					lonlatInfo.lat = Double.valueOf(srcInfo[1]);
					this.reseauPoints.add(lonlatInfo);
				}
			} else if (split_space_flag) { // 空白进行分割进行处理
				for (String src : srcArr) {
					src = src.replace("[", "").replace("]", "");
					String[] srcInfo = src.split("\\s{1,}");
					LonLatInfo lonlatInfo = new LonLatInfo();
					lonlatInfo.lon = Double.valueOf(srcInfo[0]);
					lonlatInfo.lat = Double.valueOf(srcInfo[1]);
					this.reseauPoints.add(lonlatInfo);
				}
			}
		} catch (Exception e) {
			this.reseauPoints.clear();
			LOGGER.error("解析RESEAU_POINT字段失败!", e);
		}
	}

	public static void main(String[] args) {
		// String src =
		// "[[[116.875602, 40.946510] , [116.846904, 40.437175], [116.636183, 40.626738], [116.566376, 40.651176], [116.582032, 40.720945], [116.594244, 40.712432], [116.633861, 40.723202], [116.706373, 40.718108], [116.706959, 40.938706]]]";
		String src = "[[[116.875602, 40.946510] , [116.846904, 40.437175], [116.636183, 40.626738]]]";
		// String src
		// ="[[[116.623667 40.801362]  [116.629560  40.794258]  [116.625674  40.788188]  [116.617997  40.785636]  [116.605988  40.784279]  [116.603821  40.791590]  [116.603948  40.798028]  [116.598661 40.806029]  [116.604522  40.809248]  [116.619302  40.802212]]]";
		// String src="[[[116.623667 40.801362]]]";
		ReseauCfgInfo info = new ReseauCfgInfo();
		try {
			info.splitPoints(src);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
