package cn.uway.util.apache.parquet.hadoop;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.example.data.Group;
import parquet.hadoop.ParquetOutputFormat;
import parquet.hadoop.api.WriteSupport;
import parquet.hadoop.api.WriteSupport.WriteContext;
import parquet.hadoop.metadata.CompressionCodecName;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.apache.parquet.hadoop.CodecFactory.BytesCompressor;
import cn.uway.util.apache.parquet.hadoop.ParquetFileWriter.Mode;
import cn.uway.util.parquet.FNCreater;
import cn.uway.util.parquet.ParqContext;

/**
 * 
 * 1、解析公共配置并解析 2、根据特征信息创建Writer对象
 * 
 * @author sunt
 *
 */
public class ParquetWriterFactory {
	private static ILogger LOG = LoggerManager
			.getLogger(ParquetWriterFactory.class);

	private final static String KEY_SCHEMA_STR = "parquet.example.schema";

	private static final int MAX_TRY = 10;

	/**
	 * 
	 * @param wKey
	 *            Writer的唯一标识
	 * @param tblName
	 *            表名
	 * @param partStr
	 *            分区字符串
	 * @param fnc
	 *            文件名生成器
	 * @param isSingleBlock
	 *            是否单文件单block
	 */
	public static GroupParquetRecordWriter getWriter(String wKey,
			String tblName, String partStr, FNCreater fnc, Boolean isSingleBlock)
			throws IllegalArgumentException {
		for (int i = 0; i < MAX_TRY; i++) {
			String fn = fnc.getNewName();
			Path file = new Path(fn);
			try {
				Configuration conf = ParqContext.getNewCfg();
				conf.set(KEY_SCHEMA_STR, ParqContext.getSchema(tblName));
				LOG.info("wKey:{},准备创建：{}", wKey, fn);
				return getParquetRecordWriter(conf, file, isSingleBlock);
			} catch (IllegalArgumentException ie) {
				LOG.error("wKey:{},创建失败：{};msg:{};cause:{}", new Object[] {
						wKey, fn, ie.getMessage(), ie.getCause() });
				throw ie;
			} catch (Exception e) {
				LOG.error("wKey:{},创建失败：{};msg:{};cause:{}", new Object[] {
						wKey, fn, e.getMessage(), e.getCause() });
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					LOG.debug("wKey:{}.sleep 5s", wKey);
				}
				continue;
			}
		}
		return null;
	}

	// ///////////////////////////////以下用反射构建ParquetRecordWriter对象/////////////////////////////////

	// parquet.block.size
	private static long blockSize;
	// parquet.page.size
	private static int pageSize;
	// parquet.dictionary.page.size
	private static int dictionaryPageSize;
	// parquet.enable.dictionary
	private static boolean enableDictionary;
	// parquet.validation
	private static boolean validating;
	// parquet.writer.version
	private static WriterVersion writerVersion;
	// parquet.writer.max-padding
	private static int maxPaddingSize;

	static {
		parseCfg();
	}

	/**
	 * 解析公共配置
	 */
	private static void parseCfg() {
		Configuration conf = ParqContext.getGlobalCfg();

		// parquet.block.size
		blockSize = ParquetOutputFormat.getLongBlockSize(conf);
		// parquet.page.size
		pageSize = ParquetOutputFormat.getPageSize(conf);
		// parquet.dictionary.page.size
		dictionaryPageSize = ParquetOutputFormat.getDictionaryPageSize(conf);
		// parquet.enable.dictionary
		enableDictionary = ParquetOutputFormat.getEnableDictionary(conf);
		// parquet.validation
		validating = ParquetOutputFormat.getValidation(conf);
		// parquet.writer.version
		writerVersion = ParquetOutputFormat.getWriterVersion(conf);
		// parquet.writer.max-padding
		maxPaddingSize = getMaxPaddingSize(conf);

	}

	private static BytesCompressor getCompressor(Configuration conf,
			int pageSize) throws ClassNotFoundException {
		CompressionCodecName codecName = ParquetOutputFormat
				.getCompression(conf);
		String codecClassName = codecName.getHadoopCompressionCodecClassName();
		Class<?> codecClass = Class.forName(codecClassName);
		if (codecClassName == null) {
			return null;
		}
		CompressionCodec codec = (CompressionCodec) ReflectionUtils
				.newInstance(codecClass, conf);
		BytesCompressor b = new BytesCompressor(codecName, codec, pageSize);
		return b;
	}

	// default to no padding for now
	private static final int DEFAULT_MAX_PADDING_SIZE = 8 * 1024 * 1024; // 8MB

	private static int getMaxPaddingSize(Configuration conf) {
		// default to no padding, 0% of the row group size
		return conf.getInt(ParquetOutputFormat.MAX_PADDING_BYTES,
				DEFAULT_MAX_PADDING_SIZE);
	}

	/**
	 * 
	 * @param conf
	 *            配置集合
	 * @param file
	 *            目标文件
	 * @param isSingleBlock
	 *            是否单文件单block
	 * 
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	private static GroupParquetRecordWriter getParquetRecordWriter(
			Configuration conf, Path file, Boolean isSingleBlock)
			throws IOException, ClassNotFoundException {

		// parquet.write.support.class
		final WriteSupport<Group> writeSupport = new ParquetOutputFormat<Group>()
				.getWriteSupport(conf);

		// parquet.example.schema
		WriteContext init = writeSupport.init(conf);
		Mode mode = Mode.CREATE;
		// 不适用分区时，文件存在，先删除在创建
		if(!isSingleBlock)
		{
			mode = Mode.OVERWRITE;
		}
		ParquetFileWriter w = new ParquetFileWriter(conf, init.getSchema(),
				file, mode, blockSize, maxPaddingSize);
		w.start();
		return new GroupParquetRecordWriter(w, writeSupport, init.getSchema(),
				init.getExtraMetaData(), blockSize, pageSize, getCompressor(
						conf, pageSize), dictionaryPageSize, enableDictionary,
				validating, writerVersion, isSingleBlock);
	}

}
