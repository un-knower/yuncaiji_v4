package cn.uway.ucloude.resolver;



import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;
import cn.uway.ucloude.configuration.auto.PropertiesConfigurationResolveException;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.GenericsUtils;
import cn.uway.ucloude.utils.PrimitiveTypeUtils;

/**
 * @author magic.s.g.xie
 */
public class MapResolver extends AbstractResolver {

    public static final MapResolver INSTANCE = new MapResolver();

    @SuppressWarnings("unchecked")
    public void resolve(AutoConfigContext context, PropertyDescriptor descriptor, Class<?> propertyType) {

        Method readMethod = descriptor.getReadMethod();
        final Class<?> kClass = GenericsUtils.getMethodGenericReturnType(readMethod, 0);
        final Class<?> vClass = GenericsUtils.getMethodGenericReturnType(readMethod, 1);
        // k 必须是原始类型
        if (!PrimitiveTypeUtils.isPrimitiveClass(kClass)) {
            throw new PropertiesConfigurationResolveException("Only support Map primitive type key");
        }
        final Map map = createMap(propertyType);

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.startsWith(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                String mapKey = key.substring(name.length() + 1);
                if (mapKey.startsWith("[") && mapKey.endsWith("]")) {
                    mapKey = mapKey.substring(1, mapKey.length() - 1);
                }
                if (vClass == Class.class) {
                    try {
                        Class clazz = Class.forName(value);
                        map.put(mapKey, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new PropertiesConfigurationResolveException(e);
                    }
                } else if (PrimitiveTypeUtils.isPrimitiveClass(vClass)) {
                    map.put(mapKey, PrimitiveTypeUtils.convert(value, vClass));
                } else {
                    map.put(mapKey, JsonConvert.deserialize(value, vClass));
                }
                return true;
            }
        });

        writeProperty(context, descriptor, map);
    }

    @SuppressWarnings("unchecked")
    protected Map createMap(Type type) {
        if (type == Properties.class) {
            return new Properties();
        }

        if (type == Hashtable.class) {
            return new Hashtable();
        }

        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }

        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }

        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }

        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return createMap(parameterizedType.getRawType());
        }

        Class<?> clazz = (Class<?>) type;
        if (clazz.isInterface()) {
            throw new PropertiesConfigurationResolveException("unsupported type " + type);
        }

        try {
            return (Map<Object, Object>) clazz.newInstance();
        } catch (Exception e) {
            throw new PropertiesConfigurationResolveException("unsupported type " + type, e);
        }
    }

}
