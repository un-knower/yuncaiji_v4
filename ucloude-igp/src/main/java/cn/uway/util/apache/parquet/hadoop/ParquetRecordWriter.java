package cn.uway.util.apache.parquet.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;

import parquet.example.data.Group;
import parquet.example.data.simple.SimpleGroupFactory;
import parquet.hadoop.ParquetOutputFormat;
import parquet.io.api.Binary;
import parquet.schema.MessageType;
import parquet.schema.PrimitiveType.PrimitiveTypeName;
import parquet.schema.Type;
import cn.uway.framework.warehouse.exporter.breakPoint.BpInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.parquet.FNCFactory;
import cn.uway.util.parquet.FNCreater;
import cn.uway.util.parquet.ParqContext;

public class ParquetRecordWriter {
	protected static final ILogger LOG = LoggerManager.getLogger(ParquetRecordWriter.class);

	// 唯一标识
	private String wKey;
	private String tblName;
	// 引用的状态表信息。statusId+exportId,BpInfo
	private Map<String,BpInfo> statusMap = new HashMap<String, BpInfo>();
	private String partStr;
	private FNCreater fnc;
	private Boolean isSingleBlock;

	private GroupParquetRecordWriter internalWriter;

	// ///////////////////字段转换beg////////////////////////////
	// group对象工厂，每条记录作为一个group存在
	private SimpleGroupFactory factory;
	// group中每列的值类型
	private List<PrimitiveTypeName> types;
	// <filedName,fileldType>
	private HashMap<String, FieldEx> typeMap = new HashMap<String, FieldEx>();
	// ///////////////////字段转换end////////////////////////////

	/**
	 * This memory manager is for all the real writers
	 * (InternalParquetRecordWriter) in one task.
	 */
	private static MemoryManager memoryManager;
	private static final float DEFAULT_MEMORY_POOL_RATIO = 0.95f;
	private static final long DEFAULT_MIN_MEMORY_ALLOCATION = 1 * 1024 * 1024; // 1MB

	static {
		Configuration conf = ParqContext.getGlobalCfg();
		// parquet.memory.pool.ratio
		float maxLoad = conf.getFloat(ParquetOutputFormat.MEMORY_POOL_RATIO,
				DEFAULT_MEMORY_POOL_RATIO);
		// parquet.memory.min.chunk.size
		long minAllocation = conf.getLong(
				ParquetOutputFormat.MIN_MEMORY_ALLOCATION,
				DEFAULT_MIN_MEMORY_ALLOCATION);
		memoryManager = new MemoryManager(maxLoad, minAllocation);
	}

	// /////////////////////内存自管理end//////////////////////////

	public ParquetRecordWriter(String wKey, BpInfo bpInfo, String partStr) {
		this.isSingleBlock = !"".equals(partStr);
		this.tblName = bpInfo.getTable();
		fnc = FNCFactory.getCreater(tblName, partStr,bpInfo.getCtType());
		internalWriter = ParquetWriterFactory.getWriter(wKey, tblName, partStr, fnc, isSingleBlock);
		init(internalWriter.getSchema());
		memoryManager.addWriter(internalWriter,
				internalWriter.getRowGroupSizeThreshold());
		this.wKey = wKey;
		statusMap.put(bpInfo.getUniqueId(), bpInfo);
		this.partStr = partStr;
	}
	
	public synchronized void addStatusId(BpInfo bpInfo){
		statusMap.put(bpInfo.getUniqueId(), bpInfo);
	}

	/**
	 * 初始化表字段信息
	 * 
	 * @param schema
	 */
	private void init(MessageType schema) {
		factory = new SimpleGroupFactory(schema);
		types = new ArrayList<PrimitiveTypeName>();

		List<Type> fields = schema.getFields();
		PrimitiveTypeName typeName;
		for (Type type : fields) {
			typeName = type.asPrimitiveType().getPrimitiveTypeName();
			types.add(typeName);
			typeMap.put(type.getName().toUpperCase(), new FieldEx(
					types.size() - 1, typeName));
		}
	}

	public synchronized void close() throws IOException, InterruptedException {
		if (!internalWriter.isClosed()) {
			internalWriter.close();
		}
		Set<String> remove = new HashSet<String>();
		for (BpInfo bpInfo : statusMap.values()) {
			if(!bpInfo.updateBreakPoint()){// 更新失败就不再持有了
				remove.add(bpInfo.getUniqueId());
			}
		}
		for (String r : remove) {
			statusMap.remove(r);
		}
		memoryManager.removeWriter(internalWriter);
	}

