package cn.uway.usummary.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BlockData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// 记录链表
	private transient List<Map<String,String>> data;

	public BlockData(List<Map<String,String>> data) {
		super();
		this.data = data;
	}

	public List<Map<String,String>> getData() {
		return data;
	}
	
	// 重写writeObject、readObject有助于节省序列化时使用的内存，因为默认的序列化会将变量名称也写进去了。
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
    	s.defaultWriteObject();
    	int nSize = -1;
    	if (data != null) {
    		nSize = data.size();
    	}
    	s.writeInt(nSize);
    	
    	if (data == null) 
    		return;
    		
    	for (Map<String,String> record: data){
    		s.writeObject(record);
    	}
    }
    
    // 在read完成后，还需要调用processOnAfterSerialRead方法，将keyIndexMap设置回去
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    	s.defaultReadObject();
    	
    	int nSize = s.readInt();
    	if (nSize <0)
    		return;
    	
    	data = new ArrayList<Map<String,String>>();
    	for (int i=0; i<nSize; ++i) {
    		Map<String,String> record = (Map<String,String>)s.readObject();
    		if (record != null) {
    			data.add(record);
    		}
    	}
    }
}
