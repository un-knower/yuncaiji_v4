package cn.uway.ucloude.exceptionhandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;

public abstract class ExceptionFormatter {
	@SuppressWarnings("rawtypes")
	private static final ArrayList IgnoredProperties = new ArrayList();

        private final UUID handlingInstanceId;
        private final Exception exception;
        private Map<String, Object> additionalInfo;

        /// <summary>
        /// Initializes a new instance of the <see cref="ExceptionFormatter"/> class with an <see cref="Exception"/> to format.
        /// </summary>
        /// <param name="exception">The <see cref="Exception"/> object to format.</param>
        /// <param name="handlingInstanceId">The id of the handling chain.</param>
        protected ExceptionFormatter(Exception exception, UUID handlingInstanceId)
        {
            if (exception == null) throw new java.lang.IllegalArgumentException(("exception"));

            this.exception = exception;
            this.handlingInstanceId = handlingInstanceId;
        }



        /// <summary>
        /// Gets the id of the handling chain requesting a formatting.
        /// </summary>
        /// <value>
        /// The id of the handling chain requesting a formatting, or <see cref="Guid.Empty"/> if no such id is available.
        /// </value>
        public Exception getException() {
			return exception;
		}




  



		public void setAdditionalInfo(Map additionalInfo) {
			this.additionalInfo = additionalInfo;
		}



		public static ArrayList getIgnoredproperties() {
			return IgnoredProperties;
		}



		public UUID getHandlingInstanceId() {
			return handlingInstanceId;
		}




		public Map<String, Object> getAdditionalInfo()
        {
            if (this.additionalInfo == null)
            {
                this.additionalInfo = new HashMap<String, Object>();
                this.additionalInfo.put("MachineName", getMachineName());
                this.additionalInfo.put("TimeStamp", "");
                this.additionalInfo.put("FullName", "");
                this.additionalInfo.put("AppDomainName", "");
                this.additionalInfo.put("ThreadIdentity", "");
                this.additionalInfo.put("WindowsIdentity", getIdentity());
            }

            return this.additionalInfo;
        
        }

        /// <summary>
        /// Formats the <see cref="Exception"/> into the underlying stream.
        /// </summary>
        public void format()
        {
            writeDescription();
            writeDateTime(new Date());
            writeException(this.exception, null);
        }

        /// <summary>
        /// Formats the exception and all nested inner exceptions.
        /// </summary>
        /// <param name="exceptionToFormat">The exception to format.</param>
        /// <param name="outerException">The outer exception. This 
        /// value will be null when writing the outer-most exception.</param>
        /// <remarks>
        /// <para>This method calls itself recursively until it reaches
        /// an exception that does not have an inner exception.</para>
        /// <para>
        /// This is a template method which calls the following
        /// methods in order
        /// <list type="number">
        /// <item>
        /// <description><see cref="WriteExceptionType"/></description>
        /// </item>
        /// <item>
        /// <description><see cref="WriteMessage"/></description>
        /// </item>
        /// <item>
        /// <description><see cref="WriteSource"/></description>
        /// </item>
        /// <item>
        /// <description><see cref="WriteHelpLink"/></description>
        /// </item>
        /// <item>
        /// <description><see cref="WriteReflectionInfo"/></description>
        /// </item>
        /// <item>
        /// <description><see cref="WriteStackTrace"/></description>
        /// </item>
        /// <item>
        /// <description>If the specified exception has an inner exception
        /// then it makes a recursive call. <see cref="WriteException"/></description>
        /// </item>
        /// </list>
        /// </para>
        /// </remarks>
        protected void writeException(Throwable exceptionToFormat, Throwable outerException)
        {
            if (exceptionToFormat == null) throw new NullPointerException("exceptionToFormat");

            this.writeExceptionType(exceptionToFormat.getClass());
            this.writeMessage(exceptionToFormat.getMessage());
            this.writeSource(exceptionToFormat.getCause());
            this.writeHelpLink(exceptionToFormat.getLocalizedMessage());
            this.writeReflectionInfo(exceptionToFormat);
            this.writeStackTrace(exceptionToFormat.getStackTrace());

            // We only want additional information on the top most exception
            if (outerException == null)
            {
                this.writeAdditionalInfo(this.getAdditionalInfo());
            }

            Throwable inner = exceptionToFormat.getCause();

            if (inner != null)
            {
                // recursive call
                this.writeException(inner, exceptionToFormat);
            }
        }

