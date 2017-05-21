package cn.uway.ucloude.uts.spring.tasktracker;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TaskTrackerApplicationContextRegister implements ApplicationContextAware {

	private static ApplicationContext context = null;  
	  
    @Override  
    public void setApplicationContext(ApplicationContext applicationContext)  
            throws BeansException {  
        this.context = applicationContext;  
    }  
    
    public static Object getBean(String name) {
		return context.getBean(name);
	}
	
	/**
	 * 通过beanName和class获取Bean
	 * 
	 * @param <T>
	 * @param beanName
	 * @param clazz
	 * @return Bean
	 */
	public static <T> T getBean(String beanName, Class<T> clazz) {
		return (T) context.getBean(beanName, clazz);
	}

	/**
	 * 获取该类型所有的bean
	 * 
	 * @param clazz
	 * @return beanNames of giving class type
	 */
	public static String[] getBeanNamesForType(Class<?> clazz) {
		return context.getBeanNamesForType(clazz);
	}
}
