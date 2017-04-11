package cn.uway.framework.warehouse.exporter;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.exporter.template.FieldTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.InfoBrightFileExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.TimeUtil;

/**
 * MapBufferedInfoBrightFileExporter [infoBright文件输出器]
 * 
 * @author yuy 
 * @date 2014-1-20
 * @version 1.0
 * @since 3.0
 */
public class MapBufferedCuttedFileExporter extends MapBufferedInfoBrightFileExporter{

	private static final ILogger LOGGER = LoggerManager.getLogger(MapBufferedCuttedFileExporter.class); // 日志
	
	/**
	 * 当前采集时间
	 */
	protected Date currentDateTime;
	
	/**
	 * 中间时间
	 */
	Calendar midCalendar = null;
	
	/**
	 * 数据分类关键字，某时间字段
	 */
	protected String timeKey;
	
	// 输出字段 即数据源的key值
	// 父类已定义，子类不可重定义
	//protected List<FieldTemplateBean> exportFields;
	
	protected MapBufferedInfoBrightFileExporter otherExporter;

	/**
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public MapBufferedCuttedFileExporter(FileExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exportTempletBean, exporterArgs);
		
		//初始化另一个输出器
		InfoBrightFileExportTemplateBean templetBean = (InfoBrightFileExportTemplateBean) exportTempletBean;
		this.timeKey = templetBean.getSplitDataFormatBean().getTimeKey();
		this.exportFields = exportTempletBean.getExportFileds();
		super.srcFileName = exporterArgs.getEntryNames().get(0);
		String middleTime = templetBean.getSplitDataFormatBean().getMiddleTime();
		
		//算出另一个半区的dataTime
		this.currentDateTime = exporterArgs.getDataTime();
		Calendar currCalendar = Calendar.getInstance();
		currCalendar.setTime(currentDateTime);
		Date otherDateTime = null;
		try {
			//中间时间
			Calendar midCalendarNotAdded = Calendar.getInstance();
			midCalendarNotAdded.setTime(currentDateTime);
			TimeUtil.setTime(middleTime, midCalendarNotAdded);
			
			//中间时间 +1day
			Calendar midCalendarAddedOneDay = Calendar.getInstance();
			midCalendarAddedOneDay.setTime(currentDateTime);
			midCalendarAddedOneDay.set(Calendar.DATE, midCalendarAddedOneDay.get(Calendar.DATE) + 1);
			TimeUtil.setTime(middleTime, midCalendarAddedOneDay);
			
			// 算出中间时间，即加上年月日
			long timeStampNotAdded = Math.abs(midCalendarNotAdded.getTimeInMillis() - currCalendar.getTimeInMillis());
			long timeStampAddedOneDay = Math.abs(midCalendarAddedOneDay.getTimeInMillis() - currCalendar.getTimeInMillis());
			if(timeStampNotAdded < timeStampAddedOneDay){
				midCalendar = midCalendarNotAdded;
			}else{
				midCalendar = midCalendarAddedOneDay;
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(midCalendar.getTime());
			if(midCalendar.before(currCalendar)){
				//设置上半区时间 0055
				calendar.add(Calendar.MINUTE, -5);
			} 
			if(currCalendar.before(midCalendar)){
				//设置下半区时间 0005
				calendar.add(Calendar.MINUTE, 5);
			}
			otherDateTime = calendar.getTime();
			
			//初始化另一个输出器
			exporterArgs.setDataTime(otherDateTime);
			otherExporter = new MapBufferedInfoBrightFileExporter(exportTempletBean, exporterArgs);
			otherExporter.srcFileName = super.srcFileName;
			
		} catch (Exception e) {
			LOGGER.warn("MapBufferedInfoBrightFileExporter创建失败", e);
		}
		
	}

	/* (non-Javadoc)
	 * @see cn.uway.igp3.warehouse.repository.export.MapBufferedFileExporter#writeFile(cn.uway.igp3.parser.ParseOutRecord)
	 */
	protected void writeFile(ParseOutRecord out) {
		this.current++;
		this.breakPoint++;
		Map<String, String> cdlRecord = out.getRecord();
		//分类关键字
		String time = cdlRecord.get(this.timeKey);
		String lineRecord = createLineMessage(cdlRecord);
		byte[] line = (lineRecord + "\n").getBytes();
		try {
			Date date = TimeUtil.getDate(time);
			if((date.before(this.midCalendar.getTime()) && this.currentDateTime.before(this.midCalendar.getTime()))
					|| (date.after(this.midCalendar.getTime()) && this.currentDateTime.after(this.midCalendar.getTime()))) {
				this.getFileChannel().write(ByteBuffer.wrap(line));
				this.size += line.length;
				this.rows += 1;
			} else {
				otherExporter.getFileChannel().write(ByteBuffer.wrap(line));
				otherExporter.size += line.length;
				otherExporter.rows += 1;
			}
			this.succ++;
		} catch (Exception e) {
			this.fail++;
			LOGGER.error("MapBufferedFileExporter输出失败,dest=" + this.dest, e);
		}
	}
	
	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
		super.close();
		otherExporter.close();
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
			if (i == exportFields.size() - 1) {
				stringBuffer.append(val);
				break;
			}
			stringBuffer.append(val + ",");
		}
		return stringBuffer.toString();
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

