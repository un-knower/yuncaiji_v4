package cn.uway.util;

public interface ByteArrayOutput{

	public abstract void write(byte [] context) throws Exception;

	public abstract void close() throws Exception;

}