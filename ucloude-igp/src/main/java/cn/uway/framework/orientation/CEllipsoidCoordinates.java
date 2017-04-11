package cn.uway.framework.orientation;

public class CEllipsoidCoordinates {

	public static final double PI = 3.141592653589793;

	public static final double a = 6378137.0;// 长半轴

	public static final double flattening = 1 / 298.257223563;// 扁率

	public static final double DELTA = 0.000000001;

	private double m_x_axis;

	private double m_y_axis;

	private double m_z_axis;

	public CEllipsoidCoordinates() {
		super();
		this.m_x_axis = 0;
		this.m_y_axis = 0;
		this.m_z_axis = 0;
	}

	public CEllipsoidCoordinates(double longitude /* 经度 */, double latitude/* 纬度 */, double height/* 高 */, int x) {
		this();
		SetLL(longitude, latitude, height);
	}

	public CEllipsoidCoordinates(double x_axis/* x轴 */, double y_axis/* y轴 */, double z_axis/* z轴 */) {
		this.m_x_axis = x_axis;
		this.m_y_axis = y_axis;
		this.m_z_axis = z_axis;
	}

	public boolean GetXYZ(Ref<Double> x_axis/* x轴 */, Ref<Double> y_axis/* y轴 */, Ref<Double> z_axis/* z轴 */) {
		x_axis.setObj(this.m_x_axis);
		y_axis.setObj(this.m_y_axis);
		z_axis.setObj(this.m_z_axis);
		return true;
	}

	public boolean SetXYZ(double x_axis/* x轴 */, double y_axis/* y轴 */, double z_axis/* z轴 */) {
		this.m_x_axis = x_axis;
		this.m_y_axis = y_axis;
		this.m_z_axis = z_axis;
		return true;
	}

	public boolean GetLL(Ref<Double> longitude /* 经度 */, Ref<Double> latitude/* 纬度 */, Ref<Double> height/* 高 */) {
		Ref<Double> lon = new Ref<Double>(0.00), lat = new Ref<Double>(0.00), hig = new Ref<Double>(0.00);
		boolean rtn = ToLL(lon, lat, hig);
		if (!rtn) {
			return rtn;
		}

		height.setObj(hig.getObj());
		// ll.LAT = Rad2Angle (bl.latitude);
		latitude.setObj(Rad2Angle(lat.getObj()));
		// ll.LONG = Rad2Angle(bl.longitude);
		longitude.setObj(Rad2Angle(lon.getObj()));
		// pcg->longitude
		if ( /* xyz.x */m_x_axis >= 0) {
			if ( /* xyz.y */m_y_axis >= 0) {
				// 第一象限
			} else {
				// 第4象限
				// ll.LONG *= -1;
				longitude.setObj(longitude.getObj() * -1);
			}
		} else {
			if ( /* xyz.y */m_y_axis >= 0) {// 第2象限
											// ll.LONG += 180;
				longitude.setObj(longitude.getObj() + 180);
			} else {
				// 第3象限
				// ll.LONG = 180-ll.LONG;
				longitude.setObj(180 - longitude.getObj());
			}
		}

		return true;

	}

	public boolean SetLL(double longitude /* 经度 */, double latitude/* 纬度 */, double height/* 高 */) {
		// //由大地坐标转换为笛卡尔坐标

		double lon = Angle2Rad(longitude);
		double lat = Angle2Rad(latitude);
		// 第一偏心率的平方
		double e2 = 2 * flattening - flattening * flattening;
		// 卯酉圈半径
		double N = a / Math.sqrt(1 - e2 * Math.sin(lat) * Math.sin(lat));

		m_x_axis = (N + height) * Math.cos(lat) * Math.cos(lon);
		m_y_axis = (N + height) * Math.cos(lat) * Math.sin(lon);
		m_z_axis = (N * (1 - e2) + height) * Math.sin(lat);
		return true;
	}

	public double Distance(CEllipsoidCoordinates a) {
		double x = m_x_axis - a.m_x_axis;
		double y = m_y_axis - a.m_y_axis;
		double z = m_z_axis - a.m_z_axis;
		x *= x;
		y *= y;
		z *= z;
		return Math.sqrt(x + y + z);
	}

