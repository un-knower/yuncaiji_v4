package cn.uway.framework.parser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.JdbcAccessOutObject;
import cn.uway.framework.parser.database.DatabaseParseTempletParser;
import cn.uway.framework.parser.database.DatabaseParserTemplate;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FieldTemplateBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.RemoteFileExportTemplateBean;
import cn.uway.util.DbUtil;
import cn.uway.util.StringUtil;

/**
 * 自适应输出数据SQL解析器
 * 这个类的作用，用于只需要配一个共公的输出模板，就会根据sql的字段，自动生成输出模板．
 */
public class AdaptFileExportDBParser extends DatabaseParser {
	public static class ParserDataSource {
		public DatabaseParserTemplate tpl_;
		public String sql_;
		public PreparedStatement statement_;
		public ResultSet resultSet_;
		public ResultSetMetaData metaData_;
		int columnNum_;
		int stroeMapSize_;
		int exportBeanNumber_;
		
		public void close() {
			DbUtil.close(this.resultSet_, this.statement_, null);
		}
	}
	
	protected List<DatabaseParserTemplate> parserTemplateList;
	protected List<ParserDataSource> parserDataSourceList;
	protected ParserDataSource currPDS;
	
	public AdaptFileExportDBParser() {
		
	}
	
	@Override
	public boolean hasNextRecord() throws Exception {
		try{
			while (true) {
				if (this.currPDS != null) {
					boolean bHasNext = this.currPDS.resultSet_.next();
					if (bHasNext)
						return true;
					
					if (currPDS != null) {
						currPDS.close();
						this.currPDS = null;
					}
				}
				
				if (parserDataSourceList.size() < 1)
					return false;
				
				this.currPDS = parserDataSourceList.remove(0);
				this.parserTemplate = currPDS.tpl_;
				this.statement = currPDS.statement_;
				this.resultSet = currPDS.resultSet_;
				this.metaData = currPDS.metaData_;
				this.columnNum = currPDS.columnNum_;
				this.stroeMapSize = currPDS.stroeMapSize_;
			}
			
		}catch(Exception e){
			// 发生错误的情况直接终止 JDBC一旦发生错误，则会一直报错
			throw new Exception("JDBC 错误 ", e);
		}
	}

	@Override
	public ParseOutRecord nextRecord() throws Exception {
		this.totalNum++;
		Map<String,String> data = new HashMap<String,String>(stroeMapSize);
		int i = 1;
		try {
			for(; i <= columnNum; i++){
				data.put(metaData.getColumnName(i), replace(resultSet.getString(i)));
			}
			if(!data.isEmpty()){
				this.parseSucNum++;
			}
			// 增加OMCID COLLECTTIME STAMPTIME字段
			String omc = data.get("OMC");
			if(StringUtil.isEmpty(omc))
				data.put("OMC", String.valueOf(task.getExtraInfo().getOmcId()));
			data.put("COLLECTTIME", getDateString(new Date()));
			data.put("STAMPTIME", dateTime);
		} catch (ArrayIndexOutOfBoundsException e) {
			//添加驱动包里面索引越界异常捕获，将异常数据丢弃掉。
			LOGGER.warn("数据异常，现在是第{}条记录，第{}列，总列数{}，列名{}", new Object[]{this.totalNum, i, columnNum, metaData.getColumnName(i)});
			return null;
		}
		ParseOutRecord outRecord = new ParseOutRecord();
		outRecord.setType(this.parserTemplate.getDataType());
		outRecord.setRecord(data);
		return outRecord;
	}

