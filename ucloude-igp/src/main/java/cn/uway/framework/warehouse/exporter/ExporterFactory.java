package cn.uway.framework.warehouse.exporter;

import java.sql.SQLException;
import java.util.Date;

import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.InfoBrightFileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ParqExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.RemoteFileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.SortedDataRule;
import cn.uway.framework.warehouse.exporter.template.SqlldrExportTemplateBean;
import cn.uway.util.TimeUtil;

/**
 * ExporterFactory
 * 
 * @author chenrongqiang 2012-11-1
 */
public class ExporterFactory {

	public static final Object obj = new Object();
	
	//HBASE入库连接ID
	public static final int HBASE_TARGET_CONNECTION_ID = -200;
	public static final int HBASE_TARGET_CONNECTION_ID_IMSI = -300;

	public static Exporter createExporter(ExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		if (ExportTemplateBean.JDBC_EXPORTER == exportTempletBean.getType()) {
			DbExportTemplateBean templetBean = (DbExportTemplateBean) exportTempletBean;
			DatabaseExporterBean dbExportBean = (DatabaseExporterBean)templetBean.getExportTargetBean();
			if (dbExportBean.getConnectionInfo().getId() == HBASE_TARGET_CONNECTION_ID) {
				return new HBaseExporter(templetBean, exporterArgs);
			}
			if (dbExportBean.getConnectionInfo().getId() == HBASE_TARGET_CONNECTION_ID_IMSI) {
				return new HBaseAllFieldExporter(templetBean, exporterArgs);
			}
			
			return new JdbcBatchExporter(templetBean, exporterArgs);
		}
		if (ExportTemplateBean.JDBC_TRUNCATE_AND_EXPORTER == exportTempletBean.getType()) {
			DbExportTemplateBean templetBean = (DbExportTemplateBean) exportTempletBean;
			return new JdbcBatchExExporter(templetBean, exporterArgs);
		}
		if (ExportTemplateBean.JDBC_BAKUP_AND_EXPORTER == exportTempletBean.getType()) {
			DbExportTemplateBean templetBean = (DbExportTemplateBean) exportTempletBean;
			return new JdbcBatchBackUpExporter(templetBean, exporterArgs);
		}
		// 使用数据库连接池并且具有容错能力的JDBC输出器
		if (ExportTemplateBean.FAULT_TOLERANT_POOLED_JDBC_EXPORTER == exportTempletBean.getType()) {
			DbExportTemplateBean templetBean = (DbExportTemplateBean) exportTempletBean;
			DatabaseExporterBean dbExportBean = (DatabaseExporterBean)templetBean.getExportTargetBean();
			if (dbExportBean.getConnectionInfo().getId() == HBASE_TARGET_CONNECTION_ID) {
				return new HBaseExporter(templetBean, exporterArgs);
			}
			if (dbExportBean.getConnectionInfo().getId() == HBASE_TARGET_CONNECTION_ID_IMSI) {
				return new HBaseAllFieldExporter(templetBean, exporterArgs);
			}
			
			return new PooledFaultTolerantJdbcExporter(templetBean, exporterArgs);
		}
		// greenplum方式入库 目前暂不支持
		if (ExportTemplateBean.EXPORT_DB_GREENPLUM == exportTempletBean.getType())
			throw new UnsupportedOperationException("暂不支持GreenPlum方式入库");
		// mysql方式入库 目前暂不支持
		if (ExportTemplateBean.EXPORT_DB_MYSQL == exportTempletBean.getType())
			throw new UnsupportedOperationException("暂不支持GreenPlum方式入库");

		if (ExportTemplateBean.LOACL_FILE_EXPORTER == exportTempletBean.getType()) {
			FileExportTemplateBean templetBean = (FileExportTemplateBean) exportTempletBean;
			return new MapBufferedFileExporter(templetBean, exporterArgs);
		}
		
		// 远程文件输出export(可能是ftp或sftp或hdfs之类的)
		if (ExportTemplateBean.REMOTE_FILE_EXPORTER == exportTempletBean.getType()) {
			RemoteFileExportTemplateBean templetBean = (RemoteFileExportTemplateBean) exportTempletBean;
			return new RemoteFileExporter(templetBean, exporterArgs);
		}
		
		// 远程文件输出export(可能是ftp或sftp或hdfs之类的)
		if (ExportTemplateBean.EXTEND_REMOTE_FILE_EXPORTER == exportTempletBean.getType()) {
			RemoteFileExportTemplateBean templetBean = (RemoteFileExportTemplateBean) exportTempletBean;
			return new ExtendRemoteFileExporter(templetBean, exporterArgs);
		}		

		// infobright方式入库
		if (ExportTemplateBean.EXPORT_INFOBRIGHT == exportTempletBean.getType()) {
			return infoBrightFileExporterHandler(exportTempletBean, exporterArgs);
		}

		// sqlldr工具方式入库
		if (ExportTemplateBean.EXPORT_DB_SQLLDR == exportTempletBean.getType()) {
			SqlldrExportTemplateBean templetBean = (SqlldrExportTemplateBean) exportTempletBean;
			return new SqlldrExporter(templetBean, exporterArgs);
		}
		
		// Parquet方式入库
		if (ExportTemplateBean.PARQUET_EXPORTER == exportTempletBean.getType()) {
			ParqExportTemplateBean templetBean = (ParqExportTemplateBean) exportTempletBean;
			return new ParquetExporter(templetBean, exporterArgs);
		}
		
		// 数据库入库到IMPALA入库
		if (ExportTemplateBean.CONFIGURE_PARQUET_EXPORTER == exportTempletBean.getType()) {
			ParqExportTemplateBean templetBean = (ParqExportTemplateBean) exportTempletBean;
			return new ConfigureParquetExporter(templetBean, exporterArgs);
		}		
		
		//kafka输出
		if (ExportTemplateBean.KAFKA_EXPORTER == exportTempletBean.getType()) {
			FileExportTemplateBean templetBean = (FileExportTemplateBean) exportTempletBean;
			return new KafkaExporter(templetBean, exporterArgs);
		}		
		
		// 汇总输出，入库
		if (ExportTemplateBean.EXPORT_DB_SUMMARY == exportTempletBean.getType()) {
			DbExportTemplateBean templetBean = (DbExportTemplateBean) exportTempletBean;
			ExporterSummaryArgs exporterSummaryArgs = (ExporterSummaryArgs) exporterArgs;
			SummaryDataJdbcExporter exporter = new SummaryDataJdbcExporter(templetBean, exporterSummaryArgs);
			if (exporter.validate()) {
				// 如果补汇，先delete
				if (exporterSummaryArgs.isRepair())
					deleteRecords(exporterSummaryArgs, exporter);
				return exporter;
			}
			return null;
		}
		
		if (ExportTemplateBean.EXPORT_EMPTY == exportTempletBean.getType()) {
			return new EmptyExporter(exportTempletBean, exporterArgs);
		}
		
		throw new IllegalArgumentException("创建Exporter失败，错误的输出类型");
	}

