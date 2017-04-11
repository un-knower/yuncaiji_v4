package cn.uway.framework.warehouse.exporter;

import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.util.DbUtil;

public class JdbcBatchExExporter extends JdbcBatchExporter {
	

	public JdbcBatchExExporter(DbExportTemplateBean dbExportTempletBean,
			ExporterArgs exporterArgs) {
		super(dbExportTempletBean, exporterArgs);
	}

	/**
	 * 创建数据库链接(先调用父类方法，然后添加自己的业务：清表)
	 */
	@Override
	protected void initJdbc(ExporterArgs exporterArgs) {
		super.initJdbc(exporterArgs);
		
		DbUtil.deleteTable(con, dbExportTempletBean.getTable().getTableName());
	}
	
}
