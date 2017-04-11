package cn.uway.util;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * 文件串行化操作类 OperatorFileSerial
 * 
 * @author ShiGang 2013-7-20
 */
public class OperatorFileSerial {

	// 文件操作模式
	public static enum EOPERATOR_FILE_MODE {
		e_Write, e_Read
	};

	// 字节序
	public static enum E_ENDIAN_MODE {
		e_Endian_LE, e_Endian_BE
	};

	// 缓冲区尺寸
	protected int MAX_BUFF_SIZE = (32 * 1024);

	// 文件操作模式
	protected EOPERATOR_FILE_MODE fileOperatorMode;

	// 字节序
	protected E_ENDIAN_MODE endianMode;

	// 文件名
	protected String operatorFileName;

	// 文件写入流对象
	protected OutputStream ofs;

	// 文件读入流
	protected RandomAccessFile irfs;
	protected InputStream ifs;

	// 文件读写缓冲区
	protected byte[] byteBuffer;

	// 文件缓冲区操作位置
	protected int cacheBuffOffset;

	// 文件读或写的位置;
	protected long currFilePos;

	// 文件缓冲区的有效读取数据尺寸
	protected int cacheBuffLength;

	/**
	 * 构造函数
	 * 
	 * @param eFileState
	 *            文件操作状态: 读/写
	 * @param fileName
	 *            文件名
	 * @throws Exception
	 */
	public OperatorFileSerial(EOPERATOR_FILE_MODE eFileState, String fileName)
			throws Exception {
		this(eFileState, E_ENDIAN_MODE.e_Endian_BE, fileName);
	}

	/**
	 * 构造函数
	 * 
	 * @param eFileState
	 *            文件操作状态: 读/写
	 * @param eEndianMode
	 *            文件编码格式
	 * @param fileName
	 *            文件名
	 * @throws Exception
	 */
	public OperatorFileSerial(EOPERATOR_FILE_MODE eFileState,
			E_ENDIAN_MODE eEndianMode, String fileName) throws Exception {
		if (eFileState == EOPERATOR_FILE_MODE.e_Write) {
			// 输出流按顺序写入文件
			ofs = new FileOutputStream(fileName);
		} else if (eFileState == EOPERATOR_FILE_MODE.e_Read) {
			// 输入流要随机定位置，所以用了RandomAccessFile
			irfs = new RandomAccessFile(fileName, "r");
		} else
			throw new Exception("不支持的操作模式." + eFileState);

		byteBuffer = new byte[MAX_BUFF_SIZE];
		fileOperatorMode = eFileState;
		operatorFileName = fileName;
		cacheBuffOffset = 0;
		currFilePos = 0;
		cacheBuffLength = 0;
		// java 的RandomAccessFile模式用的是BE模式
		endianMode = eEndianMode;
	}
	
	/**
	 * 构造函数
	 * 
	 * @param eFileState
	 *            文件操作状态: 读/写
	 * @param eEndianMode
	 *            文件编码格式
	 * @param fileName
	 *            文件名
	 * @param bAppendModen
	 * 			  是否追加模式           
	 * @throws Exception
	 */
	public OperatorFileSerial(EOPERATOR_FILE_MODE eFileState,
			E_ENDIAN_MODE eEndianMode, String fileName, boolean bAppendModen) throws Exception {
		if (eFileState == EOPERATOR_FILE_MODE.e_Write) {
			// 输出流按顺序写入文件
			ofs = new FileOutputStream(fileName, bAppendModen);
		} else if (eFileState == EOPERATOR_FILE_MODE.e_Read) {
			// 输入流要随机定位置，所以用了RandomAccessFile
			irfs = new RandomAccessFile(fileName, "r");
		} else
			throw new Exception("不支持的操作模式." + eFileState);

		byteBuffer = new byte[MAX_BUFF_SIZE];
		fileOperatorMode = eFileState;
		operatorFileName = fileName;
		cacheBuffOffset = 0;
		currFilePos = 0;
		cacheBuffLength = 0;
		// java 的RandomAccessFile模式用的是BE模式
		endianMode = eEndianMode;
	}
	
