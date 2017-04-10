package cn.uway.ucloude.exceptionhandler;

abstract class ExceptionManager {
	public abstract boolean handleException(Exception exceptionToHandle, String policyName, Exception exceptionToThrow);
	
    public abstract boolean HandleException(Exception exceptionToHandle, String policyName);
    
    
}
