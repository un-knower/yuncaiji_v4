package cn.uway.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.opencsv.CSVWriter;

/**
 * 将excel文件转换为csv文件的工具。
 * 
 * @author ChenSijiang 2010-10-19
 * @since 1.1
 */
public class ExcelToCsvUtil {

	private File source;

	private String keyId;

	private long taskId;

	private Date dataTime;

	private String baseDir;
	
	// csv文件的编码
	private String charSet = null;

	private ILogger logger = LoggerManager.getLogger(ExcelToCsvUtil.class);

	/**
	 * 构造方法。
	 * 
	 * @param excelFile
	 *            要转换的excel文件
	 * @param taskInfo
	 *            所属任务的信息
	 * @throws FileNotFoundException
	 *             传入参数为<code>null</code>、文件不存在、非文件时
	 */
	public ExcelToCsvUtil(File excelFile, Task taskInfo, String baseDir, String charSet) throws FileNotFoundException {
		super();
		this.baseDir = baseDir;
		long id = taskInfo.getId();
		taskId = id;
		if (taskInfo instanceof ReTask)
			id = ((ReTask) taskInfo).getrTaskId();
		keyId = taskInfo.getId() + "-" + id;
		dataTime = taskInfo.getDataTime();
		if (excelFile == null) {
			throw new FileNotFoundException(keyId + "-传入的文件路径为null");
		}
		if (!excelFile.exists()) {
			throw new FileNotFoundException(keyId + "-文件不存在:" + excelFile.getAbsolutePath());
		}
		if (excelFile.isDirectory()) {
			throw new FileNotFoundException(keyId + "-传入的路径为目录，非文件:" + excelFile.getAbsolutePath());
		}
		this.source = excelFile;
		this.charSet = charSet;
	}

	/**
	 * 构造方法。
	 * 
	 * @param excelFile
	 *            要转换的excel文件
	 * @param taskInfo
	 *            所属任务的信息
	 * @param baseDir csv文件上级目录
	 * @param charSet csv文件的编码格式
	 * @throws FileNotFoundException
	 *             传入参数为<code>null</code>、文件不存在、非文件时
	 */
	public ExcelToCsvUtil(String excelFile, Task taskInfo, String baseDir, String charSet) throws FileNotFoundException {
		this(new File(excelFile), taskInfo, baseDir, charSet);
	}

	/**
	 * 将EXCEL文件转为一个或多个CSV文件，使用逗号作为分隔符。
	 * 
	 * @return 转换后的所有CSV文件的本地路径，不会返回<code>null</code>
	 * @throws Exception
	 *             转换时出错
	 */
	public List<File> toCsv() throws Exception {
		return toCsv(null);
	}

	/**
	 * 将EXCEL文件转为一个或多个CSV文件。
	 * 
	 * @param splitChar
	 *            CSV文件的分隔符，如果传入<code>null</code>，则默认使用逗号分隔
	 * @return 转换后的所有CSV文件的本地路径，不会返回<code>null</code>
	 * @throws Exception
	 *             转换时出错
	 */
	public List<File> toCsv(Character splitChar) throws Exception {
		List<File> ret = new ArrayList<File>();
		File dir = new File(baseDir + File.separator + taskId + File.separator + TimeUtil.getDateString_yyyyMMddHH(dataTime) + File.separator
				+ source.getName() + File.separator);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception(keyId + "-创建文件夹失败:" + dir.getAbsolutePath());
			}
		}

		Workbook wb = Workbook.getWorkbook(source);
		logger.debug(keyId + "-开始将EXCEL文件转换为CSV文件:" + source.getAbsolutePath());
		Sheet[] sheets = wb.getSheets();
		for (Sheet sheet : sheets) {
			File csvFile = new File(dir, sheet.getName() + ".csv");
			int columnsCount = sheet.getRow(0).length;
			int rowCount = sheet.getRows();
			if (rowCount < 1)
				continue;
			String[] cols = new String[columnsCount];
			PrintWriter writer = null;
			try {
				if(null == charSet){
					writer = new PrintWriter(csvFile);
				}else{
					writer = new PrintWriter(csvFile,charSet);
				}
			} catch (Exception e) {
				logger.error(keyId + "csv文件转换失败");
				return ret;
			}
			Character sp = (splitChar == null ? ',' : splitChar);
			CSVWriter csvWriter = new CSVWriter(writer, sp);
			for (int i = 0; i < rowCount; i++) {
				Cell[] cells = sheet.getRow(i);
				int bad = 0;
				for (int j = 0; j < columnsCount; j++) {
					String content = j > cells.length - 1 ? "" : cells[j].getContents();
					content = content == null ? "" : content.replace('\r', ' ').replace('\n', ' ').replace(sp, ' ');
					cols[j] = content;
					if(StringUtil.isBlank(content)){
						bad++;
					}
				}
				// 发现厂家文件带空行，如果直接转换为csv空行，增加处理难度，且无意义
				if(bad == columnsCount){
					logger.debug(keyId + "-文件【{}】第{}行是空行，已去掉。", source.getName(), i+1);
				}else{
					csvWriter.writeNext(cols);
					csvWriter.flush();
				}
			}
			csvWriter.close();
			writer.close();
			ret.add(csvFile);
			// logger.debug(keyId + "-CSV文件已转换完成:" + csvFile.getAbsolutePath());
		}
		logger.debug(keyId + "-CSV文件已转换完成,所在目录:" + dir.getAbsolutePath());
		return ret;
	}

}
