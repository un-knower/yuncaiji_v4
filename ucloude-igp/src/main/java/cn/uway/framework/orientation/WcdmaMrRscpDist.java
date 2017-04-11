package cn.uway.framework.orientation;

import cn.uway.framework.orientation.Type.CellInfoType;
import cn.uway.framework.orientation.Type.DEV_TYPE;
import cn.uway.framework.orientation.Type.DiffuseInfo;

public class WcdmaMrRscpDist extends DistType {

	public WcdmaMrRscpDist(DEV_TYPE dt, DiffuseInfo diff) {
		this.m_dt = dt;
		this.m_diff = diff;
	}

	public double CalcDistance(CellInfoType cell) {
		return Distance.DistanceByRscp(cell.rscp, cell.cell_info.Radius);
	}

	public double MapDistance(CellInfoType cell) {

		return Distance.DistanceByRscp(cell.rscp, cell.cell_info.Radius);

	}
}
