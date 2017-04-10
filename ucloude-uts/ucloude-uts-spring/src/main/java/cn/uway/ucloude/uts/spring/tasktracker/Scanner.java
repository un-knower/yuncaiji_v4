package cn.uway.ucloude.uts.spring.tasktracker;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 10/20/15.
 */
public class Scanner implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor {

    private static final ILogger LOGGER = LoggerManager.getLogger(Scanner.class);

    private String[] annotationPackages;

    public void setBasePackage(String annotationPackage) {
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
                : Pattern.compile("\\s*[,]+\\s*").split(annotationPackage);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = Class.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
                Object scanner = scannerClass.getConstructor(new Class<?>[]{BeanDefinitionRegistry.class, boolean.class}).newInstance(beanFactory, true);
                // add filter
                Class<?> filterClass = Class.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
                Object filter = filterClass.getConstructor(Class.class).newInstance(UTS.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter", Class.forName("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                Method scan = scannerClass.getMethod("scan", String[].class);
                scan.invoke(scanner, new Object[]{annotationPackages});
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {

        Class<?> clazz = bean.getClass();

        if (!isMatchPackage(clazz)) {
            return bean;
        }

        if (!clazz.isAnnotationPresent(UTS.class)) {
            return bean;
        }

        JobRunnerHolder.addUTSBean(bean);

        return bean;
    }

    @Override
    public void destroy() throws Exception {

    }

    private boolean isMatchPackage(Class<?> clazz) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = clazz.getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
