package cn.uway.framework.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * AppContext 提供系统运行spring上下文 单例的实现
 * 
 * @author chenrongqiang 2012-11-11
 */
public class AppContext {

	// Spring 上下文
	private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("igpApplicationContext.xml");

	/**
	 * 私有化构造 外部无法创建该实例
	 */
	private AppContext() {
		super();
	}

	/**
	 * 将context暴露给外部调用
	 * 
	 * @return
	 */
	public static ApplicationContext getInstance() {
		return applicationContext;
	}

	/**
	 * 通过beanName获取Bean 客户端需自己实现类型转换
	 * 
	 * @param beanName
	 * @return Bean
	 */
	public static Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
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
		return (T) applicationContext.getBean(beanName, clazz);
	}

	/**
	 * 获取该类型所有的bean
	 * 
	 * @param clazz
	 * @return beanNames of giving class type
	 */
	public static String[] getBeanNamesForType(Class<?> clazz) {
		return applicationContext.getBeanNamesForType(clazz);
	}
}
