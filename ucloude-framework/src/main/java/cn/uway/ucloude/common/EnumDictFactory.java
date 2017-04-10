package cn.uway.ucloude.common;

public interface EnumDictFactory {
	public <E extends Enum<E>>E getEnum(Class<E> enumType, int value);
	
	public String getText(int id);
	
	public int getValue(int id);
}
