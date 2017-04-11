package cn.uway.framework.orientation;

import cn.uway.framework.orientation.Type.CELL_INFO;
import cn.uway.framework.orientation.Type.CellInfoType;
import cn.uway.framework.orientation.Type.LONG_LAT;
import cn.uway.framework.orientation.Type.Repeater;

public class Local {

	public static final double EPSINON = 0.00001;// 允许的误差（即精度)

	public static int CalcDiffCount(LONG_LAT[] info, int fi[], int count) {
		LONG_LAT ll[] = new LONG_LAT[Type.COUNT_CELL];
		for (int i = 0; i < Type.COUNT_CELL; i++) {
			ll[i] = new LONG_LAT();
		}
		int k = 0;

		for (int i = 0; i < count; i++) {
			boolean b = false;
			for (int j = 0; j < k; j++) {
				b = (cmp(info[i], ll[i]));
			}
			if (!b) {
				ll[k] = info[i];
				fi[i] = 1;
				k++;
			}
		}
		return k;
	}

	public static int diffCount(CellInfoType[] info, CellInfoType[] fi, int count) {
		int o[] = new int[Type.COUNT_CELL];
		LONG_LAT ill[] = new LONG_LAT[Type.COUNT_CELL];
		for (int i = 0; i < count; i++) {
			ill[i] = info[i].cell_info.LatLong;
		}
		CalcDiffCount(ill, o, count);
		int j = 0;
		for (int i = 0; i < count; i++) {
			if (o[i] == 1) {
				fi[j] = info[i];
				j++;
			}
		}
		return j;
	}

	public static boolean cmp(LONG_LAT l1, LONG_LAT l2) {
		boolean b = (Math.abs(l1.LAT - l2.LAT) < EPSINON);
		boolean b11 = (Math.abs(l1.LON - l2.LON) < EPSINON);
		return (b && b11);
	}

	public static boolean IncorporateWeight(CELL_INFO[] info, double[] weight, int count, LONG_LAT ll) {
		ll.Height = 0;
		ll.LAT = 0;
		ll.LON = 0;
		for (int i = 0; i < count; i++) {
			ll.LAT += info[i].LatLong.LAT * weight[i];
			ll.LON += info[i].LatLong.LON * weight[i];
		}
		return true;
	}

	public static void DistanceWeight(double[] cell_dis, double[] weight, int count) {
		double tmp[] = new double[Type.COUNT_CELL];
		double sum = 0.0;
		// // 求 n个点的 1/R^2 之和 为sum
		for (int i = 0; i < count; i++) {
			tmp[i] = 1 / (cell_dis[i] * cell_dis[i]);
			sum += tmp[i];
		}

		for (int i = 0; i < count; i++) {
			weight[i] = tmp[i] / sum;
		}
	}

	public static boolean ElectricalLevelWeight(double[] cell_rscps, double[] weight, int count, double k) {

		double rscp = Math.abs(cell_rscps[0]);
		double K = k * 10;
		double sum = 0.0;
		double tmp[] = new double[32];

		for (int i = 1; i < count; i++) {
			// tmp[i]= pow(10,2*(fabs(info[i].rscp)-rscp)/K);
			tmp[i] = Math.pow(10, -2 * (Math.abs(cell_rscps[i]) - rscp) / K);

			sum += tmp[i];
		}
		sum += 1;

		weight[0] = 1 / (sum);
		for (int i = 1; i < count; i++) {
			weight[i] = tmp[i] / sum;
		}
		return true;
	}

	public static boolean RandOrien(CELL_INFO info, LONG_LAT ll, double r) {
		// 角度转换为相对X轴
		double angle = Utility.AdjustAngle(info.Angle);

		double rad = Utility.Angle2Rad((Utility.Rand() - 0.5) * (info.AngleRang / 2) + angle);
		double dist = Utility.Rand() * r;// 100M 内
		Delta d = new Delta();
		d.x_axis = dist * Math.cos(rad);
		d.y_axis = dist * Math.sin(rad);
		CEllipsoidCoordinates ellip = new CEllipsoidCoordinates(info.LatLong.LON, info.LatLong.LAT, info.LatLong.Height, 0);
		ellip = ellip.addEQ(d);
		LONG_LAT l = new LONG_LAT();
		Ref<Double> lon = new Ref<Double>();
		Ref<Double> lat = new Ref<Double>();
		Ref<Double> height = new Ref<Double>();
		ellip.GetLL(lon, lat, height);
		l.LON = lon.getObj();
		l.LAT = lat.getObj();
		l.Height = height.getObj();
		ll.Height = l.Height;
		ll.LAT = l.LAT;
		ll.LON = l.LON;
		return true;
	}

