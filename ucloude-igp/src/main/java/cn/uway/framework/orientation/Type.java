package cn.uway.framework.orientation;

import java.util.Arrays;

public class Type {

	public static final int COUNT_CELL = 16;

	public static enum CELL_TYPE {
		REPEATER, // 直放站
		BEEHIVE, // 微蜂窝
		INDOOR, // 室内分布
		OUTDOOR // 室外宏站

	};
	
	public static enum COVERAGE_AREA {
		DOWNTOWN, //城区
		SUBURBS  //郊区
	};

	public static enum DEV_TYPE {
		GSM, WCDMA, CDMA_LUC, CDMA_HUWEI, CDMA_ZTE, CDMA_ZTE_DO, LTE_CDR,LTE_MR
	};

	public static class LONG_LAT {

		public double LON; // 经度

		public double LAT; // 纬度

		public double Height; // 高度。

		public LONG_LAT(double lON, double lAT, double height) {
			super();
			LON = lON;
			LAT = lAT;
			Height = height;
		}

		public LONG_LAT() {
			super();
		}

		@Override
		public String toString() {
			return "LONG_LAT [LON=" + LON + ", LAT=" + LAT + ", Height=" + Height + "]";
		}

	};

	public static class XYZ {

		public double X; // X轴坐标

		public double Y; // Y轴坐标

		public double Z; // Z轴坐标

		public XYZ() {
			super();
		}

		public XYZ(double x, double y, double z) {
			super();
			X = x;
			Y = y;
			Z = z;
		}

		@Override
		public String toString() {
			return "XYZ [X=" + X + ", Y=" + Y + ", Z=" + Z + "]";
		}

	};

	public static class Repeater {

		public LONG_LAT ll;

		public double Radius;

		public Repeater(LONG_LAT ll, double radius) {
			super();
			this.ll = ll;
			Radius = radius;
		}

		public Repeater() {
			super();
		}

		@Override
		public String toString() {
			return "Repeater [ll=" + ll + ", Radius=" + Radius + "]";
		}

	}

	public static class Repeaters {

		int count;

		Repeater[] repeaters;

		public Repeaters(int count, Repeater[] repeaters) {
			super();
			this.count = count;
			this.repeaters = repeaters;
		}

		public Repeaters() {
			super();
		}

		@Override
		public String toString() {
			return "Repeaters [count=" + count + ", repeaters=" + Arrays.toString(repeaters) + "]";
		}

	}

	public static class CELL_INFO {

		public CELL_TYPE CellType; // 小区的类型

		public LONG_LAT LatLong; // 小区的经纬度

		public Repeaters repeaters = new Repeaters();// 直放站

		public float Angle; // 方向角

		public float AngleRang;// 方向角范围

		public float Radius; // 小区半径
		
		//lte使用
		public float antenna_high; // 天线挂高
		//lte使用
		public float dl_ear_fcn; // 频点
		//lte使用    
		public COVERAGE_AREA Coverage_area; //城区或郊区

		public CELL_INFO() {
			super();
		}

		public CELL_INFO(CELL_TYPE cellType, LONG_LAT latLong, Repeaters repeaters, float angle, float angleRang, float radius) {
			super();
			CellType = cellType;
			LatLong = latLong;
			this.repeaters = repeaters;
			Angle = angle;
			AngleRang = angleRang;
			Radius = radius;
		}
		
		public CELL_INFO(CELL_TYPE cellType, LONG_LAT latLong, Repeaters repeaters, float angle, float angleRang, float radius, float antenna_high, float dl_ear_fcn) {
			super();
			CellType = cellType;
			LatLong = latLong;
			this.repeaters = repeaters;
			Angle = angle;
			AngleRang = angleRang;
			Radius = radius;
			this.antenna_high = antenna_high;
			this.dl_ear_fcn = dl_ear_fcn;
		}

		@Override
		public String toString() {
			return "CELL_INFO [CellType=" + CellType + ", LatLong=" + LatLong + ", repeaters=" + repeaters + ", Angle=" + Angle + ", AngleRang="
					+ AngleRang + ", Radius=" + Radius + "]";
		}

	};

	public static class CellInfoType {

		public CELL_INFO cell_info;

		public float rscp;
	}

	public static class GSM_CELL extends CellInfoType {

		public float TA;

		public float pathloss;

		public GSM_CELL(CELL_INFO cell_info, float tA, float rscp, float pathloss) {
			super();
			this.cell_info = cell_info;
			TA = tA;
			this.rscp = rscp;
			this.pathloss = pathloss;
		}

		public GSM_CELL() {
			super();
		}

