package cn.uway.framework.warehouse.exporter;

import java.util.Set;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * MapBufferedFileExporter
 * 
 * @author chenrongqiang 2012-11-1
 * @version 1.0
 * @since 3.0
 */
public class EmptyExporter extends AbstractExporter {

	private static final ILogger LOGGER = LoggerManager.getLogger(EmptyExporter.class); // 日志

	// 输出器参数定义
	protected ExporterArgs exporterArgs;

	/**
	 * 根据输出模版初始化输出器 如temp文件、文件流等
	 * 
	 * @param exportTempletBean
	 */
	public EmptyExporter(ExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, exportTempletBean.getId());
		this.exportId = exportTempletBean.getId();
		this.exporterArgs = exporterArgs;
		// 将导出文件的路径中的“%%”占位符改为实际值。
		createCacher(AbstractCacher.MEMORY_CACHER);
		LOGGER.debug("taskid={}, exportid={}, 正在使用空白输出，将不会处理任何数据．", exporterArgs.task.getId(),  exportId);
	}

	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 */
	public void close() {
	}

	/**
	 * 异常情况下只释放资源
	 */
	public void endExportOnException() {
		
	}

	/**
	 * 空输出
	 */
	public void export(BlockData blockData) {
		++this.succ;
		++this.total;
		++this.breakPoint;
		return;
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		propertysSet.add("START_TIME");
	}
}