	/**
	 * 构造函数
	 * 
	 * @param eFileState
	 *            文件操作状态: 读/写
	 * @param eEndianMode
	 *            文件编码格式
	 * @param fileName
	 *            文件名
	 * @throws Exception
	 */
	public OperatorFileSerial(E_ENDIAN_MODE eEndianMode, InputStream in) throws Exception {
		
		//ifs = new RandomAccessFile(fileName, "r");
		ifs = in;
		irfs = null;

		byteBuffer = new byte[MAX_BUFF_SIZE];
		fileOperatorMode = EOPERATOR_FILE_MODE.e_Read;
		operatorFileName = "";
		cacheBuffOffset = 0;
		currFilePos = 0;
		cacheBuffLength = 0;
		// java 的RandomAccessFile模式用的是BE模式
		endianMode = eEndianMode;
	}
	
	public OperatorFileSerial(E_ENDIAN_MODE eEndianMode, OutputStream out) throws Exception {
		ofs = out;
		irfs = null;
		ifs = null;

		byteBuffer = new byte[MAX_BUFF_SIZE];
		fileOperatorMode = EOPERATOR_FILE_MODE.e_Write;
		operatorFileName = "";
		cacheBuffOffset = 0;
		currFilePos = 0;
		cacheBuffLength = 0;
		// java 的RandomAccessFile模式用的是BE模式
		endianMode = eEndianMode;
	}

	public OperatorFileSerial(E_ENDIAN_MODE eEndianMode, byte[] buff)
			throws Exception {
		if (buff == null)
			throw new Exception("缓冲区不能为null.");

		byteBuffer = buff;
		fileOperatorMode = EOPERATOR_FILE_MODE.e_Read;
		cacheBuffOffset = 0;
		currFilePos = 0;
		cacheBuffLength = buff.length;
		MAX_BUFF_SIZE = buff.length;
		// java 的RandomAccessFile模式用的是BE模式
		endianMode = eEndianMode;
	}
	
	public OperatorFileSerial(EOPERATOR_FILE_MODE eFileState, E_ENDIAN_MODE eEndianMode, byte[] buff)
			throws Exception {
		if (buff == null)
			throw new Exception("缓冲区不能为null.");

		ofs = null;
		irfs = null;
		ifs = null;
		byteBuffer = buff;
		fileOperatorMode = eFileState;
		cacheBuffOffset = 0;
		currFilePos = 0;
		cacheBuffLength = buff.length;
		MAX_BUFF_SIZE = buff.length;
		// java 的RandomAccessFile模式用的是BE模式
		endianMode = eEndianMode;
	}

	public void write(byte n) throws IOException {
		write_number(n & 0xFFL, 1);
	}

	public void write(short n) throws IOException {
		write_number(n & 0xFFFFL, 2);
	}

	public void write(int n) throws IOException {
		write_number(n & 0xFFFFFFFFL, 4);
	}

	public void write(long n) throws IOException {
		write_number(n, 8);
	}

	public void write(float f) throws IOException {
		int n = Float.floatToIntBits(f);
		write_number(n, 4);
	}

	public void write(double f) throws IOException {
		long n = Double.doubleToLongBits(f);
		write_number(n, 8);
	}

	public void write(Date date) throws IOException {
		long n = date.getTime();
		write_number(n, 8);
	}

	public void write(String buff) throws IOException {
		if (buff == null) {
			// 如果字符串为null,则写入长度=Integer.MAX_VALUE;
			write(Integer.MAX_VALUE);
		} else {
			byte[] strBuff = buff.getBytes();
			int len = strBuff.length;

			// 先写入字符串的长度
			write(len);

			// 再写入字符串的内容(byte数组)
			if (len > 0) {
				if (len > MAX_BUFF_SIZE) {
					// 如果字符串的尺寸比缓冲区的最大值还要大，则直接写入字符串;
					flush();
					ofs.write(strBuff, 0, len);
					// 文件位置 + 已写入的位置
					currFilePos += len;
				} else {
					// 缓存的剩余尺寸
					int nCacheBuffSizeRight = MAX_BUFF_SIZE - cacheBuffOffset;
					// 剩余缓存放不下了，则flush一下；
					if (nCacheBuffSizeRight < len)
						flush();

					System.arraycopy(strBuff, 0, byteBuffer, cacheBuffOffset, len);
					cacheBuffOffset += len;
				}
			}
		}
	}
	
