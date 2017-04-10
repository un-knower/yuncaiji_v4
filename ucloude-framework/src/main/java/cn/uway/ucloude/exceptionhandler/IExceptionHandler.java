package cn.uway.ucloude.exceptionhandler;

import java.util.UUID;

public interface IExceptionHandler {
	Exception handleException(Exception exception, UUID handlingInstanceID);
}
