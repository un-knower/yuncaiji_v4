package cn.uway.framework.warehouse.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.template.FieldTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExporterBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.Packer;

/**
 * MapBufferedFileExporter
 * 
 * @author chenrongqiang 2012-11-1
 * @version 1.0
 * @since 3.0
 */
public class MapBufferedFileExporter extends AbstractExporter {

	private static final ILogger LOGGER = LoggerManager.getLogger(MapBufferedFileExporter.class); // 日志

	// 文件流对象 在Exporter创建的时候初始化 必须注意流的关闭和释放
	protected BufferedWriter writer;
	
	// 输出路径 从Export模版中读取
	protected String exportPath;

	// 输出文件名 在Export模版中配置 并且应该支持文件名的适配
	protected String exportFileName;

	// 写入的临时文件名
	protected String tempFileName;

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
	protected FileExportTemplateBean exportTempletBean;

	protected FileExporterBean fileExportTargetBean;
	
    protected String split =",";
    protected String split_regx;

	/**
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public MapBufferedFileExporter(FileExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, exportTempletBean.getId());
		this.exportId = exportTempletBean.getId();
		setBreakPoint();
		this.exportTempletBean = exportTempletBean;
		this.fileExportTargetBean = (FileExporterBean) exportTempletBean.getExportTargetBean();
		this.zipFlag = fileExportTargetBean.isZipFlag();
		this.compressFormat = fileExportTargetBean.getCompressFormat();
		this.exporterArgs = exporterArgs;
		// 将导出文件的路径中的“%%”占位符改为实际值。
		this.exportPath = convertPath(fileExportTargetBean.getPath(), false);
		this.exportFileName = convertPath(fileExportTargetBean.getFileName(), true);
		this.exportFields = exportTempletBean.getExportFileds();
		this.dest = exportPath + File.separator + exportFileName;
		this.exportType = 2;
		this.encode = exportTempletBean.getEncode();
		this.bHeader = fileExportTargetBean.isExportHeader();
		this.split = fileExportTargetBean.getSplit();
		this.split_regx = "\\" + this.split;
		createCacher(AbstractCacher.MEMORY_CACHER);
		try {
			checkFile();
			checkExportFile();
			this.writeHeader();
		} catch (Exception e) {
			LOGGER.warn("MapBufferedFileExporter创建失败", e);
		}
	}

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
		
		
		/**
		 * append:shig 
		 * date:2015-8-14 
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
	 * 输出文件初始化 检查temp文件是否存在 如存在 则追加的方式写入 如不存在，则新建temp文件
	 * 
	 * @throws IOException
	 */
	protected void checkExportFile() throws IOException {
		// 临时文件名先用sourceTag加上temp 如果为空则生成随机的临时文件名
		List<String> entryNames = exporterArgs.getEntryNames();
		String tempFile = null;
		if (entryNames == null || entryNames.size() == 0)
			tempFile = TempFileNameGenerator.getTempFileName();
		else
			tempFile = new StringBuilder(getRealExportFileName(this.exportFileName)).append(System.currentTimeMillis()).append(".temp").toString();
		tempFile = this.exportId + "." + tempFile;
		String outFileName = exportPath + File.separator + tempFile;
		File tmpFile = new File(outFileName);
		this.tempFileName = tmpFile.getAbsolutePath();
		File exportFile = new File(outFileName);
		if (exportFile.exists() && !exportFile.renameTo(tmpFile))
			LOGGER.warn("FileExportor异常,重命名临时文件失败!");
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
			if (compressFormat != null && compressFormat.toUpperCase().endsWith("GZ")) {
				FileOutputStream outputStream = new FileOutputStream(tempFileName, false);
				OutputStreamWriter outwriter = null;
				if (this.encode != null)
					outwriter = new OutputStreamWriter(outputStream, encode);
				else
					outwriter = new OutputStreamWriter(outputStream);
				this.writer = new BufferedWriter(outwriter, 4 * 1024 * 1024);
			} else {
				ZipEntry zipEntry = new ZipEntry(this.exportFileName);
				FileOutputStream outputStream = new FileOutputStream(tempFileName, false);
				ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
				zipOutputStream.putNextEntry(zipEntry);
				OutputStreamWriter outwriter = new OutputStreamWriter(zipOutputStream);
				this.writer = new BufferedWriter(outwriter, 4 * 1024 * 1024);
			}
			return;
		}
		// change on 2013-01-12 by liuwx ,文件输出需要设置编码，否则会出现产生的文件为乱码
		FileOutputStream fos = new FileOutputStream(exportFileName, false);
		OutputStreamWriter osw = null;
		if (this.encode == null)
			osw = new OutputStreamWriter(fos);
		else
			osw = new OutputStreamWriter(fos, this.encode);
		this.writer = new BufferedWriter(osw, 4 * 1024 * 1024);
		// end change
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
			// if (!this.bHeader || this.bAppend)
			if (!this.bHeader)
				return;
			if (this.exportFields == null || this.exportFields.isEmpty())
				return;
			int fieldNum = this.exportFields.size();
			for (int i = 0; i < fieldNum; i++) {
				String name = this.exportFields.get(i).getColumnName();
				this.writer.write(name);
				if (i < this.exportFields.size() - 1)
					this.writer.write(split);
			}
			this.writer.flush();
			this.writer.newLine();
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
			writer.write(lineRecord);
			writer.newLine();
			this.succ++;
		} catch (IOException e) {
			this.fail++;
			LOGGER.error("MapBufferedFileExporter输出失败,dest=" + this.dest, e);
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
			
			val= val.replaceAll(split_regx, " ");
			if (i == exportFields.size() - 1) {
				stringBuffer.append(val);
				break;
			}
			stringBuffer.append(val + split);
		}
		return stringBuffer.toString();
	}

	protected String getRealExportFileName(String exportFileName) {
		if (this.zipFlag)
			return exportFileName.substring(0, exportFileName.lastIndexOf(".")) + "." + (compressFormat == null ? "zip" : compressFormat);
		return exportFileName;
	}

	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
		release();
		File tmpFile = new File(tempFileName);
		if (this.total == 0L) {
			LOGGER.debug("记录数为0，文件{}无效，尝试删除文件", tempFileName);
			tmpFile.delete();
			return;
		}
		// 在关闭的时候再次进行文件名转换 因为解析过程中数据时间可能会被更新
		this.exportFileName = this.convertPath(fileExportTargetBean.getFileName(), true);
		String outFileName = exportPath + File.separator + getRealExportFileName(this.exportFileName);
		File exportFile = new File(outFileName);
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
			
			//Packer.pack内部做了多种判断，包括gz,tar.gz,zip等,zip 为默认压缩格式
			//程序在这个IF分支里只做了打包，默认的压缩格式和指定的.zip压缩格式会给输出文件带上.zip后缀
			if (this.zipFlag && !exportFile.getName().toUpperCase().endsWith("ZIP")) {
				List<String> list = new ArrayList<String>();
				String dest_f = exportPath + File.separator + this.exportFileName;
				renameFileFlag = tmpFile.renameTo(new File(dest_f));
				list.add(dest_f);
				renameFileFlag = Packer.pack(list, exportFile.getAbsolutePath());
				if (renameFileFlag && tmpFile.exists())
					tmpFile.delete();
				if(new File(dest_f).exists())
					new File(dest_f).delete();
			}else{
				renameFileFlag = tmpFile.renameTo(exportFile);
			}
		} catch (Exception e) {
			LOGGER.error("压缩文件："+tmpFile.getPath()+"出错", e);
			throw new RuntimeException(e);
		}
		LOGGER.debug("重命名文件{}至{}完成,结果={}", new Object[]{tmpFile, exportFile, renameFileFlag ? "成功" : "失败"});
	}

	//
	// private boolean fileToGZ(File srcfile, File aimGZFile) {
	// GZIPOutputStream gos = null;
	// FileInputStream fis = null;
	// FileOutputStream fos = null;
	// try {
	// fos = new FileOutputStream(aimGZFile);
	// gos = new GZIPOutputStream(fos);
	// fis = new FileInputStream(srcfile);
	// byte data[] = new byte[1024];
	// int count;
	// while ((count = fis.read(data, 0, 1024)) != -1) {
	// gos.write(data, 0, count);
	// }
	// gos.finish();
	// gos.flush();
	// return true;
	// } catch (Exception e) {
	// LOGGER.error("文件" + srcfile.getPath() + "压缩为" + aimGZFile.getPath() + "失败", e);
	// return false;
	// } finally{
	// IOUtils.closeQuietly(gos);
	// IOUtils.closeQuietly(fos);
	// IOUtils.closeQuietly(fis);
	// }
	// }
	
	/**
	 * 资源释放方法 关闭文件流
	 */
	public void release() {
		try {
			writer.flush();
			writer.close();
		} catch (Exception e) {
			LOGGER.error("文件流关闭失败!", e);
		}
	}

	/**
	 * 异常情况下只释放资源
	 */
	public void endExportOnException() {
		release();
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
}