	/**
	 * 写入定长字符串
	 * @param buff
	 * @param len
	 * @throws IOException
	 */
	public void write_fixedString(String buff, int fixLen) throws IOException {
		int len = 0;
		if (buff != null && buff.length()>0) {
			byte[] strBuff = buff.getBytes();
			len = strBuff.length;
			if (len > fixLen)
				len = fixLen;
	
			// 再写入字符串的内容(byte数组)
			if (len > MAX_BUFF_SIZE) {
				// 如果字符串的尺寸比缓冲区的最大值还要大，则直接写入字符串;
				flush();
				ofs.write(strBuff, 0, len);
				// 文件位置 + 已写入的位置
				currFilePos += len;
			} else {
				// 缓存的剩余尺寸
				int nCacheBuffSizeRight = MAX_BUFF_SIZE - cacheBuffOffset;
				// 剩余缓存放不下了，则flush一下；
				if (nCacheBuffSizeRight < len)
					flush();

				System.arraycopy(strBuff, 0, byteBuffer, cacheBuffOffset, len);
				cacheBuffOffset += len;
			}
		}
		
		//写入未完的空白串
		len = fixLen - len;
		if (len>0) {
			if (len > MAX_BUFF_SIZE) {
				// 如果字符串的尺寸比缓冲区的最大值还要大，则直接写入字符串;
				flush();
				
				byte[] space = new byte[len];
				ofs.write(space);
				
				// 文件位置 + 已写入的位置
				currFilePos += len;
			} else {
				// 缓存的剩余尺寸
				int nCacheBuffSizeRight = MAX_BUFF_SIZE - cacheBuffOffset;
				// 剩余缓存放不下了，则flush一下；
				if (nCacheBuffSizeRight < len)
					flush();
				
				for (int i=0; i<len; ++i) {
					byteBuffer[cacheBuffOffset+i] = '\0';
				}
				cacheBuffOffset += len;
			}
		}
	}

	public void write_number(long n, int nByteLength) throws IOException {
		// 缓存的剩余尺寸
		int nCacheBuffSizeRight = MAX_BUFF_SIZE - cacheBuffOffset;
		// 剩余缓存放不下了，则flush一下；
		if (nCacheBuffSizeRight < nByteLength)
			flush();

		if (endianMode == E_ENDIAN_MODE.e_Endian_BE) {
			// BE 模式
			// 每次偏移位置
			int nOffsetBit = (nByteLength - 1) * 8;
			for (int i = 0; i < nByteLength; ++i) {
				byteBuffer[cacheBuffOffset++] = (byte) (0xFF & (n >> nOffsetBit));
				nOffsetBit -= 8;
			}
		} else {
			// LE 模式
			for (int i = 0; i < nByteLength; ++i) {
				byteBuffer[cacheBuffOffset++] = (byte) (0xFF & n);
				n = n >> 8;
			}
		}
	}
	
	public void write(byte[] buff, int start, int length) throws IOException {
		// 空位补0
		if (buff == null || buff.length < 1) {
			buff = new byte[length];
			start = 0;
		}
		
		// 对于写不够的抛出异常
		if ((buff.length-start) < length)
			throw new IOException("not enough buffer length to write.");
			
		// 写入字符串的内容(byte数组)
		if (length > MAX_BUFF_SIZE) {
			// 如果字符串的尺寸比缓冲区的最大值还要大，则直接写入字符串;
			flush();
			ofs.write(buff, start, length);
			// 文件位置 + 已写入的位置
			currFilePos += length;
		} else {
			// 缓存的剩余尺寸
			int nCacheBuffSizeRight = MAX_BUFF_SIZE - cacheBuffOffset;
			// 剩余缓存放不下了，则flush一下；
			if (nCacheBuffSizeRight < length)
				flush();

			System.arraycopy(buff, start, byteBuffer, cacheBuffOffset, length);
			cacheBuffOffset += length;
		}
	}

