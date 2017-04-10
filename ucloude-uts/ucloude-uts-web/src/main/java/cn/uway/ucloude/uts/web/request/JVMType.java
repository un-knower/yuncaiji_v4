package cn.uway.ucloude.uts.web.request;

public enum JVMType {

    GC(0, "回收机制"),
    MEMORY(1,"内存"),
    THREAD(3,"线程信息");
    
    private final int value;
	public int getValue() {
		return value;
	}
	public String getText() {
		return text;
	}
	private final String text;
    JVMType(int value,String text){
    	this.value = value;
    	this.text = text;
    }
    
    public static JVMType getJVMType(int value){
    	JVMType[] jvms = JVMType.values();
    	for(JVMType jvm:jvms){
    		if(jvm.getValue() == value){
    			return jvm;
    		}
    	}
    	return null;
    }
}
