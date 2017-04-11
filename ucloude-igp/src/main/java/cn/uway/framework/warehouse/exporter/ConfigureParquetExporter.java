package cn.uway.framework.warehouse.exporter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.ParqExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.impala.ImpalaJdbc;
import cn.uway.util.parquet.FNCFactory;
import cn.uway.util.parquet.PWPool;
import cn.uway.util.parquet.ParqContext;

public class ConfigureParquetExporter extends ParquetExporter{
	
	protected static final ILogger LOG = LoggerManager.getLogger(ConfigureParquetExporter.class);
	
	private DatabaseConnectionInfo connectionInfo;
	
	public ConfigureParquetExporter(ParqExportTemplateBean templetBean, ExporterArgs exporterArgs) {
		super(templetBean, exporterArgs);
		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) templetBean.getExportTargetBean();
		connectionInfo = dbTargetBean.getConnectionInfo();
	}
	
	public void close() {
		bpInfo.setParseNum(current);
		// 立即关闭文件
		PWPool.checkClose(wKey);
		deleteFile();
		LOG.debug(
				"【入库时间统计】{}表入库耗时{}秒，组装{}秒，写入{}秒，成功{}条，失败{}条，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}",
				new Object[] { this.table, totalTime / 1000.00, packageTime / 1000.00, writeTime / 1000.00,
						this.succ, this.fail, task.getId(),
						(this.exporterArgs != null ? entryNames.get(0) : ""),
						task.getExtraInfo().getCityId(),
						task.getExtraInfo().getOmcId(),
						task.getExtraInfo().getBscId(),
						task.getExtraInfo().getVendor() });
		// 此类用于配置表同步，不需要日志入库
	}
	
	private void renameFile(String newFile,String oldFile)
	{
		try{
			Path newPath = new Path(newFile);
			Path oldPath = new Path(oldFile);
			Configuration conf = ParqContext.getNewCfg();
			FileSystem fs = FileSystem.newInstance(conf);
			if(fs.exists(newPath)){
				if(fs.rename(newPath, oldPath)){
					LOG.debug("将配置表"+this.table+"的文件"+newFile+"重命名为"+oldFile+"成功");
					ImpalaJdbc jdbc = new ImpalaJdbc(connectionInfo.getUrl());
					jdbc.refreshTable(this.table);
					jdbc.release();
					LOG.debug("刷新配置表"+this.table+"成功!");
				}
				else{
					for(int i=1 ;i<=3; i++)
					{
						LOG.debug("将配置表"+this.table+"的文件"+newFile+"重命名为"+oldFile+"失败,"+(i*2000)+"秒后开始第"+i+"次重试!");
						Thread.sleep(i*2000);
						if(fs.rename(newPath, oldPath))
						{
							LOG.debug("将配置表"+this.table+"的文件"+newFile+"重命名为"+oldFile+"成功");
							ImpalaJdbc jdbc = new ImpalaJdbc(connectionInfo.getUrl());
							jdbc.refreshTable(this.table);
							jdbc.release();
							LOG.debug("刷新配置表"+this.table+"成功!");
							break;
						}
					}
					LOG.debug("将配置表"+this.table+"的文件"+newFile+"重命名为"+oldFile+"失败");
				}
			}
		}
		catch(Exception e){
			LOG.error("将配置表"+this.table+"的文件"+newFile+"重命名为"+oldFile+"失败，原因："+e);
		}
	}
	
	private void deleteFile()
	{
		try{
			// 新生成的文件
			String newFile = FNCFactory.getCreater(table, "",bpInfo.getCtType()).getNewName();
			String oldFile = newFile.replace(table.toLowerCase()+"_tmp.parq", table.toLowerCase()+".parq");
			Path file = new Path(oldFile);
			Configuration conf = ParqContext.getNewCfg();
			FileSystem fs = FileSystem.newInstance(conf);
			if(fs.exists(file)){
				if(fs.delete(file)){
					LOG.debug("删除旧配置表"+this.table+"文件：成功");
					renameFile(newFile,oldFile);
				}
				else{
					LOG.debug("删除旧配置表"+this.table+"文件：失败");
				}
			}
			else
			{
				LOG.debug("旧配置表"+this.table+"文件不存在!");
				renameFile(newFile,oldFile);
			}
		}
		catch(Exception e){
			LOG.error("删除旧配置表"+this.table+"文件失败，原因："+e);
		}
	}
}
