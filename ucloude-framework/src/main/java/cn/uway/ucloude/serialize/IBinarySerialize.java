package cn.uway.ucloude.serialize;

public interface IBinarySerialize {
	<T> T deSerialize(byte[] bytes);

	byte[] serialize(Object obj);
}
