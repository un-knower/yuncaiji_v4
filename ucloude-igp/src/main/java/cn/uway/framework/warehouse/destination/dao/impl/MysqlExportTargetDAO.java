package cn.uway.framework.warehouse.destination.dao.impl;

import cn.uway.framework.warehouse.destination.dao.ExportTargetDAO;

/**
 * @author chenrongqiang
 * 
 */
public class MysqlExportTargetDAO extends ExportTargetDAO {

	@Override
	public String getDBExportSQL() {
		return this.loadDbExportTargetSQLForMysql;
	}

	@Override
	public String getRemoteFileExportSQL() {
		return this.loadFileExportTargetSQLForMysql;
	}

}
