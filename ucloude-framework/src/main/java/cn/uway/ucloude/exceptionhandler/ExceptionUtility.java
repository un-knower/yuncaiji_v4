package cn.uway.ucloude.exceptionhandler;

import java.io.StringWriter;
import java.util.UUID;

public class ExceptionUtility {

    private  static String HandlingInstanceToken = "%s";    
    
	public static String formatExceptionMessage(String message, UUID handlingInstanceId) throws Throwable
    {
        if(message == null) throw new IllegalArgumentException(message);
        return message.replace(HandlingInstanceToken, handlingInstanceId.toString());
    }
	
	
	public static String formatExceptionHandlingExceptionMessage(String policyName, Exception offendingException, Exception chainException, Exception originalException) throws Exception
    {
        if(policyName == null) throw new IllegalArgumentException("policyName");

        StringBuilder message = new StringBuilder();
        StringWriter writer = null;
		String result = null;
        try
        {
            writer = new StringWriter();
            writer.append(message);
            if (policyName.length() > 0)
            {
            	writer.append("\r\n");
                writer.append(("Policy:"+ policyName));
            }

            formatHandlingException(writer, "", offendingException);
            formatHandlingException(writer, "", originalException);
            formatHandlingException(writer, "", chainException);
        }
        finally
        {
            if (writer != null)
            {
				result = writer.toString();
                writer.close();
            }
        }

		return result;
    }

    private static void formatHandlingException(StringWriter writer, String header, Exception ex)
    {
        if (ex != null)
        {
            writer.append("\r\n");
            writer.append(header);
            writer.append("\r/n");
            
            TextExceptionFormatter formatter = new TextExceptionFormatter(writer, ex);
            formatter.format();
        }
    }
	
}
