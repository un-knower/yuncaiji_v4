package cn.uway.ucloude.resolver;

import java.beans.PropertyDescriptor;

import cn.uway.ucloude.configuration.auto.AutoConfigContext;

public interface Resolver {
	void resolve(AutoConfigContext context, PropertyDescriptor descriptor, Class<?> propertyType);
}
