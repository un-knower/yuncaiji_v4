package cn.uway.framework.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.uway.util.ArrayMap;
import cn.uway.util.ArrayMapKeyIndex;



public class ParseOutRecordArrayMap extends ArrayMap<String, String> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6326317172803130247L;

	public ParseOutRecordArrayMap() {
		super(null, true);
	}
	
	/**
	 * @param keyIndexMap 主键对应的存储位置索引表
	 * @param ignorePutOperationOnKeyNotExistKeyIndexMap 是否忽略不在keyIndexMap里的主键put或putAll操作(true:忽略 false:不忽略)
	 */
	public ParseOutRecordArrayMap(ArrayMapKeyIndex<String> keyIndexMap, boolean ignorePutOperationOnKeyNotExistKeyIndexMap) {
		super(keyIndexMap, ignorePutOperationOnKeyNotExistKeyIndexMap, 10);
	}
	
	/**
	 * @param keyIndexMap 主键对应的存储位置索引表
	 * @param ignorePutOperationOnKeyNotExistKeyIndexMap 是否忽略不在keyIndexMap里的主键put或putAll操作(true:忽略 false:不忽略)
	 */
	public ParseOutRecordArrayMap(ArrayMapKeyIndex<String> keyIndexMap, boolean ignorePutOperationOnKeyNotExistKeyIndexMap, int initialCapacity) {
		super(keyIndexMap, ignorePutOperationOnKeyNotExistKeyIndexMap, initialCapacity);
	}
	
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
    	s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		s.defaultReadObject();
    }
    
    public void dumpExternalKeyValues() {
    	if (this.mapAdditionKeyValues != null) {
    		this.mapAdditionKeyValues.clear();
    		this.mapAdditionKeyValues = null;
    	}
    }
    
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException {
	
//		Object[] elData = new Object[20000*500];
//		elData[elData.length / 2] = new String("sdfsdffff");
//		elData[elData.length-1] = new String("sdfsdf");
		System.gc();
		Thread.sleep(2000);
		
		double maxMemory = Runtime.getRuntime().maxMemory() / 1024. / 1024.;
		double totalMemory = Runtime.getRuntime().totalMemory() / 1024. / 1024.;
		double freeMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
		double usedMemory = totalMemory - freeMemory;
		System.out.println("[使用前] 总内存:" + totalMemory + "M 空闲内存:" + freeMemory + "M 已用内存:" + usedMemory + "M 最大内存:" + maxMemory);
		double preUsedMemory = usedMemory;
		
		List<Object> records = new LinkedList<Object>();
		final long TEST_ITEM_COUNT = 5L;
		
		
		if (true)
		{
			for (Long i=0L; i<TEST_ITEM_COUNT; ++i) {
				Map<String, String> record = new HashMap<String, String>();
				processRecord(record, 1, i);
				records.add(record);
				
				if (i < 5L)
				{
					ByteArrayOutputStream byteOut = null;
					ObjectOutputStream objOut = null;
					try {
						byteOut = new ByteArrayOutputStream();
						objOut = new ObjectOutputStream(byteOut);
						objOut.writeObject(record);
						objOut.flush();
						byteOut.flush();
						byte[] buff = byteOut.toByteArray();
						
						objOut.close();
						byteOut.close();
						
						System.out.println(buff.length);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			System.gc();
			Thread.sleep(5000);
			maxMemory = Runtime.getRuntime().maxMemory() / 1024. / 1024.;
			totalMemory = Runtime.getRuntime().totalMemory() / 1024. / 1024.;
			freeMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
			usedMemory = totalMemory - freeMemory;
			System.out.println("[HashMap内存使用情况] 总内存:" + totalMemory + "M 空闲内存:" + freeMemory + "M 已用内存:" + (usedMemory-preUsedMemory) + "M 最大内存:" + maxMemory);
			preUsedMemory = usedMemory;
		}
		
		{
			ArrayMapKeyIndex<String> keyIndexMap = null;
			for (Long i=0L; i<TEST_ITEM_COUNT; ++i) {
				Map<String, String> record = new ParseOutRecordArrayMap(keyIndexMap, true);
				processRecord(record, 2, i);
				records.add(record);
				if (i < 5L)
				{
					ByteArrayOutputStream byteOut = null;
					ObjectOutputStream objOut = null;
					try {
						byteOut = new ByteArrayOutputStream();
						objOut = new ObjectOutputStream(byteOut);
						objOut.writeObject(record);
						objOut.flush();
						byteOut.flush();
						byte[] buff = byteOut.toByteArray();
						
						System.out.println(buff.length);
						
						ByteArrayInputStream byteIn = new ByteArrayInputStream(buff);
						ObjectInputStream objIn = new ObjectInputStream(byteIn);
						Map<String, String> recordin = (Map<String, String>)objIn.readObject();
						if (recordin == null) {
							assert(false);
						}
					}
					catch (Exception e) {
						
					}
				}
				
				if (keyIndexMap == null) {
					keyIndexMap = ((ParseOutRecordArrayMap)record).getKeyIndexsMap();
					keyIndexMap.rebuildKeyIndex();
				}
			}
			
			System.gc();
			Thread.sleep(5000);
			maxMemory = Runtime.getRuntime().maxMemory() / 1024. / 1024.;
			totalMemory = Runtime.getRuntime().totalMemory() / 1024. / 1024.;
			freeMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
			usedMemory = totalMemory - freeMemory;		
			System.out.println("[ArrayMap内存使用情况] 总内存:" + totalMemory + "M 空闲内存:" + freeMemory + "M 已用内存:" +( usedMemory -preUsedMemory) + "M 最大内存:" + maxMemory);
		}
		
		System.gc();
		Thread.sleep(2000);
		maxMemory = Runtime.getRuntime().maxMemory() / 1024. / 1024.;
		totalMemory = Runtime.getRuntime().totalMemory() / 1024. / 1024.;
		freeMemory = Runtime.getRuntime().freeMemory() / 1024. / 1024.;
		usedMemory = totalMemory - freeMemory;
		System.out.println("[清理后] 总内存:" + totalMemory + "M 空闲内存:" + freeMemory + "M 已用内存:" + usedMemory + "M 最大内存:" + maxMemory);
	}
	
	public static boolean processRecord(Map<String, String> record , int param, long recordIndex) {
		Integer n = 0;
		record.put("START_TIME", (new Date()).toString());
		record.put("IMSI", "1");
		record.put("LAST_IRAT_HO_TAR_CELL_OPERAT1", "0");
		record.put("LAST_IRAT_HO_TAR_CELL_OPERAT2", "11");
		record.put("LAST_IRAT_HO_TAR_CELL_OPERAT3", "22");
		record.put("LAST_IRAT_HO_TAR_CELL_OPERAT4", "333");
		record.put("LAST_IRAT_HO_TAR_CELL_OPERAT5", "033");
		record.put("LAST_IRAT_HO_TAR_CELL_OPERAT6", "33345");
		record.put("NE_CELL_ID", "20140621001132");
		record.put("NE_CELL_NAME", "Nokia Cdt Parse.java");
		record.put("TMSI", "WT051174");
		record.put("HO_OPERATIONTYPE", "21");
		record.put("TARGETCELL_PTX", "-26.358426");
		record.put("MOVE_STATE",  (++n) + "gpeh export xmls");
		record.put("MOVE_SPEAD", (++n) + "gpeh export xmls");
		record.put("MOVE_DIRECT", (++n) + "gpeh export xmls");
		record.put("IF_POOR_COVER", "6gpeh export xmls");
		record.put("IF_PILOT_PLUT", "7gpeh export xmls");
		record.put("IF_MISS_NEI", (++n) + "gpeh export xmls");
		record.put("BAD_QOE_TYPE", (++n) + "gpeh export xmls");
		record.put("ACC_FAIL_REAL_CAUSE", (++n) + "gpeh export xmls");
		record.put("DROP_FAIL_REAL_CAUSE", (++n) + "gpeh export xmls");
		record.put("ACCESS_NE_BSC_ID", (++n) + "gpeh export xmls");
		record.put("ACCESS_NE_MSC_ID", (++n) + "gpeh export xmls");
		record.put("ACCESS_CITY_ID", (++n) + "gpeh export xmls");
		record.put("ACCESS_GMS_ID", (++n) + "begin of skip item....");

		if (recordIndex == 0) {
			for (Integer i=0; i<450; ++ i) {
				record.put("FIELD" + (++n), i.toString());
			}
		}
		
		record.put("RELEASE_NE_BSC_ID", (++n) + "... end of skip item");
		record.put("RELEASE_NE_MSC_ID", (++n) + "gpeh export xmls");
		record.put("RELEASE_CITY_ID", (++n) + "gpeh export xmls");
		record.put("RELEASE_GMS_ID", (++n) + "gpeh export xmls");
		record.put("ACCESS_LONGITUDE", (++n) + "gpeh export xmls");
		record.put("ACCESS_LATITUDE", (++n) + "gpeh export xmls");
		record.put("RELEASE_LONGITUDE", "中华人民共和国中华人民共和国中华人民共和国中华人民共和国中华人民共和国中华人民共和国");
		record.put("RELEASE_LATITUDE", (++n) + "gpeh export xmls");
		record.put("TER_VENDOR_ID", (++n) + "gpeh export xmls");
		record.put("UL_THROUGHPUT", (++n) + "gpeh export xmls");
		record.put("DL_THROUGHPUT", (++n) + "gpeh export xmls");
		record.put("UL_USER_THROUGHPUT", (++n) + "gpeh export xmls");
		record.put("DL_USER_THROUGHPUT", (++n) + "gpeh export xmls");
		record.put("LAST_RAT_MR_NE_CELL_ID", (++n) + "gpeh export xmls");
		record.put("FIRST_RAT_MR_NE_CELL_ID", (++n) + "gpeh export xmls");
		record.put("LAST_IRAT_MR_NE_CELL_ID", (++n) + "gpeh export xmls");
		record.put("RRC_Setup_CID", (++n) + "gpeh export xmls");
		record.put("AUTH_DELAY", (++n) + "gpeh export xmls");
		record.put("AUTH_RESP_TIME", (++n) + "gpeh export xmls");
		record.put("RAB_MAXBITRATE_UL", (++n) + "gpeh export xmls");
		record.put("RAB_MAXBITRATE_DL", (++n) + "gpeh export xmls");
		record.put("RAB_GUARBITRATE_UL", (++n) + "gpeh export xmls");
		record.put("CP_DATA_TIME", (++n) + "gpeh export xmls");
		record.put("IU_REL_REQ_CAUSE", "end of item");
		
		return true;
	}
}
