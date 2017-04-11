package cn.uway.framework.accessor;

import java.io.File;
import java.io.FileInputStream;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.utils.IoUtil;

public class LocalAccessor extends AbstractAccessor {

	/**
	 * 连接信息
	 */
	protected ConnectionInfo connInfo;

	protected FileInputStream in;

	@Override
	public void setConnectionInfo(ConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}

	@Override
	public AccessOutObject access(GatherPathEntry path) throws Exception {
		String filePath = path.getPath();
		File file = new File(filePath);
		if (in != null) {
			close();
		}
		in = new FileInputStream(file);
		StreamAccessOutObject out = new StreamAccessOutObject();
		out.setOutObject(in);
		out.setLen(file.length());
		out.setRawAccessName(filePath);
		return out;
	}

	public ConnectionInfo getConnInfo() {
		return connInfo;
	}

	public void setConnInfo(ConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}

	@Override
	public void close() {
		if (in != null) {
			IoUtil.closeQuietly(in);
			in = null;
		}

		super.close();
	}
}
