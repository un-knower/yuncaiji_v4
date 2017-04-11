package cn.uway.framework.connection.dao;

import cn.uway.framework.connection.ConnectionInfo;

/**
 * 连接信息查询DAO
 * 
 * @author chenrongqiang @ 2014-3-26
 */
public interface ConnectionInfoDAO {

	/**
	 * 通过ID获取连接信息
	 * 
	 * @param connId
	 * @return ConnectionInfo
	 * @see{ConnectionInfo}
	 */
	ConnectionInfo getConnectionInfo(int connId);

}
