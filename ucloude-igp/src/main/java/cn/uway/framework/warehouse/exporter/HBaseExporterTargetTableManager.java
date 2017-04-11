package cn.uway.framework.warehouse.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;

import cn.uway.framework.connection.HBaseHelper;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportTableProperty;


public class HBaseExporterTargetTableManager {
	public class HBaseTargetTable {
		public String tableName;
		public HTableInterface hTable;
		public List<Put> batchRecordList;
		
		public HBaseTargetTable(String tableName, HTableInterface hTable, int batchNum) {
			this.tableName = tableName;
			this.hTable = hTable;
			batchRecordList = new ArrayList<Put>(batchNum);
		}
		
		public void put(Put put) {
			batchRecordList.add(put);
		}
	}
	
	/**
	 * HBase数据服务器连接助手
	 */
	HBaseHelper hbaseHelper = null;
	
	/**
	 * 批量提交记录数
	 */
	private int batchNum;
	
	/**
	 * 缓存尺寸
	 */
	private int buffCacheSize;
	
	private HBaseExportTableProperty hbaseExporTableConf;
	
	
	private Map<String, HBaseTargetTable> mapHBaseTargetTables = new HashMap<String, HBaseTargetTable>();
	
	public HBaseExporterTargetTableManager(HBaseHelper hbaseHelper, HBaseExportTableProperty hbaseExporTableConf, int batchNum) {
		this.hbaseExporTableConf = hbaseExporTableConf;
		this.hbaseHelper = hbaseHelper;
		this.batchNum = batchNum;
		this.buffCacheSize = batchNum * 4 * 1024;
	}
	 
	/**
	 * 打开HBASE表，如果没有，则创建。
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public HBaseTargetTable getHBaseTargetTable(String tableName) throws Exception {
		HBaseTargetTable targetTable = mapHBaseTargetTables.get(tableName);
		if (targetTable == null) {
			List<String> columnFamilyList = new LinkedList<String>();
			columnFamilyList.add("C");
			hbaseHelper.creatTable(tableName, columnFamilyList, hbaseExporTableConf.getCompressionAlgorithm());
			HTableInterface htable = hbaseHelper.getTable(tableName, buffCacheSize);
			targetTable = new HBaseTargetTable(tableName, htable, this.batchNum);
			
			mapHBaseTargetTables.put(tableName, targetTable);
		}
		
		return targetTable;
	}
	
	/**
	 * 提交所有的记录
	 * @throws IOException
	 */
	public void flushCommits() throws IOException {
		Iterator<Entry<String, HBaseTargetTable>> iter = mapHBaseTargetTables.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, HBaseTargetTable> entry = iter.next();
			HBaseTargetTable targetTable = entry.getValue();
			
			if (targetTable.batchRecordList.size() > 0) {
				targetTable.hTable.put(targetTable.batchRecordList);
				targetTable.hTable.flushCommits();
				targetTable.batchRecordList.clear();
			}
		}
	}
	
	/**
	 * 关闭表
	 * @throws IOException
	 */
	public void close() throws IOException {
		Iterator<Entry<String, HBaseTargetTable>> iter = mapHBaseTargetTables.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, HBaseTargetTable> entry = iter.next();
			HBaseTargetTable targetTable = entry.getValue();
			
			if (targetTable.hTable != null) {
				targetTable.hTable.close();
				targetTable.hTable = null;
			}
		}
		
		mapHBaseTargetTables.clear();
	}
}
