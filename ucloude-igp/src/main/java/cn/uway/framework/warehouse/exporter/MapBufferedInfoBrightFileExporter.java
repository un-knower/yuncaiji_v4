package cn.uway.framework.warehouse.exporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.template.FieldTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExporterBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

/**
 * MapBufferedFileExporter 扩展类（即增加了文件锁）
 * 
 * @author yuy 2014-2-24
 * @version 1.0
 * @since 3.0
 */
public class MapBufferedInfoBrightFileExporter extends AbstractExporter {

	private static final ILogger LOGGER = LoggerManager.getLogger(MapBufferedInfoBrightFileExporter.class); // 日志
	
	/**
	 * 数据分类关键字，某时间字段
	 */
	protected String timeKey;
	
	/**
	 * 文件通道
	 */
	protected FileChannel fileChannel = null;
	
	/**
	 * 文件独占锁
	 */
	protected FileLock fileLock = null;
	
	protected PrintWriter printWriter;
	
	protected long fromSize;
	protected long fromLine;
	protected long size = 0;
	protected long rows = 0;
	protected String idxHead = "srcFileName|tmpFileName|fromSize|size|fromLine|lines";
	
	// 输出路径 从Export模版中读取
	protected String exportPath;

	// 输出文件名 在Export模版中配置 并且应该支持文件名的适配
	protected String exportFileName;
	
	// 解析的原始文件名
	protected String srcFileName;

	// 写入的临时文件名
	protected String tempFileName;
	
	// 写入的临时文件地址
	protected String tempFilePath;

	// 输出字段 即数据源的key值
	protected List<FieldTemplateBean> exportFields;

	/* 是否输出表头。 */
	protected boolean bHeader;

	// 此次写入是否是在之前的文件上追加的
	protected boolean bAppend;

	// 输出器参数定义
	protected ExporterArgs exporterArgs;

	protected boolean zipFlag = false;

	// 输出模版
	protected FileExportTemplateBean exportTempletBean;

	protected FileExporterBean fileExportTargetBean;

	private FileOutputStream fos;

