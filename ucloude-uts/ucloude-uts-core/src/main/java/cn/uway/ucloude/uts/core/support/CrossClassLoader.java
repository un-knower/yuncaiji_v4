package cn.uway.ucloude.uts.core.support;

import java.lang.reflect.Field;
import java.util.Vector;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 用来处理跨classLoader 共享class
 * @author uway
 *
 */
public class CrossClassLoader {
	private static final ILogger LOGGER = LoggerManager.getLogger(CrossClassLoader.class);

    private static Field classes;
    private static final Object LOCK = new Object();

    static {
        try {
            classes = ClassLoader.class.getDeclaredField("classes");
            classes.setAccessible(true);
        } catch (Throwable e) {
            LOGGER.error("get ClassLoader 'classes' Field Error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Class loadClass(String classname) throws ClassNotFoundException {

        if (classes == null) {
            return Thread.currentThread().getContextClassLoader().loadClass(classname);
        }

        try {
            synchronized (LOCK) {
                Vector v = (Vector) classes.get(CrossClassLoader.class.getClassLoader().getParent());
                for (int i = 0; i < v.size(); i++) {
                    Class o = (Class) v.get(i);
                    if (classname.equals(o.getName())) {
                        return o;
                    }
                }
                Class clazz = CrossClassLoader.class.getClassLoader().loadClass(classname);
                v.add(clazz);
                return clazz;
            }
        } catch (Exception e) {
            throw new ClassNotFoundException("load " + classname + " Error ", e);
        }
    }
}
