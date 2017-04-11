package cn.uway.framework.warehouse.destination.dao.impl;

import cn.uway.framework.warehouse.destination.dao.ExportTargetDAO;

/**
 * @author chenrongqiang
 * 
 */
public class OracleExportTargetDAO extends ExportTargetDAO {

	@Override
	public String getDBExportSQL() {
		return this.loadDbExportTargetSQLForOracle;
	}

	@Override
	public String getRemoteFileExportSQL() {
		return this.loadFileExportTargetSQLForOracle;
	}

}
