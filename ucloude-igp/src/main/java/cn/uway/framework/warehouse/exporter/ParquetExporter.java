package cn.uway.framework.warehouse.exporter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.job.LogCdrInsert;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.exporter.breakPoint.BpInfo;
import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ParqExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.TimeUtil;
import cn.uway.util.apache.parquet.hadoop.ParquetRecordWriter;
import cn.uway.util.parquet.PWPool;
import cn.uway.util.parquet.ParqContext;
import cn.uway.util.parquet.PartPool;

/**
 * parquet文件写入
 * 
 * @author sunt
 *
 */
public class ParquetExporter extends AbstractExporter {
	protected static final ILogger LOG = LoggerManager.getLogger(ParquetExporter.class);

	/**
	 * 输出字段列表
	 */
	protected List<ColumnTemplateBean> columns;

	/**
	 * 输出表名
	 */
	protected String table;

	/**
	 * 输出总时间
	 */
	protected long totalTime;
	/**
	 * 组装总时间
	 */
	protected long packageTime;
	/**
	 * 写总时间
	 */
	protected long writeTime;

	/**
	 * taskid+filename
	 */
	protected String taskfn;
	
	// 防止无效格式化
	private static final String toFormat = ParqContext.getDateFormat();
	// parquet的时间字符串均用此格式化
	private final SimpleDateFormat sdf = new SimpleDateFormat(toFormat);
	private SimpleDateFormat sdfTemp = new SimpleDateFormat();

	// 断点相关信息
	protected BpInfo bpInfo;
	
	protected ParquetRecordWriter w = null;
	
	protected String wKey;
	
	/**
	 * 构造函数
	 * 
	 * @param dbExportTempletBean
	 *            数据库输出实体定义
	 * @param exporterArgs
	 *            输出参数
	 * @throws Exception 
	 */
	public ParquetExporter(ParqExportTemplateBean templetBean, ExporterArgs exporterArgs) {
		super(exporterArgs, templetBean.getId());
		exporterArgs.setIsDelayExport(true);
		this.exportId = templetBean.getId();
		setBreakPoint();
		this.dest = templetBean.getTable().getTableName();
		this.exportType = templetBean.getType();
		this.table = templetBean.getTable().getTableName();
		int partitionType = templetBean.getPartitionType();
		bpInfo = new BpInfo(table,exporterArgs.getObjStatus().get(0),exportId,breakPoint,templetBean.getCtType());
		this.columns = templetBean.getTable().getColumnsList();
		if(task.getWorkerType() == 1){
			taskfn = task.getId() + TimeUtil.getDateString_yyyyMMddHHmmss(task.getDataTime());
		}else{
			taskfn = task.getId() + entryNames.get(0);
		}
		if(ParqContext.getCacherName().equals("BlockingCacher")){
			createCacher(AbstractCacher.BLOCK_CACHER);
		}else{
			createCacher(AbstractCacher.MEMORY_CACHER);
		}
		try {
			String partStr = PartPool.getPart(TimeUtil.getDateString_yyyyMMddHHmmss(exporterArgs.getDataTime()),partitionType);
			wKey = task.getId()+table + partStr;
			w=PWPool.getWriter(wKey, taskfn, bpInfo, partStr);
		} catch (Exception e) {
			LOG.error("创建Writer失败,表名:{};文件:{};任务:{};msg:{};cause:{}",
					new Object[] { table, entryNames.get(0), task.getId(), e.getMessage(), e.getCause() });
			LOG.error("printStackTrace", e);
		}
		LOG.debug("parq输出器初始化完成:目的地：{},输出开始断点={}", table, breakPoint);
	}
	
	@Override
	public void close() {
		bpInfo.setParseNum(current);
		LOG.debug(
				"【单文件处理时间统计】{}表处理耗时{}秒，组装{}秒，写入{}秒，成功{}条，失败{}条，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}",
				new Object[] { this.table, totalTime / 1000.00, packageTime / 1000.00, writeTime / 1000.00,
						this.succ, this.fail, task.getId(),
						(this.exporterArgs != null ? entryNames.get(0) : ""),
						task.getExtraInfo().getCityId(),
						task.getExtraInfo().getOmcId(),
						task.getExtraInfo().getBscId(),
						task.getExtraInfo().getVendor() });
		if(endTime == null)
		{
			endTime = new Date();
		}
		LogCdrInsert.getInstance()
				.insert(task.getExtraInfo().getCityId(),
						task.getExtraInfo().getOmcId(),
						task.getExtraInfo().getBscId(), table,
						exporterArgs.getDataTime(), startTime, endTime, succ,
						fail, total, task.getExtraInfo().getVendor(),
						entryNames.get(0));
	}

	@Override
	public void endExportOnException() {
		close();
	}

	@Override
	public void export(BlockData blockData) throws Exception {
		List<ParseOutRecord> outData = blockData.getData();
		if (outData == null || outData.isEmpty())
			return;
		for (ParseOutRecord outRecord : outData) {
			Map<String, String> record = outRecord.getRecord();
			export(record);
		}
	}

	/**
	 * 输出到文件
	 * 
	 * @param record
	 * @throws Exception
	 */
	private void export(Map<String, String> record) throws Exception {
		long start = System.currentTimeMillis();
		this.current++;
		if (this.current <= this.breakPoint) {
			this.fail++;
			return;
		}
		for (ColumnTemplateBean column : this.columns) {
			// 输出模板的property，对应解析模板的index
			String prop = column.getPropertyName();
			// 输出模板的name，对应表字段名
			String colName = column.getColumnName();
			String value = record.get(prop);
			
			// value处理-为空时
			if((null == value)||"".equals(value)||value.equalsIgnoreCase("nan")){
				record.put(colName, column.getDefaultValue());
				continue;
			}
			
			// format处理-不为空时
			String format = column.getFormat();
			if ((null != format)&&(!"".equals(format))
					&&(format.contains("yyyy"))&&(!toFormat.equals(format))
					&&(value.length()>=4)) {
				sdfTemp.applyPattern(format);
				value= sdf.format(sdfTemp.parse(value));
			}

			record.put(colName, value);
		}
		long endPackage = System.currentTimeMillis();
		packageTime += (endPackage - start);
		try {
			w.write(bpInfo.getUniqueId(),record);
			this.succ++;
		} catch (Exception e) {
			this.fail++;
			LOG.error("输出发生异常,表名:{};文件:{};任务:{};msg:{};cause:{}",
					new Object[] { table, entryNames.get(0), task.getId(), e.getMessage(), e.getCause()});
			LOG.error("printStackTrace", e);
		}
		long end = System.currentTimeMillis();
		writeTime += (end - endPackage);
		totalTime += (end - start);
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		int colNum = this.columns.size();
		for (int i = 0; i < colNum; i++) {
			ColumnTemplateBean column = this.columns.get(i);
			String prop = column.getPropertyName();
			propertysSet.add(prop);
		}
	}
}
