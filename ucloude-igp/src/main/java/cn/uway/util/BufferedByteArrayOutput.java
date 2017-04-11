package cn.uway.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ByteOutputFile
 * 
 * @author yanbo 2014年12月13日
 */
public class BufferedByteArrayOutput implements ByteArrayOutput{

	private final static int DEFAULT_BUFFER_LEN = 1 * 1024 * 1024;

	private FileOutputStream fileOut;

	private BufferedOutputStream out;

	public BufferedByteArrayOutput(File file) throws FileNotFoundException{
		this(file, DEFAULT_BUFFER_LEN);
	}

	public BufferedByteArrayOutput(File file, int bufferLength) throws FileNotFoundException{
		fileOut = new FileOutputStream(file);
		out = new BufferedOutputStream(fileOut, bufferLength);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.uway.util.ByteArrayOutput#write(byte[])
	 */
	@Override
	public void write(byte [] context) throws Exception{
		out.write(context);
		//		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.uway.util.ByteArrayOutput#close()
	 */
	@Override
	public void close(){
		try{
			if(null != out){
				out.flush();
			}
		}catch(IOException e){}
		try{
			if(null != fileOut){
				fileOut.close();
			}
		}catch(IOException e){}
		try{
			if(null != out){
				out.close();
			}
		}catch(IOException e){}
	}

}
