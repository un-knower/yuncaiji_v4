package cn.uway.ucloude.utils;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *<pre>
 *		ArrayMap的实现原理：
 *			1、用一个Map<键值key, 键值索引keyIndex>的方式，记录每个key对应的value在数组中存储的索引index;
 *			2、用一个数组存储所有的value值。
 *			3、put和get根据key对应的索引位置，在数组elementData中添加或获取数据的value；
 *			4、用一个size来记录，实际存储在数组elementData中的有效值的个数；
 *
 *		构造模式：
 *			1、不从外部传入keyIndex的Map, 那么ArrayMap会自动创建一个keyIndex的map,
 *			调用者在构建完第一个对象后，可以通过getKeyIndexMap来获取由ArrayMap自动创建的keyIndex map，
 *			以便其它数据集共享使用
 *
 *			2、从外部传入keyIndex 存储结构map,则ArrayMap有两个选项：
 *			(1)、不忽略不在keyIndex map中的键值put操作，
 *					此种模式下，如果通过put对应的key在keyIndex map中不存在的情况下，
 *					会将这些键值对放到一个临时的mapAdditionKeyValues中，以便调用者稍后，可以通过get方法获取到对应的value
 *			(2)、忽略不在keyIndex map中的键值put操作，
 *					此种模式下，如果通过put对应的key在keyIndex map中不存在的情况下，
 *					会直接忽略put或putall操作，调用者以后，也无法能通过get方法获取不在keyIndex map中的键值对应的value
 *
 *</pre>
 * 
 * @ 2014-4-11
 * @param <K> 主键
 * @param <V> 值
 */
