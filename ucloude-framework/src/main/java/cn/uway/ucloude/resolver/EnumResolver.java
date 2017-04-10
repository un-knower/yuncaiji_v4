package cn.uway.ucloude.resolver;


import java.beans.PropertyDescriptor;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;

/**
 * @author magic.s.g.xie
 */
public class EnumResolver extends AbstractResolver {

    public static final EnumResolver INSTANCE = new EnumResolver();

    @SuppressWarnings("unchecked")
    @Override
    public void resolve(final AutoConfigContext context, final PropertyDescriptor descriptor, final Class<?> propertyType) {

        doFilter(context, descriptor, new Filter() {
            @Override
            public boolean onCondition(String name, String key, String value) {
                return key.equals(name);
            }

            @Override
            public boolean call(String name, String key, String value) {
                Object v = Enum.valueOf((Class<Enum>) propertyType, value);
                writeProperty(context, descriptor, v);
                return false;
            }
        });
    }
}