	/**
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public MapBufferedInfoBrightFileExporter(FileExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, exportTempletBean.getId());
		this.exportId = exportTempletBean.getId();
		setBreakPoint();
		this.exportTempletBean = exportTempletBean;
		this.fileExportTargetBean = (FileExporterBean) exportTempletBean.getExportTargetBean();
		this.zipFlag = fileExportTargetBean.isZipFlag();
		this.exporterArgs = exporterArgs;
		// 将导出文件的路径中的“%%”占位符改为实际值。
		this.exportPath = convertPath(fileExportTargetBean.getPath(), false);
		this.exportFileName = convertPath(fileExportTargetBean.getFileName(), true);
		this.dest = exportPath + File.separator + exportFileName;
		this.tempFileName = new StringBuilder(this.exportFileName).append(".temp").toString();
		this.tempFilePath = exportPath + File.separator + tempFileName;
		this.exportFields = exportTempletBean.getExportFileds();
		this.exportType = 2;
		this.encode = exportTempletBean.getEncode();
		this.bHeader = fileExportTargetBean.isExportHeader();
		createCacher(AbstractCacher.MEMORY_CACHER);
		try {
			// 检查输出文件，并做初始化工作
			this.checkFile();
			this.checkExportFile();
			// 输出文件写入表头
			this.writeHeader();
			// idx信息初始化
			this.idxFileInit();
			// 定位
	        this.fileChannel.position(fromSize);
		} catch (Exception e) {
			LOGGER.warn("MapBufferedFileExtExporter创建失败", e);
		}
	}
	
	/**
	 * 输出文件初始化 检查temp文件是否存在 如存在 则追加的方式写入 如不存在，则新建temp文件
	 * 
	 * @throws IOException
	 */
	protected void checkExportFile() throws IOException {
		File tmpFile = new File(this.tempFilePath);
		File dir = new File(FilenameUtils.getFullPath(this.tempFilePath));
		if (!dir.exists() || !dir.isDirectory()) {
			if (dir.mkdirs())
				LOGGER.debug("目录{}不存在,创建目录成功.", dir);
			else
				LOGGER.error("目录{}不存在,创建目录失败。", dir);
		}
		
		//判断输出文件是否存在
		File exportFile = new File(this.dest);
		if(exportFile.exists()){
			exportFile.renameTo(tmpFile);
		}
		
		if (!tmpFile.exists()) {
			LOGGER.debug("创建临时文件={}", tmpFile.getName());
			tmpFile.createNewFile();
			fromSize = 0;
			fromLine = 0;
		} else {
			this.bAppend = true;
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
		// 如果是需要生成zip格式 writer的初始化不一样
		if (this.zipFlag) {
			throw new IOException("暂不支持zip压缩格式");
		}
		
		fos = new FileOutputStream(exportFileName, true);
		this.fileChannel = fos.getChannel();
		
		// 轮流获得文件独占锁。
        getFileLock();
	}
	
	/**
	 * idx信息初始化
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void idxFileInit() throws IOException, FileNotFoundException {
		// 初始化idx文件 命名规则：按天，如 20140228、20140301
        String idxFileName = TimeUtil.getyyyyMMddDateString(this.exporterArgs.getDataTime()) +".idx";
        String idxFilePath = this.exportPath + File.separator + idxFileName;
        File idxFile = new File(idxFilePath);
        //idx文件不存在
        if(!idxFile.exists()){
        	idxFile.createNewFile();
        	// 创建idx文件流
            FileOutputStream out = new FileOutputStream(idxFile, true);
            printWriter = new PrintWriter(out);
            printWriter.write(idxHead + "\n");
            return;
        }
        //idx文件存在
        readIdxFile(idxFile);
        
        //创建idx文件流
        FileOutputStream out = new FileOutputStream(idxFile, true);
        printWriter = new PrintWriter(out);
	}

	/**
	 * 获得文件独占锁
	 * @throws IOException
	 */
	public void getFileLock() throws IOException {
		while (true) {
           try {
        	   fileLock = this.fileChannel.lock();
        	   break;
           } catch (OverlappingFileLockException e) {
              try {
            	  LOGGER.error("文件独占锁被占用，休息1秒钟，等待释放锁");
            	  Thread.sleep(1 * 1000);
              } catch (InterruptedException e1) {
					LOGGER.error("获取文件独占锁休眠被打断");
              }
           }
        }
	}
	
	/**
	 * 读取idx文件的信息
	 * @param idxFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readIdxFile(File idxFile) throws FileNotFoundException,
			IOException {
		FileInputStream in = new FileInputStream(idxFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        boolean flag = false;
        while((line = reader.readLine()) != null){
        	String[] array = StringUtil.split(line, "|");
        	if(!array[1].equals(this.tempFileName))
        		continue;
        	flag = true;
        	//如果是同一个原始文件，重写
        	if(array[0].equals(this.srcFileName)){
        		fromSize = Integer.parseInt(array[2]);
            	fromLine = Integer.parseInt(array[4]);
            //如果不是同一个原始文件，续写
        	}else{
        		fromSize = Integer.parseInt(array[2]) + Integer.parseInt(array[3]);
            	fromLine = Integer.parseInt(array[4]) + Integer.parseInt(array[5]);
        	}
        	LOGGER.debug("原始文件{}，临时文件{}，从{}行开始写，已经写入了{}行", new Object[]{array[0], array[1], array[4], array[5]});
        }
        //在idx文件中没找到
        if(!flag){
        	fromSize = 0;
			fromLine = 0;
        }
        reader.close();
        in.close();
	}

	/**
	 * 文件夹检查 避免因为文件夹不存在生成文件失败
	 * 
	 * @throws IOException
	 */
	protected void checkFile() throws IOException {
		File rootFile = new File(exportPath);
		if (!rootFile.exists() && rootFile.mkdirs())
			LOGGER.debug("文件根目录{}不存在.尝试创建目录成功!", exportPath);
	}

	protected void writeHeader() {
		try {
			// 如果是在之前文件上追加，就不用再写表头的，不能把表头写到文件中间。
			if (!this.bHeader || this.bAppend)
				return;
			if (this.exportFields == null || this.exportFields.isEmpty())
				return;
			int fieldNum = this.exportFields.size();
			for (int i = 0; i < fieldNum; i++) {
				String name = this.exportFields.get(i).getPropertyName();
				this.fileChannel.write(ByteBuffer.wrap(name.getBytes()));
				size += name.getBytes().length;
				if (i < this.exportFields.size() - 1){
					this.fileChannel.write(ByteBuffer.wrap(",".getBytes()));
					size += ",".getBytes().length;
				}
			}
			this.fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
			size += "\n".getBytes().length;
			rows += 1;
		} catch (Exception e) {
			LOGGER.warn("写表头失败。", e);
		}
	}
	
	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
		// 写idx文件
		printWriter.write(srcFileName + "|" + tempFileName + "|" + fromSize + "|" + size + "|" + fromLine + "|" + rows + "\n");
		// 释放资源
		release();
		
		// 记录数为0，删除临时文件
		File tmpFile = new File(this.tempFilePath);
		if (this.rows == 0L) {
			LOGGER.debug("记录数为0，文件{}无效，尝试删除文件", tempFileName);
			tmpFile.delete();
			return;
		}
		
		// 更改临时文件名
		File exportFile = new File(this.dest);
		LOGGER.debug("文件输出完成,将会把临时文件{}重命名至{}", new Object[]{tmpFile, exportFile});
		if (exportFile.exists()){
			LOGGER.debug("已经存在同名文件={},不删除，输出结束", new Object[]{exportFile});
			return;
		}
		if (!tmpFile.exists()) {
			LOGGER.error("临时文件{}已不存在。重命名失败。", tmpFile);
			return;
		}
		LOGGER.debug("重命名文件{}至{}完成,结果={}", new Object[]{tmpFile, exportFile, tmpFile.renameTo(exportFile) ? "成功" : "失败"});
	}
	
