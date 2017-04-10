package cn.uway.ucloude.uts.core.queue.domain;


/**
 * 任务扩展参数
 * @author Uway-M3
 */
public class ExtParamsInfo {
	private String id;
	private int typeId;
	private String key;
	private String value;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
