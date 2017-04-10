package cn.uway.usummary.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能：压缩文件成tar.gz格式
 */
public class TarUtils {

	private static final Logger LOG = LoggerFactory.getLogger(TarUtils.class);

	private static int BUFFER = 1024 * 4; // 缓冲大小

	public static String ENCODE_UTF_8 = "UTF-8";

	public static String ENCODE_GBK = "GBK";

	public String encode;

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	/**
	 * 方法功能：打包单个文件或文件夹 参数：inputFileName 要打包的文件夹或文件的路径 targetFileName 打包后的文件路径
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void execute(String inputFileName, String targetFileName) throws UnsupportedEncodingException {
		File inputFile = new File(inputFileName);
		String base = inputFileName.substring(inputFileName.lastIndexOf(File.separator) + 1);
		TarOutputStream out = getTarOutputStream(targetFileName);
		tarPack(out, inputFile, base);
		try {
			if (null != out) {
				out.close();
			}
		} catch (IOException e) {
		}
		compress(new File(targetFileName));
	}

	/**
	 * 方法功能：打包多个文件或文件夹 参数：inputFileNameList 要打包的文件夹或文件的路径的列表 targetFileName 打包后的文件路径
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void execute(List<String> inputFileNameList, String targetFileName) throws UnsupportedEncodingException {
		TarOutputStream out = getTarOutputStream(targetFileName);
		for (String inputFileName : inputFileNameList) {
			inputFileName = inputFileName.replace("\\", "/");

			File inputFile = new File(inputFileName);
			String base = inputFileName.substring(inputFileName.lastIndexOf("/") + 1);
			tarPack(out, inputFile, base);
		}

		try {
			if (null != out) {
				out.close();
			}
		} catch (IOException e) {
		}
		compress(new File(targetFileName));
	}
	
	/**
	 * 方法功能：打包多个文件或文件夹 参数：inputFileNameList 要打包的文件夹或文件的路径的列表 targetFileName 打包后的文件路径
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void execute_tar(List<String> inputFileNameList, String targetFileName) throws UnsupportedEncodingException {
		TarOutputStream out = getTarOutputStream(targetFileName);
		for (String inputFileName : inputFileNameList) {
			inputFileName = inputFileName.replace("\\", "/");

			File inputFile = new File(inputFileName);
			String base = inputFileName.substring(inputFileName.lastIndexOf("/") + 1);
			tarPack(out, inputFile, base);
		}

		try {
			if (null != out) {
				out.close();
			}
		} catch (IOException e) {
		}
		 
	}

	/**
	 * 方法功能：打包成tar文件 参数：out 打包后生成文件的流 inputFile 要压缩的文件夹或文件 base 打包文件中的路径
	 * 
	 * @throws UnsupportedEncodingException
	 */

	private void tarPack(TarOutputStream out, File inputFile, String base) throws UnsupportedEncodingException {
		if (inputFile.isDirectory()) // 打包文件夹
		{
			packFolder(out, inputFile, base);
		} else
		// 打包文件
		{
			packFile(out, inputFile, base);
		}
	}

	/**
	 * 方法功能：遍历文件夹下的内容，如果有子文件夹，就调用tarPack方法 参数：out 打包后生成文件的流 inputFile 要压缩的文件夹或文件 base 打包文件中的路径
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private void packFolder(TarOutputStream out, File inputFile, String base) throws UnsupportedEncodingException {
		File[] fileList = inputFile.listFiles();
		try {
			// 在打包文件中添加路径
			out.putNextEntry(new TarEntry(base + "/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		base = base.length() == 0 ? "" : base + "/";
		for (File file : fileList) {
			tarPack(out, file, base + file.getName());
		}
	}

	/**
	 * 方法功能：打包文件 参数：out 压缩后生成文件的流 inputFile 要压缩的文件夹或文件 base 打包文件中的路径
	 */
	private void packFile(TarOutputStream out, File inputFile, String base) {
		TarEntry tarEntry = new TarEntry(base);

		// 设置打包文件的大小，如果不设置，打包有内容的文件时，会报错
		tarEntry.setSize(inputFile.length());
		try {
			out.putNextEntry(tarEntry);
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(inputFile);
			int b = 0;
			byte[] B_ARRAY = new byte[BUFFER];
			while ((b = in.read(B_ARRAY, 0, BUFFER)) != -1) {
				out.write(B_ARRAY, 0, b);
			}
		} catch (FileNotFoundException e) {
			LOG.error(inputFile + "文件不存在. 原因: ", e);
		} catch (Exception e) {
			LOG.error(inputFile + "读取文件出现异常. 原因: ", e);
		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.closeEntry();
				}
			} catch (IOException e) {

			}
		}
	}

	/**
	 * 方法功能：把打包的tar文件压缩成gz格式 参数：srcFile 要压缩的tar文件路径
	 */
	private void compress(File srcFile) {
		String target = srcFile.getAbsolutePath() + ".gz";
		FileInputStream in = null;
		GZIPOutputStream out = null;
		try {
			in = new FileInputStream(srcFile);

			out = new GZIPOutputStream(new FileOutputStream(target));

			int number = 0;
			byte[] B_ARRAY = new byte[BUFFER];
			while ((number = in.read(B_ARRAY, 0, BUFFER)) != -1) {
				out.write(B_ARRAY, 0, number);
			}

		} catch (Exception e) {
			LOG.error(target + "压缩文件出现异常. 原因: ", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 方法功能：获得打包后文件的流 参数：targetFileName 打包后文件的路径
	 */
	private TarOutputStream getTarOutputStream(String targetFileName) {
		// 如果打包文件没有.tar后缀名，就自动加上
		//jianglong 2015-1-7 修改 因为有targetFileName 是大写， 所以这里 加上 toLowerCase
		targetFileName = targetFileName.toLowerCase().endsWith(".tar") ? targetFileName : targetFileName + ".tar";
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(targetFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		TarOutputStream out = new TarOutputStream(bufferedOutputStream);

		// 如果不加下面这段，当压缩包中的路径字节数超过100 byte时，就会报错
		out.setLongFileMode(TarOutputStream.LONGFILE_GNU);
		return out;
	}

	public static void main(String[] args) throws Exception {
		// String inputFileName1 = "D:/aaa.csv";
		// String inputFileName2 = "D:/aaa1.csv";
		List<String> list = new ArrayList<String>();
		// list.add(inputFileName1);
		// list.add(inputFileName2);

		String inputFileName2 = "E:/uwaysoft/create_file/hw_cdma_createFile/Ericsson_CM_201610100000.xml.gz";
		list.add(inputFileName2);

		String targetFileName = "E:/uwaysoft/create_file/hw_cdma_createFile/Ericsson_CM_201610100000.tar";

		//Packer.pack(list, targetFileName);
		long start=System.currentTimeMillis();
		new TarUtils().execute(inputFileName2, targetFileName);
		//大约要1.7秒
		System.out.println(System.currentTimeMillis()-start);
		// String targetFileName1 = "D:/lws1.tar";
		// new TarUtils().execute(inputFileName1, targetFileName1);
	}

}