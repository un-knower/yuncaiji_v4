package cn.uway.framework.warehouse.exporter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.cache.Cacher;
import cn.uway.framework.cache.Element;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * <pre>
 *		该类的主要作用是，在Export过多时，将Export收入到一组中执行，避免线程过大，系统负荷太大 
 * </pre>
 * @ 2016-6-1
 */
public class ExporterGroupDispatcher extends AbstractExporter {
	private static final ILogger LOGGER = LoggerManager.getLogger(ExporterGroupDispatcher.class);
	private List<AbstractExporter> exports = new LinkedList<AbstractExporter>();
	private List<ExportReport> subExportReports = new LinkedList<ExportReport>();
	private Set<Integer> dateTypeSet = new HashSet<Integer>();
				
	public ExporterGroupDispatcher(ExporterArgs exporterArgs, int groupID) {
		super(exporterArgs, groupID);
		
		createCacher(AbstractCacher.MEMORY_CACHER);
	}
	
	public void registerExporter(Exporter exporter) {
		exports.add((AbstractExporter)exporter);
		dateTypeSet.add(exporter.getType());
	}
	
	public boolean isIncludeDataType(int dataType) {
		return dateTypeSet.contains(dataType);
	}

	@Override
	public ExportFuture call() throws Exception {
		setStartTime(new Timestamp(new Date().getTime()));
		int exportedBlockNum = 0;
		Iterator<AbstractExporter> iterExporter = exports.iterator();
		while (iterExporter.hasNext()) {
			AbstractExporter exporter = iterExporter.next();
			if (exporter.exporterInitErrorFlag) {
				exporter.setEndTime(new Timestamp(new Date().getTime()));
				ExportReport subExportReport = exporter.createExportReport();
				LOGGER.debug("Exporter初始化失败,输出中止,报表={}", subExportReport);
				subExportReports.add(subExportReport);
				iterExporter.remove();
			}
		}

		// 如果初始化没有失败 则进行输出
		Cacher cacher = getCacher();
		while (!breakProcessFlag) {
			try {
				if (cacher.isCommit() && exportedBlockNum >= cacher.size()) {
					this.close();
					cacher.shutdown();
					break;
				}
				Element element = cacher.getNextElement();
				// cacher.getNextElement只有当所有element都输出完成才返回空。但是需要使用continue再次执行一下判断。
				if (element == null) {
					continue;
				}
				BlockData blockData = (BlockData) element.getElementValue();
				exportedBlockNum++;
				this.total += blockData.getData().size();
				
				export(blockData);
			} catch (Exception e) {
				LOGGER.debug("输出异常", e);
				this.setErrorCode(-1);
				this.setCause(e.getMessage());
				this.close();
				cacher.shutdown();
				break;
			}
		}

		if (breakProcessFlag) {
			LOGGER.error("ExportFuture::call() 收到终止处理标识，exportGroup线程退出。 终止原因:{}", breakProcessCause);
		}
		
		LOGGER.debug("Cacher.size()={},Exporter group block counter={}, recordTotal={}", new Object[]{getCacher().size(), exportedBlockNum, this.total});
		setEndTime(new Timestamp(new Date().getTime()));

		// 创建子Export的输出数量
		buildExportFutures();
		
		for (ExportReport subExportReport : subExportReports) {
			LOGGER.debug(Thread.currentThread().getName() + "，输出完毕，产生报表={}", subExportReport.toString());
		}
		
		LOGGER.debug(Thread.currentThread().getName() + "，exportGroup输出完毕，产生子报表个数={}", subExportReports.size());
		ExportFuture exportFuture = new ExportFuture();
		exportFuture.setGroupExportReports(subExportReports);
		
		return exportFuture;
	}
	
	private List<ExportReport> buildExportFutures() {
		for (AbstractExporter exporter : exports) {
			ExportReport subExportReport = exporter.createExportReport();
			subExportReports.add(subExportReport);
		}
		
		return subExportReports;
	}
	
	@Override
	public void setStartTime(Date startTime) {
		super.setStartTime(startTime);
		for (AbstractExporter exporter : exports) {
			exporter.setStartTime(new Timestamp(startTime.getTime()));
		}
	}

	@Override
	public void setEndTime(Date endTime) {
		super.setEndTime(endTime);
		for (AbstractExporter exporter : exports) {
			exporter.setEndTime(new Timestamp(endTime.getTime()));
		}
	}

	@Override
	public void export(BlockData blockData) throws Exception {
		for (AbstractExporter exporter : exports) {
			if (!exporter.breakProcessFlag && exporter.getType() == blockData.getType()) {
				exporter.total += blockData.getData().size();
				try {
					exporter.export(blockData);
				} catch (Exception e) {
					exporter.breakProcess(e.getMessage());
					LOGGER.warn("任务ID={},输出模版ID={}输出器发生异常已关闭", new Object[]{exporter.task.getId(), exporter.getExportId()}, e);
				}
			}
		}
	}

	@Override
	public void close() {
		for (AbstractExporter exporter : exports) {
			try {
				exporter.close();
			} catch (Exception e) {
				LOGGER.warn("任务ID={},输出模版ID={}输出器关闭时发生异常", new Object[]{exporter.task.getId(), exporter.getExportId()}, e);
			}
		}
	}

	@Override
	public void endExportOnException() {
		for (AbstractExporter exporter : exports) {
			exporter.endExportOnException();
		}
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		for (AbstractExporter exporter : exports) {
			exporter.buildExportPropertysList(propertysSet);
		}
	}

	@Override
	public void breakProcess(String breakCause) {
		for (AbstractExporter exporter : exports) {
			exporter.breakProcess(breakCause);
		}
	}
		
}
