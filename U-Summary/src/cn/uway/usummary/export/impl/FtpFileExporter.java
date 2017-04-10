package cn.uway.usummary.export.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.cache.MemoryCacher;
import cn.uway.usummary.entity.ConnectionInfo;
import cn.uway.usummary.entity.ExporterArgs;
import cn.uway.usummary.export.AbstractExporter;
import cn.uway.usummary.export.fileoperator.AbsExporteFileOperator;
import cn.uway.usummary.export.fileoperator.FTPExportFileOperator;
import cn.uway.usummary.util.Packer;

public class FtpFileExporter extends AbstractExporter{

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileExporter.class); // 日志
	
	// 1M的文件尺寸
	private static final long SIZE_OF_MB = (1024*1024);

	// 文件流对象 在Exporter创建的时候初始化 必须注意流的关闭和释放
	protected BufferedWriter localWriter;
	
	// 文件流对象 在Exporter创建的时候初始化 必须注意流的关闭和释放
	protected BufferedOutputStream bos;
	
	// 输出路径 从Export模版中读取
	protected String exportPath;
	
	protected String localExportPath;

	// 输出文件名 在Export模版中配置 并且应该支持文件名的适配
	protected String exportFileName;
	
	// 输出完整路径+文件名 
	protected String exportFullPathName;
	
	// 输出完整路径+文件名的临时文件
	protected String exportTmpPathName;
	
	// 输出字段 即数据源的key值
	protected List<String> exportFields;

	/* 是否输出表头。 */
	protected boolean bHeader;

	// 此次写入是否是在之前的文件上追加的
	protected boolean bAppend;

	// 输出器参数定义
	protected ExporterArgs exporterArgs;

	protected boolean zipFlag = false;

	protected String compressFormat = null;
	
    protected String splitChar =",";
    
    protected int partFileIndex = 0;
    
    protected long partFileSize;
    
    protected long partFileMaxSize;
    
    protected Date fileCreateTime;
    protected Date fileUploadBeginTime;
    protected Date fileUploadEndTime;
    
    // 多少条数据一个文件
    protected int partFileNum;
    
    protected int batchNum;
    
    /**
     * 参数
     */
    protected Map<String, String> exportArgumentsMap = new HashMap<String, String>();
    
    protected OutputStream outFileStream = null;
    
    // 写入的临时文件名
 	protected String tempFileName;
    
    /**
     * 文件助手
     */
    protected AbsExporteFileOperator fileOperator;
    
    
    private ConnectionInfo connInfo = null;
    
    private String encode;
    
	/**
	 * 
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public FtpFileExporter(ExporterArgs exporterArgs) {
		super(exporterArgs);
		this.compressFormat = exporterArgs.getFtpExpInfo().getCompressFormat();
		if (this.compressFormat != null && this.compressFormat.length() > 0) {
			this.compressFormat = this.compressFormat.toLowerCase();
			if (this.compressFormat.startsWith(".")) {
				this.compressFormat = this.compressFormat.substring(1);
			}
			this.zipFlag = true;
		} else {
			this.zipFlag = false;
		}
		
		// 分解附加参数(扩展使用)
		String additionParams = exporterArgs.getFtpExpInfo().getAdditionParams();
		if (additionParams != null) {
			additionParams = additionParams.trim();
			additionParams.replace(",", ";");
			String[] params = additionParams.split("\\;");
			if (params!= null && params.length > 0) {
				for (String param : params) {
					String[] entry = param.split("\\=");
					if (entry == null || entry.length != 2)
						continue;
					
					String key = entry[0].trim().toUpperCase();
					String value = entry[1].trim();
					if (key.length()<1 || value.length()<1)
						continue;
					
					exportArgumentsMap.put(key, value);
				}
			}
		}
		connInfo = exporterArgs.getConnInfo();
		this.exporterArgs = exporterArgs;
		this.exportFields = exporterArgs.getHeaders();
		this.encode = exporterArgs.getFtpExpInfo().getEncode();
		this.bHeader = exporterArgs.getFtpExpInfo().isExportHeader();
		this.splitChar = exporterArgs.getFtpExpInfo().getSplitChar();
		this.batchNum = exporterArgs.getFtpExpInfo().getBatchNum();
		if (StringUtils.isEmpty(this.splitChar))
			this.splitChar = ",";
		
		String sPartFileMaxSize = exportArgumentsMap.get("partFileMaxSize".toUpperCase());
		if (sPartFileMaxSize != null && sPartFileMaxSize.length() > 0) {
			this.partFileMaxSize = Long.parseLong(sPartFileMaxSize);
			this.partFileMaxSize *= SIZE_OF_MB;
		}
		
		// 将导出文件的路径中的“%%”占位符改为实际值。
		this.exportPath = convertPath(exporterArgs.getFtpExpInfo().getExportPath(), false);
		this.localExportPath = ".."+this.exportPath;
		
		
		try {
			checkLocalFile();
			checkLocalExportFile();
			this.writeHeader();
			createFTP();
		} catch (Exception e) {
			LOGGER.warn("FtpFileExporter创建失败", e);
			this.errorCode = 0;
			this.cause = "获取输出数据源连接失败!";
		}
		
		this.cacher = new MemoryCacher();
	}
	
	protected void checkLocalFile() throws IOException {
		File rootFile = new File(localExportPath);
		if (!rootFile.exists() && rootFile.mkdirs())
			LOGGER.debug("文件根目录{}不存在.尝试创建目录成功!", localExportPath);
	}
	
	/**
	 * 输出文件初始化 检查temp文件是否存在 如存在 则追加的方式写入 如不存在，则新建temp文件
	 * 
	 * @throws IOException
	 */
	protected void checkLocalExportFile() throws IOException {
		this.partFileSize = 0;
		this.partFileNum = 0;
		++this.partFileIndex;
		this.exportFileName = convertPath(exporterArgs.getFtpExpInfo().getExportFileName(), true);
		String tempFile = new StringBuilder(this.exportFileName).append(".temp").toString();
		String outFileName = localExportPath + File.separator + tempFile;
		File tmpFile = new File(outFileName);
		this.tempFileName = tmpFile.getAbsolutePath();
		if (tmpFile.exists()&&tmpFile.delete())
		{
			LOGGER.debug("已删除已存在的本地文件：{}", outFileName);
		}
		File dir = new File(FilenameUtils.getFullPath(outFileName));
		if (!dir.exists() || !dir.isDirectory()) {
			if (dir.mkdirs())
				LOGGER.debug("目录{}不存在,创建目录成功.", dir);
			else
				LOGGER.error("目录{}不存在,创建目录失败。", dir);
		}
		if (!tmpFile.exists()) {
			boolean fg = tmpFile.createNewFile();
			LOGGER.debug("创建临时文件={},创建结果为{}",  new Object[]{tmpFile.getPath(), fg});
		}
		createWriter(tmpFile);
	}
	
	/**
	 * 初始化文件流
	 * 
	 * @param exportFileName
	 * @throws IOException
	 */
	protected void createWriter(File exportFileName) throws IOException {
		// 文件输出需要设置编码，否则会出现产生的文件为乱码
		FileOutputStream fos = new FileOutputStream(exportFileName, false);
		OutputStreamWriter osw = null;
		if (this.encode == null)
			osw = new OutputStreamWriter(fos);
		else
			osw = new OutputStreamWriter(fos, this.encode);
		this.localWriter = new BufferedWriter(osw, 4 * 1024 * 1024);
		// end change
	}
	
	protected String getRealExportFileName(String exportFileName) {
		if (this.zipFlag)
			return exportFileName.substring(0, exportFileName.lastIndexOf(".")) + "." + (compressFormat == null ? "zip" : compressFormat);
		return exportFileName;
	}

	protected void writeHeader() {
		if (this.localWriter == null)
			return;
		
		int nSplitCharLength = splitChar.length();
		try {
			// 如果是在之前文件上追加，就不用再写表头的，不能把表头写到文件中间。
			// if (!this.bHeader || this.bAppend)
			if (!this.bHeader)
				return;
			if (this.exportFields == null || this.exportFields.size() == 0)
				return;
			int fieldNum = this.exportFields.size();
			for (int i = 0; i < fieldNum; i++) {
				String name = this.exportFields.get(i);
				this.localWriter.write(name);
				if (i < this.exportFields.size() - 1) {
					this.localWriter.write(splitChar);
				}
				
				this.partFileSize += (name.length() + nSplitCharLength);
			}
			this.localWriter.flush();
			this.localWriter.newLine();
			++this.partFileSize;
		} catch (Exception e) {
			LOGGER.warn("写表头失败。", e);
			this.errorCode = 0;
			this.cause = "向文件写入表头失败!";
		}
	}

	protected void writeFile(Map<String,String> outDate) {
		
		this.current++;
		this.partFileNum ++;
		String lineRecord = createLineMessage(outDate);
		try {
			if (fileUploadBeginTime == null) {
				fileUploadBeginTime = new Date(System.currentTimeMillis());
			}
			
			localWriter.write(lineRecord);
			localWriter.newLine();
			this.partFileSize +=(lineRecord.length()+1);
			this.succ++;
		} catch (IOException e) {
			this.fail++;
			this.errorCode = 0;
			this.cause = "FtpFileExporter输出失败!";
			LOGGER.error("FtpFileExporter输出失败,dest=" + this.dest, e);
		}
		
		// 如果文件超过了指定的尺寸，则换行
		if (this.partFileMaxSize > 0 
				&& this.partFileMaxSize > 0
				&& this.partFileSize >= this.partFileMaxSize) {
			try {
				this.pack();
				this.createNextWriteFile();
				this.completeWriteFile();
				this.checkLocalFile();
				this.checkLocalExportFile();
				this.writeHeader();
			} catch (Exception e) {
				this.errorCode = 0;
				this.cause = "分包或打包文件失败!";
				LOGGER.error("分包或打包文件失败：", e);
			}
			
		}else if(this.batchNum > 0 
				&& this.partFileNum > this.batchNum){
			try {
				this.pack();
				this.createNextWriteFile();
				this.completeWriteFile();
				this.checkLocalFile();
				this.checkLocalExportFile();
				this.writeHeader();
			} catch (Exception e) {
				this.errorCode = 0;
				this.cause = "分包或打包文件失败!";
				LOGGER.error("分包或打包文件失败：", e);
			}
		}
	}
	private void createFTP()
	{
		try {
			fileOperator = new FTPExportFileOperator(connInfo);
		} catch (Exception e) {
			this.fileOperator = null;
			this.errorCode = 0;
			this.cause = "FtpFileExporter 连接目标服务器失败!";
			LOGGER.error("FtpFileExporter 连接目标服务器失败.", e);
			try {
				File sourceFile = new File(localExportPath + File.separator + getRealExportFileName(this.exportFileName));
				if(sourceFile.exists())
				{
//					LOGGER.debug("删除本地打包文件：{}", new Object[]{sourceFile});
					sourceFile.delete();
				}

			} catch (Exception e1) {
				this.errorCode = 0;
				this.cause = "FTP服务器异常,删除本地文件时，出现异常!";
				LOGGER.error("FTP服务器异常,删除本地文件时，出现异常，原因: ", e1);
			} 
			return;
		}
	}
	
	public void pack() {
		release();
		File tmpFile = new File(tempFileName);
		if (this.total == 0L) {
			LOGGER.debug("记录数为0，文件{}无效，尝试删除文件", tempFileName);
			tmpFile.delete();
			return;
		}
		// 在关闭的时候再次进行文件名转换 因为解析过程中数据时间可能会被更新
		String packFileName = localExportPath + File.separator + getRealExportFileName(this.exportFileName);
		String outFileName = localExportPath + File.separator + this.exportFileName;
		File exportFile = new File(outFileName);
//		File packFile = new File(packFileName);
		LOGGER.debug("文件输出完成,将会把临时文件{}重命名至{}", new Object[]{tmpFile, exportFile});
		boolean renameFileFlag = false;
		boolean deleteFlag = true;
		try {
			if (!tmpFile.exists()) {
				LOGGER.error("临时文件{}已不存在。重命名失败。", tmpFile);
				return;
			}
			if (exportFile.exists())
				LOGGER.debug("已经存在同名文件={},尝试删除{}", new Object[]{exportFile, (deleteFlag = exportFile.delete()) ? "成功" : "失败"});
			if (!deleteFlag) {
				LOGGER.error("删除同名文件{}失败,文件输出线程退出!", exportFile);
				return;
			}
			
			if (this.zipFlag && ("tar.gz".equalsIgnoreCase(compressFormat)||"zip".equalsIgnoreCase(compressFormat))) {
				renameFileFlag = tmpFile.renameTo(exportFile);
				if(!renameFileFlag)
				{
					LOGGER.debug("重命名文件{}至{}完成,结果={}", new Object[]{tmpFile, exportFile, renameFileFlag ? "成功" : "失败"});
					return;
				}
//				if(packFile.exists())
//				{
//					LOGGER.debug("打包文件已经存在：{}", new Object[]{packFile});
//				}
				List<String> list =new ArrayList<String> ();
				list.add(outFileName);
				renameFileFlag = Packer.pack(list, packFileName);
				if(renameFileFlag && exportFile.exists())
					exportFile.delete();
				
			}else{
				renameFileFlag = tmpFile.renameTo(exportFile);
			}
		} catch (Exception e) {
			this.errorCode = 0;
			this.cause = "压缩文件："+tmpFile.getPath()+"出错!";
			LOGGER.error("压缩文件："+tmpFile.getPath()+"出错", e);
			throw new RuntimeException(e);
		}
		LOGGER.debug("压缩文件{}到{},结果={}", new Object[]{exportFile,packFileName, renameFileFlag ? "成功" : "失败"});
	}
	
	/**
	 * 资源释放方法 关闭文件流
	 */
	public void release() {
		try {
			localWriter.flush();
			localWriter.close();
		} catch (Exception e) {
			LOGGER.error("文件流关闭失败!", e);
		}
	}

	/**
	 * 创建一条输出记录
	 * 
	 * @param record
	 * @return
	 */
	protected String createLineMessage(Map<String, String> record) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < exportFields.size(); i++) {
			String val = record.get(exportFields.get(i));
			if (val == null)
				val = "";
			if (val.equalsIgnoreCase("NaN")) {
				val = "";
			}
			
			val= val.replace(splitChar, " ");
			if (i == exportFields.size() - 1) {
				stringBuffer.append(val);
				break;
			}
			stringBuffer.append(val + splitChar);
		}
		return stringBuffer.toString();
	}

	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
		this.pack();
		this.createNextWriteFile();
		completeWriteFile();
		// 关闭fileOperator
		if (this.fileOperator != null) {
			fileOperator.close();
			this.fileOperator = null;
		}			
	}
	
	public void createNextWriteFile() {
		if (this.exportTmpPathName != null) {
			LOGGER.warn("exportTmpPathName:{} 不为空，输出文件可能未被正常关闭", this.exportTmpPathName);
		}
		
		fileCreateTime = null;
		fileUploadBeginTime = null;
		fileUploadEndTime = null;
		
		this.exportFullPathName = this.exportPath;
		if (!exportFullPathName.endsWith("/"))
			exportFullPathName += "/";
		exportFullPathName += this.exportFileName;
		if (this.compressFormat != null && this.compressFormat.length() > 0) {
			if (!exportFullPathName.endsWith("." + this.compressFormat)) {
				this.exportFullPathName = getRealExportFileName(this.exportFullPathName);
//				exportFullPathName += this.exportFullPathName.substring(0, this.exportFileName.lastIndexOf("."));
//				exportFullPathName += ("." + this.compressFormat);
			}
		}
		this.dest = exportFullPathName;
		this.exportTmpPathName = exportFullPathName + ".tmp";
		
		try {
			checkExportDirectory();
			checkExportFile();
		} catch (Exception e) {
			this.errorCode = 0;
			this.cause = "FtpFileExporter创建失败!";
			LOGGER.warn("FtpFileExporter创建失败", e);
		}
		
		fileCreateTime = new Date(System.currentTimeMillis());
	}

	/**
	 * 资源释放方法 关闭文件流
	 */
	public void completeWriteFile() {
		try {
			if (bos != null) {
				bos.flush();
				bos.close();
				bos = null;
			}
			
			if (outFileStream != null) {
				outFileStream.flush();
				outFileStream.close();
				outFileStream = null;
			}
			
			if (this.fileOperator != null && this.exportTmpPathName != null) {
				if (this.fileOperator.completeWrite()) {
					LOGGER.debug("临时文件:{} 已成功上传到目标服务器.", this.exportTmpPathName);
					
					LOGGER.debug("将临时文件{}重命名至{}", new Object[]{exportTmpPathName, exportFullPathName});
					boolean renameFileFlag = false;
					try {
						renameFileFlag =  fileOperator.rename(exportTmpPathName, exportFullPathName);
					} catch (Exception e) {
						LOGGER.error("将目标服务器上面的时时文件改名失败："+exportTmpPathName+"出错", e);
						throw new RuntimeException(e);
					}
					
					fileUploadEndTime = new Date(System.currentTimeMillis());
					LOGGER.debug("重命名文件{}至{}完成,结果={}", new Object[]{exportTmpPathName, exportFullPathName, renameFileFlag ? "成功" : "失败"});
				} else {
					LOGGER.error("检验目标服务器文件:{} 上传失败.", this.exportTmpPathName);
				}
				
				this.exportTmpPathName = null;
			}
		} catch (Exception e) {
			this.errorCode = 0;
			this.cause = "目标服务器文件流关闭失败!";
			LOGGER.error("目标服务器文件流关闭失败!", e);
		}
	}
	
	/**
	 * 文件夹检查 避免因为文件夹不存在生成文件失败
	 * 
	 * @throws IOException
	 */
	protected void checkExportDirectory() throws IOException {
		String [] dirLevels = exportPath.split("/");
		String workPath = "/";
		for (String subDir : dirLevels) {
			if (subDir == null || subDir.length() < 1)
				continue;
			
			if (!workPath.endsWith("/"))
				workPath += "/";
			
			workPath += subDir;
			if (!fileOperator.ensureDirecotry(workPath)) {
				return;
			}
		}
	}

	/**
	 * 输出文件初始化 检查temp文件是否存在 如存在 则追加的方式写入 如不存在，则新建temp文件
	 * 
	 * @throws IOException
	 */
	protected void checkExportFile() throws IOException {
		if (fileOperator.delete(exportTmpPathName))
			LOGGER.debug("已删除已存在的目标服务器文件：{}", exportTmpPathName);
		
		if (fileOperator.delete(exportFullPathName))
			LOGGER.debug("已删除已存在的目标服务器文件：{}", exportFullPathName);
		
		outFileStream = fileOperator.createDstFile(exportTmpPathName);
		this.bos = new BufferedOutputStream(outFileStream, 4 * 1024 * 1024);
		this.writeToFtp();
	}
	
	private void writeToFtp()
	{
		FileInputStream in = null;
		File sourceFile = null;
		try {
			sourceFile = new File(localExportPath + File.separator + getRealExportFileName(this.exportFileName));
			in = new FileInputStream(sourceFile);
			int number = 0;
			byte[] B_ARRAY = new byte[1024 * 4];
			while ((number = in.read(B_ARRAY, 0, 1024 * 4)) != -1) {
				bos.write(B_ARRAY, 0, number);
			}

		} catch (Exception e) {
			this.errorCode = 0;
			this.cause = "传输文件到FTP服务器异常!";
			LOGGER.error("传输文件到FTP服务器异常. 原因: ", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if(sourceFile.exists())
				{
//					LOGGER.debug("删除本地打包文件：{}", new Object[]{sourceFile});
					sourceFile.delete();
				}
					
			} catch (Exception e) {
				LOGGER.error("关闭流或删除本地文件异常. 原因: ", e);
			}
		}
	}

	/**
	 * 具体的输出方法 线程循环调用 指导数据输入完成
	 */
	public void export(List<Map<String,String>> records) {
		for (Map<String,String> outDate : records) {
			this.writeFile(outDate);
		}
	}
	
	protected String convertPath(String str, boolean isFileName) {
		String s = str;
		s = s.replaceAll("%%SQLNUM", this.exporterArgs.getSqlNum()+"");
		s = s.replaceAll("%%PARTFILEINDEX", String.valueOf(partFileIndex));
		// 如果当前数据时间未空 则不为处理
		if (this.exporterArgs.getDataTime() == null)
			return s;
		Calendar cal = Calendar.getInstance();
		
		// oss2.0 PM与MR采集当前时间前4个小时的数据
		cal.setTimeInMillis(this.exporterArgs.getDataTime().getTime());

		s = s.replaceAll("%%Y", String.format("%04d", cal.get(Calendar.YEAR)));
		s = s.replaceAll("%%M", String.format("%02d", cal.get(Calendar.MONTH) + 1));
		s = s.replaceAll("%%D", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
		s = s.replaceAll("%%H", String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		s = s.replaceAll("%%m", String.format("%02d", cal.get(Calendar.MINUTE)));
		s = s.replaceAll("%%S", String.format("%02d", cal.get(Calendar.SECOND)));
		
		// 将斜杠改为反斜杠
		if (!isFileName) {
			s = s.replace("\\\\", "/");
		}
		
		return s;
	}
}