	public synchronized void flush() throws IOException, InterruptedException{
		// 关闭
		internalWriter.close();
		Set<String> remove = new HashSet<String>();
		for (BpInfo bpInfo : statusMap.values()) {
			if(!bpInfo.updateBreakPoint()){// 更新失败就不再持有了
				remove.add(bpInfo.getUniqueId());
			}
		}
		for (String r : remove) {
			statusMap.remove(r);
		}
		memoryManager.removeWriter(internalWriter);
		// 重建
		do {
			internalWriter = ParquetWriterFactory.getWriter(wKey,
					tblName, partStr, fnc, isSingleBlock);
		} while (null == internalWriter);
		memoryManager.addWriter(internalWriter,
				internalWriter.getRowGroupSizeThreshold());
	}

	/**
	 * 利用真正的writer写数据
	 * 
	 * @param uniqueId 分发器id
	 * @param g
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void write(String uniqueId,Group g) throws IOException, InterruptedException, IllegalArgumentException {
		// g为空则不写
		if (null != g) {
			// writer关闭了需要新建一个
			if (internalWriter.isClosed()) {
				do {
					internalWriter = ParquetWriterFactory.getWriter(wKey,
							tblName, partStr, fnc, isSingleBlock);
				} while (null == internalWriter);
				memoryManager.addWriter(internalWriter,
						internalWriter.getRowGroupSizeThreshold());
			}
			internalWriter.write(g);
			if (internalWriter.isClosed()) {
				memoryManager.removeWriter(internalWriter);
				Set<String> remove = new HashSet<String>();
				for (BpInfo bpInfo : statusMap.values()) {
					if(!bpInfo.updateBreakPoint()){// 更新失败就不再持有了
						remove.add(bpInfo.getUniqueId());
					}
				}
				for (String r : remove) {
					statusMap.remove(r);
				}
			}
		}
	}

	/**
	 * 写入一个map
	 * 
	 * @param uniqueId 分发器id
	 * @param record
	 * @throws Exception
	 */
	public synchronized void write(String uniqueId,Map<String, String> record) throws Exception {
		statusMap.get(uniqueId).addOne();
		write(uniqueId, convertMapToGroup(record));
	}

	/**
	 * 转换一个map对象为group
	 * 
	 * @param record
	 * @return
	 * @throws Exception
	 */
	private Group convertMapToGroup(Map<String, String> record)
			throws Exception {
		Group g = factory.newGroup();
		for (Entry<String, FieldEx> entry : typeMap.entrySet()) {
			String fieldValue = record.get(entry.getKey());
			if ((null == fieldValue)||(fieldValue.equals(""))) {
				continue;
			}
			FieldEx field = entry.getValue();
			try {
				// 因为本方法是线程安全的，尽量减少异常处理，需在外层保证fieldValue非""，非null，首位不带空格
				switch (field.getFieldTypeName()) {
				case DOUBLE:
					g.add(field.getIdx(), Double.parseDouble(fieldValue));
					break;
				case BINARY:
					g.add(field.getIdx(), Binary.fromString(fieldValue));
					break;
				case BOOLEAN:
					if(fieldValue.equalsIgnoreCase("true")||fieldValue.equalsIgnoreCase("yes")){
						g.add(field.getIdx(), true);
					}else{
						g.add(field.getIdx(), false);
					}
					break;
				case INT64:
					g.add(field.getIdx(), Long.parseLong(fieldValue));
					break;
				case INT32:
					g.add(field.getIdx(), Integer.parseInt(fieldValue));
					break;
				case FLOAT:
					g.add(field.getIdx(), Float.parseFloat(fieldValue));
					break;
				case INT96:
				case FIXED_LEN_BYTE_ARRAY:
					// g.add(field.getIdx(), Binary.fromString(fieldValue));
					// break;
				default:
					throw new Exception(String.format("字段类型错误: %s",
							entry.getKey()));
				}
			} catch (Exception e) {
				throw new Exception(String.format(
						"字段转换失败，字段名：%s，字段值：%s，msg：%s", entry.getKey(),
						fieldValue, e.getMessage()));
			}
		}
		return g;
	}
	
	public synchronized String getStatistic(){
		StringBuilder sb = new StringBuilder();
		sb.append(wKey).append(":\r\n");
		for (BpInfo info : statusMap.values()) {
			info.getStatistic(sb);
			sb.append("\r\n");
		}
		return sb.toString();
	}

	/**
	 * 字段信息类
	 * 
	 * @author sunt
	 *
	 */
	class FieldEx {
		private int idx;
		private PrimitiveTypeName fieldTypeName;

		public FieldEx(int idx, PrimitiveTypeName fieldTypeName) {
			this.idx = idx;
			this.fieldTypeName = fieldTypeName;
		}

		public int getIdx() {
			return idx;
		}

		public PrimitiveTypeName getFieldTypeName() {
			return fieldTypeName;
		}
	}

	public String getWKey() {
		return wKey;
	}

}
