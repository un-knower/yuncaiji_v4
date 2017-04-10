package cn.uway.ucloude.resolver;


import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;
import cn.uway.ucloude.configuration.auto.PropertiesConfigurationResolveException;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.GenericsUtils;
import cn.uway.ucloude.utils.PrimitiveTypeUtils;

/**
 * 集合类型转换
 * @author magic.s.g.xie
 */
@SuppressWarnings("unchecked")
public class CollectionResolver extends AbstractResolver {
    public static final CollectionResolver INSTANCE = new CollectionResolver();

    @Override
    public void resolve(AutoConfigContext context, PropertyDescriptor descriptor, Class<?> propertyType) {

        final Collection collection = createCollection(propertyType);
        final Class<?> itemType = GenericsUtils.getMethodGenericReturnType(descriptor.getReadMethod(), 0);

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.startsWith(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                if (itemType == Class.class) {
                    try {
                        collection.add(Class.forName(value));
                    } catch (ClassNotFoundException e) {
                        throw new PropertiesConfigurationResolveException(e);
                    }
                } else if (PrimitiveTypeUtils.isPrimitiveClass(itemType)) {
                    collection.add(PrimitiveTypeUtils.convert(value, itemType));
                } else {
                    collection.add(JsonConvert.deserialize(value, itemType));
                }
                return true;
            }
        });

        writeProperty(context, descriptor, collection);

    }

    private Collection createCollection(Type type) {
        Class<?> rawClass = getRawClass(type);

        Collection list;
        if (rawClass == AbstractCollection.class) {
            list = new ArrayList();
        } else if (rawClass.isAssignableFrom(HashSet.class)) {
            list = new HashSet();
        } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet();
        } else if (rawClass.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet();
        } else if (rawClass.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList();
        } else if (rawClass.isAssignableFrom(EnumSet.class)) {
            Type itemType;
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            list = EnumSet.noneOf((Class<Enum>) itemType);
        } else {
            try {
                list = (Collection) rawClass.newInstance();
            } catch (Exception e) {
                throw new PropertiesConfigurationResolveException("create instance error, class " + rawClass.getName());
            }
        }
        return list;
    }

    private Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType) type).getRawType());
        } else {
            throw new PropertiesConfigurationResolveException("can't resolve the rawClass by " + type);
        }
    }
}
