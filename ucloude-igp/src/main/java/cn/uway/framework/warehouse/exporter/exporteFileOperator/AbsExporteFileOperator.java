package cn.uway.framework.warehouse.exporter.exporteFileOperator;

import java.io.IOException;
import java.io.OutputStream;

import cn.uway.framework.connection.ConnectionInfo;


public abstract class AbsExporteFileOperator {
	protected ConnectionInfo connectionInfo;
	
	public abstract boolean rename(String srcFile, String dstFile) throws IOException;
	public abstract boolean delete(String filename) throws IOException;
	public abstract boolean ensureDirecotry(String dirName) throws IOException;
	public abstract OutputStream createDstFile(String fileName) throws IOException;
	public abstract boolean completeWrite() throws IOException;
	public abstract void close();
	
	public AbsExporteFileOperator(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
}
