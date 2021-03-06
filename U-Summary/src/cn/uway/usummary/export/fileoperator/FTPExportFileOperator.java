package cn.uway.usummary.export.fileoperator;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.entity.ConnectionInfo;
import cn.uway.usummary.util.FTPClientUtil;
import cn.uway.usummary.util.FTPUtil;


public class FTPExportFileOperator extends AbsExporteFileOperator {
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPExportFileOperator.class); // 日志
    protected FTPClient	ftpClient;
    
	public FTPExportFileOperator(ConnectionInfo connectionInfo) throws Exception {
		super(connectionInfo);
		try {
			ftpClient = FTPClientUtil.connectFTP(connectionInfo);
		} catch (Exception e) {
			this.ftpClient = null;
			LOGGER.error("RemoteFTPFileExporter 连接FTP失败.", e);
			throw e;
		}	

	}
	
	@Override
	public boolean rename(String srcFile, String dstFile) throws IOException {
		return ftpClient.rename(srcFile, dstFile);
	}

	@Override
	public boolean delete(String filename) throws IOException {
		return ftpClient.deleteFile(filename);
	}

	@Override
	public boolean ensureDirecotry(String dirName) throws IOException {
		try {
			if (!ftpClient.changeWorkingDirectory(dirName)) {
				LOGGER.debug("ftp文件根目录{}不存在.开始尝试创建目录...", dirName);
				if (ftpClient.makeDirectory(dirName)) {
					LOGGER.debug("ftp文件根目录{}.创建成功!", dirName);
				} else {
					LOGGER.error("ftp文件根目录{}.创建失败!", dirName);
					return false;
				}
			}
			
			return true;
		} catch (IOException e) {
			LOGGER.error("尝试改变到ftp文件目录：" + dirName + "失败", e);
		}
		
		return false;
	}

	@Override
	public void close() {
		// 关闭ftpClent
		if (this.ftpClient!= null) {
			FTPUtil.logoutAndCloseFTPClient(this.ftpClient);
			this.ftpClient = null;
		}
	}

	@Override
	public boolean completeWrite() throws IOException {
		return this.ftpClient.completePendingCommand();
	}

	@Override
	public OutputStream createDstFile(String fileName) throws IOException {
		return ftpClient.storeFileStream(fileName);
	}

}
