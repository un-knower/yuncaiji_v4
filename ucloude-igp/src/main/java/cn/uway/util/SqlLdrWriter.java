package cn.uway.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.warehouse.exporter.template.SqlldrColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.SqlldrTableTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 用于写sqlldr文件。
 * 
 * @author  
 */
public class SqlLdrWriter {
	
	private static final ILogger LOGGER = LoggerManager.getLogger(SqlLdrWriter.class); // 日志

	// 文件名（包括路径）
	private String fileName;

	// 表示累积多少条数据后写一入次，默认100
	private int backlogCount = 100;

	private int count = 0; // 记数，记录当前写到了第多少条数据

	private String charset; // 数据库字符集

	private String split;

	private SqlldrTableTemplateBean tableTemlateBean;
	
	private DatabaseConnectionInfo connectionInfo;

	private FileWriter fileWriter;

	public SqlLdrWriter(String fileName) throws Exception {
		super();
		this.fileName = fileName;
	}

	public SqlLdrWriter(String fileName, int backlogCount, String split) throws Exception {
		super();
		this.fileName = fileName;
		this.backlogCount = backlogCount;
		this.split = split;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public int getBacklogCount() {
		return backlogCount;
	}
	
	public void setBacklogCount(int backlogCount) {
		this.backlogCount = backlogCount;
	}
	
	public String getCharset() {
		return charset;
	}
	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public SqlldrTableTemplateBean getTableTemlateBean() {
		return tableTemlateBean;
	}

	public void setTableTemlateBean(SqlldrTableTemplateBean tableTemlateBean) {
		this.tableTemlateBean = tableTemlateBean;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getSplit() {
		return split;
	}

	public DatabaseConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(DatabaseConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	/**
	 * 写数据
	 * 
	 * @param data
	 *            要写入的数据
	 * @param immediat
	 *            是否立即写入
	 * @throws Exception
	 */
	public void write(String data, boolean immediat) throws Exception {
		fileWriter.write(data + "\r\n");
		if (immediat) {
			fileWriter.flush();
		} else {
			if (count % backlogCount == 0) {
				fileWriter.flush();
			}
		}
		count++;
	}


	public void dispose() {
		try {
			if (fileWriter != null) {
				fileWriter.flush();
				fileWriter.close();
			}
		} catch (Exception e) {
			LOGGER.error("文件流关闭失败!", e);
		}
	}

	/**
	 * 提交写文件操作，并创建控制文件
	 * 
	 * @throws Exception
	 */
	public void commit() throws Exception {
		try {
			fileWriter.flush();
			fileWriter.close();
			// 创建控制文件
			createCltFile();
		} catch (Exception e) {
			LOGGER.error("写入sqlldr失败",e);
		}
	}

	/**
	 * 写入数据文件txt的head,sign为分隔符
	 * @param sign
	 * @throws Exception
	 */
	public void writeHead(String sign) throws Exception {
		List<SqlldrColumnTemplateBean> columns = tableTemlateBean.getColumnsList();
		int len = columns.size();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len - 1; i++) {
			SqlldrColumnTemplateBean pe = columns.get(i);
			sb.append(pe.getColumnName()).append(sign);
		}
		SqlldrColumnTemplateBean pe = columns.get(len - 1);
		sb.append(pe.getColumnName());

		write(sb.toString(), true);
	}

	public void open() throws Exception {
		if(fileWriter == null){
			fileWriter = new FileWriter(fileName);
			writeHead(split);
		}
	}

	/**
	 * 创建控制文件
	 * @throws Exception
	 */
	private void createCltFile() throws Exception {
		String name = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "clt";
		File file = new File(name);
		if(file.exists())
			return;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(name, false));
			bw.write("load data\r\n");
			if (Util.isNotNull(charset))
				bw.write("CHARACTERSET " + charset + " \r\n");
			else
				bw.write("CHARACTERSET ZHS16GBK \r\n");

			bw.write("infile '" + fileName + "'\r\n");
			bw.write("append into table " + tableTemlateBean.getTableName() + " \r\n");
			bw.write("FIELDS TERMINATED BY \""+getSplit()+"\"\r\n");
			bw.write("TRAILING NULLCOLS\r\n");
			bw.write("(");
			//从缓存中获取cltStr字符串
			String cltKey = connectionInfo.getUrl()+connectionInfo.getUserName()+tableTemlateBean.getTableName();
			String cltStr = SqlldrManager.getInstance().getTableClt(cltKey);
			if(Util.isNotNull(cltStr)){
				bw.write(cltStr);
			}else{
				LOGGER.warn("not exist key: "+cltKey);
				SqlldrManager.getInstance().showTableClt();
			}
			bw.write(")");
			bw.write("\r\n");
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(bw);
		}
	}
}