		@Override
		public String toString() {
			return "GSM_CELL [cell_info=" + cell_info + ", TA=" + TA + ", rscp=" + rscp + ", pathloss=" + pathloss + "]";
		}

	};

	public static class ONEWAYDELAY_CELL extends CellInfoType {
		
		//导频强度，用于排序  add by linp 20150805
		public String strength;

		public float one_way_delay;

		public ONEWAYDELAY_CELL(CELL_INFO cell_info, float one_way_delay) {
			super();
			this.cell_info = cell_info;
			this.one_way_delay = one_way_delay;
		}

		public ONEWAYDELAY_CELL() {
			super();
		}

		@Override
		public String toString() {
			return "CDMA_ONEWAYDELAY_CELL [cell_info=" + cell_info + ", one_way_delay=" + one_way_delay + "]";
		}

	};

	public static class CDMA_EcIo_CELL extends CellInfoType {

		public float EcIo;

		public CDMA_EcIo_CELL(CELL_INFO cell_info, float ecIo) {
			super();
			this.cell_info = cell_info;
			EcIo = ecIo;
		}

		public CDMA_EcIo_CELL() {
			super();
		}

		@Override
		public String toString() {
			return "CDMA_EcIo_CELL [cell_info=" + cell_info + ", EcIo=" + EcIo + "]";
		}

	};

	public static class WCAS_DELAY_CELL extends CellInfoType {

		public float delay;

		public WCAS_DELAY_CELL(CELL_INFO cell_info, float delay) {
			super();
			this.cell_info = cell_info;
			this.delay = delay;
		}

		public WCAS_DELAY_CELL() {
			super();
		}

		@Override
		public String toString() {
			return "WCAS_DELAY_CELL [cell_info=" + cell_info + ", delay=" + delay + "]";
		}

	};

	public static enum DIFFUSE_TYPE {
		DIFF_T_UNUSE, DIFF_T_1, // 使用 DIFF_1 计算
		DIFF_T_2, // 使用 DIFF_2 计算

	};

	public static class WcdmaMr_Rscp_CELL extends CellInfoType {

		public WcdmaMr_Rscp_CELL() {
			super();
		}

	}

	public static class DIFF_1 {

		public double K; // 传播模型公式 系数K

		public double X; // 传播模型公式 系数X

		public DIFF_1() {
			super();
		}

		public DIFF_1(double k, double x) {
			super();
			K = k;
			X = x;
		}

		@Override
		public String toString() {
			return "DIFF_1 [K=" + K + ", X=" + X + "]";
		}

	};

	public static class DIFF_2 {

		public double a; // 暂初始值3.5 代表传播模型公式中的a

		public double A2; // 暂初始值38.45 代表传播模型公式中的A2

		public double r2; // 暂初始值1 代表传播模型公式中的r2

		public double X1; // 暂初始值0 代表传播模型公式中的X1

		public double X2; // 暂初始值0 代表传播模型公式中的X2

		public DIFF_2(double a, double a2, double r2, double x1, double x2) {
			super();
			this.a = a;
			A2 = a2;
			this.r2 = r2;
			X1 = x1;
			X2 = x2;
		}

		public DIFF_2() {
			super();
		}

		@Override
		public String toString() {
			return "DIFF_2 [a=" + a + ", A2=" + A2 + ", r2=" + r2 + ", X1=" + X1 + ", X2=" + X2 + "]";
		}

	};

	public static class DiffuseInfo {

		public static final DiffuseInfo DEFAULT_VAL = new DiffuseInfo(DIFFUSE_TYPE.DIFF_T_1, 78.125, 78.125 / 2, new DIFF_1(0, 32), new DIFF_2(0, 0,
				0, 0, 0));

		public static final DiffuseInfo DEFAULT_WCDMA_VAL = new DiffuseInfo(DIFFUSE_TYPE.DIFF_T_1, 540, 540 / 2, new DIFF_1(3.5, 0), new DIFF_2(0, 0,
				0, 0, 0));

		public DIFFUSE_TYPE type;

		public double TA_STEP; // 步长值

		public double amend; // 步长修正值

		public DIFF_1 d1;

		public DIFF_2 d2;

		public DiffuseInfo(DIFFUSE_TYPE type, double tA_STEP, double amend, DIFF_1 d1, DIFF_2 d2) {
			super();
			this.type = type;
			TA_STEP = tA_STEP;
			this.amend = amend;
			this.d1 = d1;
			this.d2 = d2;
		}

		public DiffuseInfo() {
			super();
		}

		@Override
		public String toString() {
			return "DiffuseInfo [type=" + type + ", TA_STEP=" + TA_STEP + ", amend=" + amend + ", d1=" + d1 + ", d2=" + d2 + "]";
		}

	};
}