	public void flush() throws IOException {
		if (fileOperatorMode != EOPERATOR_FILE_MODE.e_Write)
			throw new IOException("不支持当前的操作模式");

		if (cacheBuffOffset > 0 && ofs != null) {
			// 将缓存写入到文件中;
			ofs.write(byteBuffer, 0, cacheBuffOffset);
			// 文件位置 + 已写入的位置
			currFilePos += cacheBuffOffset;
			// 文件写完后，记住将偏移位置置0;
			cacheBuffOffset = 0;
		}
	}

	// 对于gzip等压缩流，每次read的字节数并不一定等于指定的字节数，所以这里做了个循环来保持每次读取的数据长度是需要的数据长度
	private int read_s(byte[] b, int off, int len) throws IOException {
		if (ifs==null && irfs == null) {
			throw new IOException("null of file input stream.");
		}
		
		int nReadTotalLength = 0;
		do {
			int nRead = 0;
			
			try {
				if (ifs != null) {
					nRead = ifs.read(b, off + nReadTotalLength, len - nReadTotalLength);
				}
				else if (irfs != null) {
					nRead = irfs.read(b, off + nReadTotalLength, len - nReadTotalLength);
				}
			}
			catch (EOFException e) {
				break;
			}
			catch (IOException e) {
				throw e;
			}
			
			if (nRead <= 0) {
				if (nReadTotalLength == 0) {
					return nRead;
				}
				else {
					break;
				}
			}
			else {
				currFilePos += nRead;
				nReadTotalLength += nRead;
			}
			
		} while (nReadTotalLength<len);
		
		return nReadTotalLength;
	}
	
	public byte[] read_bytes(int length) throws IOException {
		// 如果缓冲区已不够本次读取，则将文件预读到缓存中
		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		if (nCacheBuffSizeRight < length) {
			if (length > MAX_BUFF_SIZE) {
				// 如果要读取的字节数，比缓存最大值还要大，则直接读取
				// 先将未读的缓存复制到字符串buff中
				byte[] buff = new byte[length];
				int nReaded = 0;
				if (nCacheBuffSizeRight > 0) {
					System.arraycopy(byteBuffer, cacheBuffOffset, buff, 0,
							nCacheBuffSizeRight);
					length -= nCacheBuffSizeRight;
					// 已读尺寸
					nReaded = nCacheBuffSizeRight;

					// 缓冲区全部读完后，则要复位缓冲区数据
					cacheBuffOffset = 0;
					cacheBuffLength = 0;
				}

				// 再从文件中读取剩余需要的尺寸
				if (-1 == read_s(buff, nReaded, length))
					throw new EOFException("end of read file.");

				return buff;
			} else {
				cacheRead();
				// 再判断一下，缓存数据是否够本次读取
				nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
				if (nCacheBuffSizeRight < length)
					throw new EOFException("end of read file.");
			}
		}

		byte[] buff = new byte[length];
		System.arraycopy(byteBuffer, cacheBuffOffset, buff, 0, length);
		cacheBuffOffset += length;

		return buff;
	}
	
	public void read_byte(byte[] b, int off, int len) throws IOException {
		if (b.length<(off+len))
			throw new IOException("read_byte() out of read range. b.length:" + b.length + " less than buff range(" + off + "-" + off+len  + ")");
		
		// 如果缓冲区已不够本次读取，则将文件预读到缓存中
		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		if (nCacheBuffSizeRight < len) {
			int nReaded = 0;
			// 先将未读的缓存复制到字符串buff中
			if (nCacheBuffSizeRight > 0) {
				System.arraycopy(byteBuffer, cacheBuffOffset, b, off,
						nCacheBuffSizeRight);
				len -= nCacheBuffSizeRight;
				// 已读尺寸
				nReaded = nCacheBuffSizeRight;

				// 缓冲区全部读完后，则要复位缓冲区数据
				cacheBuffOffset = 0;
				cacheBuffLength = 0;
			}
			
			// 再从文件中读取剩余需要的尺寸
			if (len > 0) {
				if (-1 == read_s(b, off+nReaded, len)) {
					throw new EOFException("end of read file.");
				}
			}
		} else {
			System.arraycopy(byteBuffer, cacheBuffOffset, b, off, len);
			cacheBuffOffset += len;
		}
	}

	public byte read_byte() throws IOException {
		return (byte) read_number(1);
	}

	public short read_ubyte() throws IOException {
		return (short) (read_number(1) & 0xFF);
	}

