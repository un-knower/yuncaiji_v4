package cn.uway.ucloude.exceptionhandler;

public class ExceptionPolicy {
	private static final ExceptionPolicyFactory defaultFactory = new ExceptionPolicyFactory();

	public static boolean handleException(Exception exceptionToHandle, String policyName) {

		return handleException(exceptionToHandle, policyName, defaultFactory);
	}

	private static boolean handleException(Exception exceptionToHandle, String policyName,
			ExceptionPolicyFactory policyFactory) {
		ExceptionPolicyImpl policy = getExceptionPolicy(exceptionToHandle, policyName, policyFactory);
		try {
			return policy.handleException(exceptionToHandle);
		} catch (Exception t) {
			return true;
		}
	}

	private static ExceptionPolicyImpl getExceptionPolicy(Exception exceptionToHandle,String policyName, ExceptionPolicyFactory policyFactory){
		return null;
//		 try
//         {
//             //return EnterpriseLibraryContainer.Current.GetInstance<ExceptionPolicyImpl>(policyName);
//         }
//         catch (ActivationException configurationException)
//         {
//             try
//             {
//                 DefaultExceptionHandlingEventLogger logger = EnterpriseLibraryContainer.Current.GetInstance<DefaultExceptionHandlingEventLogger>();
//                 logger.LogConfigurationError(configurationException, policyName);
//             }
//             catch(Exception ex) { }
//
//             throw configurationException
//         }
//         catch (Exception ex)
//         {
//             try
//             {
//                 string exceptionMessage = ExceptionUtility.FormatExceptionHandlingExceptionMessage(policyName, ex, null, exception);
//
//                 DefaultExceptionHandlingEventLogger logger = EnterpriseLibraryContainer.Current.GetInstance<DefaultExceptionHandlingEventLogger>();
//                 logger.LogInternalError(policyName, exceptionMessage);
//             }
//             catch(Exception) { }
//
//             throw new ExceptionHandlingException(ex.Message, ex);
//         }
	}
}
