package cn.uway.framework.warehouse.exporter.exporteFileOperator;

import cn.uway.framework.connection.ConnectionInfo;


public class ExporterFileOperatorFactory {
	public static AbsExporteFileOperator createExporterFileOperator(ConnectionInfo connectionInfo) throws Exception {
		if (connectionInfo.getConnType() == ConnectionInfo.CONNECTION_TYPE_FTP || connectionInfo.getConnType() == ConnectionInfo.CONNECTION_TYPE_SFTP)
			return new FTPExportFileOperator(connectionInfo);
		
		if (connectionInfo.getConnType() == ConnectionInfo.CONNECTION_TYPE_HDFS)
			return new HDFSExportFileOperator(connectionInfo);
		
		throw new Exception("unknow connection type:" + connectionInfo.getConnType());
	}
}
