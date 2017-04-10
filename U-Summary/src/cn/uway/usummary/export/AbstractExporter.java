package cn.uway.usummary.export;

import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.cache.Cacher;
import cn.uway.usummary.entity.BlockData;
import cn.uway.usummary.entity.Element;
import cn.uway.usummary.entity.ExportFuture;
import cn.uway.usummary.entity.ExporterArgs;

public abstract class AbstractExporter implements Exporter{
	
	private static Logger LOG = LoggerFactory.getLogger(AbstractExporter.class);
	
	protected ExporterArgs exportInfo;
	
	/**
	 * 输出总时间
	 */
	protected long totalTime;
	
	// 当前处理条数
	protected long current = 0L;

	// 总共条数
	protected long total = 0L;

	// 成功条数
	protected long succ = 0L;

	// 失败条数
	protected long fail = 0L;

	// 失败码
	protected int errorCode = 1;

	// 失败原因
	protected String cause;

	// 输出目的地
	protected String dest;

		// 输出开始时间
	protected Date startTime;

	// 输出结束时间
	protected Date endTime;
	
	protected Cacher cacher;
	
	// 终止处理标识(当向cacher加入blockData发生异常时，此标识将被启用)
	public volatile boolean breakProcessFlag = false;

	// 终止原因
	public volatile String breakProcessCause;
	
	// 异常标志 主要用于记录处理输出器初始化异常
	protected boolean exporterInitErrorFlag = false;
	
	protected int batchNum;
	
	public  AbstractExporter(ExporterArgs exportInfo){
		this.exportInfo = exportInfo;
	}
	
	public ExportFuture call() throws Exception {
		startTime = new Timestamp(new Date().getTime());
		int exportedNum = 0;
		ExportFuture exportFuture = null;
		if (exporterInitErrorFlag) {
			this.endTime = new Timestamp(new Date().getTime());
			exportFuture = createExportFuture();
			LOG.debug("Exporter初始化失败,输出中止,报表={}", exportFuture.toString());
			return exportFuture;
		}
		BlockData blockData = null;
		// 如果初始化没有失败 则进行输出
		while (!breakProcessFlag) {
			try {
				if (cacher.isCommit() && exportedNum >= cacher.size()) {
					this.close();
					this.cacher.shutdown();
					break;
				}
				Element element = this.cacher.getNextElement();
				// cacher.getNextElement只有当所有element都输出完成才返回空。但是需要使用continue再次执行一下判断。
				if (element == null) {
					continue;
				}
				blockData = (BlockData) element.getElementValue();
				exportedNum++;
				this.total += blockData.getData().size();
				this.export(blockData.getData());
			} catch (Exception e) {
				LOG.error("输出异常", e);
				this.setErrorCode(0);
				this.setCause("输出发生异常!");
				this.close();
				cacher.shutdown();
				break;
			}
		}

		if (breakProcessFlag) {
			LOG.error("ExportFuture::call() 收到终止处理标识，export线程退出。 终止原因:{}", breakProcessCause);
		}

		LOG.debug("Cacher.size()={},Exporter counter={}", new Object[]{this.cacher.size(), exportedNum});
		endTime = new Timestamp(new Date().getTime());
		exportFuture = createExportFuture();
		LOG.debug(Thread.currentThread().getName() + "，输出完毕，产生报表={}", exportFuture.toString());
		return exportFuture;
	}
	
	protected ExportFuture createExportFuture() {
		ExportFuture exportFuture = new ExportFuture();
		exportFuture.setStartTime(this.startTime);
		exportFuture.setEndTime(this.endTime);
		exportFuture.setDest(this.dest);
		exportFuture.setSucc(this.succ);
		exportFuture.setFail(this.fail);
		exportFuture.setTotal(this.total);
		exportFuture.setErrorCode(this.errorCode);
		exportFuture.setCause(this.cause);
		return exportFuture;
	}
	
	@Override
	public void breakProcess(String breakCause) {
		this.breakProcessFlag = true;
		this.breakProcessCause = breakCause;
	}

	public ExporterArgs getExportInfo() {
		return exportInfo;
	}

	public void setExportInfo(ExporterArgs exportInfo) {
		this.exportInfo = exportInfo;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getSucc() {
		return succ;
	}

	public void setSucc(long succ) {
		this.succ = succ;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Cacher getCacher() {
		return cacher;
	}

	public void setCacher(Cacher cacher) {
		this.cacher = cacher;
	}

	public boolean isExporterInitErrorFlag() {
		return exporterInitErrorFlag;
	}

	public void setExporterInitErrorFlag(boolean exporterInitErrorFlag) {
		this.exporterInitErrorFlag = exporterInitErrorFlag;
	}
	
}
