package cn.uway.usummary.entity;

import cn.uway.usummary.cache.store.Store;

public class ElementIndex {

	// 位置 在内存中为0.在文件中为1
	private byte location;

	// Element的名字
	private String elementName;

	// 如果Element存储在文件中 则store即为所在的MemoryMappintDiskStore
	private Store store;

	public byte getLocation() {
		return location;
	}

	public void setLocation(byte location) {
		this.location = location;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}
}