public class ArrayMap<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = -3658595166035089217L;
	
	// 在array中有效的存储的个数
	protected transient int size = 0;
	
	// value数据组
	protected transient Object elementData[];
	
	// value Key值对应在数组中存放的顺序(注意:keyIndexsMap暂将不被序列化)
	protected transient ArrayMapKeyIndex<K> keyIndexsMap;
	
	// 用于存储在非数组内的串
	protected transient Map<K, V> mapAdditionKeyValues = null;
	
	// 是否是创建Map模式
	private transient boolean createKeyIndexMapMode;
	
	// 是否忽略不在keyIndexsMap里面的put
	private transient boolean ignorePutOperationOnKeyNotExistKeyIndexMap;
	
	public ArrayMap() {
		this(null, true);
	}
	
	/**
	 * @param keyIndexMap 主键对应的存储位置索引表
	 * @param ignorePutOperationOnKeyNotExistKeyIndexMap 是否忽略不在keyIndexMap里的主键put或putAll操作(true:忽略 false:不忽略)
	 */
	public ArrayMap(ArrayMapKeyIndex<K> keyIndexMap, boolean ignorePutOperationOnKeyNotExistKeyIndexMap) {
		this(keyIndexMap, ignorePutOperationOnKeyNotExistKeyIndexMap, 10);
	}
	
	/**
	 * @param keyIndexMap 主键对应的存储位置索引表
	 * @param ignorePutOperationOnKeyNotExistKeyIndexMap 是否忽略不在keyIndexMap里的主键put或putAll操作(true:忽略 false:不忽略)
	 */
	public ArrayMap(ArrayMapKeyIndex<K> keyIndexMap, boolean ignorePutOperationOnKeyNotExistKeyIndexMap, int initialCapacity) {
		if (keyIndexMap == null) {
			elementData = new Object[initialCapacity];
			this.keyIndexsMap = new ArrayMapKeyIndex<K>();
			this.createKeyIndexMapMode = true;
		}
		else {
			this.keyIndexsMap = keyIndexMap;
			elementData = new Object[keyIndexMap.getMaxIndex()];
		}
		this.ignorePutOperationOnKeyNotExistKeyIndexMap = ignorePutOperationOnKeyNotExistKeyIndexMap;
	}

	@Override
	public void clear() {
		for (int i=0; i<elementData.length; ++i) {
			elementData[i] = null;
		}
		
		if (mapAdditionKeyValues != null) {
			mapAdditionKeyValues.clear();
		}
		
		size = 0;
	}

	@Override
	public boolean containsKey(Object arg0) {
		if (keyIndexsMap.containsKey(arg0))
			return true;
		
		if (mapAdditionKeyValues != null) {
			return mapAdditionKeyValues.containsKey(arg0);
		}
		
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		if (arg0 == null)
			return false;
		
		for (int i=0; i<elementData.length; ++i) {
			if (elementData[i] != null && arg0.equals(elementData[i])) {
				return true;
			}
		}
		
		if (mapAdditionKeyValues != null)
			return mapAdditionKeyValues.containsValue(arg0);
		
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<Entry<K, V>> entrySet = new HashSet<Entry<K, V>>();
		for (Entry<K, Integer> indexEntry : this.keyIndexsMap.entrySet()) {
			K key = indexEntry.getKey();
			Integer index =  indexEntry.getValue();
			
			//keyIndexsMap采用自动索引方式后,索引有可能大于当前实际存储个数
			if (index >= elementData.length || index < 0)
				continue;
			
			boundsCheck(index);
			
			if (elementData[index] != null) {
				Entry<K, V> entry =  new AbstractMap.SimpleEntry<K, V>(key, (V)elementData[index]);
				entrySet.add(entry);
			}
		}
		
		if (mapAdditionKeyValues != null) {
			entrySet.addAll(mapAdditionKeyValues.entrySet());
		}
		
		return entrySet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object arg0) {
		Integer index = keyIndexsMap.getKeyIndex((K)arg0, false);
		if (index != null && index>=0) {
			//keyIndexsMap采用自动索引方式后,索引有可能大于当前实际存储个数
			if (index >= elementData.length)
				return null;
			
			boundsCheck(index);
			return ((V) elementData[index]);
		}
		else if (mapAdditionKeyValues != null) {
			return mapAdditionKeyValues.get(arg0);
		}
		
		return null;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Set<K> keySet() {
		Set<K> keySets = new HashSet<K>();
		for (Entry<K, Integer> indexEntry : this.keyIndexsMap.entrySet()) {
			K key = indexEntry.getKey();
			Integer index =  indexEntry.getValue();
			
			if (index >= elementData.length || index < 0)
				continue;
			
			boundsCheck(index);
			
			if (elementData[index] != null) {
				keySets.add(key);
			}
		}
		
		if (mapAdditionKeyValues != null) {
			keySets.addAll(mapAdditionKeyValues.keySet()); 
		}
		
		return keySets;
	}

	@Override
	public V put(K arg0, V arg1) {
		Integer index = keyIndexsMap.getKeyIndex(arg0, true);
		if (index != null) {
			//boundsCheck(index);
			ensureCapacity(index);
			// 如果不存在，size才加一个
			if (elementData[index] == null && arg1 != null) {
				++size;
			}
			
			elementData[index] = arg1;
		}
		else if (createKeyIndexMapMode) {
			keyIndexsMap.addKey(arg0);
			index = keyIndexsMap.getKeyIndex(arg0, true);
			
			ensureCapacity(index);
			elementData[index] = arg1;
			++size;
		}
		else if (!ignorePutOperationOnKeyNotExistKeyIndexMap) {
			if (mapAdditionKeyValues == null) {
				mapAdditionKeyValues = new HashMap<K, V>();
			}
			
			mapAdditionKeyValues.put(arg0, arg1);
		}
		
		return arg1;
	}
	
	/**
	 * 根据index值，设置对应数组位置的值，如果index越界，则数据自动扩容
	 * @param index	位置
	 * @param arg1	值
	 * @return
	 */
	public boolean set(Integer index, V arg1) {
		if (index != null) {
			//boundsCheck(index);
			ensureCapacity(index);
			// 如果不存在，size才加一个
			if (elementData[index] == null && arg1 != null) {
				++size;
			}
			
			elementData[index] = arg1;
			return true;
		}
		
		return false;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		for (java.util.Map.Entry<? extends K, ? extends V> entry : arg0.entrySet()){
			K key = entry.getKey();
			V value = entry.getValue();
			
			this.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object arg0) {
		Integer index = keyIndexsMap.getKeyIndex((K)arg0, false);
		if (index != null && index>=0) {
			//keyIndexsMap采用自动索引方式后,索引有可能大于当前实际存储个数
			if (index >= elementData.length)
				return null;
			
			boundsCheck(index);
			V v = (V)elementData[index];
			if (v != null) {
				elementData[index] = null;
				--size;
			}
			
			return v;
		}
		else if (mapAdditionKeyValues != null) {
			return mapAdditionKeyValues.remove(arg0);
		}
		
		return null;
	}

	@Override
	public int size() {
		return this.size + (mapAdditionKeyValues==null?0:mapAdditionKeyValues.size());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<V> values() {
		ArrayList<V> valList = new ArrayList<V>(size());
		if (mapAdditionKeyValues != null) {
			valList.addAll(mapAdditionKeyValues.values());
		}
		
		for (int i=0; i<elementData.length; ++i) {
			if (elementData[i] != null) {
				valList.add((V)elementData[i]);
			}
		}
		
		return valList;
	}
	
	/**
	 * 检测index是否在可读写边界
	 * @param index 索引值
	 * @throws IndexOutOfBoundsException
	 */
	public void boundsCheck(int index) throws IndexOutOfBoundsException {
		if (index<0 || index>=elementData.length) {
			throw new IndexOutOfBoundsException("out of bound. index=" + index);
		}
	}
	
	/**
	 *  确保当前的list可以存储得下index索引
	 * @param index 索引值
	 */
    public void ensureCapacity(int index) {
    	if (index < 0)
    		throw new IndexOutOfBoundsException("out of bound. index=" + index);
    	
		if (index >= elementData.length) {
		    int newCapacity = (elementData.length * 3)/2 + 1;
		    if (newCapacity <= index) {
		    	newCapacity = index+1;
		    }
		    
	       // minCapacity is usually close to size, so this is a win:
	       elementData = Arrays.copyOf(elementData, newCapacity);
		}
    }
    
    public Map<K, V> getMapAdditionKeyValues() {
		return mapAdditionKeyValues;
	}

	public void setMapAdditionKeyValues(Map<K, V> mapAdditionKeyValues) {
		this.mapAdditionKeyValues = mapAdditionKeyValues;
	}
	
	public ArrayMapKeyIndex<K> getKeyIndexsMap() {
		return keyIndexsMap;
	}
	
	public void setKeyIndexsMap(ArrayMapKeyIndex<K> keyIndexsMap) {
		this.keyIndexsMap = keyIndexsMap;
	}
      
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
    	s.defaultWriteObject();
    	s.writeInt(this.size);
    	s.writeBoolean(this.createKeyIndexMapMode);
    	s.writeBoolean(this.ignorePutOperationOnKeyNotExistKeyIndexMap);
        
    	// Write out array length(elementData的个数取决于keyIndexsMap个数)
    	int length = this.elementData.length;
//    	if (this.keyIndexsMap != null && length > this.keyIndexsMap.getMaxIndex()) {
//    		length = this.keyIndexsMap.getMaxIndex();
//    	}
        s.writeInt(length);
		// Write out all elements in the proper order.
		for (int i=0; i<length; i++) {
			s.writeObject(this.elementData[i]);
		}
    }

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		s.defaultReadObject();
		this.size = s.readInt();
		this.createKeyIndexMapMode = s.readBoolean();
		this.ignorePutOperationOnKeyNotExistKeyIndexMap = s.readBoolean();
		
        // Read in array length and allocate array
        int length = s.readInt();
        this.elementData = new Object[length];

		// Read in all elements in the proper order.
		for (int i=0; i<length; i++) {
			this.elementData[i] = s.readObject();
		}
    }
}
