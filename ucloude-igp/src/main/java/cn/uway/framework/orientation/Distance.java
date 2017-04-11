package cn.uway.framework.orientation;

import cn.uway.framework.orientation.Type.DEV_TYPE;
import cn.uway.framework.orientation.Type.DIFF_1;
import cn.uway.framework.orientation.Type.DiffuseInfo;

public class Distance {

	public static final double RSCP_MAX = -47.0;

	public static final double RSCP_MIN = -116.0;

	public static final double ECIO_MAX = (-3.0);

	public static final double ECIO_MIN = (-15.0);

	public static double DistanceByOneWayDelay(DEV_TYPE type, double OneWayDelay, DiffuseInfo diff) {
		double dis = 0.0;
		switch (type) {
			case CDMA_LUC :
				dis = (OneWayDelay + 1) / 16 * 244 - diff.amend;
				break;
			case CDMA_HUWEI :
				dis = (OneWayDelay + 1) / 8 * 244 - diff.amend;
				break;
			case CDMA_ZTE :
				// dis = (OneWayDelay+1)/16* 244 - diff.amend;
				dis = (OneWayDelay + 1) / 8 * 244 - diff.amend;
				break;
			default :
				dis = (OneWayDelay + 1) / 8 * 244 - diff.amend;
		}
		return dis;
	}

	public static double DistanceByCdmaEcIo(DEV_TYPE type, double ecio, double radius, DiffuseInfo diff) {
		return Math.abs((ecio - ECIO_MAX) / (ECIO_MIN - ECIO_MAX)) * radius;
	}

	public static double DistanceByGsmTA(double TA, DiffuseInfo diff) {
		double dis = (TA + 1) * diff.TA_STEP - diff.amend;
		return dis;
	}

	public static double DistanceByRscp(double rscp, double radius) {
		double dis = 0;
		if (rscp < RSCP_MIN || rscp > RSCP_MAX) {
			dis = 100;
		} else {

			dis = radius * (Math.abs(rscp) + RSCP_MAX) / (RSCP_MAX - RSCP_MIN);
		}
		return dis;
	}

	public static double DistanceByGsmPathloss(double pathloss, DIFF_1 dif) {

		double exponent = (pathloss - dif.X) / (10 * dif.K);
		return Math.pow(10, exponent);

	}

}
