package cn.uway.ucloude.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.uway.ucloude.utils.PrimitiveTypeUtils;

/**
 * 转化工具
 * @author uway
 *
 */
public class ResolverUtils {
	 public static Resolver getResolver(Class<?> clazz) {

	        if (clazz == Class.class) {

	            return ClassResolver.INSTANCE;

	        } else if (PrimitiveTypeUtils.isPrimitiveClass(clazz)) {

	            return PrimitiveTypeResolver.INSTANCE;

	        } else if (clazz.isEnum()) {

	            return EnumResolver.INSTANCE;

	        } else if (clazz.isArray()) {

	            return ArrayResolver.INSTANCE;

	        } else if (clazz == Set.class || clazz == HashSet.class || clazz == Collection.class || clazz == List.class
	                || clazz == ArrayList.class) {

	            return CollectionResolver.INSTANCE;

	        } else if (Collection.class.isAssignableFrom(clazz)) {

	            return CollectionResolver.INSTANCE;

	        } else if (Map.class.isAssignableFrom(clazz)) {

	            return MapResolver.INSTANCE;

	        } else {
	            return JavaBeanResolver.INSTANCE;
	        }
	    }

}