	public short read_short() throws IOException {
		return (short) read_number(2);
	}

	public int read_ushort() throws IOException {
		return (int) (read_number(2) & 0xFFFF);
	}

	public int read_int() throws IOException {
		return (int) read_number(4);
	}

	public long read_uint() throws IOException {
		return (long) (read_number(4) & 0xFFFFFFFFL);
	}

	public long read_long() throws IOException {
		return (long) read_number(8);
	}

	public float read_float() throws IOException {
		int n = (int) read_number(4);
		return Float.intBitsToFloat(n);
	}

	public double read_double() throws IOException {
		long n = read_number(8);
		return Double.longBitsToDouble(n);
	}

	public Date read_date() throws IOException {
		long n = read_number(8);
		return new Date(n);
	}

	public String read_string() throws IOException {
		int n = (int) read_number(4);
		if (n == Integer.MAX_VALUE)
			return null;

		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		// 如果缓冲区已不够本次读取，则将文件预读到缓存中
		if (nCacheBuffSizeRight < n) {
			// 如果字符串的尺寸，比缓存最大值还要大，则直接读取
			if (n > MAX_BUFF_SIZE) {
				// 先将未读的缓存复制到字符串buff中
				byte[] buff = new byte[n];
				int nReaded = 0;
				if (nCacheBuffSizeRight > 0) {
					System.arraycopy(byteBuffer, cacheBuffOffset, buff, 0,
							nCacheBuffSizeRight);
					n -= nCacheBuffSizeRight;
					// 已读尺寸
					nReaded = nCacheBuffSizeRight;

					// 缓冲区全部读完后，则要复位缓冲区数据
					cacheBuffOffset = 0;
					cacheBuffLength = 0;
				}

				// 再从文件中读取剩余需要的尺寸
				if (-1 == read_s(buff, nReaded, n))
					throw new EOFException("end of read file");

				// 构建String
				String str = new String(buff);
				return str;
			} else {
				cacheRead();
				// 再判断一下，缓存数据是否够本次读取
				nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
				if (nCacheBuffSizeRight < n)
					throw new EOFException("end of read file");
			}
		}

		String str = new String(byteBuffer, cacheBuffOffset, n);
		cacheBuffOffset += n;

		return str;
	}
	
	public String read_string(int size, boolean checkBreakTag) throws IOException {
		if (size == Integer.MAX_VALUE)
			return null;

		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		// 如果缓冲区已不够本次读取，则将文件预读到缓存中
		if (nCacheBuffSizeRight < size) {
			// 如果字符串的尺寸，比缓存最大值还要大，则直接读取
			if (size > MAX_BUFF_SIZE) {
				// 先将未读的缓存复制到字符串buff中
				byte[] buff = new byte[size];
				if (nCacheBuffSizeRight > 0) {
					System.arraycopy(byteBuffer, cacheBuffOffset, buff, 0,
							nCacheBuffSizeRight);
					size -= nCacheBuffSizeRight;

					// 缓冲区全部读完后，则要复位缓冲区数据
					cacheBuffOffset = 0;
					cacheBuffLength = 0;
				}

				// 再从文件中读取剩余需要的尺寸
				if (-1 == read_s(buff, nCacheBuffSizeRight, size))
					throw new EOFException("end of read file");
				
				// 组装成字符串读到'\0'结束(C/C++字符串) 
				int validEndBuffPos = 0;
				if (checkBreakTag) {
					while (validEndBuffPos < buff.length){
						if (buff[validEndBuffPos++] == '\0')
							break;
					}
				} else {
					validEndBuffPos = buff.length;
				}
				
				// 构建String
				String str = new String(buff, 0, validEndBuffPos);
				return str;
			} else {
				cacheRead();
				// 再判断一下，缓存数据是否够本次读取
				nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
				if (nCacheBuffSizeRight < size)
					throw new EOFException("end of read file");
			}
		}
		
		// 组装成字符串读到'\0'结束(C/C++字符串) 
		int validByteLength = 0;
		int pos = cacheBuffOffset;
		if (checkBreakTag) {
			while (validByteLength < size){
				if (byteBuffer[pos++] == '\0')
					break;
				
				++validByteLength;
			}
		} else {
			validByteLength = size;
		}
		String str = new String(byteBuffer, cacheBuffOffset, validByteLength);
		cacheBuffOffset += size;

		return str;
	}

