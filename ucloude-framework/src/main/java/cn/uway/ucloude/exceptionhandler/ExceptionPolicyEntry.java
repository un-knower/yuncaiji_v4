package cn.uway.ucloude.exceptionhandler;

import java.util.Collection;
import java.util.UUID;

import cn.uway.ucloude.exceptionhandler.Instrumentation.ExceptionHandlingInstrumentationProvider;

final class ExceptionPolicyEntry {
	
	public ExceptionPolicyEntry(PostHandlingAction postHandlingAction, Collection<IExceptionHandler> handlers){
		this.policyName = policyName;
		this.handlers = handlers;
	}
	
	protected void setPolicyName(String policyName) {
		this.policyName = policyName;
		
	}
	
	private PostHandlingAction postHandlingAction;
	private Collection<IExceptionHandler> handlers;
	private String policyName;
	private ExceptionHandlingInstrumentationProvider instrumentationProvider;
	
	public boolean handle(Exception exceptionToHandle) throws Exception{
		UUID handlingInstanceID = UUID.randomUUID();
		Exception chainException = executeHandlerChain(exceptionToHandle,handlingInstanceID);
		if(instrumentationProvider != null) {
			
		}
		
		return rethrowRecommend(chainException,exceptionToHandle);
	}
	
	private Exception executeHandlerChain(Exception ex, UUID handlingInstanceID) throws Exception{
		String lastHandlerName = "";
		Exception originalException = ex;
		try{
			for(IExceptionHandler handler:handlers){
				lastHandlerName = handler.getClass().getName();
				ex = handler.handleException(ex, handlingInstanceID);
				if(instrumentationProvider != null) {
					
				}
			}
		}
		catch(Exception handlingException){
			throw handlingException;
		}
		
		return ex;
	}
	
	private boolean rethrowRecommend(Exception chainException, Exception originalException) throws Exception{
		if (postHandlingAction == PostHandlingAction.None) return false;

        if (postHandlingAction == PostHandlingAction.ThrowNewException)
        {
            throw intentionalRethrow(chainException, originalException);
        }
        return true;
	}
	
	 private Exception intentionalRethrow(Exception chainException, Exception originalException) throws Exception
     {
         if (chainException != null)
         {
             throw chainException;
         }

         Exception wrappedException = new ExceptionHandlingException("Exception Null Exception");
         
         instrumentationProvider.fireExceptionHandlingErrorOccurred(
             ExceptionUtility.formatExceptionHandlingExceptionMessage(policyName, wrappedException, chainException, originalException));

         return wrappedException;
     }
}
