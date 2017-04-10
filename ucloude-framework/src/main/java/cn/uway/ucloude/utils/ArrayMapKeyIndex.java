package cn.uway.ucloude.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *	本类和ArrayMap配套使用，用于记录ArrayMap中的每个key所在的索引位置 
 * @ 2014-4-17
 * @param <K>	键值类型
 * 注意：该类不保证getKeyIndex和addKey的线程安全性
 */
public class ArrayMapKeyIndex<K> {
	// value Key值对应在数组中存放的顺序(注意:keyIndexsMap暂将不被序列化)
	protected HashMap<K, Integer>  keyIndexsMap;
	
	private static final Integer INVLID_KEY_INDEX = -1;
	
	// 最大的索引编号
	protected Integer maxIndex = 0;
	
	public ArrayMapKeyIndex() {
		keyIndexsMap = new HashMap<K, Integer>();
	}
	
	/**
	 * 获取一个键值的索引
	 * getKeyIndex是线程安全的，即使在get时，有人在调用put方法，详见makeIndexNumber
	 * @param key 键值名称
	 * @param autoBuildKeyIndex 是否自动创建索引
	 * @return 键值索引，如果键值索引为INVLID_KEY_INDEX，则给它编一个号
	 */
	public Integer getKeyIndex(K key, boolean autoBuildKeyIndex) {
		Integer index = keyIndexsMap.get(key);
		if (index != null && index == -1) {
			if (autoBuildKeyIndex)
				return makeIndexNumber(key);
			else
				return null;
		}
		
		return index;
	}
	
	/**
	 * 给对应的Key编索引
	 * <pre>
	 * 在本类中，多线程地调用getKeyIndex方法是安全的，即使在getKeyIndex时会调用makeIndexNumber方法，会修改keyIndexsMap表。
	 * 说明：
	 * 			HashMap的put方法，是先根据Key的HashIndex找到table，再找到对应的Entry，
	 * 		如果存在相同的key，则直接将Entry的值改掉，而不会改变对应的table和Entry的结构，
	 * 		所以这里，能进入makeIndexNumber方法，表明key是一定存在的，在该例中，只要保证makeIndexNumber
	 * 		方法是同步的，那么makeIndexNumber是安全的，即使有多线程在调用getIndex方法。
	 * 			另外，在JDK中，还有一个LinkedHashMap继承HashMap，共用父类HashMap的put方法，put改值会调用HashMap.entry.recordAccess()方法，
	 * 		该方法在HashMap类实现是空的，但在LinkedHashMap.entry.recordAccess方法，会将最近访问的key前置操作，这样如果在
	 * 		多线程情况下，将会带来线程安全问题，所以keyIndexsMap只能用HashMap,切不可改为子类的LinkedHashMap，不然会出问题.
	 * </pre>
	 */
	private synchronized Integer makeIndexNumber(K key) {
		keyIndexsMap.put(key, maxIndex);
		
		return maxIndex++;
	}
	
	/**
	 * 添加一个主键
	 * @param key 键值名称
	 */
	public synchronized void addKey(K key) {
		if (keyIndexsMap.containsKey(key))
			return;
		
		keyIndexsMap.put(key, INVLID_KEY_INDEX);
	}
	
	public synchronized void rebuildKeyIndex() {
		
		Set<K> keySet = keyIndexsMap.keySet();
		for (K key : keySet) {
			keyIndexsMap.put(key, INVLID_KEY_INDEX);
		}
		maxIndex = 0;
	}
	
	/**
	 * 获取最大的索引序号
	 * @return
	 */
	public int getMaxIndex() {
		return maxIndex;
	}
	
	public boolean containsKey(Object arg0) {
		return this.keyIndexsMap.containsKey(arg0);
	}
	
	public Set<Map.Entry<K, Integer>> entrySet() {
		return this.keyIndexsMap.entrySet();
	}
	
	public Set<K> keySet() {
		return this.keyIndexsMap.keySet();
	}
}