	/**
	 * 根据时间/bsc/city/omc/vendor等转换文件名
	 * @param str
	 * @param isFileName
	 * @return
	 */
	String convertPath(String str, boolean isFileName) {
		String s = str;
		Task task = exporterArgs.getTask();
		s = s.replaceAll("%%CITY_ID_WCDR", String.valueOf(task.getCityIdWcdr()));
		s = s.replaceAll("%%VENDOR", task.getExtraInfo().getVendor());
		s = s.replaceAll("%%GROUPID", String.valueOf(task.getGroupId()));
		s = s.replaceAll("%%BSC", String.valueOf(task.getExtraInfo().getBscId()));
		s = s.replaceAll("%%CITY_ID", String.valueOf(task.getExtraInfo().getCityId()));
		s = s.replaceAll("%%OMC", String.valueOf(task.getExtraInfo().getOmcId()));
		s = s.replaceAll("%%TASKID", String.valueOf(task.getId()));
		s = s.replaceAll("%%EXPORTID", String.valueOf(exportTempletBean.getId()));
		
		
		
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
		cal.setTime(this.exporterArgs.getDataTime());
		s = s.replaceAll("%%Y", String.format("%04d", cal.get(Calendar.YEAR)));
		s = s.replaceAll("%%M", String.format("%02d", cal.get(Calendar.MONTH) + 1));
		s = s.replaceAll("%%D", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
		s = s.replaceAll("%%H", String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		s = s.replaceAll("%%m", String.format("%02d", cal.get(Calendar.MINUTE)));
		s = s.replaceAll("%%S", String.format("%02d", cal.get(Calendar.SECOND)));
		return s;
	}

	/**
	 * 资源释放方法 关闭文件流
	 */
	public void release() {
		try {
			this.fileChannel.close();
			this.printWriter.close();
		} catch (Exception e) {
			LOGGER.error("文件流关闭失败!", e);
		}
	}

	public FileChannel getFileChannel() {
		return fileChannel;
	}

	public void setFileChannel(FileChannel fileChannel) {
		this.fileChannel = fileChannel;
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

	public long getFromSize() {
		return fromSize;
	}

	public void setFromSize(long fromSize) {
		this.fromSize = fromSize;
	}

	public long getFromLine() {
		return fromLine;
	}

	public void setFromLine(long fromLine) {
		this.fromLine = fromLine;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getRows() {
		return rows;
	}

	public void setRows(long rows) {
		this.rows = rows;
	}

	@Override
	public void export(BlockData blockData) throws Exception {
	}

	@Override
	public void endExportOnException() {
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
}