	public long read_number(int nByteLength) throws IOException {
		// 如果缓冲区已不够本次读取，则将文件预读到缓存中
		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		if (nCacheBuffSizeRight < nByteLength) {
			cacheRead();
			// 再判断一下，缓存数据是否够本次读取
			nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
			if (nCacheBuffSizeRight < nByteLength)
				throw new EOFException("end of read file");
		}

		if (endianMode == E_ENDIAN_MODE.e_Endian_BE) {
			// BE字节序
			long n = 0xFFL & byteBuffer[cacheBuffOffset];
			for (int i = 1; i < nByteLength; ++i) {
				n = n << 8;
				n |= (0xFFL & byteBuffer[cacheBuffOffset + i]);
			}
			cacheBuffOffset += nByteLength;

			return n;
		} else {
			// LE字节序
			int nOffsetBit = 8;
			long n = 0xFFL & byteBuffer[cacheBuffOffset];
			for (int i = 1; i < nByteLength; ++i) {
				n |= ((0xFFL & byteBuffer[cacheBuffOffset + i]) << nOffsetBit);
				nOffsetBit += 8;
			}
			cacheBuffOffset += nByteLength;

			return n;
		}
	}

	// 将文件内容读入到缓冲区
	public int cacheRead() throws IOException {
		if (fileOperatorMode != EOPERATOR_FILE_MODE.e_Read)
			throw new IOException("不支持当前的操作模式");

		// 未处理的缓存数据尺寸 = 缓存尺寸 - 已读尺寸(偏移位)
		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		if (nCacheBuffSizeRight >= MAX_BUFF_SIZE)
			return nCacheBuffSizeRight;

		// 将剩余未读完的数据放到缓冲区前面去
		for (int i = 0; i < nCacheBuffSizeRight; ++i)
			byteBuffer[i] = byteBuffer[cacheBuffOffset + i];

		// 读取数据
		int nRead = read_s(byteBuffer, nCacheBuffSizeRight, MAX_BUFF_SIZE - nCacheBuffSizeRight);
		if (nRead == -1)
			throw new EOFException("end of read file");

		// 缓存长度 = 上次未读完长度 + 本次读入的长度
		cacheBuffLength = nCacheBuffSizeRight + nRead;
		// 设置读取偏移位至0;
		cacheBuffOffset = 0;

		return nCacheBuffSizeRight + nRead;
	}

	// 判断文件是否结束
	public boolean isEndOfFile() throws IOException {
		if (fileOperatorMode != EOPERATOR_FILE_MODE.e_Read)
			throw new IOException("不支持当前的操作模式");

		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		if (nCacheBuffSizeRight > 0)
			return false;

		try {
			if (cacheRead() > 0)
				return false;
			else
				return true;
		} catch (IOException e) {
			return true;
		}
	}

	public void seek(long pos) throws IOException {
		if (fileOperatorMode != EOPERATOR_FILE_MODE.e_Read)
			throw new IOException("unsuport seek position by not read model.");

		if (irfs == null) {
			if (pos > cacheBuffLength)
				throw new IOException(
						"OperatorFileSerial::seek() out of memory!");

			cacheBuffOffset = (int) pos;
			return;
		}

		irfs.seek(pos);
		currFilePos += pos;

		// 文件指针移动后，所有的缓冲区，必须要重置
		cacheBuffOffset = 0;
		cacheBuffLength = 0;
	}

