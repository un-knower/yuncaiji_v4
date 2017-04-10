package cn.uway.ucloude.serialize;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

public abstract class TypeReference<T> {
	 private final Type type;

	    public TypeReference() {
	        Type superClass = getClass().getGenericSuperclass();

	        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	    }

	    public Type getType() {
	        return type;
	    }
}
