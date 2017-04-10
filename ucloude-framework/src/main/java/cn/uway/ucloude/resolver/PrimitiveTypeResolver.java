package cn.uway.ucloude.resolver;


import java.beans.PropertyDescriptor;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;
import cn.uway.ucloude.utils.PrimitiveTypeUtils;

/**
 * @author magic.s.g.xie
 */
public class PrimitiveTypeResolver extends AbstractResolver {

    public static final PrimitiveTypeResolver INSTANCE = new PrimitiveTypeResolver();

    @Override
    public void resolve(final AutoConfigContext context, final PropertyDescriptor descriptor, final Class<?> propertyType) {

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.equals(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                Object v = PrimitiveTypeUtils.convert(value, propertyType);
                writeProperty(context, descriptor, v);
                return false;
            }
        });
    }

}
