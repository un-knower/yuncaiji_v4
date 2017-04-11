package cn.uway.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * ByteOutputFile
 * 
 * @author yanbo 2014年12月13日
 */
public class FileChannelByteArrayOutput implements ByteArrayOutput{

	private final static int DEFAULT_BUFFER_LEN = 1 * 1024 * 1024;

	private FileOutputStream fileOut;

	private FileChannel outChannel;

	private ByteBuffer buffer;

	private int bufferLength;

	private int tempLength = 0;

	public FileChannelByteArrayOutput(File file) throws FileNotFoundException{
		this(file, DEFAULT_BUFFER_LEN);
	}

	public FileChannelByteArrayOutput(File file, int bufferLength) throws FileNotFoundException{
		fileOut = new FileOutputStream(file);
		outChannel = fileOut.getChannel();
		buffer = ByteBuffer.allocate(bufferLength);
		this.bufferLength = bufferLength;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see cn.uway.util.ByteArrayOutput#write(byte[])
	//	 */
	//	@Override
	//	public void write(byte [] context) throws Exception{
	//		int contextLeng = context.length;
	//		if(tempLength + contextLeng > bufferLength){
	//			wirte();
	//		}
	//		buffer.put(context);
	//		tempLength = tempLength + contextLeng;
	//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.uway.util.ByteArrayOutput#write(byte[])
	 */
	@Override
	public void write(byte [] context) throws Exception{
		buffer.put(context);
		wirte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.uway.util.ByteArrayOutput#close()
	 */
	@Override
	public void close() throws Exception{
		wirte();
		try{
			if(null != fileOut){
				fileOut.close();
			}
		}catch(IOException e){}
		try{

			if(null != outChannel){
				outChannel.close();
			}
		}catch(IOException e){}
	}

	private void wirte() throws Exception{
		buffer.flip();
		outChannel.write(buffer);
		buffer.clear();
		tempLength = 0;
	}
}
