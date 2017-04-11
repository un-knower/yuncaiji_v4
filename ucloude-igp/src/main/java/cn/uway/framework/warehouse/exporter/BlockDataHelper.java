package cn.uway.framework.warehouse.exporter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.framework.warehouse.repository.BufferedMultiExportRepository;
import cn.uway.util.ArrayMapKeyIndex;


public class BlockDataHelper {
	private BufferedMultiExportRepository repository;
	
	// 数据类型对应的keyMap的主键索引，和ArrayMap配套使用
	protected Map<Integer, ArrayMapKeyIndex<String>> dataKeyIndexMap = new ConcurrentHashMap<Integer, ArrayMapKeyIndex<String>> ();
	
	public BlockDataHelper(BufferedMultiExportRepository repository) {
		this.repository = repository;
	}
	
	/**
	 * 为对应的数据类型注册一个主键索引Map
	 * @param dataType		数据类型
	 * @param keyIndexMap	对应的主键索引Map
	 */
	public synchronized void registerKeyIndexMap(int dataType, ArrayMapKeyIndex<String> keyIndexMap) {
		dataKeyIndexMap.put(dataType, keyIndexMap);
	}
	
	/**
	 * 根据指定的数据类型，返回相应的主键索引Map
	 * @param dataType	数据类型
	 * @return
	 */
	public synchronized ArrayMapKeyIndex<String> getDataKeyIndexByDataType(int dataType) {
		ArrayMapKeyIndex<String> exportPropertysMap = dataKeyIndexMap.get(dataType);
		if (exportPropertysMap == null) {
			exportPropertysMap = getExportUsesPropertysMapByDataType(dataType);
			dataKeyIndexMap.put(dataType, exportPropertysMap);
		}
		
		return exportPropertysMap;
	}
	
	/**
	 * <pre>
	 * 根据指定的数据类型，获取输出用到的数据属性列表
	 * </pre>
	 * @param dataType 数据类型
	 * @return 属性列表
	 * 			key: 	属性名
	 * 			value:	属性index
	 */
	public ArrayMapKeyIndex<String> getExportUsesPropertysMapByDataType(int dataType) {
		ArrayMapKeyIndex<String> exportPropertysMap = new ArrayMapKeyIndex<String>();
		Set<String> propertysSet = repository.getExportUsesPropertys(dataType);
		if (propertysSet != null) {
			for (String prop:propertysSet) {
				exportPropertysMap.addKey(prop);
			}
		}
		
		return exportPropertysMap;
	}
}
