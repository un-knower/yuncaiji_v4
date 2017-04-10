package cn.uway.usummary.entity;

public class USummaryConfInfo {
	
	private Long sqlNum;
	
	private String sql;
	
	private int isPlaceholder;
	
	private int operationType;
	
	private int storageType;
	
	private int groupId;
	
	private int isUsed;

	public Long getSqlNum() {
		return sqlNum;
	}

	public void setSqlNum(Long sqlNum) {
		this.sqlNum = sqlNum;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int getIsPlaceholder() {
		return isPlaceholder;
	}

	public void setIsPlaceholder(int isPlaceholder) {
		this.isPlaceholder = isPlaceholder;
	}

	public int getOperationType() {
		return operationType;
	}

	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}

	public int getStorageType() {
		return storageType;
	}

	public void setStorageType(int storageType) {
		this.storageType = storageType;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(int isUsed) {
		this.isUsed = isUsed;
	}
	
}
