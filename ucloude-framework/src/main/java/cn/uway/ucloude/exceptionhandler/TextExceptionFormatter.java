package cn.uway.ucloude.exceptionhandler;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TextExceptionFormatter extends ExceptionFormatter {

	
	protected TextExceptionFormatter(Exception exception, UUID handlingInstanceId) {
		super(exception, handlingInstanceId);
		// TODO Auto-generated constructor stub
	}

	public TextExceptionFormatter(StringWriter writer, Exception ex) {
		// TODO Auto-generated constructor stub
		this(writer, ex, UUID.randomUUID());
	}
	
	
	public TextExceptionFormatter(StringWriter writer, Exception ex, UUID handlingInstanceId) {
		// TODO Auto-generated constructor stub
		super(ex, handlingInstanceId);
		
	}


	@Override
	protected void writeDescription() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeDateTime(Date utcNow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeExceptionType(Class<?> exceptionType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeSource(Throwable source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeHelpLink(String helpLink) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeStackTrace(StackTraceElement[] stackTrace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeFieldInfo(Field field, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeAdditionalInfo(Map<String, Object> additionalInformation) {
		// TODO Auto-generated method stub
		
	}

}
