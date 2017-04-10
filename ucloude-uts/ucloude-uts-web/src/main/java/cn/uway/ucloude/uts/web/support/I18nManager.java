package cn.uway.ucloude.uts.web.support;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class I18nManager {
	private static final ILogger logger = LoggerManager.getLogger(I18nManager.class);

    private static MessageSource messageSource;

    @Autowired
    public I18nManager(MessageSource messageSource) {
        I18nManager.messageSource = messageSource;
    }

    public static String getMessage(String messageKey) {
        return resolveMessage(null, messageKey, null);
    }

    public static String getMessage(String messageKey, Object... args) {
        return resolveMessage(null, messageKey, args);
    }

    public static String getMessage(Locale locale, String messageKey, Object... args) {
        return resolveMessage(locale, messageKey, args);
    }

    private static String resolveMessage(Locale locale, String key, Object[] args) {
        if (locale == null) {
            locale = new Locale("zh_CN");
        }
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Throwable t) {
            logger.error("i18n error, message not found by key :" + key, t);
            return key;
        }
    }
}