	@Override
	public void parse(AccessOutObject accessOutObject) throws Exception {
		this.startTime = new Date();
		this.task = accessOutObject.getTask();
		this.currentDataTime = this.task.getDataTime();
		this.parserTemplateList = DatabaseParseTempletParser.parseTemplates(templates);
		this.dateTime = getDateString(task.getDataTime());
		JdbcAccessOutObject outObject = (JdbcAccessOutObject)accessOutObject;
		connection = outObject.getConnection();
		parserDataSourceList = new LinkedList<ParserDataSource>();
		
		// 设置模板
		List<ExportTemplateBean> exportBeanList = this.getCurrentJob().getExportTemplateBeans();
		List<ExportTemplateBean> validExportBeanList = new ArrayList<ExportTemplateBean>(exportBeanList.size()/2);
		for (DatabaseParserTemplate tpl : parserTemplateList) {
			ParserDataSource pds = new ParserDataSource();
			
			pds.tpl_ = tpl;
			pds.sql_ = StringUtil.convertCollectPath(tpl.getSql(), this.task.getDataTime());
			pds.statement_ = connection.prepareStatement(pds.sql_);
			pds.resultSet_ = pds.statement_.executeQuery();
			pds.metaData_ = pds.resultSet_.getMetaData();
			pds.columnNum_ = pds.metaData_.getColumnCount();
			pds.stroeMapSize_ = getInitialMapSize(pds.columnNum_);
			
			// 将parser datatype对应的模板逐个加载进来;
			for (ExportTemplateBean bean : exportBeanList) {
				if (bean.getDataType() != tpl.getDataType()) {
					continue;
				}
				
				//this.parserTemplate = tpl;
				if (bean instanceof FileExportTemplateBean) {
					++pds.exportBeanNumber_;
					FileExportTemplateBean fileExportBean = (FileExportTemplateBean)bean;
					List<FieldTemplateBean>fileExportBeanList = buildExportBeanList(pds.metaData_);
					fileExportBean.setExportFileds(fileExportBeanList);
				} else if (bean instanceof  RemoteFileExportTemplateBean) {
					++pds.exportBeanNumber_;
					RemoteFileExportTemplateBean remoteFileExportBean = (RemoteFileExportTemplateBean)bean;
					List<FieldTemplateBean>fileExportBeanList = buildExportBeanList(pds.metaData_);
					remoteFileExportBean.setExportFileds(fileExportBeanList);				
				} else {
					++pds.exportBeanNumber_;
					validExportBeanList.add(bean);
					continue;
				}
			}
			
			// 对于没有输出内容的模板，则不进行输出
			if (pds.exportBeanNumber_ < 1) {
				pds.close();
				pds = null;
				continue;
			}
			parserDataSourceList.add(pds);
		}
		
		if (validExportBeanList.size()>0) {
			this.getCurrentJob().setExportTemplateBeans(validExportBeanList);
		}
	}

	protected List<FieldTemplateBean> buildExportBeanList(ResultSetMetaData rsMetaData) throws Exception {
		List<FieldTemplateBean> fileExportBeanList = new ArrayList<FieldTemplateBean>();
		try {
			int metaColumncount = rsMetaData.getColumnCount();
			for (int i=1; i<=metaColumncount; ++i) {
				String dbColumnName = rsMetaData.getColumnName(i);
				FieldTemplateBean bean = new FieldTemplateBean();
				/*String format = null;
				switch (metaData.getColumnType(i)) {
					case Types.DATE :
					case Types.TIMESTAMP :
					case Types.TIME :
						format = "yyyy-mm-dd hh24:mi:ss";
						break;
					default:
						break;
				}*/
				
				bean.setColumnName(dbColumnName.toUpperCase());
				bean.setPropertyName(dbColumnName);
				//bean.setFormat(format);
				
				fileExportBeanList.add(bean);
			}
		} catch (Exception e) {
			LOGGER.error("AdaptFileExportDBParser::buildExportTemplate() 创建输出模板错误.", e);
			throw e;
		}
		
		return fileExportBeanList;
	}

	@Override
	public void close() {
		if (this.currPDS != null) {
			this.currPDS.close();
			this.currPDS = null;
		}
		
		while (parserDataSourceList.size() > 0) {
			ParserDataSource pds = parserDataSourceList.remove(0);
			pds.close();
			pds = null;
		}
		
		super.close();
	}
}
