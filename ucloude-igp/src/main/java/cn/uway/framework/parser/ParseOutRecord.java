package cn.uway.framework.parser;

import java.io.Serializable;
import java.util.Map;

/**
 * 解析输出记录
 * 
 * @author MikeYang
 * @Date 2012-10-30
 * @version 1.0
 * @since 3.0
 */
public class ParseOutRecord implements Serializable {
	/**
	 * 默认数据类型
	 */
	public transient static final int DEFAULT_DATA_TYPE = -100; 
	
	private static final long serialVersionUID = 7306441924327195014L;

	private transient int type = DEFAULT_DATA_TYPE; // 记录类型,不同的场景各自赋值取值，默认值为-100

	private transient Map<String, String> record; // 具体记录,采取键值对的方式存储 <记录字段名,字段取值>

	/**
	 * 构造方法
	 */
	public ParseOutRecord() {
		super();
	}

	/**
	 * 获取输出对象的类型
	 */
	public int getType() {
		return type;
	}

	/**
	 * 设置输出对象的类型
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * 获取解码输出记录
	 */
	public Map<String, String> getRecord() {
		return record;
	}

	public void setRecord(Map<String, String> record) {
		this.record = record;
	}

	//重写writeObject、readObject有助于节省序列化时使用的内存，因为默认的序列化会将变量名称也写进去了。
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
    	s.defaultWriteObject();
    	// type的串行化，由BockData存储，这里就不重复存储
    	s.writeObject(record);
    	
    }

    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    	s.defaultReadObject();
    	// type的串行化，由BockData设置进来
    	record = ((Map<String, String>)s.readObject());
    }
}
