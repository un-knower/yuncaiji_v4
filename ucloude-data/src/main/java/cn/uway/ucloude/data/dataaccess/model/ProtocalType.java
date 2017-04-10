package cn.uway.ucloude.data.dataaccess.model;

public enum ProtocalType {
	DB(0),
	FTP(1),
	Memory(2),
	File(3),
	Http(4);
	
	private int value;
	private ProtocalType(int value){
		this.setValue(value);
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
