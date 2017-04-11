package cn.uway.framework.orientation;

public class Delta {

	public Delta() {
		x_axis = 0;
		y_axis = 0;
	}

	public double x_axis; // 横坐标距离

	public double y_axis; // 纵坐标距离

	public Delta(double x_axis, double y_axis) {
		super();
		this.x_axis = x_axis;
		this.y_axis = y_axis;
	}

	@Override
	public String toString() {
		return "Delta [x_axis=" + x_axis + ", y_axis=" + y_axis + "]";
	}
}