	/**
	 * 重汇时先删除记录
	 * 
	 * @param exporterSummaryArgs
	 * @param exporter
	 */
	public static void deleteRecords(ExporterSummaryArgs exporterSummaryArgs, SummaryDataJdbcExporter exporter) {
		try {
			exporter.deleteRecords();
		} catch (SQLException e) {
			exporter = null;
			throw new IllegalArgumentException("在创建SummaryDataJdbcExporter时删除记录失败(重汇)");
		}
	}

	/**
	 * infobright方式即type = 5时的输出器生成
	 * 
	 * @param exportTempletBean
	 * @param exporterArgs
	 * @return Exporter
	 */
	public static Exporter infoBrightFileExporterHandler(ExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		FileExportTemplateBean fileTempletBean = (FileExportTemplateBean) exportTempletBean;
		InfoBrightFileExportTemplateBean templetBean = (InfoBrightFileExportTemplateBean) fileTempletBean;
		SortedDataRule sortedDataRule = templetBean.getSplitDataFormatBean();
		if (sortedDataRule == null) {
			return new MapBufferedFileExporter(templetBean, exporterArgs);
		}
		Date dataTime = exporterArgs.getDataTime();
		String dimension = sortedDataRule.getDimension();// 分离维度，即按照月/日来分离数据
		String beginTime = sortedDataRule.getBeginTime();// 开始归类时间
		String endTime = sortedDataRule.getEndTime();// 结束归类时间
		try {
			// dimension = MONTH
			if ("MONTH".equals(dimension)) {
				// 判断是否是时间区间内
				if (TimeUtil.isBetweenTimeByMonth(dataTime, beginTime, endTime)) {
					return getAcquiredExporter(exporterArgs, templetBean);
				} else {
					return new MapBufferedFileExporter(templetBean, exporterArgs);
				}
			}
			// dimension = DAY
			if ("DAY".equals(dimension)) {
				// 判断是否是时间区间内
				if (TimeUtil.isBetweenTimeByDay(dataTime, beginTime, endTime)) {
					return getAcquiredExporter(exporterArgs, templetBean);
				} else {
					return new MapBufferedFileExporter(templetBean, exporterArgs);
				}
			}
			throw new IllegalArgumentException("创建Exporter失败，错误的dimension");
		} catch (Exception e) {
			throw new IllegalArgumentException("创建Exporter失败，type = " + exportTempletBean.getType());
		}
	}

	/**
	 * 对MapBufferedCuttedFileExporter初始化时加锁控制，以防写文件时发生死锁
	 * 
	 * @param exporterArgs
	 * @param templetBean
	 * @return
	 * @throws InterruptedException
	 */
	public static synchronized MapBufferedCuttedFileExporter getAcquiredExporter(ExporterArgs exporterArgs,
			InfoBrightFileExportTemplateBean templetBean) throws InterruptedException {
		MapBufferedCuttedFileExporter exporter = new MapBufferedCuttedFileExporter(templetBean, exporterArgs);
		return exporter;
	}
}
