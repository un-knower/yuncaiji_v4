package cn.uway.framework.warehouse.exporter;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.dao.ConnectionInfoDAO;
import cn.uway.framework.connection.dao.impl.DatabaseConnectionInfoDAO;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.context.Vendor;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.exporteFileOperator.AbsExporteFileOperator;
import cn.uway.framework.warehouse.exporter.exporteFileOperator.ExporterFileOperatorFactory;
import cn.uway.framework.warehouse.exporter.template.FieldTemplateBean;
import cn.uway.framework.warehouse.exporter.template.RemoteFileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.RemoteFileExporterBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.MappingUtil;
import cn.uway.util.Packer;
import cn.uway.util.SHAJDKUtil;
import cn.uway.util.StringUtil;

public class ExtendRemoteFileExporter extends AbstractExporter{

	private static final ILogger LOGGER = LoggerManager.getLogger(ExtendRemoteFileExporter.class); // 日志
	
	// 1M的文件尺寸
	private static final long SIZE_OF_MB = (1024*1024);
	
	/**
	 * 连接信息查询DAO
	 */
	protected static ConnectionInfoDAO connectionInfoDAO = AppContext.getBean("connectionInfoDAO", DatabaseConnectionInfoDAO.class);

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
	protected List<FieldTemplateBean> exportFields;

	/* 是否输出表头。 */
	protected boolean bHeader;

	// 此次写入是否是在之前的文件上追加的
	protected boolean bAppend;

	// 输出器参数定义
	protected ExporterArgs exporterArgs;

	protected boolean zipFlag = false;

	protected String compressFormat = null;

	// 输出模版
	protected RemoteFileExportTemplateBean exportTempletBean;

	protected RemoteFileExporterBean remoteFileExportTargetBean;
	
    protected String splitChar =",";
    
    protected int partFileIndex = 0;
    
    protected long partFileSize;
    
    protected long partFileMaxSize;
    
    protected Date fileCreateTime;
    protected Date fileUploadBeginTime;
    protected Date fileUploadEndTime;
    
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
    
