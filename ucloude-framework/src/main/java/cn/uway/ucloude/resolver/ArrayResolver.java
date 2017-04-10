package cn.uway.ucloude.resolver;



import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;
import cn.uway.ucloude.configuration.auto.PropertiesConfigurationResolveException;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.PrimitiveTypeUtils;

/**
 * 数组类型转换
 * @author magic.s.g.xie
 */
public class ArrayResolver extends AbstractResolver {

    public static final ArrayResolver INSTANCE = new ArrayResolver();

    @Override
    public void resolve(AutoConfigContext context, PropertyDescriptor descriptor, Class<?> propertyType) {
        Type componentType = propertyType.getComponentType();
        Class componentClass = propertyType.getComponentType();

        final Map<String, String> kvMap = new HashMap<String, String>();

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.startsWith(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                kvMap.put(key, value);
                return true;
            }
        });

        if (kvMap.size() > 0) {
            Object array = Array.newInstance(componentClass, kvMap.size());
            int index = 0;
            for (Map.Entry<String, String> entry : kvMap.entrySet()) {
                String value = entry.getValue();

                if (componentClass == Class.class) {
                    try {
                        Array.set(array, index++, Class.forName(value));
                    } catch (ClassNotFoundException e) {
                        throw new PropertiesConfigurationResolveException(e);
                    }
                } else if (PrimitiveTypeUtils.isPrimitiveClass(componentClass)) {
                    Array.set(array, index++, PrimitiveTypeUtils.convert(value, componentClass));
                } else {
                    Array.set(array, index++, JsonConvert.deserialize(value, componentClass));
                }
            }
            writeProperty(context, descriptor, array);
        }

    }
}
