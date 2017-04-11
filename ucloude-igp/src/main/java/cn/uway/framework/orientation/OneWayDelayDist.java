package cn.uway.framework.orientation;

import cn.uway.framework.orientation.Type.CellInfoType;
import cn.uway.framework.orientation.Type.DEV_TYPE;
import cn.uway.framework.orientation.Type.DiffuseInfo;
import cn.uway.framework.orientation.Type.ONEWAYDELAY_CELL;

public class OneWayDelayDist extends DistType {

	public OneWayDelayDist(DEV_TYPE dt, DiffuseInfo diff) {
		this.m_dt = dt;
		this.m_diff = diff;
	}

	@Override
	public double CalcDistance(CellInfoType cell) {
		return LocalOperation.CalcDistanceByOneWayDelay(m_dt, (ONEWAYDELAY_CELL) cell, m_diff);
	}

	@Override
	public double MapDistance(CellInfoType cell) {
		return Distance.DistanceByOneWayDelay(m_dt, ((ONEWAYDELAY_CELL) cell).one_way_delay, m_diff);
	}

}
