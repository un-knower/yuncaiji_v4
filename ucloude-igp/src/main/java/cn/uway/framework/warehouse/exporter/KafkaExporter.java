package cn.uway.framework.warehouse.exporter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FieldTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExporterBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.kafka.KafkaIgpProducer;

/**
 * KafkaExporter
 * 
 * @version 1.0
 * @since 3.0
 */
public class KafkaExporter extends AbstractExporter {

	private static final ILogger LOGGER = LoggerManager.getLogger(KafkaExporter.class); // 日志

	// 输出字段 即数据源的key值
	protected List<FieldTemplateBean> exportFields;

	// 输出器参数定义
	protected ExporterArgs exporterArgs;

	// 输出模版
	protected FileExportTemplateBean exportTempletBean;
	
    protected String split;
    protected String split_regx;
    
    protected String brokerlist;
    protected String topic;
    protected KafkaIgpProducer producer;

	/**
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public KafkaExporter(FileExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, exportTempletBean.getId());
		this.exportId = exportTempletBean.getId();
		setBreakPoint();
		this.exportTempletBean = exportTempletBean;
		this.exporterArgs = exporterArgs;
		this.exportFields = exportTempletBean.getExportFileds();
		this.exportType = ExportTemplateBean.KAFKA_EXPORTER;
		this.encode = exportTempletBean.getEncode();
		
		FileExporterBean fileExportTargetBean = (FileExporterBean)exportTempletBean.getExportTargetBean();
		if (fileExportTargetBean != null) {
			this.split = fileExportTargetBean.getSplit();
			this.brokerlist = fileExportTargetBean.getPath();
			this.topic = fileExportTargetBean.getFileName();
		}
		this.split_regx = "\\" + this.split;
		this.producer = KafkaIgpProducer.getProducer(this.brokerlist, this.topic);
		createCacher(AbstractCacher.MEMORY_CACHER);
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

	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
		release();
		
		LOGGER.debug("【Kafka推送统计】 成功推送{}条数据，推送失败{}条数据，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}", new Object[]{
				this.succ, this.fail, task.getId(), (this.exporterArgs != null ? entryNames.get(0) : ""),
				task.getExtraInfo().getCityId(), task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), task.getExtraInfo().getVendor()});
	}

	/**
	 * 资源释放方法 关闭文件流
	 */
	public void release() {
		try {
			if (producer != null) {
				producer.close();
				producer = null;
			}
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
		String [] lines = new String[outDatas.size()];
		int i=0;
		for (ParseOutRecord outDate : outDatas) {
			++this.current;
			lines[i++] = createLineMessage(outDate.getRecord());
		}

		producer.send(lines);
		this.breakPoint += lines.length;
		this.succ += lines.length;
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
