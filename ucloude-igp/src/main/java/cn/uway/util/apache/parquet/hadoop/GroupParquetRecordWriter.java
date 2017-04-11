package cn.uway.util.apache.parquet.hadoop;

import java.io.IOException;
import java.util.Map;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.example.data.Group;
import parquet.hadoop.api.WriteSupport;
import parquet.schema.MessageType;
import cn.uway.util.apache.parquet.hadoop.CodecFactory.BytesCompressor;

/**
 * @author sunt
 *
 */
public class GroupParquetRecordWriter extends
		InternalParquetRecordWriter<Group> {
	private Boolean isClosed = false;

	public GroupParquetRecordWriter(ParquetFileWriter w,
			WriteSupport<Group> writeSupport, MessageType schema,
			Map<String, String> extraMetaData, long blockSize, int pageSize,
			BytesCompressor compressor, int dictionaryPageSize,
			boolean enableDictionary, boolean validating,
			WriterVersion writerVersion, boolean isSingleBlock) {
		super(w, writeSupport, schema, extraMetaData, blockSize, pageSize,
				compressor, dictionaryPageSize, enableDictionary, validating,
				writerVersion, isSingleBlock);
	}

	@Override
	public void close() throws IOException, InterruptedException {
		super.close();
		isClosed = true;
	}

	public Boolean isClosed() {
		return isClosed;
	}
}
