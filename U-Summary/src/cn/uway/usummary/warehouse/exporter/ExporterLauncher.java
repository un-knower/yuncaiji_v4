package cn.uway.usummary.warehouse.exporter;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.entity.ExportFuture;
import cn.uway.usummary.entity.ExporterArgs;
import cn.uway.usummary.export.AbstractExporter;
import cn.uway.usummary.export.impl.FtpFileExporter;
import cn.uway.usummary.export.impl.JDBCExporter;
import cn.uway.usummary.export.impl.MapBufferedFileExporter;

public class ExporterLauncher extends Thread {

	/**
	 * 仓库和输出器参数定义
	 */
	private ExporterArgs exporterArgs;
	
	private static int JDBC_EXPORTER = 1;
	
	private static int LOCAL_EXPORTER = 2;
	
	private static int FTP_EXPORTER = 3;
	
	private AbstractExporter exporter;
	
	private ExecutorService es;

	private CompletionService<ExportFuture> cs;
	
	private boolean isFinish = false;
	
	private int errCode = 1;
	
	private String cause;

	private static final Logger LOG = LoggerFactory.getLogger(ExporterLauncher.class); // 日志

	public ExporterLauncher(ExporterArgs exporterArgs) {
		this.exporterArgs = exporterArgs;
		createExporters();
	}

	/**
	 * 根据输出定义和模版 动态创建exporter
	 */
	void createExporters() {
		if(JDBC_EXPORTER == exporterArgs.getStorageType()){
			exporter = new JDBCExporter(exporterArgs);
		}else if(LOCAL_EXPORTER == exporterArgs.getStorageType()){
			exporter = new MapBufferedFileExporter(exporterArgs);
		}else if(FTP_EXPORTER == exporterArgs.getStorageType()){
			exporter = new FtpFileExporter(exporterArgs);
		}
	}

	@Override
	public void run() {
		es = Executors.newFixedThreadPool(1);
		cs = new ExecutorCompletionService<ExportFuture>(es);
		LOG.debug("ExporterLauncher线程池创建。");
		cs.submit(exporter);
		afterExporter();
	}
	
	private void afterExporter(){
		Future<ExportFuture> future = null;
		try {
			future = cs.take();
			if(future.get().getErrorCode() == 0){
				this.errCode = future.get().getErrorCode();
				this.cause = future.get().getCause();
			}
		} catch (InterruptedException e) {
			this.errCode = 0;
			this.cause = "输出器线程中断!";
			LOG.error("输出器线程中断!", e);
		} catch(Exception e){
			this.errCode = 0;
			this.cause = "输出器线程其它异常!";
			LOG.error("输出器线程其它异常!", e);
		}finally{
			isFinish = true;
			LOG.debug("完成数据输出!");
			// 输出完毕后关闭线程池
			shutdown();
		}
	}

	public AbstractExporter getExporter() {
		return exporter;
	}

	void shutdown() {
		if (es != null) {
			es.shutdown();
			LOG.debug("ExporterLauncher线程池关闭。");
		}
	}

	public boolean isFinish() {
		return isFinish;
	}

	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}
	
}
