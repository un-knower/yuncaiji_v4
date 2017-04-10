package cn.uway.ucloude.uts.core.domain;

public enum JobType {
    REAL_TIME(0, "实时"),
    TRIGGER_TIME(1, "定时"),
    CRON(2,"周期"),
    REPEAT(3,"重复");
    private final int value;
	public int getValue() {
		return value;
	}
	public String getText() {
		return text;
	}
	private final String text;
    JobType(int value,String text){
    	this.value = value;
    	this.text = text;
    }
    
    public static JobType getJobType(int value){
    	JobType[] jobTypes = JobType.values();
    	for(JobType item :jobTypes){
    		if(item.getValue() == value)
    			return item;
    	}
    	return null;
    }
}