        /// <summary>
        /// Formats an <see cref="Exception"/> using reflection to get the information.
        /// </summary>
        /// <param name="exceptionToFormat">
        /// The <see cref="Exception"/> to be formatted.
        /// </param>
        /// <remarks>
        /// <para>This method reflects over the public, instance properties 
        /// and public, instance fields
        /// of the specified exception and prints them to the formatter.  
        /// Certain property names are ignored
        /// because they are handled explicitly in other places.</para>
        /// </remarks>
        protected void writeReflectionInfo(Throwable exceptionToFormat)
        {
            if (exceptionToFormat == null) throw new NullPointerException("exceptionToFormat");

            Class<?> type = exceptionToFormat.getClass();
            Field[] fields = type.getFields();
            
            
            Object value;

           
            for (Field field:fields)
            {
                try
                {
                    value = field.get(exceptionToFormat);
                }
                catch (IllegalAccessException ex)
                {
                    value = "Field not allowed get value";
                }
                writeFieldInfo(field, value);
            }
        }

        /// <summary>
        /// When overridden by a class, writes a description of the caught exception.
        /// </summary>
        protected abstract void writeDescription();

        /// <summary>
        /// When overridden by a class, writes the current time.
        /// </summary>
        /// <param name="utcNow">The current time.</param>
        protected abstract void writeDateTime(Date utcNow);

        /// <summary>
        /// When overridden by a class, writes the <see cref="Type"/> of the current exception.
        /// </summary>
        /// <param name="exceptionType">The <see cref="Type"/> of the exception.</param>
        protected abstract void writeExceptionType(Class<?> exceptionType);

        /// <summary>
        /// When overridden by a class, writes the <see cref="System.Exception.Message"/>.
        /// </summary>
        /// <param name="message">The message to write.</param>
        protected abstract void writeMessage(String message);

        /// <summary>
        /// When overridden by a class, writes the value of the <see cref="System.Exception.Source"/> property.
        /// </summary>
        /// <param name="source">The source of the exception.</param>
        protected abstract void writeSource(Throwable source);

        /// <summary>
        /// When overridden by a class, writes the value of the <see cref="System.Exception.HelpLink"/> property.
        /// </summary>
        /// <param name="helpLink">The help link for the exception.</param>
        protected abstract void writeHelpLink(String helpLink);

        /// <summary>
        /// When overridden by a class, writes the value of the <see cref="System.Exception.StackTrace"/> property.
        /// </summary>
        /// <param name="stackTrace">The stack trace of the exception.</param>
        protected abstract void writeStackTrace(StackTraceElement[] stackTrace);

        
        /// <summary>
        /// When overridden by a class, writes the value of a <see cref="FieldInfo"/> object.
        /// </summary>
        /// <param name="fieldInfo">The reflected <see cref="FieldInfo"/> object.</param>
        /// <param name="value">The value of the <see cref="FieldInfo"/> object.</param>
        protected abstract void writeFieldInfo(Field field, Object value);

        /// <summary>
        /// When overridden by a class, writes additional properties if available.
        /// </summary>
        /// <param name="additionalInformation">Additional information to be included with the exception report</param>
        protected abstract void writeAdditionalInfo(Map<String,Object> additionalInformation);

        private static String getMachineName()
        {
            String machineName = "";
            
            return machineName;
        }

        private static String getIdentity()
        {
            String windowsIdentity ="";
           

            return windowsIdentity;
        }
}