	// 一个位置点，一个小区
	public static void OnePoint1Cell(CELL_INFO info, double d, LONG_LAT ll) {

		if (info.Radius > 0 && d > info.Radius) {
			d = info.Radius;
		}

		double angle = Utility.AdjustAngle(info.Angle);
		double a = Utility.Angle2Rad(angle + (Utility.Rand() - 0.5) * (info.AngleRang / 2));
		double dx = d * Math.cos(a);
		double dy = d * Math.sin(a);

		CEllipsoidCoordinates ellip = new CEllipsoidCoordinates(info.LatLong.LON, info.LatLong.LAT, info.LatLong.Height, 0);

		Delta dt = new Delta();
		dt.x_axis = dx;
		dt.y_axis = dy;

		ellip = ellip.addEQ(dt);
		Ref<Double> lon = new Ref<Double>();
		Ref<Double> lat = new Ref<Double>();
		Ref<Double> height = new Ref<Double>();
		ellip.GetLL(lon, lat, height);
		ll.LON = lon.getObj();
		ll.LAT = lat.getObj();
		ll.Height = height.getObj();

		@SuppressWarnings("unused")
		double dddd = Utility.Distance(ll, info.LatLong);
	}

	public static boolean TwoCell(CELL_INFO[] info, double[] d, int count, LONG_LAT ll) {
		double angle = 0.0;
		double a = 0.0;
		double dx = 0.0;
		double dy = 0.0;
		// double d = 0;
		Delta dt = new Delta();
		LONG_LAT t = new LONG_LAT();
		t.LON = 0;
		t.LAT = 0;
		t.Height = 0;
		ll.Height = 0;
		ll.LAT = 0;
		ll.LON = 0;
		for (int i = 0; i < count; ++i) {
			angle = Utility.AdjustAngle(info[i].Angle);
			a = Utility.Rand() * info[i].AngleRang + (angle - info[i].AngleRang / 2.0);
			dx = d[i] * Math.cos(Utility.Angle2Rad(a));
			dy = d[i] * Math.sin(Utility.Angle2Rad(a));
			// LL2XYZ(info[i].LatLong, xyz);
			CEllipsoidCoordinates ellip = new CEllipsoidCoordinates(info[i].LatLong.LON, info[i].LatLong.LON, info[i].LatLong.Height, 0);
			dt.x_axis = dx;
			dt.y_axis = dy;
			// xyz+=dt;
			ellip = ellip.addEQ(dt);
			// XYZ2LL(xyz, t);
			Ref<Double> lon = new Ref<Double>();
			Ref<Double> lat = new Ref<Double>();
			Ref<Double> height = new Ref<Double>();
			ellip.GetLL(lon, lat, height);
			t.LON = lon.getObj();
			t.LAT = lat.getObj();
			t.Height = height.getObj();
			ll.LAT += t.LAT;
			ll.LON += t.LON;
			ll.Height += t.Height;
			// xyz.X+=dx;
			// xyz.Y+=dy;

			// fxyz.Z += xyz.Z;
		}
		ll.LAT /= count;
		ll.LON /= count;
		// ll.Height =xyz.Z;

		// XYZ2LL(fxyz, ll);

		return true;
	}

	// 判断是否是直放站
	public static int HaveRepeater(CELL_INFO[] cells, double[] dis, int count) {

		for (int i = 0; i < count; i++) {
			if (cells[i].repeaters.count > 0) {
				if (dis[i] > (cells[i].Radius)) {
					return i; // 是直放站
				}
			}
		}

		return -1; // 不是

	}

	public static boolean RepeaterOrien(CELL_INFO[] cells, double[] dis, int count, LONG_LAT ll) {
		int index = HaveRepeater(cells, dis, count);
		if (index <= 0 || cells == null || cells.length == 0 || dis == null || dis.length == 0) { // 没有只放站
			return false;
		}
		Repeater pRepeater = new Repeater();
		double ms_dist = dis[index];
		double cell_ms_dist = 0;
		double min_dist = 999999999.0;
		for (int i = 0; i < cells[index].repeaters.count; i++) {
			// double tt = Distance(cell.LatLong,
			// cell.repeaters.pRepeater[i].ll);
			if ((cell_ms_dist = Math.abs(ms_dist - Utility.Distance(cells[index].LatLong, cells[index].repeaters.repeaters[i].ll))) < min_dist) {
				min_dist = cell_ms_dist;
				pRepeater = cells[index].repeaters.repeaters[i];
			}
		}

		double rad = Utility.Angle2Rad(Utility.Rand() * 360);
		double dist = Utility.Rand() * pRepeater.Radius;
		double dx = dist * Math.cos(rad);
		double dy = dist * Math.sin(rad);
		// XYZ xyz;
		// LL2XYZ(pRepeater->ll, xyz);
		CEllipsoidCoordinates ellip = new CEllipsoidCoordinates(pRepeater.ll.LON, pRepeater.ll.LAT, pRepeater.ll.Height, 0);
		Delta dt = new Delta();
		dt.x_axis = dx;
		dt.y_axis = dy;

		// xyz += dt;
		ellip = ellip.addEQ(dt);
		// LAT_LONG l;
		Ref<Double> lon = new Ref<Double>();
		Ref<Double> lat = new Ref<Double>();
		Ref<Double> height = new Ref<Double>();
		ellip.GetLL(lon, lat, height);
		ll.LON = lon.getObj();
		ll.LAT = lat.getObj();
		ll.Height = height.getObj();
		// LONG_LAT l;
		// XYZ2LL(xyz, l);
		// ll.Height = l.Height;
		// ll.LAT = l.LAT;
		// ll.LONG = l.LONG;
		return true;
	}
}