	public CEllipsoidCoordinates add(Delta d) {
		CEllipsoidCoordinates t = new CEllipsoidCoordinates(this.m_x_axis, this.m_y_axis, this.m_z_axis);
		t.PX(d.x_axis);
		t.PY(d.y_axis);
		return t;
	}

	public CEllipsoidCoordinates addEQ(Delta d) {
		PX(d.x_axis);
		PY(d.y_axis);
		return this;
	}

	// 经度方向
	protected void PX(double x) {
		double rr = (m_x_axis * m_x_axis + m_y_axis * m_y_axis);
		double r = Math.sqrt(rr);
		double aa = x / (r);
		double sa = Math.atan(m_y_axis / m_x_axis);
		if (m_x_axis >= 0) {
			if (m_y_axis > 0) {// 1
			} else {// 4
			}
		} else {
			if (m_y_axis > 0) {// 2
				sa += PI;
			} else {
				sa = PI - sa;
			}
		}
		double b = sa + aa;
		double dx = r * Math.cos(b);
		double dy = r * Math.sin(b);
		m_x_axis = dx;
		m_y_axis = dy;
	}

	// 纬度方向
	protected void PY(double x) {
		double b = a - a * flattening;
		double rr = (m_x_axis * m_x_axis + m_y_axis * m_y_axis);// (xyz.x*xyz.x+xyz.y*xyz.y);
		double r = Math.sqrt(rr);
		double k = (b * b * r) / (a * a * m_z_axis/* xyz.z */);
		double ang = Math.atan(k);
		@SuppressWarnings("unused")
		double angl = Rad2Angle(ang);
		double dz = x * Math.sin(ang);
		double dx = x * Math.cos(ang);
		double sa = Math.atan(/* xyz.y */m_y_axis / m_x_axis /* xyz.x */);
		if (m_x_axis >= 0) {
			if (m_y_axis > 0) {// 1
			} else {// 4
			}
		} else {
			if (m_y_axis > 0) {// 2
				sa += PI;
			} else {
				sa = PI - sa;
			}
		}

		double dxx = dx * Math.cos(sa);
		double dy = dx * Math.sin(sa);
		m_z_axis += dz;
		m_y_axis -= dy;
		m_x_axis -= dxx;
	}

	protected boolean ToLL(Ref<Double> longitude /* 经度 */, Ref<Double> latitude/* 纬度 */, Ref<Double> height/* 高 */) {
		int i = 0;
		// 第一偏心率的平方
		double e2 = 2 * flattening - flattening * flattening;

		longitude.setObj(Math.atan(m_y_axis / m_x_axis));
		double W, N, N1 = 0, B = 0, B1;

		double dis_x_y = Math.sqrt(m_x_axis * m_x_axis + m_y_axis * m_y_axis);
		B1 = Math.atan(m_z_axis / dis_x_y);
		double SinB1 = 0.0;
		while (++i < 64) {
			SinB1 = Math.sin(B1);
			W = Math.sqrt(1 - e2 * SinB1 * SinB1);
			N1 = a / W;
			B = Math.atan((m_z_axis + N1 * e2 * SinB1) / dis_x_y);

			if (Math.abs(B - B1) < DELTA) {
				break;
			} else {
				B1 = B;
			}
		}

		latitude.setObj(B);
		N = a / Math.sqrt(1 - e2 * Math.sin(latitude.getObj()) * Math.sin(latitude.getObj()));
		height.setObj(dis_x_y / Math.cos(B) - N);
		return true;
	}

	protected double Rad2Angle(double rad) {
		return rad * 180.0 / PI;
	}

	protected double Angle2Rad(double ang) {
		return ang * PI / 180.0;
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		CEllipsoidCoordinates ce = new CEllipsoidCoordinates();
		for (int i = 0; i < 250000; i++)
			ce.PX(11.1);
		long end = System.currentTimeMillis();
		System.err.println((end - start) / 1000.);
	}
}
