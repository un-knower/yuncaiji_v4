package cn.uway.ucloude.resolver;



import java.beans.PropertyDescriptor;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;
import cn.uway.ucloude.configuration.auto.PropertiesConfigurationResolveException;

/**
 * 类类型转换
 * @author magic.s.g.xie
 */
public class ClassResolver extends AbstractResolver {

    public static final ClassResolver INSTANCE = new ClassResolver();

    @Override
    public void resolve(final AutoConfigContext context, final PropertyDescriptor descriptor, Class<?> propertyType) {

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.equals(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                try {
                    Class clazz = Class.forName(value);
                    writeProperty(context, descriptor, clazz);
                } catch (ClassNotFoundException e) {
                    throw new PropertiesConfigurationResolveException(e);
                }
                return false;
            }
        });

    }
}