    protected StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);
    
    private ConnectionInfo connInfo = null;
    
	/**
	 * 
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public ExtendRemoteFileExporter(RemoteFileExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, exportTempletBean.getId());
		this.exportId = exportTempletBean.getId();
		setBreakPoint();
		this.exportTempletBean = exportTempletBean;
		this.remoteFileExportTargetBean = (RemoteFileExporterBean) exportTempletBean.getExportTargetBean();
		this.compressFormat = remoteFileExportTargetBean.getCompressFormat();
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
		String additionParams = remoteFileExportTargetBean.getAdditionParams();
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
		connInfo = connectionInfoDAO.getConnectionInfo(remoteFileExportTargetBean.getConnID());
		this.exporterArgs = exporterArgs;
		this.exportFields = exportTempletBean.getExportFileds();
		this.exportType = exportTempletBean.getType();
		this.encode = exportTempletBean.getEncode();
		this.bHeader = remoteFileExportTargetBean.isExportHeader();
		this.splitChar = remoteFileExportTargetBean.getSplitChar();
		if (StringUtil.isEmpty(this.splitChar))
			this.splitChar = ",";
		
		String sPartFileMaxSize = exportArgumentsMap.get("partFileMaxSize".toUpperCase());
		if (sPartFileMaxSize != null && sPartFileMaxSize.length() > 0) {
			this.partFileMaxSize = Long.parseLong(sPartFileMaxSize);
			this.partFileMaxSize *= SIZE_OF_MB;
		}
		
		// 将导出文件的路径中的“%%”占位符改为实际值。
		this.exportPath = convertPath(remoteFileExportTargetBean.getExportPath(), false);
		this.localExportPath = ".."+this.exportPath;
		
		
		try {
			checkLocalFile();
			checkLocalExportFile();
			this.writeHeader();
		} catch (Exception e) {
			LOGGER.warn("ExtendRemoteFileExporter创建失败", e);
		}
		
		createCacher(AbstractCacher.MEMORY_CACHER);
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
		++this.partFileIndex;
		this.exportFileName = convertPath(remoteFileExportTargetBean.getExportFileName(), true);
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
			if (this.exportFields == null || this.exportFields.isEmpty())
				return;
			int fieldNum = this.exportFields.size();
			for (int i = 0; i < fieldNum; i++) {
				String name = this.exportFields.get(i).getColumnName();
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
		}
	}

	protected void writeFile(ParseOutRecord out) {
		
		this.current++;
		this.breakPoint++;
		Map<String, String> cdlRecord = out.getRecord();
		String lineRecord = createLineMessage(cdlRecord);
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
			LOGGER.error("ExtendRemoteFileExporter输出失败,dest=" + this.dest, e);
		}
		
		// 如果文件超过了指定的尺寸，则换行
		if (this.partFileMaxSize > 0 && this.partFileSize >= this.partFileMaxSize) {
			try {
				this.pack();
				createFTP();
				this.createNextWriteFile();
				this.completeWriteFile();
				this.checkLocalFile();
				this.checkLocalExportFile();
				this.writeHeader();
			} catch (Exception e) {
				LOGGER.warn("分包或打包文件失败：", e);
			}
			
		}
	}
	private void createFTP()
	{
		try {
			fileOperator = ExporterFileOperatorFactory.createExporterFileOperator(connInfo);
		} catch (Exception e) {
			this.fileOperator = null;
			this.errorCode = -1;
			this.cause = e.getMessage();
			LOGGER.error("ExtendRemoteFileExporter 连接目标服务器失败.", e);
			try {
				File sourceFile = new File(localExportPath + File.separator + getRealExportFileName(this.exportFileName));
				if(sourceFile.exists())
				{
//					LOGGER.debug("删除本地打包文件：{}", new Object[]{sourceFile});
					sourceFile.delete();
				}

			} catch (Exception e1) {
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
			FieldTemplateBean temp = exportFields.get(i);
			String val = record.get(temp.getPropertyName());
			
			if (val == null)
				val = "";
			if (val.equalsIgnoreCase("NaN")) {
				val = "";
			}
			val = valueFormate(val,temp);
			val= val.replace(splitChar, " ");
			if (i == exportFields.size() - 1) {
				stringBuffer.append(val);
				break;
			}
			stringBuffer.append(val + splitChar);
		}
		return stringBuffer.toString();
	}
	
	private String valueFormate(String val,FieldTemplateBean temp){
		if(!StringUtils.isEmpty(val)){
			String format = temp.getFormat();
			if(format != null && format.equalsIgnoreCase("sha1")){
				try {
					return SHAJDKUtil.jdksha(val,SHAJDKUtil.SHAUtilAGORITHM_1);
				} catch (NoSuchAlgorithmException e) {
					LOGGER.error("sha1加密出错！",e);
				}
			}
		}
		return val;
	}

	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
		this.pack();
		createFTP();
		this.createNextWriteFile();
		completeWriteFile();
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
				exportFullPathName += this.exportFullPathName.substring(0, this.exportFileName.lastIndexOf("."));
				exportFullPathName += ("." + this.compressFormat);
			}
		}
		this.dest = exportFullPathName;
		this.exportTmpPathName = exportFullPathName + ".tmp";
		
		try {
			checkExportDirectory();
			checkExportFile();
		} catch (Exception e) {
			LOGGER.warn("ExtendRemoteFileExporter创建失败", e);
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
			LOGGER.error("目标服务器文件流关闭失败!", e);
		}
		
		// 关闭fileOperator
		if (this.fileOperator != null) {
			fileOperator.close();
			this.fileOperator = null;
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
	 * 异常情况下只释放资源
	 */
	public void endExportOnException() {
		release();
		completeWriteFile();
	}

	/**
	 * 具体的输出方法 线程循环调用 指导数据输入完成
	 */
	public void export(BlockData blockData) {
		List<ParseOutRecord> outDatas = blockData.getData();
		for (ParseOutRecord outDate : outDatas) {
			this.writeFile(outDate);
		}
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		if (propertysSet == null || exportFields == null)
			return;

		for (int i = 0; i < exportFields.size(); i++) {
			FieldTemplateBean temp = exportFields.get(i);
			String propName = temp.getPropertyName();
			propertysSet.add(propName);
		}
	}
	
	protected String convertPath(String str, boolean isFileName) {
		String s = str;
		Task task = exporterArgs.getTask();
		s = s.replaceAll("%%CITY_ID_WCDR", String.valueOf(task.getCityIdWcdr()));
		
		s = s.replaceAll("%%VENDOR", Vendor.getEnVendorName(task.getExtraInfo().getVendor()));
		s = s.replaceAll("%%GROUPID", String.valueOf(task.getGroupId()));
		s = s.replaceAll("%%BSC", String.valueOf(task.getExtraInfo().getBscId()));
		s = s.replaceAll("%%CITY_ID", MappingUtil.instance().getEnCityNameById(String.valueOf(task.getExtraInfo().getCityId())));
		s = s.replaceAll("%%OMC", String.valueOf(task.getExtraInfo().getOmcId()));
		s = s.replaceAll("%%TASKID", String.valueOf(task.getId()));
		s = s.replaceAll("%%EXPORTID", String.valueOf(exportTempletBean.getId()));
		s = s.replaceAll("%%PARTFILEINDEX", String.valueOf(partFileIndex));
		
		/**
		 * explain:		用原文件名命名，避一个时间点的文件有多个，导致parser多次采集一个文件时重名不能合并.
		 * 　	   	当parser同一批次解析同一时间点多个文件，因为warehouse是用同一个，数据将不受覆盖影响．
		 * 			所以下面的exporterArgs.getEntryNames().get(0)，只用首个文件作为文件名中的一部份是没有问题的．
		 */
		if (exporterArgs != null && exporterArgs.getEntryNames().size()>0) {
			s = s.replaceAll("%%RAWFILE", String.valueOf(exporterArgs.getEntryNames().get(0) ));
		}
		
		if (task.getExtraInfo().getEnName() != null)
			s = s.replaceAll("%%ENNAME", task.getExtraInfo().getEnName());
		String cityBscNum = String.valueOf(task.getExtraInfo().getCityId() * 10000L + task.getExtraInfo().getBscId());
		if (!isFileName) {
			while (cityBscNum.length() < 7) {
				cityBscNum = "0" + cityBscNum;
			}
		}

		s = s.replaceAll("%%CITYBSCNUM", cityBscNum);
		// 如果当前数据时间未空 则不为处理
		if (this.exporterArgs.getDataTime() == null)
			return s;
		Calendar cal = Calendar.getInstance();
		
		// oss2.0 PM与MR采集当前时间前4个小时的数据
		cal.setTimeInMillis(this.exporterArgs.getDataTime().getTime()-task.getDelayDataTimeDelay());

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
