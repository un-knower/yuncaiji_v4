package cn.uway.framework.warehouse.exporter.exporteFileOperator;



import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;


public class HDFSExportFileOperator extends AbsExporteFileOperator {
	private static final ILogger LOGGER = LoggerManager.getLogger(HDFSExportFileOperator.class); // 日志
	protected FileSystem fileSystem;
	public HDFSExportFileOperator(ConnectionInfo connectionInfo) throws Exception {
		super(connectionInfo);
		
		if (connectionInfo instanceof FTPConnectionInfo) {
			FTPConnectionInfo sourceConnectionInfo = (FTPConnectionInfo)connectionInfo;
			try {
				LOGGER.debug("开始尝试连接HDFS文件服务器 ip:{}, port:{} user:{} pass:{}", new Object[]{sourceConnectionInfo.getIp(), sourceConnectionInfo.getPort(), sourceConnectionInfo.getUserName(), sourceConnectionInfo.getPassword()});
				fileSystem = getFileSystem(sourceConnectionInfo.getIp(), sourceConnectionInfo.getPort(), sourceConnectionInfo.getUserName(), sourceConnectionInfo.getPassword());
				LOGGER.debug("连接HDFS文件服务器成功. ip:{}, port:{} user:{} pass:{}", new Object[]{sourceConnectionInfo.getIp(), sourceConnectionInfo.getPort(), sourceConnectionInfo.getUserName(), sourceConnectionInfo.getPassword()});
			} catch (Exception e) {
				this.fileSystem = null;
				LOGGER.error("HDFSExporterFileOperator 连接HDFS服务器失败.", e);
				throw e;
			}
		} else {
			throw new Exception("不正确的接连配置方式.");
		}
	}

	@Override
	public boolean rename(String srcFile, String dstFile) throws IOException {
		Path filePathSrc = new Path(srcFile);
		Path filePathDst = new Path(dstFile);
		return fileSystem.rename(filePathSrc, filePathDst);
	}

	@Override
	public boolean delete(String filename) throws IOException {
		Path filePath = new Path(filename);
		return fileSystem.delete(filePath, false);
	}

	@Override
	public boolean ensureDirecotry(String dirName) throws IOException {
		Path dirPath = new Path(dirName);
		if (fileSystem.exists(dirPath)) {
			return true;
		}
		
		return fileSystem.mkdirs(dirPath);
	}

	@Override
	public void close() {
		if (fileSystem != null) {
			try {
				fileSystem.close();
			} catch (IOException e) {
				LOGGER.error("HDFSExportFileOperator::close() 关闭fileSystem发生异常.", e);
			}
			fileSystem = null;
		}
	}

	@Override
	public boolean completeWrite() throws IOException {
		return true;
	}

	@Override
	public OutputStream createDstFile(String fileName) throws IOException {
		Path pathfile = new Path(fileName);
		return fileSystem.create(pathfile);
	}
	
	/**
	 * 
	 * @param ip		hdfs server ip address
	 * @param port		dfs.namenode.servicerpc-address
	 * @param user		hdfs username
	 * @param pass		hdfs password
	 * @return			FileSystem
	 * @throws Exception
	 */
    public synchronized static FileSystem getFileSystem(String ip, int port, String user, String pass) throws Exception {  
        FileSystem fs = null;  
        String url = "hdfs://" + ip + ":" + String.valueOf(port);  
        Configuration config = new Configuration();  
        config.set("fs.default.name", url);  
        try {  
        	
        	URI uri = new URI(url);
            fs = FileSystem.get(uri, config, user);  
        } catch (Exception e) {
        	throw e;
        }  
        return fs;  
    } 
}
