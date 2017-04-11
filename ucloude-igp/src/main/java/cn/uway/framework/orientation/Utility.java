package cn.uway.framework.orientation;

import java.util.Random;

import cn.uway.framework.orientation.Type.LONG_LAT;
import cn.uway.framework.orientation.Type.XYZ;

public class Utility {

	public static final double PI = Math.PI;

	public static final double RAND_MAX = 0x7fff;

	public static double Angle2Rad(double ang) {
		return ang * PI / 180.0;
	}

	public static double Rad2Angle(double rad) {
		return rad * 180.0 / PI;
	}

	/**
	 * 调整为算数角度，相对x轴
	 */
	public static double AdjustAngle(double ang) {
		double aa = 90 - ang;
		if (aa < 0) {
			aa += 360;
		}
		return aa;
	}

	public static void LL2XYZ(LONG_LAT ll, XYZ pos) {
		CEllipsoidCoordinates e1 = new CEllipsoidCoordinates(ll.LON, ll.LAT, ll.Height, 0);
		Ref<Double> x = new Ref<Double>(0.00);
		Ref<Double> y = new Ref<Double>(0.00);
		Ref<Double> z = new Ref<Double>(0.00);
		e1.GetXYZ(x, y, x);
		pos.X = x.getObj();
		pos.Y = y.getObj();
		pos.Z = z.getObj();
	}

	public static void XYZ2LL(XYZ pos, LONG_LAT ll) {
		CEllipsoidCoordinates e1 = new CEllipsoidCoordinates(pos.X, pos.Y, pos.Z);
		Ref<Double> lon = new Ref<Double>(0.00);
		Ref<Double> lat = new Ref<Double>(0.00);
		Ref<Double> height = new Ref<Double>(0.00);
		e1.GetLL(lon, lat, height);
		ll.Height = height.getObj();
		ll.LAT = lat.getObj();
		ll.LON = lon.getObj();
	}

	public static double Distance(XYZ p1, XYZ p2) {
		CEllipsoidCoordinates e1 = new CEllipsoidCoordinates(p1.X, p1.Y, p1.Z);
		CEllipsoidCoordinates e2 = new CEllipsoidCoordinates(p2.X, p2.Y, p2.Z);
		double dis = e1.Distance(e2);
		return dis;

	}

	public static double Distance(LONG_LAT p1, LONG_LAT p2) {
		CEllipsoidCoordinates elli0 = new CEllipsoidCoordinates(p1.LON, p1.LAT, p1.Height, 0);
		CEllipsoidCoordinates elli2 = new CEllipsoidCoordinates(p2.LON, p2.LAT, p2.Height, 0);
		return elli0.Distance(elli2);

	}

	public static double Rand() {
		double d = RAND_MAX;
		double t = new Random().nextInt((int) RAND_MAX) / d;
		return t;
	}

}