	public void skip(int nOffsetByte) throws IOException {
		//写模式
		if (fileOperatorMode == EOPERATOR_FILE_MODE.e_Write) {
			if (ofs == null) {
				if ((cacheBuffOffset + nOffsetByte) > cacheBuffLength
						|| (cacheBuffOffset + nOffsetByte) < 0)
					throw new IOException(
							"OperatorFileSerial::skip() out of memory!");

				cacheBuffOffset += nOffsetByte;
				return;
			}
			
			if (nOffsetByte < 1) {
				throw new IOException(
						"unsuport skip back.");
			}
			
			byte[] emptyBytes = new byte[nOffsetByte];
			ofs.write(emptyBytes, 0, nOffsetByte);
			
			return;
		}
		
		// 读模式
		if (irfs == null) {
			if ((cacheBuffOffset + nOffsetByte) > cacheBuffLength
					|| (cacheBuffOffset + nOffsetByte) < 0)
				throw new IOException(
						"OperatorFileSerial::skip() out of memory!");

			cacheBuffOffset += nOffsetByte;
			return;
		}
		
		int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;
		// 往回跳
		if (nOffsetByte < 0) {
			// 如果缓存的偏移位置，大于往回跳的字节数，只接移动偏位置即可
			if (cacheBuffOffset + nOffsetByte >= 0) {
				cacheBuffOffset += nOffsetByte;
			} else {
				nOffsetByte += cacheBuffOffset;

				long nFileCurrPos = getCurrPosition();
				nFileCurrPos += nOffsetByte;

				// 移动文件指针
				irfs.seek(nFileCurrPos);
				currFilePos = nFileCurrPos;

				// 缓冲区，重置
				cacheBuffOffset = 0;
				cacheBuffLength = 0;
			}
		}
		// 如果偏移的位置，大于缓存剩余的中用尺寸
		else if (nOffsetByte > nCacheBuffSizeRight) {
			nOffsetByte -= nCacheBuffSizeRight;
			irfs.skipBytes(nOffsetByte);

			// 缓冲区，重置
			cacheBuffOffset = 0;
			cacheBuffLength = 0;
		} else {
			cacheBuffOffset += nOffsetByte;
		}
	}

	public void close() throws IOException {
		if (fileOperatorMode == EOPERATOR_FILE_MODE.e_Write) {
			if (ofs != null) {
				// 关闭前，要将缓存写入到文件中
				flush();
				ofs.close();
				ofs = null;
			}
		} else if (fileOperatorMode == EOPERATOR_FILE_MODE.e_Read) {
			if (irfs != null) {
				irfs.close();
				irfs = null;
			}
			
			if (ifs != null) {
				ifs.close();
				ifs = null;
			}
		}
	}

	public long getCurrPosition() throws IOException {
		if (fileOperatorMode == EOPERATOR_FILE_MODE.e_Write) {
			return getCurrFileWriteLength();
		} else if (fileOperatorMode == EOPERATOR_FILE_MODE.e_Read) {
			long offset = cacheBuffLength;
			if (irfs != null) {
				offset = irfs.getFilePointer();
			}
			else if (ifs != null) {
				//对于纯流的数据，不支持获取当前数据读取的位置
				offset = currFilePos;
			}
			int nCacheBuffSizeRight = cacheBuffLength - cacheBuffOffset;

			return offset - nCacheBuffSizeRight;
		}

		return -1;
	}
	
	/**
	 * 设置读写缓存位置（不支持文件操作时，使用该方法)
	 * @param nPosition
	 * @throws UnsupportedOperationException
	 */
	public void setCurrPosition(int nPosition) throws UnsupportedOperationException {
		if (this.ifs != null || this.irfs != null || this.ofs != null)
			throw new UnsupportedOperationException("不支持文件操作模式，使用该方法");
		
		if (nPosition < 0)
			nPosition = 0;
		else if (nPosition >= this.cacheBuffLength)
			nPosition = this.cacheBuffLength - 1;
		
		this.cacheBuffOffset = nPosition;
	}

	/**
	 * 取得当前文件已写入的长度
	 * 
	 * @return
	 */
	public long getCurrFileWriteLength() {
		return currFilePos + cacheBuffOffset;
	}

	/**
	 * java 的析构函数很不靠谱，调用者还是需要手工调用一下close();
	 */
	@Override
	protected void finalize() throws Throwable {
		close();

		super.finalize();
	}

	public void setEndianMode(E_ENDIAN_MODE endianMode) {
		this.endianMode = endianMode;
	}
	
	public String getOperatorFileName() {
		return this.operatorFileName;
	}

	@Override
	public String toString() {
		Long pos = null;
		try {
			pos = getCurrPosition();
		} catch (IOException e) {
		}
		return "fileName=[" + operatorFileName + "] pos=" + pos.toString();
	}
}
