package cn.uway.ucloude.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.RuntimeErrorException;

public class NormalBinarySerialize implements IBinarySerialize {

	@Override
	public <T> T deSerialize(byte[] bytes) {
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return (T)objectInputStream.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(obj);
			return byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
