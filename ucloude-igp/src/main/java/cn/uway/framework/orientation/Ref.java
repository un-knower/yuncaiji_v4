package cn.uway.framework.orientation;

public class Ref<T> {

	private T obj;

	public Ref() {
		super();
	}

	public Ref(T obj) {
		super();
		this.obj = obj;
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

	@Override
	public String toString() {
		return "Ref [obj=" + obj + "]";
	}

}
