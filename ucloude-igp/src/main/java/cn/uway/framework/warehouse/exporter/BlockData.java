package cn.uway.framework.warehouse.exporter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.parser.ParseOutRecordArrayMap;
import cn.uway.framework.warehouse.repository.BufferedMultiExportRepository;
import cn.uway.framework.warehouse.repository.Repository;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.ArrayMapKeyIndex;

/**
 * 数据块
 * 
 * @author chenrongqiang 2012-11-1
 * @version 1.0
 * @since 3.0
 */
public class BlockData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// 记录链表
	private transient List<ParseOutRecord> data;
	
	// 数据类型
	private transient int type;
	
	// 是否是原因状态，如果为true,则代表，刚从内存或文件中系列化出来，此时，仍然需要加后，才可以使用data记录数据
	private transient boolean crudeState = false;
	
	private transient static final ILogger LOGGER = LoggerManager.getLogger(BufferedMultiExportRepository.class);

	public BlockData(List<ParseOutRecord> data, int type) {
		super();
		this.data = data;
		this.type = type;
	}

	public List<ParseOutRecord> getData() {
		if (crudeState) {
			LOGGER.error("BlockData在系列化后，未调用processOnAfterSerialRead()方法，这将导到如果使用到ParseOutRecordArrayMap时，会出错");
			assert(false);
			return null;
		}
		
		return data;
	}

	public int getType() {
		return type;
	}
	
	/**
	 * 在加入到缓存中的前置处理动作
	 * <pre>
	 * 	1、dump掉不必要(非Export输出需要)的内存
	 * </pre>
	 */
	public void processOnbeforeAddToCacher() {
		if (data == null)
			return;
		
		for (ParseOutRecord record : data) {
			Map<String, String> mapKeyValues = record.getRecord();
			// 非ParseOutRecordArrayMap对象，不需作下面的内存倾泄动作
			if(!(mapKeyValues instanceof ParseOutRecordArrayMap)) { 
				break;
			}
				
			ParseOutRecordArrayMap arrMapKeyValues = (ParseOutRecordArrayMap)mapKeyValues;
			arrMapKeyValues.dumpExternalKeyValues();
		}
	}
	
	/**
	 * 在缓存系列化回来后，将keyIndex Map设回ParseOutRecordArrayMap
	 */
	public void processOnAfterSerialRead(Repository repository) {
		if (!crudeState || (repository == null) || !(repository instanceof BufferedMultiExportRepository))
			return;
		
		// 经过调用processOnAfterSerialRead方法后，将crudeState原始状态复位
		crudeState = false;
		BufferedMultiExportRepository buffMultiExportRepository = (BufferedMultiExportRepository)repository;
		ArrayMapKeyIndex<String> keyIndexMap = buffMultiExportRepository.getBlockDataHelper().getDataKeyIndexByDataType(this.type);
		if (keyIndexMap == null) {
			return;
		}
		
		for (ParseOutRecord record : data) {
			Map<String, String> mapKeyValues = record.getRecord();
			// 非ParseOutRecordArrayMap对象，不需作下面的处理动作
			if(!(mapKeyValues instanceof ParseOutRecordArrayMap)) { 
				break;
			}
			
			ParseOutRecordArrayMap arrMapKeyValues = (ParseOutRecordArrayMap)mapKeyValues;
			arrMapKeyValues.setKeyIndexsMap(keyIndexMap);
		}
	}
	
	// 重写writeObject、readObject有助于节省序列化时使用的内存，因为默认的序列化会将变量名称也写进去了。
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
    	s.defaultWriteObject();
    	
    	s.writeInt(type);
    	int nSize = -1;
    	if (data != null) {
    		nSize = data.size();
    	}
    	s.writeInt(nSize);
    	
    	if (data == null) 
    		return;
    		
    	for (ParseOutRecord record: data){
    		s.writeObject(record);
    	}
    }
    
    // 在read完成后，还需要调用processOnAfterSerialRead方法，将keyIndexMap设置回去
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    	s.defaultReadObject();
    	
    	type = s.readInt();
    	int nSize = s.readInt();
    	if (nSize <0)
    		return;
    	
    	// 设置为原始状态，此时仍然不可用，需要调用processOnAfterSerialRead后，才可用ParseOutRecord对象
    	crudeState = true;
    	data = new LinkedList<ParseOutRecord>();
    	for (int i=0; i<nSize; ++i) {
    		ParseOutRecord record = (ParseOutRecord)s.readObject();
    		if (record != null) {
    			record.setType(type);
    			data.add(record);
    		}
    	}
    }
}
